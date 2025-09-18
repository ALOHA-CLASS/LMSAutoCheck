package com.aloha;

import java.io.IOException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class Main extends Application {

    private static Scene scene;
    // WebDriver 생성
    public static WebDriver driver = new ChromeDriver();
    // 서브도메인
    public static String subdomain = null;
    // 도메인
    public static String domain = null;
    

    @Override
    public void start(Stage stage) throws IOException {
        // driver.get("https://tjoeun.atosoft.kr");
        scene = new Scene(loadFXML("Main"));
        Image icon = new Image("icon.png");
		stage.getIcons().add(icon);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    /**
     * APP 종료 시 호출
     * - WebDriver 종료
     */
    @Override
    public void stop() throws Exception {
        if (driver != null) {
            driver.quit();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }

}