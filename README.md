# 🛡️ InsurEdge Automation Framework

![Java](https://img.shields.io/badge/Java-17-orange) ![Selenium](https://img.shields.io/badge/Selenium-4.18.1-green) ![TestNG](https://img.shields.io/badge/TestNG-7.11.0-blue) ![Maven](https://img.shields.io/badge/Build-Maven-red)

## 📖 Project Overview
**InsurEdge** is a robust test automation framework designed to validate the core functionalities of the Insurance Management System. It utilizes **Selenium WebDriver** with **Java** and follows the **Page Object Model (POM)** design pattern to ensure scalability, maintainability, and seamless collaboration among the QA team.

This repository currently hosts the **Sprint 2 Regression Suite**, focusing specifically on the **Category Management Module**.

---

## 🎯 Scope of Testing
The automation framework covers the End-to-End (E2E) flows for the following modules based on the Functional Requirements Document (FRD):

### 1. Main Category Management
* **Creation:** Verifying the ability to add new insurance main categories.
* **Editing:** Updating existing category details.
* **Deletion:** Removing categories and verifying dependency constraints.
* **Search & Filtering:** Validating the search functionality for quick retrieval.

### 2. Sub-Category Management
* **Creation:** Adding sub-categories linked to specific main categories.
* **Synchronization:** Ensuring sub-categories correctly map to their parent categories.
* **CRUD Operations:** Full coverage of Create, Read, Update, and Delete flows.

### 3. Advanced Features
* **Bulk Import:** Automating the validation of CSV/XLSX import functionality for categories.
* **Error Handling:** Verifying system responses for invalid inputs and duplicate entries.

---

## 🛠️ Tech Stack
| Component | Technology | Version |
| :--- | :--- | :--- |
| **Language** | Java | 17+ |
| **Web Driver** | Selenium WebDriver | 4.18.1 |
| **Test Runner** | TestNG | 7.11.0 |
| **Build Tool** | Maven | 3.x |
| **Design Pattern** | Page Object Model (POM) | Standard |
| **Data Source** | Apache POI (Excel) | 5.x |

---

## 🚀 How to Run the Tests
### Prerequisites
1.  **Java JDK 17+** installed and configured in system path.
2.  **Maven** installed and configured.
3.  **Chrome Browser** updated to the latest version.
4.  **Git** installed.

### Setup & Execution
1.  **Clone the Repository:**
    ```bash
    git clone [https://github.com/soupal-c/InsurEdge.git](https://github.com/soupal-c/InsurEdge.git)
    ```
2.  **Navigate to Project Directory:**
    ```bash
    cd InsurEdge
    ```
3.  **Install Dependencies:**
    ```bash
    mvn clean install
    ```
4.  **Run the Regression Suite:**
    ```bash
    mvn clean test -DsuiteXmlFile=testng.xml
    ```

---

## 👥 The Automation Team
* **Soupal** - Lead Automation Architect
* **Vivek** - Main Category UI & Search
* **Dhanya** - Import Functionality & Data Validation
* **Sahil** - Sub-Category Management
* **Mustafa** - Delete Logic & Dependency Testing
* **Shivansh** - Cross-Module Synchronization

---

## 📂 Project Structure
```text
InsurEdge/
├── src/main/java/pages    # Locators & Actions (Page Objects)
├── src/test/java/tests    # Test Execution Scripts (@Test)
├── src/test/resources     # Test Data (Excel/Properties)
├── GUIDEBOOK.md           # Developer Documentation & Rules
├── pom.xml                # Project Dependencies
└── testng.xml             # Test Suite Runner
