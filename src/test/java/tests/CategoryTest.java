package tests;

import org.openqa.selenium.*;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;
import pages.CategoryPage;
import java.util.List;

public class CategoryTest extends BaseTest {

    String autoName = "Auto_" + System.currentTimeMillis();
    String preEditName = "PreEdit_" + System.currentTimeMillis();
    String postEditName = "PostEdit_" + System.currentTimeMillis();
    String dupName = "Dup_" + System.currentTimeMillis();


    // US2-MC-01 - Main Category Page UI & Row Actions

    // Task 1 - Automate UI verification for headers, breadcrumbs, and table header order/styling
    @Test(priority = 1)
    public void US2_MC_01_Task1_VerifyUI() {
        CategoryPage catPage = new CategoryPage(driver);
        catPage.navigateToMainCategory();

        Assert.assertEquals(catPage.getPageTitle(), "Create Main Insurance Category", "Header Mismatch!");

        List<String> headers = catPage.getTableHeaders();
        Assert.assertTrue(headers.contains("Category Name") && headers.contains("Status") && headers.contains("Actions"));
        Reporter.log("UI Verified");
    }

    // Task 2 - Implement assertions for row action icon presence and alignment (Edit/Delete)
    @Test(priority = 2)
    public void US2_MC_01_Task2_VerifyRowActionIcons() {
        CategoryPage catPage = new CategoryPage(driver);
        catPage.navigateToMainCategory();

        Assert.assertTrue(catPage.areRowActionIconsVisible(), "Icons missing!");
        Reporter.log("Icons Verified");
    }

    // US2-MC-02 - Search, Add, Edit, and Status

    // Task 1 - Automate Search bar functionality (input, partial matches, and clear)
    @Test(priority = 3)
    public void US2_MC_02_Task1_SearchFunctionality() {
        CategoryPage catPage = new CategoryPage(driver);
        catPage.navigateToMainCategory();

        catPage.searchFor("Phone");
        Assert.assertTrue(catPage.isCategoryInTable("Phone"));

        catPage.clickClear();
        Assert.assertTrue(catPage.isCategoryInTable("Life"));
        Reporter.log("Search Verified");
    }

    // Task 2 - Implement "Add Category" and verify if the category is added
    @Test(priority = 4)
    public void US2_MC_02_Task2_AddCategory() {
        CategoryPage catPage = new CategoryPage(driver);
        catPage.navigateToMainCategory();

        catPage.openAddModal();
        catPage.fillForm(autoName, "Active");
        catPage.clickCreate();

        catPage.searchFor(autoName);
        Assert.assertTrue(catPage.isCategoryInTable(autoName));
        Reporter.log("Add Verified");
    }

    // Task 3 - Create "Edit Category" script including field update verification
    @Test(priority = 5)
    public void US2_MC_02_Task3_EditCategory() {
        CategoryPage catPage = new CategoryPage(driver);
        catPage.navigateToMainCategory();

        catPage.openAddModal();
        catPage.fillForm(preEditName, "Active");
        catPage.clickCreate();

        catPage.searchFor(preEditName);
        catPage.editFirstCategory(postEditName);

        catPage.searchFor(postEditName);
        Assert.assertTrue(catPage.isCategoryInTable(postEditName));
        Reporter.log("Edit Verified");
    }

    // Task 4 - Automate negative testing for duplicate Name+Status combinations
    @Test(priority = 6)
    public void US2_MC_02_Task4_DuplicateCheck() {
        CategoryPage catPage = new CategoryPage(driver);
        catPage.navigateToMainCategory();

        catPage.openAddModal();
        catPage.fillForm(dupName, "Active");
        catPage.clickCreate();

        catPage.openAddModal();
        catPage.fillForm(dupName, "Active");
        catPage.clickCreate();

        String error = catPage.getErrorMessage();
        Assert.assertTrue(error.contains("exists") || error.contains("Duplicate"));
        Reporter.log("Duplicate Verified");
    }


    // DEFECT FIXES / REGRESSION


    // CLEANUP

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        CategoryPage catPage = new CategoryPage(driver);
        catPage.navigateToMainCategory();
        catPage.cleanUpAllTestArtifacts();
    }
}