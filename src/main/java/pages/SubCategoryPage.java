package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SubCategoryPage {
    private WebDriver driver;
    private WebDriverWait wait;

    private By categoryMenu = By.xpath("//a[contains(@class,'nav-link')]/span[text()='Category']");
    private By subCategoryLink = By.xpath("//a[contains(@href,'AdminCreateSubCategory.aspx')]");

    private By btnAddSubCategory = By.linkText("Add Subcategory");
    private By btnRefresh = By.xpath("//a[contains(@onclick, 'location.reload')]");
    private By table = By.id("ContentPlaceHolder_Admin_gvSubCategories");

    private By ddlAddMainCategory = By.id("ContentPlaceHolder_Admin_ddlMainCategory");
    private By txtAddName = By.id("ContentPlaceHolder_Admin_txtSubCategory");
    private By ddlAddStatus = By.id("ContentPlaceHolder_Admin_ddlStatus");
    private By btnSaveAdd = By.id("ContentPlaceHolder_Admin_btnSaveSubCategory");

    private By txtEditName = By.xpath("//input[contains(@id,'txtSubCategory')]");
    private By ddlEditStatus = By.xpath("//select[contains(@id,'ddlStatus')]");
    private By btnUpdate = By.xpath("//a[contains(@id,'lnkUpdate')]");

    public SubCategoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void hardResetAndNavigate() {
        // FIX: Catch any lingering delayed alerts before attempting to refresh the page
        try { driver.switchTo().alert().accept(); } catch (Exception ignored) {}

        driver.navigate().refresh();

        // Wait for page to fully load
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));

        WebElement menu = wait.until(ExpectedConditions.presenceOfElementLocated(categoryMenu));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menu);

        WebElement link = wait.until(ExpectedConditions.presenceOfElementLocated(subCategoryLink));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
    }

    public boolean isPaginationVisible() {
        return !driver.findElements(By.xpath("//table//a[contains(@href,'Page')]")).isEmpty()
                || !driver.findElements(By.xpath("//table//span[text()='1']")).isEmpty();
    }

    public void clickAddSubCategory() {
        WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(btnAddSubCategory));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addBtn);
    }

    public void createSubCategory(String parentName, String subName, String status) {
        hardResetAndNavigate();
        String main = driver.getWindowHandle();
        clickAddSubCategory();

        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String h : driver.getWindowHandles()) {
            if (!h.equals(main)) driver.switchTo().window(h);
        }

        new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(ddlAddMainCategory))).selectByVisibleText(parentName);
        driver.findElement(txtAddName).sendKeys(subName);
        new Select(driver.findElement(ddlAddStatus)).selectByVisibleText(status);

        WebElement saveBtn = driver.findElement(btnSaveAdd);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);

        try { wait.until(ExpectedConditions.alertIsPresent()).accept(); } catch (Exception e) {}

        if (driver.getWindowHandles().size() > 1) driver.close();
        driver.switchTo().window(main);
        driver.navigate().refresh();
    }

    public void clickEditButton(String targetName) {
        WebElement editBtn = driver.findElement(By.xpath("//tr[td[contains(., '" + targetName + "')]]//a[contains(@id, 'lnkEdit')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editBtn);
    }

    public void saveEdit(String newName, String status) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(txtEditName));
        input.clear();
        input.sendKeys(newName);
        new Select(driver.findElement(ddlEditStatus)).selectByVisibleText(status);

        WebElement updateBtnElement = driver.findElement(btnUpdate);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", updateBtnElement);

        try { wait.until(ExpectedConditions.alertIsPresent()).accept(); } catch (Exception e) {}
    }

    public String deleteSubCategoryByName(String targetName) {
        WebElement oldTable = driver.findElement(table);

        WebElement deleteBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//tr[td[contains(., '" + targetName + "')]]//a[contains(@id, 'lnkDelete')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);

        wait.until(ExpectedConditions.alertIsPresent()).accept();

        Alert successAlert = wait.until(ExpectedConditions.alertIsPresent());
        String alertText = successAlert.getText();
        successAlert.accept();

        try { wait.until(ExpectedConditions.stalenessOf(oldTable)); } catch (Exception e) {}

        return alertText;
    }

    public List<String> getAddPageDropdownOptions() {
        wait.until(d -> new Select(d.findElement(ddlAddMainCategory)).getOptions().size() > 0);
        Select select = new Select(driver.findElement(ddlAddMainCategory));

        List<String> textOptions = new ArrayList<>();
        for (WebElement option : select.getOptions()) {
            textOptions.add(option.getText().trim());
        }
        return textOptions;
    }

    public String getEditNameValue() {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(txtEditName));
        return input.getAttribute("value");
    }

    public String handleDualDeleteAlerts() {
        WebElement oldTable = driver.findElement(table);

        wait.until(ExpectedConditions.alertIsPresent()).accept();

        Alert successAlert = wait.until(ExpectedConditions.alertIsPresent());
        String alertText = successAlert.getText();
        successAlert.accept();

        try { wait.until(ExpectedConditions.stalenessOf(oldTable)); } catch (Exception e) {}

        return alertText;
    }

    public void clickRefreshButton() {
        WebElement refreshBtn = driver.findElement(btnRefresh);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", refreshBtn);
    }

    public boolean searchAcrossPages(String targetName) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(table));

        while (true) {
            List<WebElement> rows = driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr"));
            for (WebElement row : rows) {
                if (row.getText().contains(targetName)) return true;
            }

            List<WebElement> nextPager = driver.findElements(By.xpath("//table[contains(@id,'gvSubCategories')]//tr[last()]//td[span]/following-sibling::td[1]/a"));
            if (nextPager.size() > 0) {
                WebElement oldTable = driver.findElement(table);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextPager.get(0));
                wait.until(ExpectedConditions.stalenessOf(oldTable));
            } else {
                return false;
            }
        }
    }

    public int countOccurrencesAcrossPages(String targetName) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(table));
        int count = 0;

        while (true) {
            List<WebElement> rows = driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr"));
            for (WebElement row : rows) {
                if (row.getText().contains(targetName)) count++;
            }

            List<WebElement> nextPager = driver.findElements(By.xpath("//table[contains(@id,'gvSubCategories')]//tr[last()]//td[span]/following-sibling::td[1]/a"));
            if (nextPager.size() > 0) {
                WebElement oldTable = driver.findElement(table);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextPager.get(0));
                wait.until(ExpectedConditions.stalenessOf(oldTable));
            } else {
                break;
            }
        }
        return count;
    }

    public void cleanUpSubCategoryArtifacts() {
        String[] artifacts = {"AutoSub-", "AutoReflect-", "Retire_", "RetireUp_", "ToDelete_", "Child_"};

        for (String artifact : artifacts) {
            hardResetAndNavigate();
            while (searchAcrossPages(artifact)) {
                WebElement oldTable = driver.findElement(table);
                WebElement deleteBtn = driver.findElement(By.xpath("//tr[td[contains(., '" + artifact + "')]]//a[contains(@id, 'lnkDelete')]"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);

                // Alert 1
                wait.until(ExpectedConditions.alertIsPresent()).accept();

                // Alert 2 (Increased wait slightly to guarantee we catch the delayed success alert)
                try {
                    WebDriverWait alertWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                    alertWait.until(ExpectedConditions.alertIsPresent()).accept();
                } catch(Exception e) {}

                // Wait for Auto-Refresh safely
                try { wait.until(ExpectedConditions.stalenessOf(oldTable)); } catch(Exception e) {}

                hardResetAndNavigate();
            }
        }
    }
}