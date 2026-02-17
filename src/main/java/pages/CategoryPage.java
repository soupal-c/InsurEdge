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
    private By categoryMenu = By.xpath("//a[contains(@class,'nav-link')]/span[text()='Category']");
    private By mainCategoryLink = By.xpath("//a[contains(@href, 'AdminCreateMainCategory.aspx')]");

    // MC-01 UI
    private By pageHeader = By.xpath("//div[@class='pagetitle']/h1");
    private By breadcrumbs = By.xpath("//ol[@class='breadcrumb']/li");
    private By tableHeaders = By.xpath("//table[@id='ContentPlaceHolder_Admin_gvCategories']//th");
    private By editIcon = By.xpath("//table[contains(@id, 'gvCategories')]//tr[2]//a[contains(@id, 'lnkEdit')]");
    private By deleteIcon = By.xpath("//table[contains(@id, 'gvCategories')]//tr[2]//a[contains(@id, 'lnkDelete')]");

    // MC-02 Functional
    private By searchInput = By.id("ContentPlaceHolder_Admin_txtSearch");
    private By searchBtn = By.id("ContentPlaceHolder_Admin_btnSearch");
    private By clearBtn = By.id("ContentPlaceHolder_Admin_btnClear");

    private By addBtn = By.id("ContentPlaceHolder_Admin_btnAdd");
    private By categoryModal = By.id("categoryModal");
    private By nameInput = By.id("ContentPlaceHolder_Admin_txtCategoryName");
    private By statusDropdown = By.id("ContentPlaceHolder_Admin_ddlStatus");
    private By createBtn = By.xpath("//input[@value='Create']");
    private By updateBtn = By.xpath("//input[@value='Update']");
    private By cancelBtn = By.xpath("//input[@value='Cancel']");
    private By errorMessage = By.id("ContentPlaceHolder_Admin_lblMessage");
    private By table = By.id("ContentPlaceHolder_Admin_gvCategories");

    // --- Additional Locators ---
    private By btnAdd = By.cssSelector("[id*='btnAdd']");
    private By txtCategoryName = By.cssSelector("input[id*='txtCategoryName']");
    private By btnSave = By.cssSelector("[id*='btnSave']");
    private By successMessage = By.xpath("//div[contains(@class, 'alert-success')]");
    private By modalOverlay = By.className("modal-backdrop");

    public CategoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // --- ACTIONS ---

    public void handleStuckModal() {
        try {
            if (driver.findElements(categoryModal).size() > 0 && driver.findElement(categoryModal).isDisplayed()) {
                driver.findElement(cancelBtn).click();
                wait.until(ExpectedConditions.invisibilityOfElementLocated(categoryModal));
            }
        } catch (Exception e) { /* Ignore if no modal */ }
    }

    public void waitForModalToClose() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(categoryModal));
    }

    public void navigateToMainCategory() {
        // Step 1: Attempt to clear any blocking modals via JS (The "Force" move)
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var backdrops = document.getElementsByClassName('modal-backdrop');" +
                            "for(var i=0; i<backdrops.length; i++){backdrops[i].remove();}" +
                            "document.body.classList.remove('modal-open');"
            );
        } catch (Exception e) {
            // If it's not there, no harm done
        }

        // Step 2: Use JS Click to navigate - this is much more stable
        WebElement menu = wait.until(ExpectedConditions.presenceOfElementLocated(categoryMenu));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menu);

        WebElement link = wait.until(ExpectedConditions.presenceOfElementLocated(mainCategoryLink));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
    }

    // UI Methods
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
    public boolean areActionIconsVisible() {
        try {
            return driver.findElement(editIcon).isDisplayed() && driver.findElement(deleteIcon).isDisplayed();
        } catch (Exception e) { return false; }
    }

    // Functional Methods
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

    public void openAddModal() {
        handleStuckModal(); // Ensure clean slate
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
    }

    public void editFirstCategory(String newName) {
        wait.until(ExpectedConditions.elementToBeClickable(editIcon)).click();
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.accept();
        } catch (Exception e) { /* No alert */ }

        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
        input.clear();
        input.sendKeys(newName);
        driver.findElement(updateBtn).click();
        waitForModalToClose();
    }

    public boolean isCategoryInTable(String name) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(table)).getText().contains(name);
        } catch (Exception e) { return false; }
    }
    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText();
    }

    public void createMainCategory(String categoryName) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(modalOverlay));
        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(btnAdd));
        addButton.click();

        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(txtCategoryName));
        nameInput.clear();
        nameInput.sendKeys(categoryName);

        wait.until(ExpectedConditions.elementToBeClickable(btnSave)).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(modalOverlay));
    }

    // --- CLEANUP METHOD ---
    public void deleteMainCategoryByName(String categoryName) {
        try {
            // 1. Locate Search Box using class variables (Better reliability)
            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
            searchBox.clear();
            searchBox.sendKeys(categoryName);

            // 2. CRITICAL: Click the Search Button! (This was missing before)
            driver.findElement(searchBtn).click();

            // 3. Wait for the specific row to appear
            // Using 'lnkDelete' because your locators above use 'lnkDelete', not 'btnDelete'
            By deleteBtn = By.xpath("//td[contains(text(),'" + categoryName + "')]/..//a[contains(@id,'lnkDelete')]");
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(deleteBtn));

            // 4. Click & Confirm
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            wait.until(ExpectedConditions.alertIsPresent()).accept();

            System.out.println("Robin: Main Category '" + categoryName + "' deleted.");

            // Optional: Click Clear to reset the table for the next test
            try { driver.findElement(clearBtn).click(); } catch (Exception ignore) {}

        } catch (Exception e) {
            System.out.println("Robin Alert: Cleanup failed. Category might already be gone.");
        }
    }
}