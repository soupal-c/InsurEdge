package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class CategoryPage {

    private WebDriver driver;
    private WebDriverWait wait;

    // --- 1. LOCATORS (Copied from their code, but organized) ---
    private By categoryMenu = By.xpath("//a[contains(@class,'nav-link')]/span[text()='Category']");
    private By createCategoryLink = By.xpath("//a[contains(@href, 'AdminCreateMainCategory.aspx')]");

    // Header & UI Elements
    private By pageHeader = By.xpath("//div[@class='pagetitle']/h1");
    private By breadcrumbs = By.xpath("//ol[@class='breadcrumb']/li");

    // Table Elements
    private By tableHeaders = By.xpath("//table[@id='ContentPlaceHolder_Admin_gvCategories']//th");
    // Note: These IDs end in '_0', meaning they target the FIRST row.
    private By firstEditBtn = By.id("ContentPlaceHolder_Admin_gvCategories_lnkEdit_0");
    private By firstDeleteBtn = By.id("ContentPlaceHolder_Admin_gvCategories_lnkDelete_0");

    // --- 2. CONSTRUCTOR ---
    public CategoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // Centralized Wait
    }

    // --- 3. ACTIONS ---

    public void navigateToCreateCategory() {
        wait.until(ExpectedConditions.elementToBeClickable(categoryMenu)).click();
        wait.until(ExpectedConditions.elementToBeClickable(createCategoryLink)).click();
    }

    public String getHeaderText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeader)).getText().trim();
    }

    // Convert List<WebElement> to List<String> so the Test Class is clean
    public List<String> getBreadcrumbText() {
        List<WebElement> elements = driver.findElements(breadcrumbs);
        List<String> texts = new ArrayList<>();
        for (WebElement e : elements) {
            texts.add(e.getText().trim());
        }
        return texts;
    }

    public List<String> getTableHeaderText() {
        List<WebElement> elements = driver.findElements(tableHeaders);
        List<String> texts = new ArrayList<>();
        for (WebElement e : elements) {
            texts.add(e.getText().trim());
        }
        return texts;
    }

    public boolean isEditIconDisplayed() {
        try {
            return driver.findElement(firstEditBtn).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isDeleteIconDisplayed() {
        try {
            return driver.findElement(firstDeleteBtn).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}