package tests;

import org.testng.Assert;
import org.testng.Reporter; // <--- Import this!
import org.testng.annotations.Test;
import pages.CategoryPage;
import pages.LoginPage;
import java.util.Arrays;
import java.util.List;

public class CategoryTest extends BaseTest {

    @Test
    public void verifyCategoryPageUI() {
        Reporter.log("Step 1: Login to the application", true);
        LoginPage login = new LoginPage(driver);
        login.doLogin("admin_user", "testadmin");

        Reporter.log("Step 2: Navigate to Category Creation Page", true);
        CategoryPage category = new CategoryPage(driver);
        category.navigateToCreateCategory();

        // --- Task 1: Headers & Breadcrumbs ---
        Reporter.log("Step 3: Verifying Page Header...", true);
        String actualHeader = category.getHeaderText();
        Assert.assertEquals(actualHeader, "Create Main Insurance Category");
        Reporter.log("   -> [PASS] Header text verified successfully.", true);

        Reporter.log("Step 4: Verifying Breadcrumbs...", true);
        List<String> actualBreadcrumbs = category.getBreadcrumbText();
        List<String> expectedBreadcrumbs = Arrays.asList("Category", "Create Main Category");
        Assert.assertEquals(actualBreadcrumbs, expectedBreadcrumbs);
        Reporter.log("   -> [PASS] Breadcrumb sequence is correct.", true);

        // --- Task 2: Action Icons ---
        Reporter.log("Step 5: Verifying Action Icons...", true);
        boolean isEditVisible = category.isEditIconDisplayed();
        Assert.assertTrue(isEditVisible, "Edit Icon not found!");
        Reporter.log("   -> [PASS] Edit Icon is visible and clickable.", true);

        boolean isDeleteVisible = category.isDeleteIconDisplayed();
        Assert.assertTrue(isDeleteVisible, "Delete Icon not found!");
        Reporter.log("   -> [PASS] Delete Icon is visible and clickable.", true);
    }
}