\import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.List;
import java.util.Random;

public class ArenaMain {

    static Random random = new Random();

    public static void main(String[] args) {

        String user = System.getenv("GAME_ID");
        String pass = System.getenv("GAME_PASSWORD");

        if (user == null || pass == null) {
            throw new RuntimeException("Missing credentials");
        }

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);

        try {

            login(driver, user, pass);

            driver.get("https://elem.cards/survival/");

            sleepRandom(2000, 4000);

            // enter first arena
            clickEnter(driver);

            System.out.println("Entered Arena ✔");

            // waiting room
            sleepRandom(25000, 30000);

            driver.navigate().refresh();

            // wait cards load
            sleepRandom(9000, 12000);

            while (true) {

                boolean attacked = attackCards(driver);

                // attacks worked
                if (attacked) {

                    sleepRandom(9000, 12000);

                    driver.navigate().refresh();

                    sleepRandom(8000, 11000);

                    continue;
                }

                // check enter again
                boolean enteredAgain = clickEnterAgain(driver);

                if (enteredAgain) {

                    System.out.println("Started next arena ✔");

                    sleepRandom(25000, 30000);

                    driver.navigate().refresh();

                    sleepRandom(9000, 12000);

                    continue;
                }

                // probably dead waiting for battle end
                System.out.println("Waiting for battle to finish...");

                sleepRandom(9000, 12000);

                driver.navigate().refresh();

                sleepRandom(5000, 8000);
            }

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            driver.quit();
        }
    }

    // ---------------- LOGIN ----------------

    private static void login(WebDriver driver, String user, String pass) {

        driver.get("https://elem.cards/login/");

        driver.findElement(By.name("plogin")).sendKeys(user);
        driver.findElement(By.name("ppass")).sendKeys(pass);

        driver.findElement(By.cssSelector("input[type='submit']")).click();

        sleep(4000);

        try {

            driver.findElement(By.cssSelector("a.urfin")).click();

        } catch (Exception ignored) {
        }

        sleep(3000);
    }

    // ---------------- CLICK ENTER ----------------

    private static boolean clickEnter(WebDriver driver) {

        try {

            List<WebElement> enterBtn = driver.findElements(
                    By.xpath("//span[text()='Enter']/ancestor::a")
            );

            if (!enterBtn.isEmpty()) {

                click(driver, enterBtn.get(0));

                return true;
            }

        } catch (Exception ignored) {
        }

        return false;
    }

    // ---------------- CLICK ENTER AGAIN ----------------

    private static boolean clickEnterAgain(WebDriver driver) {

        try {

            List<WebElement> btns = driver.findElements(
                    By.xpath("//span[contains(text(),'Enter again')]/ancestor::a")
            );

            if (!btns.isEmpty()) {

                click(driver, btns.get(0));

                System.out.println("Clicked Enter again");

                sleepRandom(3000, 5000);

                return true;
            }

        } catch (Exception ignored) {
        }

        return false;
    }

    // ---------------- ATTACK CARDS ----------------

    private static boolean attackCards(WebDriver driver) {

        boolean attacked = false;

        attacked |= clickAttack(driver, "attack0");

        sleepRandom(300, 700);

        attacked |= clickAttack(driver, "attack1");

        sleepRandom(300, 700);

        attacked |= clickAttack(driver, "attack2");

        return attacked;
    }

    // ---------------- CLICK SINGLE ATTACK ----------------

    private static boolean clickAttack(WebDriver driver, String attackType) {

        try {

            List<WebElement> cards = driver.findElements(
                    By.xpath("//a[@class='card']")
            );

            for (WebElement card : cards) {

                String href = card.getAttribute("href");

                if (href == null) {
                    continue;
                }

                if (href.contains(attackType)) {

                    click(driver, card);

                    System.out.println("Clicked " + attackType);

                    sleepRandom(700, 1200);

                    return true;
                }
            }

        } catch (Exception ignored) {
        }

        return false;
    }

    // ---------------- CLICK ----------------

    private static void click(WebDriver driver, WebElement el) {

        try {

            el.click();

        } catch (Exception e) {

            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", el);
        }
    }

    // ---------------- RANDOM SLEEP ----------------

    private static void sleepRandom(int min, int max) {

        int time = random.nextInt(max - min + 1) + min;

        sleep(time);
    }

    // ---------------- SLEEP ----------------

    private static void sleep(int ms) {

        try {

            Thread.sleep(ms);

        } catch (Exception ignored) {
        }
    }
}
