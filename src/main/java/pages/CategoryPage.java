package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class CategoryPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // --- LOCATORS ---

    // Navigation
    private By categoryMenu = By.xpath("//a[contains(@class,'nav-link')]/span[text()='Category']");
    private By mainCategoryLink = By.xpath("//a[contains(@href, 'AdminCreateMainCategory.aspx')]");

    // MC-01: UI Elements
    private By pageHeader = By.xpath("//div[@class='pagetitle']/h1");
    private By breadcrumbs = By.xpath("//ol[@class='breadcrumb']/li");
    private By tableHeaders = By.xpath("//table[@id='ContentPlaceHolder_Admin_gvCategories']//th");

    // MC-01 Task 2: Row Actions (Looking at the first row of data)
    private By editIcon = By.xpath("//table[contains(@id, 'gvCategories')]//tr[2]//a[contains(@id, 'lnkEdit')]");
    private By deleteIcon = By.xpath("//table[contains(@id, 'gvCategories')]//tr[2]//a[contains(@id, 'lnkDelete')]");

    // MC-02: Functional Elements
    private By searchInput = By.id("ContentPlaceHolder_Admin_txtSearch");
    private By searchBtn = By.id("ContentPlaceHolder_Admin_btnSearch");
    private By clearBtn = By.id("ContentPlaceHolder_Admin_btnClear");

    private By addBtn = By.id("ContentPlaceHolder_Admin_btnAdd");
    private By categoryModal = By.id("categoryModal");
    private By nameInput = By.id("ContentPlaceHolder_Admin_txtCategoryName");
    private By statusDropdown = By.id("ContentPlaceHolder_Admin_ddlStatus");
    private By createBtn = By.xpath("//input[@value='Create']");
    private By updateBtn = By.xpath("//input[@value='Update']");
    private By errorMessage = By.id("ContentPlaceHolder_Admin_lblMessage");
    private By table = By.id("ContentPlaceHolder_Admin_gvCategories");

    // --- CONSTRUCTOR ---
    public CategoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // --- NAVIGATION ---
    public void navigateToMainCategory() {
        wait.until(ExpectedConditions.elementToBeClickable(categoryMenu)).click();
        wait.until(ExpectedConditions.elementToBeClickable(mainCategoryLink)).click();
    }

    // --- MC-01: UI ACTIONS ---
    public String getPageTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeader)).getText();
    }

    public List<String> getBreadcrumbs() {
        List<WebElement> elements = driver.findElements(breadcrumbs);
        List<String> texts = new ArrayList<>();
        for (WebElement e : elements) {
            texts.add(e.getText().trim());
        }
        return texts;
    }

    public List<String> getTableHeaders() {
        List<WebElement> elements = driver.findElements(tableHeaders);
        List<String> texts = new ArrayList<>();
        for (WebElement e : elements) {
            texts.add(e.getText().trim());
        }
        return texts;
    }

    // Checks if Edit AND Delete icons are visible in the first row
    public boolean areActionIconsVisible() {
        try {
            boolean editVisible = driver.findElement(editIcon).isDisplayed();
            boolean deleteVisible = driver.findElement(deleteIcon).isDisplayed();
            return editVisible && deleteVisible;
        } catch (Exception e) {
            return false;
        }
    }

    // --- MC-02: SEARCH ACTIONS ---
    public void searchFor(String keyword) {
        WebElement box = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        box.clear();
        box.sendKeys(keyword);
        driver.findElement(searchBtn).click();
        wait.until(ExpectedConditions.stalenessOf(box));
    }

    public void clickClear() {
        driver.findElement(clearBtn).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(table));
    }

    // --- MC-02: ADD/EDIT ACTIONS ---
    public void openAddModal() {
        wait.until(ExpectedConditions.elementToBeClickable(addBtn)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(categoryModal));
    }

    public void fillForm(String name, String status) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
        input.clear();
        input.sendKeys(name);
        new Select(driver.findElement(statusDropdown)).selectByVisibleText(status);
    }

    public void clickCreate() {
        driver.findElement(createBtn).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(categoryModal));
    }

    public void editFirstCategory(String newName) {
        wait.until(ExpectedConditions.elementToBeClickable(editIcon)).click();
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.accept();
        } catch (Exception e) { /* No alert, proceed */ }

        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
        input.clear();
        input.sendKeys(newName);
        driver.findElement(updateBtn).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(categoryModal));
    }

    // --- VALIDATION HELPER ---
    public boolean isCategoryInTable(String name) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(table)).getText().contains(name);
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText();
    }

    // --- DELETE ACTION (Robust Cleanup) ---
    public void deleteCategory(String categoryName) {
        try {
            // 1. Search to isolate the row
            searchFor(categoryName);

            // 2. Click Delete
            wait.until(ExpectedConditions.elementToBeClickable(deleteIcon)).click();

            // 3. Handle Alert
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.accept();

            // 4. THE FIX: Wait for the specific text to vanish
            // We use 'invisibilityOfElementLocated' instead of 'stalenessOf'
            // This covers both full page reloads AND partial table updates.
            By categoryText = By.xpath("//td[contains(text(), '" + categoryName + "')]");
            wait.until(ExpectedConditions.invisibilityOfElementLocated(categoryText));

            System.out.println("Cleanup: Successfully deleted '" + categoryName + "'");

            // 5. Reset the table (Clear search) so the next test starts fresh
            clickClear();

        } catch (Exception e) {
            System.out.println("Cleanup Warning: Could not delete '" + categoryName + "'. It might not exist. Error: " + e.getMessage());
        }
    }
}