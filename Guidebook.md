
# 📘 InsurEdge Automation Guidebook

**Version:** 1.0

**Stack:** Selenium 4.18.1 | TestNG 7.11.0 | Java | Maven

**Purpose:** The definitive "One-Stop" reference for the InsurEdge team.

---

## 🏗️ 1. Project Architecture (Where to put files)

We follow the **Page Object Model (POM)** design pattern strictly.

| Directory | What goes here? | Rules |
| --- | --- | --- |
| **`src/main/java/pages/`** | **Page Classes** | Store Locators (`By`) and Actions (Methods). **NO Assertions here.** |
| **`src/test/java/tests/`** | **Test Scripts** | Store `@Test` cases. **NO `driver.findElement` here.** |
| **`src/test/resources/`** | **Test Data** | Excel sheets, property files, and driver executables. |
| **`src/main/java/utils/`** | **Utilities** | Helper code for Excel, Screenshots, or Reporting. |

---

## 🎯 2. Locators (Finding Elements)

**Golden Rule:** `ID` > `Name` > `CSS Selector` > `XPath`

### ✅ Basic Locators

* `By.id("username")` - **Best option** (Fastest & Unique).
* `By.name("email")` - Good alternative.
* `By.className("form-control")` - **Careful:** multiple elements often share the same class.
* `By.tagName("h1")` - Finds all `<h1>` headers.
* `By.linkText("Logout")` - Exact text match for `<a>` links.
* `By.partialLinkText("Log")` - Partial text match for `<a>` links.

### ⚔️ XPath (The "Swiss Army Knife")

Use this when no ID or Name is available.

* **Syntax:** `//tagName[@attribute='value']`

| Scenario | Syntax | Example |
| --- | --- | --- |
| **Basic Attribute** | `//tag[@attr='val']` | `//input[@id='user']` |
| **Contains Text** | `//tag[contains(text(),'Val')]` | `//button[contains(text(),'Login')]` |
| **Exact Text** | `//tag[text()='Value']` | `//h2[text()='Dashboard']` |
| **Multiple Attributes** | `//tag[@a1='v1' and @a2='v2']` | `//input[@type='submit' and @name='btn']` |
| **Parent to Child** | `//parent/child` | `//div[@id='menu']/ul/li` |
| **Following Sibling** | `//tag/following-sibling::tag` | `//label[text()='Name']/following-sibling::input` |

### 🎨 CSS Selectors (Faster than XPath)

* **ID:** `#username` (Starts with `#`)
* **Class:** `.login-btn` (Starts with `.`)
* **Attribute:** `input[name='email']`
* **Direct Child:** `div > p`

---

## 🖱️ 3. WebElements (Interactions)

Common commands to control the browser.

```java
// 1. Find the element
WebElement btn = driver.findElement(By.id("submit"));

// 2. Interact
btn.click();                 // Clicks the button
btn.clear();                 // Clears text (Crucial for input boxes!)
btn.sendKeys("Text");        // Types text
btn.submit();                // Submits a form (alternative to click)

// 3. Validation (Getters)
String text = btn.getText();            // Returns visible text (e.g., "Submit")
String val = btn.getAttribute("value"); // Returns hidden value (e.g., text inside input box)
boolean isVis = btn.isDisplayed();      // True if visible
boolean isEn = btn.isEnabled();         // True if clickable (not grayed out)
boolean isSel = btn.isSelected();       // True if Checkbox/Radio is checked

```

---

## ⏳ 4. Waits & Timeouts (Synchronization)

**⛔ NEVER use `Thread.sleep(5000)**`. It makes tests slow and flaky.

### A. Implicit Wait (Global)

* Set **once** in `BaseTest.java`.
* Tells WebDriver to wait *up to* 10 seconds for an element to appear in the DOM.

### B. Explicit Wait (Specific)

Use this when an element takes longer to load or needs to be interactive.

```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

// 1. Wait until element is visible
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("msg")));

// 2. Wait until element is clickable (Essential for buttons!)
wait.until(ExpectedConditions.elementToBeClickable(By.id("submitBtn")));

// 3. Wait for alert to appear
wait.until(ExpectedConditions.alertIsPresent());

// 4. Wait for URL to change
wait.until(ExpectedConditions.urlContains("dashboard"));

```

---

## 🎮 5. Advanced Interactions

### A. Dropdowns (`<select>` tag only)

```java
Select dropdown = new Select(driver.findElement(By.id("country")));

dropdown.selectByVisibleText("India"); // Best method
dropdown.selectByValue("IN");          // Uses the HTML 'value' attribute
dropdown.selectByIndex(1);             // Uses position (Risky if list changes)

```

### B. Mouse & Keyboard (Actions Class)

Used for complex user inputs.

```java
Actions act = new Actions(driver);

// Hover over a menu
act.moveToElement(menuItem).perform();

// Double Click
act.doubleClick(element).perform();

// Right Click
act.contextClick(element).perform();

// Drag and Drop
act.dragAndDrop(sourceElement, targetElement).perform();

// Keyboard Shortcuts (e.g., Ctrl+C)
act.keyDown(Keys.CONTROL).sendKeys("c").keyUp(Keys.CONTROL).perform();

```

### C. JavaScript Executor

Use this when Selenium's normal `.click()` fails (e.g., "Element Click Intercepted").

```java
JavascriptExecutor js = (JavascriptExecutor) driver;

// Force Click
js.executeScript("arguments[0].click();", element);

// Scroll into View
js.executeScript("arguments[0].scrollIntoView(true);", element);

// Handle specific Alerts
js.executeScript("window.alert = function() {};");

```

---

## 🖼️ 6. Handling Windows, Frames & Alerts

### Windows/Tabs

```java
String mainHandle = driver.getWindowHandle();
Set<String> allHandles = driver.getWindowHandles();

for(String handle : allHandles) {
    if(!handle.equals(mainHandle)) {
        driver.switchTo().window(handle); // Switch to new tab
    }
}
// Switch back to main window
driver.switchTo().window(mainHandle);

```

### iFrames

If an element is inside an `<iframe>`, you must switch to it first.

```java
driver.switchTo().frame("frameName"); // By Name or ID
// OR
driver.switchTo().frame(0); // By Index

// Switch back to main page context
driver.switchTo().defaultContent();

```

### Alerts (Popups)

```java
Alert alert = driver.switchTo().alert();
String text = alert.getText(); // Read alert text
alert.accept();                // Click OK
alert.dismiss();               // Click Cancel
alert.sendKeys("Text");        // Type into alert

```

---

## ✅ 7. TestNG Annotations & Assertions

### Annotations (The Workflow)

* `@BeforeSuite`: Runs once before the entire suite.
* `@BeforeTest`: Runs before `<test>` tag in XML.
* `@BeforeClass`: Runs once before the first method in a class.
* `@BeforeMethod`: **(Crucial)** Runs before **every** `@Test`. We use this to Open Browser.
* `@Test`: The actual test case.
* `@AfterMethod`: **(Crucial)** Runs after **every** `@Test`. We use this to Close Browser.

### Assertions (Validation)

If an assertion fails, the test stops immediately.

* `Assert.assertEquals(actual, expected, "Error Message");` -> Compares text/numbers.
* `Assert.assertTrue(condition, "Error Message");` -> Expects `true`.
* `Assert.assertFalse(condition, "Error Message");` -> Expects `false`.
* `Assert.assertNotNull(object);` -> Verifies object is not null.

---

## 📊 8. Excel Utilities (Apache POI)

For Data-Driven Testing (Importing/Searching).

**Dependency (Add to POM):** `poi-ooxml`

**Helper Method:**

```java
public String getCellData(String sheetName, int rowNum, int colNum) throws IOException {
    FileInputStream fis = new FileInputStream("src/test/resources/TestData.xlsx");
    XSSFWorkbook workbook = new XSSFWorkbook(fis);
    String data = workbook.getSheet(sheetName).getRow(rowNum).getCell(colNum).toString();
    workbook.close();
    return data;
}

```

---

## 🚨 9. Troubleshooting Common Exceptions

| Exception | Why it happened | How to fix |
| --- | --- | --- |
| **`NoSuchElementException`** | Element not found in DOM. | Check locator accuracy. Use Explicit Wait. |
| **`ElementNotInteractableException`** | Element is present but hidden/disabled. | Check if another element covers it. Scroll to it. |
| **`StaleElementReferenceException`** | Page refreshed; element ID changed. | Find the element again (`driver.findElement`) right before using it. |
| **`ElementClickInterceptedException`** | A popup/overlay is blocking the click. | Close the popup or use `JavascriptExecutor` click. |
| **`TimeoutException`** | Element didn't appear within wait time. | Increase wait time or check if app is slow. |
| **`SessionNotCreatedException`** | Browser/Driver version mismatch. | Update Selenium version in POM. |

---

## 📸 10. Taking Screenshots (For Failures)

Add this to your `BaseTest` or a Utility class.

```java
public void takeScreenshot(String fileName) throws IOException {
    File srcFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
    File destFile = new File("screenshots/" + fileName + ".png");
    FileUtils.copyFile(srcFile, destFile);
}

```