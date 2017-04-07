package com.lazerycode.selenium.tests;

import com.lazerycode.selenium.DriverBase;
import com.sun.javafx.animation.TickCalculation;
import org.junit.Ignore;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.util.List;

public class GoogleExampleIT extends DriverBase {

    @Test
    public void googleCheeseExample() throws Exception {
        // Create a new WebDriver instance
        // Notice that the remainder of the code relies on the interface,
        // not the implementation.
        WebDriver driver = getDriver();

        // And now use this to visit Google
        driver.get("http://test.optit.net/hum");
        // Alternatively the same thing can be done like this
        // driver.navigate().to("http://www.google.com");

        //<input type="text" class="v-textfield v-widget inline-icon v-textfield-inline-icon v-has-width v-textfield-prompt" id="gwt-uid-7" aria-labelledby="gwt-uid-6" tabindex="0" style="width: 300px;">

        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return d.findElement(By.id("gwt-uid-7")).isDisplayed();
            }
        });


        // Find the text input element by its name
        List<WebElement> elements = driver.findElements(By.tagName("input"));
        System.out.println("Lunghezza lista elementi trovati: " + elements.size());
        for (WebElement element: elements) {
            if (element.getAttribute("type").equalsIgnoreCase("password")) {
                element.sendKeys("12345");
            } else {
                element.sendKeys("utente.admin");
            }
        }

        WebElement submitElement = driver.findElement(By.className("v-button-primary"));

        submitElement.click();

        //<span class="v-button-caption">Sign In</span>
       // <div id="hum-103680" class="v-app humui optit-1-mobile"><div tabindex="1" class="v-ui valo-menu-responsive v-scrollable" width-range="801px-" style="width: 100%; height: 100%;"><div class="v-loading-indicator first" style="position: absolute; display: none;"></div><div class="v-horizontallayout v-layout v-horizontal v-widget v-has-width v-has-height" style="position: absolute; width: 100%; height: 100%;"><div class="v-expand" style="padding-left: 273px;"><div class="v-slot" style="margin-left: -273px;"><div class="valo-menu v-widget" style="overflow: hidden; padding-top: 0px; padding-bottom: 0px;"><div class="v-panel-captionwrap" style="margin-top: 0px;"><div class="v-panel-nocaption"><span></span></div></div><div class="v-panel-content v-scrollable" tabindex="-1" style="position: relative;"><div class="v-csslayout v-layout v-widget"><div class="v-horizontallayout v-layout v-horizontal v-widget valo-menu-title v-horizontallayout-valo-menu-title" style="height: 20px;"><div class="v-slot v-align-center v-align-middle"><div class="v-label v-widget v-label-undef-w">HUM</div></div></div><div tabindex="0" class="v-menubar v-widget user-menu v-menubar-user-menu"><span class="v-menubar-menuitem"><span class="v-menubar-submenu-indicator">►</span><span class="v-menubar-menuitem-caption"><img class="v-icon" src="http://test.optit.net/hum/VAADIN/themes/optit-1-mobile/img/profile-pic-300px.jpg">Amm Sistema</span></span></div><div tabindex="0" role="button" class="v-button v-widget valo-menu-toggle v-button-valo-menu-toggle borderless v-button-borderless"><span class="v-button-wrap"><span class="v-icon FontAwesome"></span><span class="v-button-caption">Menu</span></span></div><div class="valo-menuitems v-layout v-widget"><div class="valo-menu-subtitle v-widget colored valo-menu-subtitle-colored v-label-undef-w">Amministrazione</div><div tabindex="0" role="button" class="valo-menu-item v-widget"><span class="valo-menu-item-wrap"><span class="v-icon FontAwesome"></span><span class="valo-menu-item-caption">Scheduler</span></span></div><div class="valo-menu-subtitle v-widget colored valo-menu-subtitle-colored v-label-undef-w">Teddy Kit</div><div tabindex="0" role="button" class="valo-menu-item v-widget"><span class="valo-menu-item-wrap"><span class="v-icon FontAwesome"></span><span class="valo-menu-item-caption">Gruppi parametri</span></span></div><div tabindex="0" role="button" class="valo-menu-item v-widget"><span class="valo-menu-item-wrap"><span class="v-icon FontAwesome"></span><span class="valo-menu-item-caption">Produzione</span></span></div><div tabindex="0" role="button" class="valo-menu-item v-widget"><span class="valo-menu-item-wrap"><span class="v-icon FontAwesome"></span><span class="valo-menu-item-caption">Gruppi ordini</span></span></div><div tabindex="0" role="button" class="valo-menu-item v-widget"><span class="valo-menu-item-wrap"><span class="v-icon FontAwesome"></span><span class="valo-menu-item-caption">Simulazione</span></span></div></div></div></div><div class="v-panel-deco" style="margin-bottom: 0px;"></div></div></div><div class="v-slot v-slot-valo-content v-slot-optit-mobile v-align-center v-align-middle" style="width: 100%;"><div class="v-csslayout v-layout v-widget valo-content v-csslayout-valo-content optit-mobile v-csslayout-optit-mobile v-has-width v-has-height" style="width: 100%; height: 100%;"></div></div></div></div></div></div>

        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return d.findElement(By.className("optit-1-mobile")).isDisplayed();
            }
        });

        // Google's search is rendered dynamically with JavaScript.
        // Wait for the page to load, timeout after 10 seconds


        // Should see: "cheese! - Google Search"
        System.out.println("Page title is: " + driver.getTitle());
    }


    public void googleMilkExample() throws Exception {
        // Create a new WebDriver instance
        // Notice that the remainder of the code relies on the interface,
        // not the implementation.
        WebDriver driver = getDriver();

        // And now use this to visit Google
        driver.get("http://www.google.com");
        // Alternatively the same thing can be done like this
        // driver.navigate().to("http://www.google.com");

        // Find the text input element by its name
        WebElement element = driver.findElement(By.name("q"));

        // Enter something to search for
        element.clear();
        element.sendKeys("Milk!");

        // Now submit the form. WebDriver will find the form for us from the element
        element.submit();

        // Check the title of the page
        System.out.println("Page title is: " + driver.getTitle());

        // Google's search is rendered dynamically with JavaScript.
        // Wait for the page to load, timeout after 10 seconds
        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return d.getTitle().toLowerCase().startsWith("milk!");
            }
        });

        // Should see: "cheese! - Google Search"
        System.out.println("Page title is: " + driver.getTitle());
    }
}