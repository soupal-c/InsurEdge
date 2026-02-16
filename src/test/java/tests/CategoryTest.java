package tests;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;
import pages.CategoryPage;
import java.util.Arrays;
import java.util.List;

public class CategoryTest extends BaseTest {

    // USER STORY: US2-MC-01 (UI Verification)

     //Task 1: Verify Page Title, Breadcrumbs, and Table Headers.

    @Test(priority = 1)
    public void US2_MC_01_Task1_VerifyPageUI() {
        Reporter.log("=== EXECUTING: US2-MC-01 Task 1 (Header/Breadcrumbs) ===", true);

        CategoryPage category = new CategoryPage(driver);
        category.navigateToMainCategory();

        // 1. Header Check
        String actualHeader = category.getPageTitle();
        Assert.assertEquals(actualHeader, "Create Main Insurance Category", "Header Title Mismatch!");
        Reporter.log("Step 1: [PASS] Header verified: " + actualHeader, true);

        // 2. Breadcrumb Check
        List<String> expectedBreadcrumbs = Arrays.asList("Category", "Create Main Category");
        Assert.assertEquals(category.getBreadcrumbs(), expectedBreadcrumbs, "Breadcrumb Mismatch!");
        Reporter.log("Step 2: [PASS] Breadcrumbs verified.", true);

        // 3. Table Header Check
        List<String> expectedTableHeaders = Arrays.asList("Category Name", "Status", "Actions");
        Assert.assertEquals(category.getTableHeaders(), expectedTableHeaders, "Table Column Headers Mismatch!");
        Reporter.log("Step 3: [PASS] Table Headers verified.", true);
    }

      //Task 2: Implement assertions for row action icon presence (Edit/Delete).
    @Test(priority = 2)
    public void US2_MC_01_Task2_VerifyRowActions() {
        Reporter.log("=== EXECUTING: US2-MC-01 Task 2 (Row Actions) ===", true);
        CategoryPage category = new CategoryPage(driver);

        // Check if icons exist in the first row
        boolean areIconsVisible = category.areActionIconsVisible();
        Assert.assertTrue(areIconsVisible, "Edit or Delete icons are missing from the table row!");
        Reporter.log("Step 1: [PASS] Edit and Delete icons are visible and aligned.", true);
    }


    // USER STORY: US2-MC-02 (Functionality)

        //Task 1: Automate Search bar functionality.

    @Test(priority = 3)
    public void US2_MC_02_Task1_VerifySearchFunctionality() {
        Reporter.log("=== EXECUTING: US2-MC-02 Task 1 (Search) ===", true);
        CategoryPage category = new CategoryPage(driver);

        // --- STEP 0: PRE-CONDITION (Create data so search doesn't fail!) ---
        String uniqueName = "Pet Insurance " + System.currentTimeMillis(); // Make it unique
        category.openAddModal();
        category.fillForm(uniqueName, "Active");
        category.clickCreate();
        Reporter.log("Step 0: Created '" + uniqueName + "' to ensure data exists.", true);

        // --- STEP 1: Search for what we just created ---
        category.searchFor(uniqueName);

        // Verify
        boolean isFound = category.isCategoryInTable(uniqueName);
        Assert.assertTrue(isFound, "FAILURE: Search for '" + uniqueName + "' failed. Table text might not have updated.");
        Reporter.log("Step 1: [PASS] Partial match search successful.", true);

        // --- STEP 2: Clear ---
        category.clickClear();
        Assert.assertTrue(category.isCategoryInTable("House Insurance"), "FAILURE: Clear button failed.");
        Reporter.log("Step 2: [PASS] Clear functionality successful.", true);
    }

    //Task 2: Implement "Add Category".

    @Test(priority = 4)
    public void US2_MC_02_Task2_VerifyAddCategory() {
        Reporter.log("=== EXECUTING: US2-MC-02 Task 2 (Add Category) ===", true);
        CategoryPage category = new CategoryPage(driver);

        // 1. Generate Unique Name
        String newName = "AutoTest_" + System.currentTimeMillis();

        try {
            // --- TEST STEPS ---
            category.openAddModal();
            category.fillForm(newName, "Active");
            category.clickCreate();

            Assert.assertTrue(category.isCategoryInTable(newName), "New category not found in table.");
            Reporter.log("Step 1: [PASS] Category '" + newName + "' added successfully.", true);

        } finally {
            // --- CLEANUP (Always runs, even if assertion fails) ---
            Reporter.log("...Cleaning up: Deleting '" + newName + "'", true);
            category.deleteCategory(newName);

            // Optional: Verify deletion
            category.clickClear(); // Reset table
            boolean exists = category.isCategoryInTable(newName);
            if (!exists) {
                Reporter.log("Step 2: [PASS] Cleanup successful. Category deleted.", true);
            } else {
                Reporter.log("Step 2: [WARNING] Cleanup failed. Category still exists.", true);
            }
        }
    }

    //Task 3: Create "Edit Category" script.

    @Test(priority = 5)
    public void US2_MC_02_Task3_VerifyEditCategory() {
        Reporter.log("=== EXECUTING: US2-MC-02 Task 3 (Edit Category) ===", true);
        CategoryPage category = new CategoryPage(driver);

        String updatedName = "Edited_" + System.currentTimeMillis();
        category.editFirstCategory(updatedName);

        Assert.assertTrue(category.isCategoryInTable(updatedName), "Edited category not found.");
        Reporter.log("Step 1: [PASS] Category edited to '" + updatedName + "' successfully.", true);
    }

    //Task 4: Automate negative testing (Duplicates).

    @Test(priority = 6)
    public void US2_MC_02_Task4_VerifyNegativeTest() {
        Reporter.log("=== EXECUTING: US2-MC-02 Task 4 (Duplicate Check) ===", true);
        CategoryPage category = new CategoryPage(driver);

        // 1. Create Base Category
        String duplicateName = "Duplicate_" + System.currentTimeMillis();
        category.openAddModal();
        category.fillForm(duplicateName, "Active");
        category.clickCreate();

        // 2. Try to Duplicate it
        category.openAddModal();
        category.fillForm(duplicateName, "Active");
        category.clickCreate();

        // 3. Verify Error
        String error = category.getErrorMessage();
        Assert.assertTrue(error.contains("already exists"), "Duplicate error missing!");
        Reporter.log("Step 1: [PASS] Duplicate blocked with error: " + error, true);
    }
}