package tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class Template_Page {
    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;

    // =======================================================
    // 1. LOCATORS (The "Objects")
    // =======================================================
    // Syntax: private By variableName = By.locatorStrategy("value");
    private By usernameField = By.id("user_name");
    private By submitButton  = By.cssSelector("button[type='submit']");
    private By dropdownMenu  = By.name("country_select");
    private By hoverMenu     = By.xpath("//div[@class='menu-item']");

    // =======================================================
    // 2. CONSTRUCTOR (Required)
    // =======================================================
    public Template_Page(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.actions = new Actions(driver);
    }

    // =======================================================
    // 3. ACTIONS (The "Methods")
    // =======================================================

    // BASIC CLICKS & TYPING
    public void enterUsername(String name) {
        // Wait until visible, then clear and type
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameField));
        element.clear();
        element.sendKeys(name);
    }

    public void clickSubmit() {
        // Wait until clickable, then click
        wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();
    }

    // DROPDOWNS (Select Class)
    public void selectCountry(String countryName) {
        WebElement dropdown = driver.findElement(dropdownMenu);
        Select select = new Select(dropdown);
        select.selectByVisibleText(countryName);
    }

    // MOUSE ACTIONS (Hover, Double Click)
    public void hoverOverMenu() {
        WebElement menu = driver.findElement(hoverMenu);
        actions.moveToElement(menu).perform(); // Always call .perform()!
    }
    
    // JAVASCRIPT CLICK (Use if normal click fails)
    public void forceClickButton() {
         WebElement btn = driver.findElement(submitButton);
         ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }
}