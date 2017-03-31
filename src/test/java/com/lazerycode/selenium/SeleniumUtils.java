package it.bper.test.util;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import it.bper.test.common.censimentocompleto.CensimentoCompletoCmnOps;
import it.bper.test.common.censimentocompleto.ICensimentoCompleto;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;

import it.bper.test.common.censimentolight.CensimentoLightCmnOps;
import it.bper.test.common.home.IHome;

public class SeleniumUtils {

    public static final String KPIPRODOTTISERVICE_MSG = "Errore durante la chiamata al servizio KPIProdottiService";
    public static final String XPATH_PACE_READY = "//body[contains(@class,'pace-done')]";
    private static final Logger LOG = LoggerFactory.getLogger(SeleniumUtils.class);

    private SeleniumUtils() {
    }

    public static void backBrowserButton(WebDriver driver) {
        driver.navigate().back();
        // aspetto che il loader NPV scompaia
        SeleniumUtils.waitNpvPageLoaded(driver);
    }

    public static void checkForPDFPagesToBeLoaded(WebDriver driver, By targetLocator,
                                                  int numberOfExpectedWindowHandle) {

        String NPVPageWindowHandle = driver.getWindowHandle();
        Set<String> windowsHandlesIniziale = driver.getWindowHandles();
        int numberOfInitialWindowHandle = driver.getWindowHandles().size();
        SeleniumUtils.clickJavascript(driver, targetLocator);
        //SeleniumUtils.clickRetryAndWait(driver, );
        waitForNewPDFWindowIsLoaded(driver, numberOfInitialWindowHandle, numberOfExpectedWindowHandle);
        Set<String> windowsHandlesFinale = driver.getWindowHandles();

        windowsHandlesFinale.removeAll(windowsHandlesIniziale);

        for (String targetWinHandle : windowsHandlesFinale) {
            driver = driver.switchTo().window(targetWinHandle);
            driver.close();
        }

        driver.switchTo().window(NPVPageWindowHandle);
    }

    public static void clickAndSwitchToNewPage(WebDriver driver, By targetLocator) {

        Set<String> windowsHandlesIniziale = driver.getWindowHandles();
        int numberOfInitialWindowHandle = driver.getWindowHandles().size();
        SeleniumUtils.clickJavascript(driver, targetLocator);
        waitForNewPDFWindowIsLoaded(driver, numberOfInitialWindowHandle, 1);

    }

    /**
     * Azione di click su un oggetto e conseguente attesa che il loader NPV
     * sparisca
     *
     * @param locator elemento da ricercare e cliccare
     */
    public static void clickAndWaitNpvPageLoaded(WebDriver driver, By locator) {
        SeleniumUtils.clickJavascript(driver, locator);
        // aspetto che il loader NPV scompaia
        SeleniumUtils.waitNpvPageLoaded(driver);
    }

    public static void clickAndWaitNpvPageLoadedWithMessage(WebDriver driver, By locator, String message) {
        SeleniumUtils.clickJavascript(driver, locator);
        // aspetto che il loader NPV scompaia
        waitForElementToBePresent(driver, By.xpath(XPATH_PACE_READY));
        if (SeleniumUtils.isElementPresent(driver, By.xpath(IHome.XPATH_POPUP_GENERICO)) /*&& !allowedPopUpMsg(driver)*/ &&
                !message.equals(CensimentoLightCmnOps.getTestoErroreIndirizzo(driver))) {
            Assert.fail("c'è un pop-up non aspettato");
        }
    }

    /**
     * Azione di click su un oggetto e conseguente attesa che il loader NPV
     * sparisca
     *
     * @param locator elemento da ricercare e cliccare
     */
    public static void clickRetryAndWaitNpvPageLoaded(final WebDriver driver, int numRetries, By locator,
                                                      final By nextExpectedLocator, long timeOutDuration) {
        for (int i = 0; i < numRetries; i++) {

            if (SeleniumUtils.isElementPresent(driver, locator)) {
                // click sul pulsante target
                SeleniumUtils.clickJavascript(driver, locator);
                // aspetto che il loader NPV scompaia
                SeleniumUtils.waitNpvPageLoaded(driver);
            }
            try {
                // aspetto l'elemento della pagina successiva
                new FluentWait<WebDriver>(driver).withTimeout(timeOutDuration, TimeUnit.SECONDS)
                        .pollingEvery(100, TimeUnit.MILLISECONDS).until(new Predicate<WebDriver>() {
                    public boolean apply(WebDriver d) {
                        return SeleniumUtils.isElementPresent(driver, nextExpectedLocator);
                    }
                });
                // l'elemento è presente e posso proseguire con la logica
                break;
            } catch (Exception e) {
                LOG.info("ClickRetryAndWait: Timed out after " + timeOutDuration + " seconds waiting");
            }
        }
    }

    /**
     * Azione di click su un oggetto e conseguente attesa che il loader NPV
     * sparisca con Messaggio di controllo
     *
     * @param locator elemento da ricercare e cliccare
     */
    public static void clickRetryAndWaitNpvPageLoadedWithMessage(final WebDriver driver, int numRetries, By locator,
                                                                 final By nextExpectedLocator, String message) {
        for (int i = 0; i < numRetries; i++) {

            if (SeleniumUtils.isElementPresent(driver, locator)) {
                // click sul pulsante target
                SeleniumUtils.clickJavascript(driver, locator);
                // aspetto che il loader NPV scompaia
                waitForElementToBePresent(driver, By.xpath(XPATH_PACE_READY));
                if (SeleniumUtils.isElementPresent(driver, By.xpath(IHome.XPATH_POPUP_GENERICO)) /*&& !allowedPopUpMsg(driver)*/ &&
                        !message.equals(CensimentoLightCmnOps.getTestoErroreIndirizzo(driver))) {
                    Assert.fail("c'è un pop-up non aspettato");
                } else {
                    LOG.info("Ho trovato il popUp aspettato e posso proseguire con la logica");
                    break;
                }
            }
            try {
                // aspetto l'elemento della pagina successiva
                new FluentWait<WebDriver>(driver).withTimeout(15, TimeUnit.SECONDS)
                        .pollingEvery(100, TimeUnit.MILLISECONDS).until(new Predicate<WebDriver>() {
                    public boolean apply(WebDriver d) {
                        return SeleniumUtils.isElementPresent(driver, nextExpectedLocator);
                    }
                });
                // l'elemento è presente e posso proseguire con la logica
                break;
            } catch (Exception e) {
                LOG.info("ClickRetryAndWait: Timed out after 10 seconds waiting");
            }
        }
    }

    /**
     * Azione di click su un oggetto e conseguente attesa che il loader NPV
     * sparisca con condizione di Retry
     *
     * @param locator elemento da ricercare e cliccare
     */
    public static void clickRetryAndWaitNpvPageLoadedWithCondition(final WebDriver driver, int numRetries, By locator,
                                                                   final By nextExpectedLocator, By condition) {
        for (int i = 0; i < numRetries; i++) {

            if (SeleniumUtils.isElementPresent(driver, locator)) {
                // click sul pulsante target
                SeleniumUtils.clickJavascript(driver, locator);
                // aspetto che il loader NPV scompaia
                SeleniumUtils.waitNpvPageLoaded(driver);
            }
            try {
                // aspetto l'elemento della pagina successiva
                new FluentWait<WebDriver>(driver).withTimeout(20, TimeUnit.SECONDS)
                        .pollingEvery(100, TimeUnit.MILLISECONDS).until(new Predicate<WebDriver>() {
                    public boolean apply(WebDriver d) {
                        return SeleniumUtils.isElementPresent(driver, nextExpectedLocator);
                    }
                });
                // l'elemento è presente e posso proseguire con la logica
                break;
            } catch (Exception e) {
                // è presente un risultato inatteso?
                // Per individuare eventuali fallimento come, per esempio, il KYCQuestionnaire su step3 o step 4
                if (SeleniumUtils.isElementPresent(driver, condition)) {
                    LOG.info("E' stato trovato un errore per il seguente xpath: " + condition.toString());
                    Assert.fail("Test bloccato da un errore inatteso sul flusso di Frontend");
                } else {
                    LOG.info("ClickRetryAndWait: Timed out after 10 seconds waiting");
                }
            }
        }
    }

    /**
     * Azione di click su un oggetto con l'attesa e retry
     *
     * @param locator elemento da ricercare e cliccare
     */
    public static void clickRetryAndWait(final WebDriver driver, int numRetries, final By locator) {
        for (int i = 0; i < numRetries; i++) {

            if (SeleniumUtils.isElementPresent(driver, locator)) {
                SeleniumUtils.clickJavascript(driver, locator);
            }
            try {
                new FluentWait<WebDriver>(driver)
                        .withTimeout(5, TimeUnit.SECONDS)
                        .pollingEvery(100, TimeUnit.MILLISECONDS)
                        .until(new Predicate<WebDriver>() {
                            public boolean apply(WebDriver d) {
                                return SeleniumUtils.isElementPresent(driver, locator);
                            }
                        });
                break;
            } catch (Exception e) {
                LOG.info("ClickRetryAndWait senza NpvPageLoaded: Timed out after 10 seconds waiting");
            }
        }
    }

    public static void clickJavascript(WebDriver driver, By locator) {
        SeleniumUtils.waitForElementToBePresent(driver, locator);
        SeleniumUtils.waitForElementToBeVisible(driver, locator);
        // SeleniumUtils.waitForElementToBeClickable(driver, locator);
        WebElement we = driver.findElement(locator);
        SeleniumUtils.mouseOverTargetElement(driver, we);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", we);
    }

    public static void compilaInput(WebDriver driver, By locator, String content) {
        SeleniumUtils.waitForElementToBePresent(driver, locator);
        SeleniumUtils.waitForElementToBeVisible(driver, locator);
        SeleniumUtils.waitForElementToBeClickable(driver, locator);
        SeleniumUtils.waitForElementToBeEnabled(driver, locator);
        WebElement we = driver.findElement(locator);
        SeleniumUtils.mouseOverTargetElement(driver, we);
        we.clear();
        we.sendKeys(content);
    }

    public static void forceEventOnElement(WebDriver driver, String idElement, String event) {
        ((JavascriptExecutor) driver).executeScript("var element = document.getElementById('" + idElement + "'); "
                + "var evt = document.createEventObject(); " + "element.fireEvent('on" + event + "',evt);");
    }

    public static String getTextOfValueElement(WebDriver driver, By locator) {
        return driver.findElement(locator).getAttribute("value");
    }

    public static boolean isApplicativeError(WebDriver driver, Exception ex) {
        By byError = By.id("errorMessage");
        boolean presentError = SeleniumUtils.isElementPresent(driver, byError);

        By byErrorDetails = By.id("errorDetailsDiv");
        boolean presentErrorDetail = SeleniumUtils.isElementPresent(driver, byErrorDetails);

        if (!presentError && !presentErrorDetail) {
            return false;
        }

        String error = driver.findElement(byError).getText();
        JavascriptExecutor je = (JavascriptExecutor) driver;
        String errorDetails = (String) je.executeScript("return document.getElementById('errorDetailsDiv').innerHTML");

        LOG.error("Descrizione generica:\n" + error + "\nDescrizione dettaglio:\n" + errorDetails);
        System.out.println("Descrizione generica\n: " + error + "\nDescrizione dettaglio:\n" + errorDetails);
        return true;
    }

    /**
     * Esegue la ricerca di un dato elemento. Restituisce:<br>
     * - 'false': se elemento non presente o non unico all'interno del DOM<br>
     * - 'true': se elemento presente e unico all'interno del DOM
     *
     * @param driver
     * @param locator
     * @return 'false': se elemento non presente o non unico all'interno del
     * DOM\n 'true': se elemento presente e unico all'interno del DOM
     */
    public static boolean isElementPresent(WebDriver driver, By locator) {
        // ricerca elemento istantanea
        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
        List<WebElement> list = driver.findElements(locator);
        driver.manage().timeouts().implicitlyWait(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS);

        return list.size() == 1;
    }

    public static void mouseClickTargetElement(WebDriver driver, By locator) {
        WebElement we = driver.findElement(locator);
        Actions driverActions = new Actions(driver);

        mouseOverTargetElement(driver, driverActions, we);
        ((JavascriptExecutor) driver)
                .executeScript("window.scrollTo(" + we.getLocation().x + "," + we.getLocation().y + ")");
        driverActions.moveToElement(we).click().build().perform();
    }

    public static void mouseOverTargetElement(WebDriver webDriver, Actions driverActions, WebElement targetElement) {
        ((JavascriptExecutor) webDriver).executeScript(
                "window.scrollTo(" + targetElement.getLocation().x + "," + (targetElement.getLocation().y - 300) + ")");
        driverActions.moveToElement(targetElement).build().perform();
    }

    public static void mouseOverTargetElement(WebDriver webDriver, WebElement targetElement) {
        ((JavascriptExecutor) webDriver).executeScript(
                "window.scrollTo(" + targetElement.getLocation().x + "," + (targetElement.getLocation().y - 300) + ")");
        new Actions(webDriver).moveToElement(targetElement).build().perform();
    }

    public static void ricercaParolaChiave(WebDriver driver, By locator, String parolaChiave) {
        SeleniumUtils.verifyElement(driver, locator);
        WebElement we = driver.findElement(locator);
        SeleniumUtils.mouseOverTargetElement(driver, we);
        we.clear();
        we.sendKeys(parolaChiave);
        we.sendKeys(Keys.ENTER);

    }

    public static void selezionaValoreDaSelectFromValue(WebDriver driver, By locator, String value) {
        SeleniumUtils.verifyElement(driver, locator);
        WebElement we = driver.findElement(locator);
        SeleniumUtils.mouseOverTargetElement(driver, we);
        Select select = new Select(we);
        select.selectByValue(value);
    }

    public static void verifyElement(WebDriver driver, By locator) {
        SeleniumUtils.waitForElementToBePresent(driver, locator);
        SeleniumUtils.waitForElementToBeVisible(driver, locator);
        SeleniumUtils.waitForElementToBeClickable(driver, locator);
    }

    public static void waitForElementToBeClickable(WebDriver driver, By locator) {
        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
        WebDriverWait driverWait = new WebDriverWait(driver, Constants.TIMEOUT_SECONDS_GUI, 500);
        driverWait.until(ExpectedConditions.elementToBeClickable(locator));
        driver.manage().timeouts().implicitlyWait(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    public static void waitForElementToBeEnabled(WebDriver driver, final By locator) {
        SeleniumUtils.waitForElementToBePresent(driver, locator);
        SeleniumUtils.waitForElementToBeVisible(driver, locator);

        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
        WebDriverWait driverWait = new WebDriverWait(driver, Constants.TIMEOUT_SECONDS_GUI, 500);
        driverWait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver driver) {
                return driver.findElement(locator).isEnabled();
            }
        });
        driver.manage().timeouts().implicitlyWait(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    public static void waitForElementToBePresent(WebDriver driver, By locator) {
        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
        WebDriverWait driverWait = new WebDriverWait(driver, Constants.TIMEOUT_SECONDS_GUI, 500);
        driverWait.until(ExpectedConditions.presenceOfElementLocated(locator));
        driver.manage().timeouts().implicitlyWait(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    public static void waitForElementToBeVisible(WebDriver driver, By locator) {
        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
        WebDriverWait driverWait = new WebDriverWait(driver, Constants.TIMEOUT_SECONDS_GUI, 500);
        driverWait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
        driver.manage().timeouts().implicitlyWait(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private static void waitForNewPDFWindowIsLoaded(final WebDriver driver, final int numberOfInitialWindowHandle,
                                                    final int numberOfExpectedWindowHandle) {

        new FluentWait<WebDriver>(driver).withTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .pollingEvery(100, TimeUnit.MILLISECONDS).until(new Predicate<WebDriver>() {
            public boolean apply(WebDriver d) {
                return numberOfInitialWindowHandle + numberOfExpectedWindowHandle == driver.getWindowHandles()
                        .size();
            }
        });
    }

    // TODO RIMUOVERE QUANDO SI TROVA SOLUZIONE OTTIMIZZATA
//	public static void waitNpvPageLoaded(WebDriver driver, String message) {
//		waitForElementToBePresent(driver, By.xpath(XPATH_PACE_READY));
//		if(SeleniumUtils.isElementPresent(driver, By.xpath(IHome.XPATH_POPUP_GENERICO)) && !allowedPopUpMsg(driver) && 
//				!message.equals(CensimentoLightCmnOps.getTestoErroreIndirizzo(driver))) {
//			Assert.fail("c'è un pop-up non aspettato");
//		} 
//	}

    /**
     * Attesa che il loader NPV sparisca
     */
    public static void waitNpvPageLoaded(WebDriver driver) {
        waitForElementToBePresent(driver, By.xpath(XPATH_PACE_READY));
        if (SeleniumUtils.isElementPresent(driver, By.xpath(IHome.XPATH_POPUP_GENERICO))) { //&& !allowedPopUpMsg(driver)
            Assert.fail("c'è un pop-up non aspettato");
        }

    }

    // TODO RIMUOVERE QUANDO SI TROVA SOLUZIONE OTTIMIZZATA
//	public static boolean allowedPopUpMsg(WebDriver driver) {
//		List<String> errorsMsg = Arrays.asList(KPIPRODOTTISERVICE_MSG);
//		//LOG.info("CHE PRENDI? " + HomeCmnOps.doCercaTestoErrore(driver));
//		return Arrays.asList(errorsMsg).contains(CensimentoLightCmnOps.getTestoErroreIndirizzo(driver));
//	}

    public static void waitTextExpectedInsideAttributeValue(WebDriver driver, final By locator,
                                                            final String expectedValue) {
        SeleniumUtils.waitForElementToBePresent(driver, locator);
        SeleniumUtils.waitForElementToBeVisible(driver, locator);

        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
        WebDriverWait driverWait = new WebDriverWait(driver, Constants.TIMEOUT_SECONDS_GUI, 500);
        driverWait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver driver) {
                return expectedValue.equals(driver.findElement(locator).getAttribute("value"));
            }
        });
        driver.manage().timeouts().implicitlyWait(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    public static void waitTextPresentInsideAttributeValue(WebDriver driver, final By locator) {
        SeleniumUtils.waitForElementToBePresent(driver, locator);
        SeleniumUtils.waitForElementToBeVisible(driver, locator);

        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
        WebDriverWait driverWait = new WebDriverWait(driver, Constants.TIMEOUT_SECONDS_GUI, 500);
        driverWait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver driver) {
                return !StringUtils.isEmpty(driver.findElement(locator).getAttribute("value"));
            }
        });
        driver.manage().timeouts().implicitlyWait(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    public static void waitTextPresentInsideXMLElement(WebDriver driver, final By locator) {
        SeleniumUtils.waitForElementToBePresent(driver, locator);
        SeleniumUtils.waitForElementToBeVisible(driver, locator);

        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
        WebDriverWait driverWait = new WebDriverWait(driver, Constants.TIMEOUT_SECONDS_GUI, 500);
        driverWait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver driver) {
                return !StringUtils.isEmpty(driver.findElement(locator).getText());
            }
        });
        driver.manage().timeouts().implicitlyWait(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    public static void selectFile(WebDriver driver, final By locator, final String filePath) {
        WebElement fileInput = driver.findElement(locator);
        fileInput.sendKeys(filePath);
    }

    //metodo per acquisizione file
    public static void acquisisciDocumenti(WebDriver driver, final By locatorOpenScan, final By locatorCartaIdentita, final By locatorCodiceFiscale, String cartaIdentita, String codiceFiscale) {
        SeleniumUtils.waitNpvPageLoaded(driver);
        if (Constants.SCAN_DOCUMENTS) {
            // Skip Scansione Documenti
            ((JavascriptExecutor) driver).executeScript("$('#scanDoc').remove();");
            ((JavascriptExecutor) driver).executeScript("$('#scanDoc-wrapper').remove();");
            ((JavascriptExecutor) driver).executeScript("dojo.publish('update-errors');");
        } else {
            // Scansione Documenti
            SeleniumUtils.clickJavascript(driver, locatorOpenScan);
            SeleniumUtils.selectFile(driver, locatorCartaIdentita, cartaIdentita);
            SeleniumUtils.selectFile(driver, locatorCodiceFiscale, codiceFiscale);
        }


    }


}
