package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class SubCategoryPage {
    private WebDriver driver;
    private WebDriverWait wait;


    // LOCATORS

    // Navigation
    private By categoryMenu = By.xpath("//a[contains(@class,'nav-link')]/span[text()='Category']");
    private By subCategoryLink = By.xpath("//a[contains(@href,'AdminCreateSubCategory.aspx')]");

    // US2-SC-01 - SubCategory Management UI

    // Task 1 - UI Elements

    // Task 2 - Pagination


    // US2-SC-02 - SubCategory Functional Flows

    // Task 1 - Dropdown Sync Elements
    private By btnAddSubCategory = By.linkText("Add Subcategory");
    private By ddlMainCategory = By.id("ContentPlaceHolder_Admin_ddlMainCategory");

    // Task 2 - Add SubCategory Elements

    // Task 3 - List Refreshing


    // US2-SC-03


    // US2-SC-04


    // DEFECTS

    public SubCategoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }


    // ACTIONS

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

    //US2-SC-01 - Task 1

    //US2-SC-01 - Task 2



    // US2-SC-02 - Task 1 - Dropdown Sync
    public void clickAddSubCategory() {
        wait.until(ExpectedConditions.elementToBeClickable(btnAddSubCategory)).click();
    }

    public List<String> getDropdownOptions() {
        wait.until(d -> new Select(d.findElement(ddlMainCategory)).getOptions().size() > 1);
        Select select = new Select(driver.findElement(ddlMainCategory));
        return select.getOptions().stream().map(opt -> opt.getText().trim()).collect(Collectors.toList());
    }

    // US2-SC-02 - Task 2 - Add SubCategory Actions

    // US2-SC-02 - Task 3



    // US2-SC-03 - Task 1

    // US2-SC-03 - Task 2



    // US2-SC-04 - Task 1

    // US2-SC-04 - Task 2

}