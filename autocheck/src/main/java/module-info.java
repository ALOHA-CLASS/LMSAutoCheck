module com.aloha {
    requires javafx.controls;
    requires javafx.fxml;

    // lombok
    requires lombok;

    // Selenium
    requires com.google.common;
    requires org.seleniumhq.selenium.api;
    requires org.seleniumhq.selenium.remote_driver;
    requires org.seleniumhq.selenium.chrome_driver;
    requires dev.failsafe.core;
    requires org.seleniumhq.selenium.support;

    opens com.aloha to javafx.fxml;
    exports com.aloha;
}
