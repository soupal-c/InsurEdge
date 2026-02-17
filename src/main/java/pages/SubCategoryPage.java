package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class SubCategoryPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // --- Locators based on your HTML ---
    private By categoryMenu = By.xpath("//a[contains(@class,'nav-link')]/span[text()='Category']");
    private By subCategoryLink = By.xpath("//a[contains(@href,'AdminCreateSubCategory.aspx')]");
    // Matches the 'Add Subcategory' button that opens a new tab
    private By btnAddSubCategory = By.linkText("Add Subcategory");
    // Matches the dropdown in the new tab
    private By ddlMainCategory = By.id("ContentPlaceHolder_Admin_ddlMainCategory");

    public SubCategoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    /**
     * SUITE FIX: Navigates to the page but first NUKES any blocking modals
     * or backdrops left over from previous tests in the suite.
     */
    public void hardResetAndNavigate() {
        try {
            // 1. Javascript to physically remove modal elements from the DOM
            ((JavascriptExecutor) driver).executeScript(
                    "var modals = document.querySelectorAll('.modal, .modal-backdrop');" +
                            "modals.forEach(m => m.remove());" +
                            "document.body.classList.remove('modal-open');"
            );

            // 2. Safe Click on Sidebar
            wait.until(ExpectedConditions.elementToBeClickable(categoryMenu)).click();
            wait.until(ExpectedConditions.elementToBeClickable(subCategoryLink)).click();

        } catch (Exception e) {
            System.out.println("Robin Recovery: Retrying navigation...");
            // Fallback: Refresh and try again
            driver.navigate().refresh();
            wait.until(ExpectedConditions.elementToBeClickable(categoryMenu)).click();
            wait.until(ExpectedConditions.elementToBeClickable(subCategoryLink)).click();
        }
    }

    public void clickAddSubCategory() {
        wait.until(ExpectedConditions.elementToBeClickable(btnAddSubCategory)).click();
    }

    public List<String> getDropdownOptions() {
        // ROBIN FIX: Wait for the dropdown to actually populate with data from DB
        // checks that there is more than 1 option (the default is usually "--Select--")
        wait.until(d -> new Select(d.findElement(ddlMainCategory)).getOptions().size() > 1);

        Select select = new Select(driver.findElement(ddlMainCategory));
        return select.getOptions().stream()
                .map(opt -> opt.getText().trim())
                .collect(Collectors.toList());
    }
}