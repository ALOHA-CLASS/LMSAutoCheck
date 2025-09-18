module com.aloha {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;

    // lombok
    requires lombok;

    // Selenium
    requires transitive com.google.common;
    requires transitive org.seleniumhq.selenium.api;
    requires transitive org.seleniumhq.selenium.remote_driver;
    requires transitive org.seleniumhq.selenium.chrome_driver;
    requires transitive dev.failsafe.core;
    requires transitive org.seleniumhq.selenium.support;

    opens com.aloha to javafx.fxml;
    exports com.aloha;
}
