package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class MainCategoryPage {
    private WebDriver driver;
    private WebDriverWait wait;

    private By categoryMenu = By.xpath("//a[contains(@class,'nav-link')]/span[text()='Category']");
    private By mainCategoryLink = By.xpath("//a[contains(@href, 'AdminCreateMainCategory.aspx')]");
    private By pageHeader = By.xpath("//div[@class='pagetitle']/h1");
    private By importButton = By.id("ContentPlaceHolder_Admin_btnImport");

    private By txtSearch = By.id("ContentPlaceHolder_Admin_txtSearch");
    private By btnSearch = By.id("ContentPlaceHolder_Admin_btnSearch");
    private By btnClear = By.id("ContentPlaceHolder_Admin_btnClear");
    private By table = By.id("ContentPlaceHolder_Admin_gvCategories");

    private By btnAdd = By.id("ContentPlaceHolder_Admin_btnAdd");
    private By categoryModal = By.id("categoryModal");
    private By txtCategoryName = By.id("ContentPlaceHolder_Admin_txtCategoryName");
    private By ddlStatus = By.id("ContentPlaceHolder_Admin_ddlStatus");
    private By btnCreate = By.xpath("//input[@value='Create']");
    private By btnUpdate = By.xpath("//input[@value='Update']");
    private By modalOverlay = By.className("modal-backdrop");
    private By lblErrorMessage = By.id("ContentPlaceHolder_Admin_lblMessage");

    public MainCategoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void navigateToMainCategory() {
        driver.navigate().refresh();
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));

        WebElement menu = wait.until(ExpectedConditions.presenceOfElementLocated(categoryMenu));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menu);

        WebElement link = wait.until(ExpectedConditions.presenceOfElementLocated(mainCategoryLink));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
    }

    public String getPageTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeader)).getText();
    }

    public boolean areRowActionIconsVisible() {
        try {
            WebElement row = getRowByIndex(1);
            boolean edit = row.findElement(By.xpath(".//a[contains(@id, 'lnkEdit')]")).isDisplayed();
            boolean delete = row.findElement(By.xpath(".//a[contains(@id, 'lnkDelete')]")).isDisplayed();
            return edit && delete;
        } catch (Exception e) { return false; }
    }

    public void searchFor(String keyword) {
        WebElement box = wait.until(ExpectedConditions.visibilityOfElementLocated(txtSearch));
        box.clear();
        box.sendKeys(keyword);

        WebElement oldTable = driver.findElement(table);
        WebElement searchBtn = driver.findElement(btnSearch);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", searchBtn);

        try { wait.until(ExpectedConditions.stalenessOf(oldTable)); } catch (Exception e) {}
    }

    public void clickClear() {
        try {
            if (driver.findElements(table).isEmpty()) return;
            WebElement oldTable = driver.findElement(table);
            WebElement clearBtn = driver.findElement(btnClear);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", clearBtn);
            wait.until(ExpectedConditions.stalenessOf(oldTable));
        } catch (Exception e) {}
    }

    public void openAddModal() {
        WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(btnAdd));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addBtn);
        wait.until(ExpectedConditions.visibilityOfElementLocated(categoryModal));
    }

    public void fillForm(String name, String status) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(txtCategoryName));
        input.clear();
        input.sendKeys(name);
        new Select(driver.findElement(ddlStatus)).selectByVisibleText(status);
    }

    public void clickCreate() {
        WebElement submitBtn = driver.findElement(btnCreate);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtn);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(modalOverlay));
    }

    public void clickCreateFailureExpected() {
        WebElement submitBtn = driver.findElement(btnCreate);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtn);
    }

    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(lblErrorMessage)).getText();
    }

    private WebElement getRowByIndex(int index) {
        int xpathIndex = index + 1;
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table[@id='ContentPlaceHolder_Admin_gvCategories']//tr[" + xpathIndex + "]")
        ));
    }

    public String getCategoryNameByIndex(int index) {
        try {
            return getRowByIndex(index).findElement(By.xpath(".//td[1]")).getText().trim();
        } catch (Exception e) {
            return null;
        }
    }

    public void editCategoryByIndex(int index, String newName) {
        WebElement editBtn = getRowByIndex(index).findElement(By.xpath(".//a[contains(@id, 'lnkEdit')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editBtn);

        try { wait.until(ExpectedConditions.alertIsPresent()).accept(); } catch (Exception e) {}

        wait.until(ExpectedConditions.visibilityOfElementLocated(categoryModal));
        fillForm(newName, "Active");
        WebElement updateBtnElement = driver.findElement(btnUpdate);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", updateBtnElement);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(modalOverlay));
    }

    // FIX: Handled the dual-alert and auto-refresh logic
    public void deleteCategoryByIndex(int index) {
        WebElement oldTable = driver.findElement(table);
        WebElement deleteBtn = getRowByIndex(index).findElement(By.xpath(".//a[contains(@id, 'lnkDelete')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);

        // 1. Accept "Are you sure?"
        wait.until(ExpectedConditions.alertIsPresent()).accept();

        // 2. Accept "Deleted Successfully!"
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            shortWait.until(ExpectedConditions.alertIsPresent()).accept();
        } catch (Exception e) {}

        // 3. Wait for the automatic page refresh to finish before moving on
        try { wait.until(ExpectedConditions.stalenessOf(oldTable)); } catch (Exception e) {}
    }

    public void deleteCategoryByIndexAndCancel(int index) {
        WebElement deleteBtn = getRowByIndex(index).findElement(By.xpath(".//a[contains(@id, 'lnkDelete')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);
        wait.until(ExpectedConditions.alertIsPresent()).dismiss();
    }

    public WebElement getImportButton() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(importButton));
    }

    public void clickImport() {
        try {
            ((JavascriptExecutor) driver).executeScript("document.getElementById('ContentPlaceHolder_Admin_lblMessage').innerText = '';");
        } catch (Exception e) {}

        WebElement impBtn = getImportButton();
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", impBtn);
    }

    public boolean isImportResponseVisible() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            return shortWait.until(d -> {
                WebElement msg = d.findElement(By.id("ContentPlaceHolder_Admin_lblMessage"));
                boolean hasNewMessage = msg.isDisplayed() && msg.getText().trim().length() > 0;
                boolean hasModal = !d.findElements(By.cssSelector(".modal.show")).isEmpty();
                return hasNewMessage || hasModal;
            });
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void cleanUpAllTestArtifacts() {
        String[] artifacts = {"Auto_", "Dup_", "PreEdit_", "PostEdit_", "Delete_", "Parent_", "Cancel_", "Del_"};

        for (String artifact : artifacts) {
            try {
                if (driver.findElements(table).isEmpty()) return;
                searchFor(artifact);
                while (true) {
                    String rowName = getCategoryNameByIndex(1);
                    if (rowName != null && rowName.contains(artifact)) {
                        deleteCategoryByIndex(1);
                        clickClear();
                        searchFor(artifact);
                    } else {
                        break;
                    }
                }
                clickClear();
            } catch (Exception e) {
                try { clickClear(); } catch (Exception ex) {}
            }
        }
    }
}