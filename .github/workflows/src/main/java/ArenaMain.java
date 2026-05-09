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

            for (int arenaCount = 1; arenaCount <= 15; arenaCount++) {

                System.out.println("Starting Arena #" + arenaCount);

                enterArena(driver);

                sleepRandom(25000, 30000);

                driver.navigate().refresh();

                sleepRandom(8000, 12000);

                while (true) {

                    boolean attacked = attackCards(driver);

                    if (!attacked) {
                        System.out.println("No attacks left.");
                        break;
                    }

                    sleepRandom(8000, 12000);

                    driver.navigate().refresh();

                    sleepRandom(5000, 8000);
                }
            }

            System.out.println("All arena runs completed ✔");

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
        } catch (Exception ignored) {}

        sleep(3000);
    }

    // ---------------- ENTER ARENA ----------------

    private static void enterArena(WebDriver driver) {

        driver.get("https://elem.cards/");

        sleepRandom(2000, 4000);

        List<WebElement> arenaBtn = driver.findElements(
                By.cssSelector("a.bttn.arena")
        );

        if (!arenaBtn.isEmpty()) {
            click(driver, arenaBtn.get(0));
        }

        sleepRandom(3000, 5000);

        List<WebElement> enterBtn = driver.findElements(
                By.xpath("//span[text()='Enter']/ancestor::a")
        );

        if (!enterBtn.isEmpty()) {

            click(driver, enterBtn.get(0));

            System.out.println("Entered Arena ✔");

        } else {

            System.out.println("No Enter button found.");
        }
    }

    // ---------------- ATTACK CARDS ----------------

    private static boolean attackCards(WebDriver driver) {

        List<WebElement> cards = driver.findElements(
                By.cssSelector("a.card")
        );

        boolean attacked = false;

        for (WebElement card : cards) {

            try {

                String href = card.getAttribute("href");

                if (href == null) continue;

                if (
                        href.contains("attack0") ||
                        href.contains("attack1") ||
                        href.contains("attack2")
                ) {

                    click(driver, card);

                    attacked = true;

                    System.out.println("Attack clicked: " + href);

                    sleepRandom(700, 1500);
                }

            } catch (Exception ignored) {}
        }

        return attacked;
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

        } catch (Exception ignored) {}
    }
}
