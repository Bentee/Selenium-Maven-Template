package it.bper.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.security.ProtectionDomain;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import it.bper.test.util.Constants;
import it.bper.test.util.PropertiesUtils;

public abstract class AbstractTest extends AbstractJUnit4SpringContextTests {

    public final static String IEXPLORE_PROCESS = "iexplore.exe";
    protected final static String TD_PERSONA_FISICA_CODE = "PF";
    private static final Logger LOG = LoggerFactory
            .getLogger(AbstractTest.class);
    private static final String NPV_URL_WEB_LOGIN = "npv.url.web.login";
    private final static String TASKLIST = "tasklist";
    private final static String TASKKILL = "taskkill /PID {0} /F";
    protected WebDriver driver;
    protected Boolean testEsitoOk = null;

    @Rule
    public TestWatcher logTaker = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            LOG.info(nome(description)+": SUCCESSO");
        }

        @Override
        protected void failed(Throwable e, Description description) {
            LOG.info(nome(description)+": FALLIMENTO");
        }

        @Override
        protected void skipped(AssumptionViolatedException e, Description description) {
            LOG.info(nome(description)+": SALTATO");
        }
    };

    private static String nome(Description description){
        String tmp = description.getClassName();
        int lastDot = tmp.lastIndexOf('.');

        if (lastDot > 0) {
            tmp = tmp.substring(lastDot + 1);
        }
        return tmp;
    }

    /**
     * Cattura uno screenshot se il test fallisce.
     */
    @Rule
    public TestWatcher screenshotTaker = new TestWatcher() {

        @Override
        protected void failed(Throwable e, Description description) {
            String fileName = "screenshot-"
                    + description.getTestClass().getSimpleName() + ".png";
            File file = new File(getJarFolder(), fileName);

            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write(((TakesScreenshot) driver)
                        .getScreenshotAs(OutputType.BYTES));
            } catch (WebDriverException | IOException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }

        private File getJarFolder() {
            ProtectionDomain domain = getClass().getProtectionDomain();
            File f = new File(domain.getCodeSource().getLocation().getPath());

            if (f.isDirectory()) {
                return f;
            }
            return f.getParentFile();
        }



        @Override
        protected void finished(Description description) {
            if(Boolean.valueOf(PropertiesTD.getDatoDaStringaPassata(PropertiesTD.quitAfterTest))) {
                driver.quit();
            }
        }
    };
    protected String baseUrl;
    protected long startTime;
    protected long endTime;

    private static List<String> killAllInstanceOfGivenProcess(String serviceName)
            throws IOException {
        Process p = Runtime.getRuntime().exec(TASKLIST);
        List<String> out = new ArrayList<String>();
        String line;

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                p.getInputStream()));
        while ((line = reader.readLine()) != null) {
            if (line.contains(serviceName)) {
                killProcessByPid(getPidProcess(line));
            }
        }
        return out;
    }

    private static String getPidProcess(String taskLine) {
        String[] taskInfo = taskLine.split("\\s+");
        // il PID presente in posizione 2
        return taskInfo[1];
    }

    private static void killProcessByPid(String servicePid) throws IOException {
        if (!StringUtils.isEmpty(servicePid)) {
            String cmdToExecute = MessageFormat.format(TASKKILL, servicePid);
            Runtime.getRuntime().exec(cmdToExecute);
            LOG.warn("KillProcess: Processo {} con PID {} killato",
                    IEXPLORE_PROCESS, servicePid);
        }
    }

    // ***********************
    // * METODI UTILITA TEST *
    // ***********************

    protected static void assertEqualsIgnoreCase(String expected, String result) {
        Assert.assertEquals("Atteso: " + expected + ". Ricevuto: " + result,
                expected.toUpperCase(), result.toUpperCase());
    }

    protected static Integer toInteger(BigDecimal bd) {
        if (bd != null) {
            try {
                return Integer.valueOf(bd.intValueExact());
            } catch (ArithmeticException ex) {
                Assert.fail("Il numero " + bd + " non e' un intero!");
            }
        }
        return null;
    }

    @Before
    public void setUp() throws Exception {
        try {
            killAllInstanceOfGivenProcess(IEXPLORE_PROCESS);
        } catch (IOException e) {
            LOG.warn("impossibile cancellare il processo iexplore.exe");
        }

        DesiredCapabilities dc = DesiredCapabilities.internetExplorer();
        dc.setCapability(
                InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,
                true);
        dc.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
        driver = new InternetExplorerDriver(dc);
        driver.manage().window().maximize();
        baseUrl = PropertiesUtils.getProperty(NPV_URL_WEB_LOGIN);
        startTime = System.currentTimeMillis();
        driver.get(baseUrl);

        if (PropertiesUtils.getProperty(Constants.NPV_URL_WEB_LOGIN).equals(Constants.NPV_URL_COLLAUDO)) {
            LOG.info("***L'AMBIENTE E' COLLAUDO***");
        } else {
            LOG.info("***L'AMBIENTE E' SVILUPPO***");
            // Switch to new window opened
            for (String winHandle : driver.getWindowHandles()) {
                driver.switchTo().window(winHandle);
            }
        }
    }

    // *******************************
    // * METODI UTILITA ASSERT JUNIT *
    // *******************************

    @After
    public void tearDown() throws Exception {
        endTime = System.currentTimeMillis();
        LOG.info((endTime - startTime) / 1000 + "s");

        // Non chiudiamo qui il driver Selenium perche', in caso di fallimento,
        // verra' attivato il TestWatcher che ha bisogno del driver Selenium
        // attivo per poter catturare lo screenshot
    }

    protected void riapriSessioneDriver() {
        // chiudi la sessione attuale
        driver.quit();
        DesiredCapabilities dc = DesiredCapabilities.internetExplorer();
        dc.setCapability(
                InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,
                true);
        dc.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
        driver = new InternetExplorerDriver(dc);
        driver.manage().window().maximize();
        baseUrl = PropertiesUtils.getProperty(NPV_URL_WEB_LOGIN);
        startTime = System.currentTimeMillis();
        driver.get(baseUrl);

        if (!PropertiesUtils.getProperty(Constants.NPV_URL_WEB_LOGIN).equals(Constants.NPV_URL_COLLAUDO)) {
            // Switch to new window opened (solo nell'ambiente sviluppo)
            for (String winHandle : driver.getWindowHandles()) {
                driver.switchTo().window(winHandle);
            }
        }
    }

}
