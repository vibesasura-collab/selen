import org.openqa.selenium.*;
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

            // enter arena
            clickEnter(driver);

            System.out.println("Entered Arena ✔");

            // waiting room
            sleepRandom(20000, 25000);

            driver.navigate().refresh();

            sleepRandom(4000, 7000);

            while (true) {

                boolean attacked = attackCards(driver);

                // attacked successfully
                if (attacked) {

                    driver.navigate().refresh();

                    sleepRandom(3000, 5000);

                    continue;
                }

                // try enter again
                boolean enteredAgain = clickEnterAgain(driver);

                // if no enter again try normal enter
                if (!enteredAgain) {
                    enteredAgain = clickEnter(driver);
                }

                if (enteredAgain) {

                    System.out.println("Started next arena ✔");

                    sleepRandom(20000, 25000);

                    driver.navigate().refresh();

                    sleepRandom(4000, 7000);

                    continue;
                }

                // nothing found
                System.out.println("Waiting for battle to finish...");

                sleepRandom(3000, 5000);

                driver.navigate().refresh();

                sleepRandom(2000, 4000);
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

                System.out.println("Clicked Enter");

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

                sleepRandom(2000, 4000);

                return true;
            }

        } catch (Exception ignored) {
        }

        return false;
    }

    // ---------------- ATTACK ALL CARDS ----------------

    private static boolean attackCards(WebDriver driver) {

        boolean attacked = false;

        while (true) {

            boolean found = false;

            found |= clickAll(driver, "attack0");

            sleepRandom(200, 500);

            found |= clickAll(driver, "attack1");

            sleepRandom(200, 500);

            found |= clickAll(driver, "attack2");

            if (!found) {
                break;
            }

            attacked = true;

            sleepRandom(500, 1000);
        }

        return attacked;
    }

    // ---------------- CLICK ALL ----------------

    private static boolean clickAll(WebDriver driver, String attackType) {

        boolean clicked = false;

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

                    clicked = true;

                    sleepRandom(300, 700);
                }
            }

        } catch (Exception ignored) {
        }

        return clicked;
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
