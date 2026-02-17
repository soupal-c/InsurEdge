package tests;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;
import pages.CategoryPage;
import java.util.Arrays;
import java.util.List;

public class CategoryTest extends BaseTest {

    // --- US2-MC-01 (UI) ---
    @Test(priority = 1)
    public void US2_MC_01_Task1_VerifyPageUI() {
        driver.navigate().refresh();
        Reporter.log("=== EXECUTING: US2-MC-01 Task 1 (UI) ===", true);
        CategoryPage category = new CategoryPage(driver);
        category.navigateToMainCategory();

        Assert.assertEquals(category.getPageTitle(), "Create Main Insurance Category");
        Assert.assertEquals(category.getBreadcrumbs(), Arrays.asList("Category", "Create Main Category"));
        Reporter.log("[PASS] UI Elements Verified", true);
    }

    @Test(priority = 2)
    public void US2_MC_01_Task2_VerifyRowActions() {
        driver.navigate().refresh();
        Reporter.log("=== EXECUTING: US2-MC-01 Task 2 (Icons) ===", true);
        CategoryPage category = new CategoryPage(driver);

        Assert.assertTrue(category.areActionIconsVisible(), "Action icons missing!");
        Reporter.log("[PASS] Icons Visible", true);
    }

    // --- US2-MC-02 (Functionality) ---
    @Test(priority = 3)
    public void US2_MC_02_Task1_VerifySearchFunctionality() {
        driver.navigate().refresh();
        Reporter.log("=== EXECUTING: US2-MC-02 Task 1 (Search) ===", true);
        CategoryPage category = new CategoryPage(driver);
        category.navigateToMainCategory();

        category.searchFor("Pet");
        category.clickClear();
        Assert.assertTrue(category.isCategoryInTable("House"), "Clear failed");
        Reporter.log("[PASS] Search & Clear Verified", true);
    }

    @Test(priority = 4)
    public void US2_MC_02_Task2_VerifyAddCategory() {
        driver.navigate().refresh();
        Reporter.log("=== EXECUTING: US2-MC-02 Task 2 (Add) ===", true);
        CategoryPage category = new CategoryPage(driver);
        category.navigateToMainCategory();

        String name = "Add_" + System.nanoTime();
        category.openAddModal();
        category.fillForm(name, "Active");
        category.clickCreate();
        category.waitForModalToClose(); // Ensure modal is gone before next test!

        Assert.assertTrue(category.isCategoryInTable(name), "Add failed");
        Reporter.log("[PASS] Added: " + name, true);
    }

    @Test(priority = 5)
    public void US2_MC_02_Task3_VerifyEditCategory() {
        driver.navigate().refresh();
        Reporter.log("=== EXECUTING: US2-MC-02 Task 3 (Edit) ===", true);
        CategoryPage category = new CategoryPage(driver);
        category.navigateToMainCategory();

        // 1. Create fresh category
        String preEditName = "PreEdit_" + System.nanoTime();
        category.openAddModal();
        category.fillForm(preEditName, "Active");
        category.clickCreate();
        category.waitForModalToClose(); // Critical wait

        // 2. Edit it
        category.searchFor(preEditName);
        String postEditName = "PostEdit_" + System.nanoTime();
        category.editFirstCategory(postEditName);

        // 3. Verify
        category.clickClear();
        Assert.assertTrue(category.isCategoryInTable(postEditName), "Edit failed");
        Reporter.log("[PASS] Edited " + preEditName + " to " + postEditName, true);
    }

    @Test(priority = 6)
    public void US2_MC_02_Task4_VerifyNegativeTest() {
        driver.navigate().refresh();
        Reporter.log("=== EXECUTING: US2-MC-02 Task 4 (Duplicate) ===", true);
        CategoryPage category = new CategoryPage(driver);
        category.navigateToMainCategory();

        String name = "Dup_" + System.nanoTime();

        try {
            // 1. Create Initial
            category.openAddModal();
            category.fillForm(name, "Active");
            category.clickCreate();
            category.waitForModalToClose(); // Critical wait

            // 2. Try Duplicate
            category.openAddModal();
            category.fillForm(name, "Active");
            category.clickCreate();

            // 3. Verify Error
            String error = category.getErrorMessage();
            Assert.assertTrue(error.contains("already exists"), "Error missing");
            Reporter.log("[PASS] Duplicate Blocked", true);

        } finally {
            category.handleStuckModal(); // Always close modal
        }
    }
}