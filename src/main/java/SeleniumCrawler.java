import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class SeleniumCrawler {

    public void crawl() throws InterruptedException {

        String queryToRun = "SELECT\n" +
                "   \"Procurement and Spend - Invoice Lines\".\"Plant\".\"Plant Location Name\" s_1,\n" +
                "   \"Procurement and Spend - Invoice Lines\".\"Product\".\"Product Name\" s_2,\n" +
                "   \"Procurement and Spend - Invoice Lines\".\"Product\".\"Product Number\" s_3,\n" +
                "   \"Procurement and Spend - Invoice Lines\".\"Product\".\"Standard UOM Code\" s_4,\n" +
                "   \"Procurement and Spend - Invoice Lines\".\"Supplier\".\"Supplier Name\" s_5,\n" +
                "   \"Procurement and Spend - Invoice Lines\".\"Supplier\".\"Supplier Number\" s_6,\n" +
                "   \"Procurement and Spend - Invoice Lines\".\"Time\".\"Year\" s_7,\n" +
                "   \"Procurement and Spend - Invoice Lines\".\"Fact - Purchasing - Invoice\".\"Invoiced Quantity\" s_8,\n" +
                "   \"Procurement and Spend - Invoice Lines\".\"Fact - Purchasing - Invoice\".\"Spend\" s_9\n" +
                "FROM \"Procurement and Spend - Invoice Lines\"\n" +
                "WHERE\n" +
                "\"Plant\".\"Plant Location Name\" <> 'Unspecified' AND \"Product\".\"Product Number\" NOT LIKE '0%'";

        //property setup
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\trbye\\scripts\\chromedriver.exe");
        String downloadPath = "C:\\Users\\trbye\\Downloads";

        //browser prefs
        HashMap<String, Object> preferences = new HashMap<>();
        preferences.put("profile.default_content_settings.popups", 0);
        preferences.put("download.default_directory", downloadPath);

        //browser options config
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", preferences);

        DesiredCapabilities cap = DesiredCapabilities.chrome();
        cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        cap.setCapability(ChromeOptions.CAPABILITY, options);

        //add capabilities to options
        options.merge(cap);

        WebDriver driver = new ChromeDriver(options);
        //get start url
        driver.get("http://devora57.westfarm.com:9502/analytics/");

        //login to OBIE
        login(driver);
        System.out.print(" Login page title test: " + driver.getTitle() + " ");

        //run query
        navigateToAnalysisAndRunQuery(driver, queryToRun);
        System.out.print(" OK: Query success.");

        downloadQueryResultsToCsv(driver);
        System.out.print(" OK: CSV downloaded. ");

        //don't terminate until file exists
        File file = new File("C:\\Users\\trbye\\Downloads\\Untitled Analysis.csv");

        do {
            Thread.sleep(3000);
        } while (!file.exists());

        //end session and close browser
        driver.quit();
    }

    private void login(WebDriver driver) throws InterruptedException {
        //wait until logon form is visible
        waitUntilVisible(driver, By.cssSelector("input[id=sawlogonuser][class=margin-top5][name=NQUser]"), 10);

        //find username and pass <input> elements
        WebElement username = driver.findElement(By.id("sawlogonuser"));
        username.clear();
        username.sendKeys("trbye");

        WebElement password = driver.findElement(By.id("sawlogonpwd"));
        password.clear();
        password.sendKeys("change_to_real_pass");

        //click form submit
        WebElement submitButton = driver.findElement(By.id("idlogon"));
        submitButton.click();
    }

    private void navigateToAnalysisAndRunQuery(WebDriver driver, String queryToRun) {

        //wait until links are visible
        waitUntilVisible(driver, By.className("CatalogActionLink"), 10);

        //navigate to analysis link
        List<WebElement> anchorList = driver.findElements(By.className("CatalogActionLink"));
        WebElement targetAnchor = null;

        for (WebElement anchor : anchorList) {
            if (anchor.getText().equals("Analysis")) {
                targetAnchor = anchor;
            }
        }

        targetAnchor.click();

        //navigate to SQL entry
        waitUntilVisible(driver, By.id("idSubjectAreasMenuWithDirectDBReq"), 10);
        WebElement sqlAnchor = driver.findElement(By.xpath("//*[@id='create_simple_sql_req_r1']/td/a"));
        sqlAnchor.click();

        //wait until floating div is visible
        waitUntilVisible(driver, By.id("idNewSimpleSqlRequestInput"), 10);

        //get text area and enter query
        WebElement queryTextArea = driver.findElement(By.id("idNewSimpleSqlRequestInput"));
        WebElement queryOkButton = driver.findElement(By.xpath("//*[@class='floatingWindowDiv dialogBox']/div/table/tbody[1]/tr/td/div[3]/a[1]"));

        queryTextArea.sendKeys(queryToRun);
        queryOkButton.click();
    }

    private void downloadQueryResultsToCsv(WebDriver driver) throws InterruptedException {

        //wait until table is visible after query run
        waitUntilVisible(driver, By.id("idAnswersCompoundViewToolbar"), 30);

        //csv dropdown
        WebElement dropdownArrowSpan = driver.findElement(By.xpath("//*[@id='idAnswersCompoundViewToolbar_export']/span"));
        dropdownArrowSpan.click();

        //wait for dropdown div to be visible
        waitUntilVisible(driver, By.id("idAnswersReportEditorExportMenu"), 10);

        //get main table data and find target anchor
        WebElement tableData = driver.findElement(By.xpath("//*[@id='idAnswersReportEditorExportMenu']/table/tbody/tr[1]/td[1]"));
        List<WebElement> anchorList = tableData.findElements(By.tagName("a"));
        WebElement targetAnchor = anchorList.get(4);

        //assemble actions to move to next pane
        Actions builder = new Actions(driver);
        builder.moveToElement(targetAnchor).build().perform();

        //wait for secomd popup menu dependent on previous moveEvent
        waitUntilVisible(driver, By.id("idExportDataMenu"), 10);
        WebElement popupTableAnchor = driver.findElement(By.xpath("//*[@id='idExportDataMenu']/table/tbody/tr[1]/td[1]/a[1]"));
        popupTableAnchor.click();

        //wait for download completion
        waitUntilVisible(driver, By.xpath("//*[@class='masterHeader dialogTitleBar']"), 30);
        WebElement confirmationSpan = driver.findElement(By.xpath("//*[@class='masterHeader dialogTitleBar']/table/tbody/tr/td[1]/span"));

        if (!(confirmationSpan.getText().equals("Confirmation"))) {
            System.out.print("download successful.");
        }

    }

    private void waitUntilVisible(WebDriver driver, By location, int waitTimeSec) {
        WebDriverWait wait = new WebDriverWait(driver, waitTimeSec);
        wait.until(ExpectedConditions.visibilityOfElementLocated(location));
    }
}
