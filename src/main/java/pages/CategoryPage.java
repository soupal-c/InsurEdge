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


    // LOCATORS

    // Navigation
    private By categoryMenu = By.xpath("//a[contains(@class,'nav-link')]/span[text()='Category']");
    private By mainCategoryLink = By.xpath("//a[contains(@href, 'AdminCreateMainCategory.aspx')]");

    // US2-MC-01 - Main Category Page UI & Row Actions
    // Task 1 - Header & Breadcrumbs
    private By pageHeader = By.xpath("//div[@class='pagetitle']/h1");
    private By breadcrumbs = By.xpath("//ol[@class='breadcrumb']/li");
    private By tableHeaders = By.xpath("//table[@id='ContentPlaceHolder_Admin_gvCategories']//th");

    // Task 2 - Row Actions
    private By firstRowEditIcon = By.xpath("//table[contains(@id, 'gvCategories')]//tr[2]//a[contains(@id, 'lnkEdit')]");
    private By firstRowDeleteIcon = By.xpath("//table[contains(@id, 'gvCategories')]//tr[2]//a[contains(@id, 'lnkDelete')]");

    // US2-MC-02 - Search, Add, Edit, and Status
    // Task 1 - Search Elements
    private By txtSearch = By.id("ContentPlaceHolder_Admin_txtSearch");
    private By btnSearch = By.id("ContentPlaceHolder_Admin_btnSearch");
    private By btnClear = By.id("ContentPlaceHolder_Admin_btnClear");
    private By table = By.id("ContentPlaceHolder_Admin_gvCategories");

    // Task 2 - Add Category Modal
    private By btnAdd = By.id("ContentPlaceHolder_Admin_btnAdd");
    private By categoryModal = By.id("categoryModal");
    private By txtCategoryName = By.id("ContentPlaceHolder_Admin_txtCategoryName");
    private By ddlStatus = By.id("ContentPlaceHolder_Admin_ddlStatus");
    private By btnCreate = By.xpath("//input[@value='Create']");
    private By modalOverlay = By.className("modal-backdrop");

    // Task 3 - Edit Elements
    private By btnUpdate = By.xpath("//input[@value='Update']");

    // Task 4 - Validation Messages
    private By lblErrorMessage = By.id("ContentPlaceHolder_Admin_lblMessage");

    // DEFECT FIXES / REGRESSION
    // DEF-MC-0X - [Description]
    // private By defectLocator = By.id("..."); // TODO: Add defect locators here

    public CategoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }


    // ACTIONS

    public void navigateToMainCategory() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var modals = document.querySelectorAll('.modal, .modal-backdrop');" +
                            "modals.forEach(m => m.remove());" +
                            "document.body.classList.remove('modal-open');"
            );
        } catch (Exception e) { /* Ignore */ }

        wait.until(ExpectedConditions.elementToBeClickable(categoryMenu)).click();
        wait.until(ExpectedConditions.elementToBeClickable(mainCategoryLink)).click();
    }

    // US2-MC-01 - Task 1 - UI Verification
    public String getPageTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeader)).getText();
    }

    public List<String> getBreadcrumbs() {
        List<WebElement> elements = driver.findElements(breadcrumbs);
        List<String> texts = new ArrayList<>();
        for (WebElement e : elements) texts.add(e.getText().trim());
        return texts;
    }

    public List<String> getTableHeaders() {
        List<WebElement> elements = driver.findElements(tableHeaders);
        List<String> texts = new ArrayList<>();
        for (WebElement e : elements) texts.add(e.getText().trim());
        return texts;
    }

    // US2-MC-01 - Task 2 - Row Actions
    public boolean areRowActionIconsVisible() {
        try {
            return driver.findElement(firstRowEditIcon).isDisplayed() &&
                    driver.findElement(firstRowDeleteIcon).isDisplayed();
        } catch (Exception e) { return false; }
    }

    // US2-MC-02 - Task 1 - Search & Clear
    public void searchFor(String keyword) {
        WebElement box = wait.until(ExpectedConditions.visibilityOfElementLocated(txtSearch));
        box.clear();
        box.sendKeys(keyword);

        WebElement oldTable = driver.findElement(table);
        driver.findElement(btnSearch).click();

        try { wait.until(ExpectedConditions.stalenessOf(oldTable)); } catch (Exception e) {}
    }

    public void clickClear() {
        WebElement oldTable = driver.findElement(table);
        driver.findElement(btnClear).click();
        try { wait.until(ExpectedConditions.stalenessOf(oldTable)); } catch (Exception e) {}
    }

    public boolean isCategoryInTable(String name) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(table)).getText().contains(name);
        } catch (Exception e) { return false; }
    }

    // US2-MC-02 - Task 2 - Add Category
    public void openAddModal() {
        wait.until(ExpectedConditions.elementToBeClickable(btnAdd)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(categoryModal));
    }

    public void fillForm(String name, String status) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(txtCategoryName));
        input.clear();
        input.sendKeys(name);
        if (status != null && !status.isEmpty()) {
            new Select(driver.findElement(ddlStatus)).selectByVisibleText(status);
        }
    }

    public void clickCreate() {
        driver.findElement(btnCreate).click();
        try { wait.until(ExpectedConditions.invisibilityOfElementLocated(modalOverlay)); } catch (Exception e) {}
    }

    // US2-MC-02 - Task 3 - Edit Category
    public void editFirstCategory(String newName) {
        wait.until(ExpectedConditions.elementToBeClickable(firstRowEditIcon)).click();
        try { wait.until(ExpectedConditions.alertIsPresent()).accept(); } catch (Exception e) {}
        wait.until(ExpectedConditions.visibilityOfElementLocated(categoryModal));

        fillForm(newName, "Active");
        driver.findElement(btnUpdate).click();

        try { wait.until(ExpectedConditions.invisibilityOfElementLocated(modalOverlay)); } catch (Exception e) {}
    }

    // US2-MC-02 - Task 4 - Duplicate Validation
    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(lblErrorMessage)).getText();
    }

    // CLEANUP

    public void deleteMainCategoryByName(String categoryName) {
        try {
            searchFor(categoryName);
            By deleteBtn = By.xpath("//td[contains(text(),'" + categoryName + "')]/..//a[contains(@id,'lnkDelete')]");
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(deleteBtn));

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            wait.until(ExpectedConditions.alertIsPresent()).accept();
            try { clickClear(); } catch (Exception ignore) {}
        } catch (Exception e) { /* Ignore */ }
    }

    public void cleanUpAllTestArtifacts() {
        String[] artifacts = {"Auto_", "Dup_", "PreEdit_", "PostEdit_", "AutoSync_"};
        for (String artifact : artifacts) {
            deleteMainCategoryByName(artifact);
        }
    }
}