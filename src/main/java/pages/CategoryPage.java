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
    private By categoryMenu = By.xpath("//a[contains(@class,'nav-link')]/span[text()='Category']");
    private By mainCategoryLink = By.xpath("//a[contains(@href, 'AdminCreateMainCategory.aspx')]");
    private By pageHeader = By.xpath("//div[@class='pagetitle']/h1");
    private By tableHeaders = By.xpath("//table[@id='ContentPlaceHolder_Admin_gvCategories']//th");

    // Search & Table
    private By txtSearch = By.id("ContentPlaceHolder_Admin_txtSearch");
    private By btnSearch = By.id("ContentPlaceHolder_Admin_btnSearch");
    private By btnClear = By.id("ContentPlaceHolder_Admin_btnClear");
    private By table = By.id("ContentPlaceHolder_Admin_gvCategories");

    // Modal
    private By btnAdd = By.id("ContentPlaceHolder_Admin_btnAdd");
    private By categoryModal = By.id("categoryModal");
    private By txtCategoryName = By.id("ContentPlaceHolder_Admin_txtCategoryName");
    private By ddlStatus = By.id("ContentPlaceHolder_Admin_ddlStatus");
    private By btnCreate = By.xpath("//input[@value='Create']");
    private By btnUpdate = By.xpath("//input[@value='Update']");
    private By modalOverlay = By.className("modal-backdrop");

    // Validation
    private By lblErrorMessage = By.id("ContentPlaceHolder_Admin_lblMessage");

    public CategoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // --- NAVIGATION & UI (Robust) ---

    public void navigateToMainCategory() {
        // 1. Aggressively clean up any stuck modals/backdrops
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var modals = document.querySelectorAll('.modal');" +
                            "modals.forEach(m => { m.classList.remove('show'); m.style.display = 'none'; });" +
                            "var backdrops = document.querySelectorAll('.modal-backdrop');" +
                            "backdrops.forEach(b => b.remove());" +
                            "document.body.classList.remove('modal-open');" +
                            "document.body.style.paddingRight = '0px';"
            );
        } catch (Exception e) { /* Ignore */ }

        // 2. Use JS Click to avoid interception
        try {
            WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(categoryMenu));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menu);

            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(mainCategoryLink));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
        } catch (Exception e) {
            // Fallback
            driver.findElement(categoryMenu).click();
            driver.findElement(mainCategoryLink).click();
        }
    }

    public String getPageTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeader)).getText();
    }

    public List<String> getTableHeaders() {
        List<WebElement> elements = driver.findElements(tableHeaders);
        List<String> texts = new ArrayList<>();
        for (WebElement e : elements) texts.add(e.getText().trim());
        return texts;
    }

    // --- INDEX BASED METHODS (Robust) ---

    private WebElement getRowByIndex(int index) {
        int xpathIndex = index + 1;
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table[@id='ContentPlaceHolder_Admin_gvCategories']//tr[" + xpathIndex + "]")
        ));
    }

    public String getCategoryNameByIndex(int index) {
        try {
            WebElement row = getRowByIndex(index);
            return row.findElement(By.xpath(".//td[1]")).getText().trim();
        } catch (Exception e) {
            return null;
        }
    }

    public void editCategoryByIndex(int index, String newName) {
        WebElement row = getRowByIndex(index);
        WebElement editBtn = row.findElement(By.xpath(".//a[contains(@id, 'lnkEdit')]"));

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editBtn);

        try {
            wait.until(ExpectedConditions.alertIsPresent()).accept();
        } catch (TimeoutException e) { /* Alert might be suppressed */ }

        wait.until(ExpectedConditions.visibilityOfElementLocated(categoryModal));
        fillForm(newName, "Active");

        WebElement updateBtn = driver.findElement(btnUpdate);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", updateBtn);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(modalOverlay));
    }

    public void deleteCategoryByIndex(int index) {
        WebElement row = getRowByIndex(index);
        WebElement deleteBtn = row.findElement(By.xpath(".//a[contains(@id, 'lnkDelete')]"));

        WebElement oldTable = driver.findElement(table);

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);
        wait.until(ExpectedConditions.alertIsPresent()).accept();

        wait.until(ExpectedConditions.stalenessOf(oldTable));
    }

    public void deleteCategoryByIndexAndCancel(int index) {
        WebElement row = getRowByIndex(index);
        WebElement deleteBtn = row.findElement(By.xpath(".//a[contains(@id, 'lnkDelete')]"));

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);
        wait.until(ExpectedConditions.alertIsPresent()).dismiss();
    }

    public boolean areRowActionIconsVisible() {
        try {
            WebElement row = getRowByIndex(1);
            boolean edit = row.findElement(By.xpath(".//a[contains(@id, 'lnkEdit')]")).isDisplayed();
            boolean delete = row.findElement(By.xpath(".//a[contains(@id, 'lnkDelete')]")).isDisplayed();
            return edit && delete;
        } catch (Exception e) { return false; }
    }

    // --- SEARCH & CRUD (Robust) ---

    public void searchFor(String keyword) {
        WebElement box = wait.until(ExpectedConditions.visibilityOfElementLocated(txtSearch));
        box.clear();
        box.sendKeys(keyword);

        WebElement oldTable = driver.findElement(table);
        WebElement btn = driver.findElement(btnSearch);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);

        try { wait.until(ExpectedConditions.stalenessOf(oldTable)); } catch (TimeoutException e) {}
    }

    public void clickClear() {
        try {
            if (driver.findElements(table).isEmpty()) return;
            WebElement oldTable = driver.findElement(table);
            WebElement btn = driver.findElement(btnClear);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            wait.until(ExpectedConditions.stalenessOf(oldTable));
        } catch (Exception e) { /* Ignore */ }
    }

    public boolean isCategoryInTable(String name) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(table)).getText().contains(name);
        } catch (Exception e) { return false; }
    }

    public void openAddModal() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(btnAdd));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
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
        WebElement btn = driver.findElement(btnCreate);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(modalOverlay));
    }

    public void clickCreateFailureExpected() {
        WebElement btn = driver.findElement(btnCreate);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(lblErrorMessage)).getText();
    }

    // --- HELPER FOR SUB-CATEGORY CLEANUP ---
    // This restores the missing method while using the new Index Logic internally
    // --- SAFER CLEANUP METHOD ---
    public void deleteMainCategoryByName(String name) {
        try {
            // 1. Filter the table
            searchFor(name);

            // 2. SAFETY CHECK: Only delete if the FIRST row actually matches the name
            // This prevents deleting "Life Insurance" if the search mistakenly shows all records
            String firstRowName = getCategoryNameByIndex(1);

            if (firstRowName != null && firstRowName.contains(name)) {
                deleteCategoryByIndex(1);
                System.out.println("Safely deleted test artifact: " + firstRowName);
            } else {
                System.out.println("Skipped deletion: Row 1 [" + firstRowName + "] did not match target [" + name + "]");
            }

            // 3. Clear search to reset table
            clickClear();
        } catch (Exception e) {
            // Ensure search is cleared even if something fails
            try { clickClear(); } catch (Exception ex) {}
        }
    }

    public void cleanUpAllTestArtifacts() {
        // These are the ONLY prefixes we will ever try to delete
        String[] artifacts = {"Auto_", "Dup_", "PreEdit_", "PostEdit_", "Delete_", "SyncTest_", "Retirement_"};

        for (String artifact : artifacts) {
            // Loop to delete all occurrences (in case multiple tests ran)
            try {
                if (driver.findElements(table).isEmpty()) return;

                searchFor(artifact);
                // Keep deleting as long as the first row matches our test prefix
                while (true) {
                    String rowName = getCategoryNameByIndex(1);
                    if (rowName != null && rowName.contains(artifact)) {
                        deleteCategoryByIndex(1);
                        clickClear(); // Reset to refresh table state
                        searchFor(artifact); // Search again to see if more exist
                    } else {
                        break; // Stop if table is empty OR first row is not a test artifact
                    }
                }
                clickClear();
            } catch (Exception e) {
                try { clickClear(); } catch (Exception ex) {}
            }
        }
    }
}