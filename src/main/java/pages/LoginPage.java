package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage {

    WebDriver driver;

    // --- 1. REAL LOCATORS (Extracted from your HTML file) ---
    // Found on line 88 of your HTML file
    private By usernameField = By.id("txtUsername");

    // Found on line 94 of your HTML file
    private By passwordField = By.id("txtPassword");

    // Found on line 105 of your HTML file
    private By loginButton   = By.id("BtnLogin");

    // --- 2. CONSTRUCTOR ---
    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    // --- 3. ACTIONS ---
    public void doLogin(String user, String pass) {
        // Clear the fields first to be safe
        driver.findElement(usernameField).clear();
        driver.findElement(usernameField).sendKeys(user);

        driver.findElement(passwordField).clear();
        driver.findElement(passwordField).sendKeys(pass);

        driver.findElement(loginButton).click();
    }
}