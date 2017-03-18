
package service.crawler;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

/**
 * Tests base class.
 * Takes care of initialising the Remote WebDriver
 */
public abstract class BaseCrawler {
    private WebDriver mDriver                      = null;
    private boolean mAutoQuitDriver                = true;

    private static final String CONFIG_FILE        = "config.ini";
    private static final String DRIVER_FIREFOX     = "firefox";
    private static final String DRIVER_CHROME      = "chrome";
    private static final String DRIVER_PHANTOMJS   = "phantomjs";

    protected static Properties sConfig;
    protected static DesiredCapabilities sCaps;
    final static Logger logger = Logger.getLogger(BaseCrawler.class);
   
    private static boolean isUrl(String urlString) {
        try {
            new URL(urlString);
            return true;
        } catch (MalformedURLException mue) {
            return false;
        }
    }

    @BeforeClass
    public void configure() throws IOException {
        // Read config file
        sConfig = new Properties();
        sConfig.load(new FileReader(BaseCrawler.class.getClassLoader().getResource(CONFIG_FILE).getFile()));

        // Prepare capabilities
        sCaps = new DesiredCapabilities();
        sCaps.setJavascriptEnabled(true);
        sCaps.setCapability("takesScreenshot", false);
        sCaps.setCapability("webdriver-selenium-grid-hub", "http://127.0.0.1:4444" );
        sCaps.setCapability("webdriver", "8080" );

        String driver = sConfig.getProperty("driver", DRIVER_PHANTOMJS);

        // Fetch PhantomJS-specific configuration parameters
        if (driver.equals(DRIVER_PHANTOMJS)) {
            // "phantomjs_exec_path"
            if (sConfig.getProperty("phantomjs_exec_path") != null) {
                sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, sConfig.getProperty("phantomjs_exec_path"));
            } else {
                throw new IOException(String.format("Property '%s' not set!", PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY));
            }
            // "phantomjs_driver_path"
            if (sConfig.getProperty("phantomjs_driver_path") != null) {
                System.out.println("Test will use an external GhostDriver");
                sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_GHOSTDRIVER_PATH_PROPERTY, sConfig.getProperty("phantomjs_driver_path"));
            } else {
                System.out.println("Test will use PhantomJS internal GhostDriver");
            }
        }

        // Disable "web-security", enable all possible "ssl-protocols" and "ignore-ssl-errors" for PhantomJSDriver
//        sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, new String[] {
//            "--web-security=false",
//            "--ssl-protocol=any",
//            "--ignore-ssl-errors=true"
//        });
        ArrayList<String> cliArgsCap = new ArrayList<String>();
        cliArgsCap.add("--web-security=false");
        cliArgsCap.add("--ssl-protocol=any");
        cliArgsCap.add("--ignore-ssl-errors=true");
        sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, cliArgsCap);

        // Control LogLevel for GhostDriver, via CLI arguments
        sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_GHOSTDRIVER_CLI_ARGS, new String[] {
            "--logLevel=" + (sConfig.getProperty("phantomjs_driver_loglevel") != null ? sConfig.getProperty("phantomjs_driver_loglevel") : "INFO")
        });
    }

    @Before
    public void prepareDriver() throws Exception {
        // Which driver to use? (default "phantomjs")
        String driver = sConfig.getProperty("driver", DRIVER_PHANTOMJS);

        // Start appropriate Driver
        if (isUrl(driver)) {
            sCaps.setBrowserName("phantomjs");
            mDriver = new RemoteWebDriver(new URL(driver), sCaps);
        } else if (driver.equals(DRIVER_FIREFOX)) {
            mDriver = new FirefoxDriver(sCaps);
        } else if (driver.equals(DRIVER_CHROME)) {
            mDriver = new ChromeDriver(sCaps);
        } else if (driver.equals(DRIVER_PHANTOMJS)) {
            mDriver = new PhantomJSDriver(sCaps);
        }
    }

    protected WebDriver getDriver() {
        return mDriver;
    }

    protected void disableAutoQuitDriver() {
        mAutoQuitDriver = false;
    }

    protected void enableAutoQuitDriver() {
        mAutoQuitDriver = true;
    }

    protected boolean isAutoQuitDriverEnabled() {
        return mAutoQuitDriver;
    }

    @After
    public void quitDriver() {
        if (mAutoQuitDriver && mDriver != null) {
            mDriver.quit();
            mDriver = null;
        }
    }

    protected void assumePhantomJS() {
        assumeTrue(sConfig.getProperty("driver").equals("phantomjs"));
    }

    protected void assumeNotPhantomJS() {
        assumeFalse(sConfig.getProperty("driver").equals("phantomjs"));
    }

    protected void failIfPhantomJS() {
        if (sConfig.getProperty("driver").equals("phantomjs")) fail();
    }

    protected void failIfNotPhantomJS() {
        if (!sConfig.getProperty("driver").equals("phantomjs")) fail();
    }
}