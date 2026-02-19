package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class SubCategoryPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // --- LOCATORS ---
    // Navigation
    private By categoryMenu = By.xpath("//a[contains(@class,'nav-link')]/span[text()='Category']");
    private By subCategoryLink = By.xpath("//a[contains(@href,'AdminCreateSubCategory.aspx')]");

    // Main Page Elements
    private By btnAddSubCategory = By.linkText("Add Subcategory");
    private By btnRefresh = By.xpath("//a[contains(@onclick, 'location.reload')]");
    private By table = By.id("ContentPlaceHolder_Admin_gvSubCategories");
    private By tableHeaders = By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//th");

    // Add Page Elements
    private By txtAddName = By.id("ContentPlaceHolder_Admin_txtSubCategory");
    private By ddlAddMainCategory = By.id("ContentPlaceHolder_Admin_ddlMainCategory");
    private By ddlAddStatus = By.id("ContentPlaceHolder_Admin_ddlStatus");
    private By btnSaveAdd = By.id("ContentPlaceHolder_Admin_btnSaveSubCategory");

    // Edit Page Elements
    private By txtEditName = By.xpath("//input[contains(@id,'txtSubCategoryName')]");
    private By ddlEditStatus = By.xpath("//select[contains(@id,'ddlStatus')]");
    private By btnUpdate = By.xpath("//a[text()='Update' or contains(@id,'lnkUpdate')]");

    public SubCategoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // --- NAVIGATION ---
    public void hardResetAndNavigate() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var modals = document.querySelectorAll('.modal, .modal-backdrop');" +
                            "modals.forEach(m => m.remove());" +
                            "document.body.classList.remove('modal-open');"
            );
            wait.until(ExpectedConditions.elementToBeClickable(categoryMenu)).click();
            wait.until(ExpectedConditions.elementToBeClickable(subCategoryLink)).click();
        } catch (Exception e) {
            driver.navigate().refresh();
            wait.until(ExpectedConditions.elementToBeClickable(categoryMenu)).click();
            wait.until(ExpectedConditions.elementToBeClickable(subCategoryLink)).click();
        }
    }

    public void clickAddSubCategory() {
        wait.until(ExpectedConditions.elementToBeClickable(btnAddSubCategory)).click();
    }

    // Checks if the Refresh button (alternative to filter) is present
    public boolean isRefreshButtonVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(btnRefresh)).isDisplayed();
        } catch (Exception e) { return false; }
    }

    // Gets headers to verify columns like 'Status'
    public List<String> getTableHeaders() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(table));
        return driver.findElements(tableHeaders).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    // Used inside the Add Window
    public List<String> getAddPageDropdownOptions() {
        wait.until(d -> new Select(d.findElement(ddlAddMainCategory)).getOptions().size() > 0);
        Select select = new Select(driver.findElement(ddlAddMainCategory));
        return select.getOptions().stream().map(opt -> opt.getText().trim()).collect(Collectors.toList());
    }

    public boolean isPaginationVisible() {
        return !driver.findElements(By.xpath("//table//a[contains(@href,'Page')]")).isEmpty()
                || !driver.findElements(By.xpath("//table//span[text()='1']")).isEmpty();
    }

    // --- ADD PAGE ACTIONS ---
    public void fillAndSaveAddForm(String name, int mainCategoryIndex, String status) {
        WebElement txtName = wait.until(ExpectedConditions.visibilityOfElementLocated(txtAddName));
        txtName.clear();
        txtName.sendKeys(name);
        new Select(driver.findElement(ddlAddMainCategory)).selectByIndex(mainCategoryIndex);
        new Select(driver.findElement(ddlAddStatus)).selectByVisibleText(status);
        driver.findElement(btnSaveAdd).click();
    }

    // --- SEARCH & EDIT ACTIONS ---
    public boolean searchAndLocateSubCategory(String subCategoryName) {
        boolean found = false;
        int pageIndex = 1;

        while (!found && pageIndex < 10) {
            List<WebElement> elements = driver.findElements(By.xpath("//span[contains(normalize-space(),'" + subCategoryName + "')]"));

            if (!elements.isEmpty()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", elements.get(0));
                return true;
            }

            pageIndex++;
            List<WebElement> nextPager = driver.findElements(By.xpath("//table//td/a[text()='" + pageIndex + "']"));

            if (!nextPager.isEmpty()) {
                WebElement oldTable = driver.findElement(table);
                nextPager.get(0).click();
                try {
                    wait.until(ExpectedConditions.stalenessOf(oldTable));
                    wait.until(ExpectedConditions.visibilityOfElementLocated(table));
                } catch (Exception e) {
                    try { Thread.sleep(1000); } catch (InterruptedException ex) {}
                }
            } else {
                break;
            }
        }
        return false;
    }

    public void clickEditForSubCategory(String subCategoryName) {
        By editXpath = By.xpath("//span[contains(normalize-space(),'" + subCategoryName + "')]/ancestor::tr//a[contains(@id,'lnkEdit')]");
        wait.until(ExpectedConditions.elementToBeClickable(editXpath)).click();
    }

    // --- EDIT PAGE ACTIONS ---
    public String getEditNameFieldValue() {
        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(txtEditName));
        return nameInput.getAttribute("value");
    }

    public void performUpdate(String newName, String status) {
        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(txtEditName));
        nameInput.clear();
        nameInput.sendKeys(newName);

        if (status != null) {
            Select select = new Select(driver.findElement(ddlEditStatus));
            select.selectByVisibleText(status);
        }

        driver.findElement(btnUpdate).click();

        try {
            wait.until(ExpectedConditions.alertIsPresent()).accept();
        } catch (TimeoutException e) { /* Ignore */ }
    }

    public boolean isSubCategoryVisibleInTable(String name) {
        try {
            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//span[contains(normalize-space(),'" + name.trim() + "')]")
            ));
            return el.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}