package tests;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pages.MainCategoryPage;
import pages.SubCategoryPage;
import java.util.List;

public class GlobalDeletionTest extends BaseTest {

    private MainCategoryPage catPage;
    private SubCategoryPage subPage;

    private String targetCategory = nameGenerator("Del_");

    @BeforeClass
    public void setupPages() {
        catPage = new MainCategoryPage(driver);
        subPage = new SubCategoryPage(driver);
    }

    @Test(priority = 1)
    public void US2_DEF_01_Task1_DeleteMainCategory() {
        catPage.navigateToMainCategory();
        catPage.openAddModal();
        catPage.fillForm(targetCategory, "Active");
        catPage.clickCreate();

        catPage.searchFor(targetCategory);
        catPage.deleteCategoryByIndex(1);

        catPage.clickClear();
        catPage.searchFor(targetCategory);
        Assert.assertNull(catPage.getCategoryNameByIndex(1), "Task 1 Failed: Category is still present in the Main Category table!");
        Reporter.log("Task 1: Main Category deleted successfully.");
    }

    @Test(priority = 2, dependsOnMethods = "US2_DEF_01_Task1_DeleteMainCategory")
    public void US2_DEF_01_Task2_and_3_VerifyGlobalRemoval() {
        subPage.hardResetAndNavigate();

        boolean foundInGrid = subPage.searchAcrossPages(targetCategory);
        Assert.assertFalse(foundInGrid, "Task 2 Failed: The deleted Main Category is still lingering inside the SubCategory grid!");
        Reporter.log("Task 2: Verified complete absence in SubCategory data grid.");

        String mainWindow = driver.getWindowHandle();
        subPage.clickAddSubCategory();

        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(mainWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        List<String> dropdownOptions = subPage.getAddPageDropdownOptions();
        boolean foundInDropdown = dropdownOptions.contains(targetCategory);

        if (driver.getWindowHandles().size() > 1) {
            driver.close();
        }
        driver.switchTo().window(mainWindow);

        Assert.assertFalse(foundInDropdown, "Task 3 Failed: The deleted Main Category is still selectable in the Add Subcategory dropdown!");
        Reporter.log("Task 3: Verified complete absence in SubCategory dropdown options.");
    }

    @AfterClass(alwaysRun = true)
    public void cleanupFailedData() {
        if (driver != null && catPage != null) {
            catPage.navigateToMainCategory();
            catPage.cleanUpAllTestArtifacts();
        }
    }
}