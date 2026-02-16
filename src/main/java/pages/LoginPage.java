package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage {

    WebDriver driver;

    // --- LOCATORS ---
    private By usernameField = By.id("txtUsername");
    private By passwordField = By.id("txtPassword");
    private By loginButton   = By.id("BtnLogin");

    // --- CONSTRUCTOR ---
    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    // --- ACTIONS ---
    public void doLogin(String user, String pass) {
        // Clear the fields first to be safe
        driver.findElement(usernameField).clear();
        driver.findElement(usernameField).sendKeys(user);

        driver.findElement(passwordField).clear();
        driver.findElement(passwordField).sendKeys(pass);

        driver.findElement(loginButton).click();
    }
}