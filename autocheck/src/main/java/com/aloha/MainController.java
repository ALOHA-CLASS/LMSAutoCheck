package com.aloha;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.aloha.domain.MainInfo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class MainController {

    @FXML
    private CheckBox cbSave;

    @FXML
    private TextArea taNote;

    @FXML
    private TextArea taOrder;

    @FXML
    private TextArea taEtc;

    @FXML
    private TextField tfA;

    @FXML
    private TextField tfB;

    @FXML
    private TextField tfC;

    @FXML
    private TextField tfId;

    @FXML
    private TextField tfLink;

    @FXML
    private PasswordField tfPw;

    boolean autoLogin = false;


    @FXML
    void initialize() {
        this.loadMainInfo();
        String id = this.tfId.getText();
        String pw = this.tfPw.getText();
        String link = this.tfLink.getText();
        boolean isChecked = this.cbSave.isSelected();
        if( isChecked ) {
          login(id, pw);
        } 
    }

    @FXML
    void open(ActionEvent event) {
        
        String id = this.tfId.getText();
        String pw = this.tfPw.getText();
        // String link = this.tfLink.getText();
        // this.login(id, pw);
        // WebDriverWait wait = new WebDriverWait(Main.driver, Duration.ofSeconds(10L));
        // String targetUrl = "https://tjoeun.atosoft.kr/worknet/Course/CourseList.asp";
        // wait.until(ExpectedConditions.urlToBe(targetUrl));
        // System.out.println("로그인 완료!");

        WebDriverWait wait = new WebDriverWait(Main.driver, Duration.ofSeconds(5L));
        try {
            String link = this.tfLink.getText();
            Main.driver.get(link);
            wait.until(ExpectedConditions.urlToBe(link));
        } catch (Exception e) {
            System.out.println("로그인이 되어있지 않습니다.");
            login(id, pw);
            open(event);
        }

        System.out.println("훈련일지 들어옴!");
        WebElement diaryRegButton = Main.driver.findElement(By.xpath("//a[text()='훈련일지 등록']"));
        diaryRegButton.click();
        WebElement dateElement = (WebElement)wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("strWDate")));
        LocalDate today = LocalDate.now();
        String formattedDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE);
        dateElement.sendKeys(new CharSequence[]{formattedDate});
        JavascriptExecutor js = (JavascriptExecutor)Main.driver;
        js.executeScript("DiaryRegEx();", new Object[0]);

        try {
            Thread.sleep(1500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String 결석 = this.tfA.getText();
        String 지각 = this.tfB.getText();
        String 조퇴 = this.tfC.getText();
        this.check결석(결석);
        this.check지각(지각);
        this.check조퇴(조퇴);
        this.init지시사항();
        this.init기타사항();
        this.init출석노트();

        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        js.executeScript("check_submit();", new Object[0]);

        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Actions actions = new Actions(Main.driver);
        actions.sendKeys(new CharSequence[]{Keys.ENTER}).perform();

    }

    @FXML
    void save(ActionEvent event) {

        String id = this.tfId.getText();
        String pw = this.tfPw.getText();
        String link = this.tfLink.getText();
        boolean isChecked = this.cbSave.isSelected();
        if (!isChecked) {
            System.out.println("저장한 정보를 삭제 합니다.");
            this.cbSave.setSelected(false);
            String filePath = "MainInfo.ser";
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }

            this.tfId.clear();
            this.tfPw.clear();
            this.tfLink.clear();
        } else {
            MainInfo mainInfo = new MainInfo(id, pw, link);
            System.out.println(mainInfo);
            System.out.println("정보를 저장합니다.");

            try (
                FileOutputStream fos = new FileOutputStream("MainInfo.ser");
                ObjectOutputStream oos = new ObjectOutputStream(fos) 
            ) {
                oos.writeObject(mainInfo);
                System.out.println("정보를 저장했습니다.");
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("파일을 저장하는 중 오류가 발생했습니다.");
            }
        }


    }

    void login(String id, String pw) {
        System.setProperty("webdriver.chrome.driver", "driver/chromedriver.exe");
        String url = "https://tjoeun.atosoft.kr/worknet/TLogin.asp";
        Main.driver.get(url);
        WebElement loginId = Main.driver.findElement(By.id("strLoginID"));
        loginId.sendKeys(new CharSequence[]{id});
        WebElement loginPwd = Main.driver.findElement(By.id("strLoginPwd"));
        loginPwd.sendKeys(new CharSequence[]{pw});
        WebElement loginButton = Main.driver.findElement(By.xpath("//input[@type='submit']"));
        loginButton.click();
        try {
            WebDriverWait wait = new WebDriverWait(Main.driver, Duration.ofSeconds(5L));
            String targetUrl = "https://tjoeun.atosoft.kr/worknet/Course/CourseList.asp";
            wait.until(ExpectedConditions.urlToBe(targetUrl));
        } catch (Exception e) {
            boolean isChecked = this.cbSave.isSelected();
            if( isChecked ) {
                System.err.println("5초 안에 targetUrl에 도달하지 못했습니다.");
                Alert alert = new Alert(Alert.AlertType.ERROR, "로그인에 실패했습니다. 다시 시도해주세요.");
                alert.showAndWait();
            }
            return;
        }
        System.out.println("로그인 완료!");
    }

    void loadMainInfo() {
        String filePath = "MainInfo.ser";
        File file = new File(filePath);
        if (file.exists()) {
            try ( 
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis) 
            ) {
                MainInfo mainInfo = (MainInfo) ois.readObject();
                tfId.setText(mainInfo.getId());
                tfPw.setText(mainInfo.getPw());
                tfLink.setText(mainInfo.getLink());
                cbSave.setSelected(true);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("파일을 읽는 중 오류가 발생했습니다.");
            }
        }  
        else {
            System.err.println("파일이 존재하지 않습니다.");
        } 
    }


    void check결석(String 결석) {
      System.out.println("결석 입력 : " + 결석);
      if (결석 != null && !결석.equals("")) {
         List<String> list = Arrays.asList(결석.split(" "));
         Iterator<String> it = list.iterator();

         while(it.hasNext()) {
            String item = (String)it.next();
            System.out.println(item);
            this.selectCheckBoxes(item, 0);
         }

      }
   }

   void check지각(String 지각) {
      if (지각 != null && !지각.equals("")) {
         List<String> list = Arrays.asList(지각.split(" "));
         Iterator<String> it = list.iterator();

         while(it.hasNext()) {
            String item = (String)it.next();
            System.out.println(item);
            this.selectCheckBoxes(item, 1);
         }

      }
   }

   void check조퇴(String 조퇴) {
      if (조퇴 != null && !조퇴.equals("")) {
         List<String> list = Arrays.asList(조퇴.split(" "));
         Iterator<String> it = list.iterator();

         while(it.hasNext()) {
            String item = (String)it.next();
            System.out.println(item);
            this.selectCheckBoxes(item, 2);
         }

      }
   }

   void selectCheckBoxes(String value, int index) {
      WebDriverWait wait = new WebDriverWait(Main.driver, Duration.ofSeconds(10L));
      List<WebElement> checkBoxes = (List)wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//input[@type='checkbox' and @value='" + value + "']")));
      WebElement checkBox = (WebElement)checkBoxes.get(index);
      if (!checkBox.isSelected()) {
         checkBox.click();
      }

   }

   void waitForInputElementWithValue(String value, WebDriverWait wait) {
      WebElement element = (WebElement)wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@value='" + value + "']")));
      System.out.println("요소가 나타났습니다: " + String.valueOf(element));
   }

   String init출석노트() {
      String 결석 = this.tfA.getText();
      String 지각 = this.tfB.getText();
      String 조퇴 = this.tfC.getText();
      결석 = 결석.isEmpty() ? "0" : (결석 + " (" + 결석.split(" ").length + ")" );
      지각 = 지각.isEmpty() ? "0" : (지각 + " (" + 지각.split(" ").length + ")" );
      조퇴 = 조퇴.isEmpty() ? "0" : (조퇴 + " (" + 조퇴.split(" ").length + ")" );
      System.out.println(결석);
      System.out.println(지각);
      System.out.println(조퇴);
      String pattern = "결석 : {0}\n지각 : {1}\n조퇴 : {2}";
      new MessageFormat(pattern);
      String result = MessageFormat.format(pattern, 결석, 지각, 조퇴);
      if( 결석.equals("0") && 지각.equals("0") && 조퇴.equals("0")) {
         result = "올출";
      }
      System.out.println("[출석노트]");
      System.out.println(result);
      this.taNote.setText(result);
      return result;
   }

   void init지시사항() {
      String 지시사항 = this.taOrder.getText();
      WebElement element = Main.driver.findElement(By.name("strOrder"));
      if (element != null) {
         element.sendKeys(new CharSequence[]{지시사항});
         System.out.println("값을 입력했습니다: " + 지시사항);
      } else {
         System.out.println("해당 요소를 찾을 수 없습니다.");
      }
   }


   void init기타사항() {
      String 기타사항 = this.taEtc.getText();
      if( 기타사항.isEmpty() ) {
        String etcNode = init출석노트();
        기타사항 = etcNode;
        taEtc.setText(기타사항);
      }
      WebElement element = Main.driver.findElement(By.name("strMatter4"));
      if (element != null) {
         element.sendKeys(new CharSequence[]{기타사항});
         System.out.println("값을 입력했습니다: " + 기타사항);
      } else {
         System.out.println("해당 요소를 찾을 수 없습니다.");
      }

   }

}
