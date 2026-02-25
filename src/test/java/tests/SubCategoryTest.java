package tests;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;
import pages.SubCategoryPage;
import pages.MainCategoryPage;
import java.time.Duration;
import java.util.List;

public class SubCategoryTest extends BaseTest {

    private SubCategoryPage subPage;
    private MainCategoryPage catPage;

    public String lastCreatedMainCategory = nameGenerator("AutoSync_");
    String targetSubCat = nameGenerator("Retire_");
    String updatedSubCat = nameGenerator("RetireUp_");
    private String deletedRecordName;

    @BeforeClass
    public void setupPage() {
        subPage = new SubCategoryPage(driver);
        catPage = new MainCategoryPage(driver);
    }

    @Test(priority = 1)
    public void US2_SC_01_Task1_VerifyUIElements() {
        subPage.hardResetAndNavigate();
        Assert.assertTrue(driver.findElement(org.openqa.selenium.By.linkText("Add Subcategory")).isDisplayed(), "Task 1: Add Button not displayed");
        Reporter.log("Task 1: UI Elements Verified");
    }

    @Test(priority = 2)
    public void US2_SC_01_Task2_VerifyPagination() {
        subPage.hardResetAndNavigate();
        if(subPage.isPaginationVisible()) {
            Reporter.log("Task 2: Pagination controls are visible.");
        }
    }

    @Test(priority = 3)
    public void US2_SC_02_Task1_VerifyDropdownSync() {
        catPage.navigateToMainCategory();
        catPage.openAddModal();
        catPage.fillForm(lastCreatedMainCategory, "Active");
        catPage.clickCreate();

        subPage.hardResetAndNavigate();
        String main = driver.getWindowHandle();
        subPage.clickAddSubCategory();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(d -> d.getWindowHandles().size() > 1);

        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(main)) driver.switchTo().window(handle);
        }

        List<String> options = subPage.getAddPageDropdownOptions();
        boolean isFound = options.contains(lastCreatedMainCategory);

        driver.close();
        driver.switchTo().window(main);
        Assert.assertTrue(isFound, "Task 1: Sync Failed: Category not found in dropdown!");
    }

    @Test(priority = 4)
    public void US2_SC_02_Task2_DuplicateSubCategoryNegative() {
        String subName = nameGenerator("AutoSub-");
        subPage.createSubCategory(lastCreatedMainCategory, subName, "Active");

        boolean foundFirst = subPage.searchAcrossPages(subName);
        Assert.assertTrue(foundFirst, "Task 2: Initial subcategory creation not found!");

        subPage.createSubCategory(lastCreatedMainCategory, subName, "Active");
        int occurrences = subPage.countOccurrencesAcrossPages(subName);

        Assert.assertEquals(occurrences, 1, "Task 2 BUG: System allowed duplicate subcategory!");
    }

    @Test(priority = 5)
    public void US2_SC_02_Task3_VerifyImmediateReflection() {
        String subName = nameGenerator("AutoReflect-");
        subPage.createSubCategory(lastCreatedMainCategory, subName, "Active");

        boolean isNameFound = subPage.searchAcrossPages(subName);
        Assert.assertTrue(isNameFound, "Task 3 FAIL: '" + subName + "' did not appear in the grid!");
    }

    @Test(priority = 6)
    public void US2_SC_03_Task1_VerifyEditVerification() {
        subPage.createSubCategory(lastCreatedMainCategory, targetSubCat, "Active");

        boolean found = subPage.searchAcrossPages(targetSubCat);
        Assert.assertTrue(found, "Setup: Data created not found in table!");

        subPage.clickEditButton(targetSubCat);

        String actualValue = subPage.getEditNameValue();
        Assert.assertEquals(actualValue, targetSubCat, "Task 1: Edit form field did NOT pre-populate correctly!");
    }

    @Test(priority = 7, dependsOnMethods = "US2_SC_03_Task1_VerifyEditVerification")
    public void US2_SC_03_Task2_VerifyUpdateReflection() {
        subPage.saveEdit(updatedSubCat, "Active");

        boolean isUpdated = subPage.searchAcrossPages(updatedSubCat);
        Assert.assertTrue(isUpdated, "TASK 2 FAILED: Updated name not reflected in table immediately!");
    }

    @Test(priority = 8)
    public void US2_SC_04_Task1_DeleteAndConfirmPopups() {
        deletedRecordName = nameGenerator("ToDelete_");
        subPage.createSubCategory(lastCreatedMainCategory, deletedRecordName, "Active");
        subPage.hardResetAndNavigate();

        boolean found = subPage.searchAcrossPages(deletedRecordName);
        Assert.assertTrue(found, "Task 1 Setup: Record to delete was not found!");

        // FIX: The new merged method returns the string directly! No DOM crashes.
        String alertText = subPage.deleteSubCategoryByName(deletedRecordName);

        Assert.assertTrue(alertText.toLowerCase().contains("successfully"), "Task 1: Success message mismatch!");
    }

    @Test(priority = 9, dependsOnMethods = "US2_SC_04_Task1_DeleteAndConfirmPopups")
    public void US2_SC_04_Task2_VerifyPersistenceAfterRefresh() {
        subPage.clickRefreshButton();
        boolean isPresent = subPage.searchAcrossPages(deletedRecordName);
        Assert.assertFalse(isPresent, "Task 2: The record '" + deletedRecordName + "' was not permanently removed!");
    }

    @AfterClass(alwaysRun = true)
    public void cleanupEnvironment() {
        if (driver != null && subPage != null && catPage != null) {
            subPage.cleanUpSubCategoryArtifacts();
            try { catPage.cleanUpAllTestArtifacts(); } catch (Exception ignored) {}
        }
    }
}