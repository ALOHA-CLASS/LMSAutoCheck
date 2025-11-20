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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.aloha.domain.MainInfo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class MainController {

    @FXML private TextField tfSubDomain;      // 서브도메인
    @FXML private TextField tfId;             // 아이디
    @FXML private PasswordField tfPw;         // 비밀번호
    @FXML private CheckBox cbSave;            // 정보저장 체크박스

    @FXML private TextField tfCourseCode;   // 과정선택 콤보박스
    @FXML private ComboBox<Map.Entry<String,String>> cBoxCourseList;   // 과정선택 콤보박스

    @FXML private TextField tfA;              // 결석
    @FXML private TextField tfB;              // 지각
    @FXML private TextField tfC;              // 조퇴
    @FXML private TextArea taOrder;           // 지시사항
    @FXML private TextArea taEtc;             // 기타사항
    @FXML private TextArea taNote;            // 출석노트
    boolean autoLogin = false;

    @FXML private ListView<String> lvStudentList;  // 출석부 리스트뷰
    String selectedStudents = "";  // 선택된 학생들

    @FXML
    void initialize() {
      this.loadMainInfo();
      set도메인();
      String id = this.tfId.getText();
      String pw = this.tfPw.getText();
      boolean isChecked = this.cbSave.isSelected();
      if( isChecked ) {
        login(id, pw);
      } 

      // ListView 널체크
      if (lvStudentList == null) {
        System.err.println("lvStudentList가 null입니다. FXML 파일의 fx:id를 확인해주세요.");
        return;
      }
      lvStudentList.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
      lvStudentList.setOnMouseClicked(event -> {
          if (event.getClickCount() == 1) {
            
            try {
              selectedStudents = String.join(" ", lvStudentList.getSelectionModel().getSelectedItems());
              System.out.println("선택된 학생: " + selectedStudents);
            } catch (Exception e) {
              System.err.println("학생 선택 중 오류 발생: " + e.getMessage());
              e.printStackTrace();
            }
          }
      });
    }

    /**
     * 콤보박스 선택 변경 이벤트
     * - 과정코드 TextField에 선택된 과정코드 표시
     */
    private void onChangeCourseList() {
      // 콤보박스 선택 변경 이벤트 등록
      cBoxCourseList.setOnAction(e -> {
          Map.Entry<String, String> selectedEntry = cBoxCourseList.getSelectionModel().getSelectedItem();
          if (selectedEntry != null) {
            String courseCode = selectedEntry.getKey();
            tfCourseCode.setText(courseCode);
            System.out.println("선택된 과정코드: " + courseCode);
            // MainInfo 객체에 선택된 과정코드 저장
            MainInfo currentMainInfo = loadMainInfo();
            if( currentMainInfo == null ) {
              currentMainInfo = new MainInfo();
            }
            currentMainInfo.setSelectedCourseCode(courseCode);
            save(currentMainInfo);
            tfCourseCode.setText(courseCode);
          } else {
            tfCourseCode.clear();
            System.out.println("선택된 과정이 없습니다.");
          }
      });
    }

    @FXML
    void open(ActionEvent event) {
        
        String id = this.tfId.getText();
        String pw = this.tfPw.getText();
        // String link = this.tfSubDomain.getText();
        // this.login(id, pw);
        // WebDriverWait wait = new WebDriverWait(Main.driver, Duration.ofSeconds(10L));
        // String targetUrl = "https://tjoeun.atosoft.kr/worknet/Course/CourseList.asp";
        // wait.until(ExpectedConditions.urlToBe(targetUrl));
        // System.out.println("로그인 완료!");

        WebDriverWait wait = new WebDriverWait(Main.driver, Duration.ofSeconds(5L));
        try {
            String url = Main.domain + "/worknet/Diary/SubjectDiaryList.asp?strCCode=" + this.tfCourseCode.getText();
            Main.driver.get(url);
            wait.until(ExpectedConditions.urlToBe(url));
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

        // Wait for alert and accept it
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            org.openqa.selenium.Alert alert = Main.driver.switchTo().alert();
            System.out.println("Alert text: " + alert.getText());
            alert.accept(); // Press OK/Enter on the alert
            System.out.println("Alert accepted successfully");
            Alert jAlert = new Alert(Alert.AlertType.INFORMATION, "훈련일지가 정상적으로 등록되었습니다.");
            jAlert.showAndWait();
        } catch (Exception e) {
            System.out.println("No alert found or error handling alert: " + e.getMessage());
            Alert jAlert = new Alert(Alert.AlertType.INFORMATION, "훈련일지가 작성에 실패하였습니다. 다시 시도해주세요.");
            jAlert.showAndWait();
        }

    }

    @FXML
    void save(ActionEvent event) {
      String subDomain = this.tfSubDomain.getText();
      String id = this.tfId.getText();
      String pw = this.tfPw.getText();
      String courseCode = this.tfCourseCode.getText();
      MainInfo mainInfo = MainInfo.builder()
                                  .subDomain(subDomain)  
                                  .id(id)
                                  .pw(pw)
                                  .selectedCourseCode(courseCode)
                                  .build();
      save(mainInfo);
    }

    void save(MainInfo mainInfo) {
      
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
          this.tfSubDomain.clear();
          this.tfCourseCode.clear();
      } else {
          
          System.out.println(mainInfo);
          System.out.println("정보를 저장합니다.");
          // login(id, pw);
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

    @FXML
    void login() {
        String id = this.tfId.getText();
        String pw = this.tfPw.getText();
        login(id, pw);
    }

    /**
     * 로그인
     * - 서브도메인 설정 필요
     * - 로그인 후 강의목록 페이지 도달 확인
     * @param id
     * @param pw
     */
    void login(String id, String pw) {
        System.setProperty("webdriver.chrome.driver", "driver/chromedriver.exe");
        set도메인();
        if( Main.subdomain == null || Main.subdomain.isEmpty() ) {
          Alert alert = new Alert(Alert.AlertType.ERROR, "서브도메인을 입력해주세요.");
          alert.showAndWait();
          return;
        }
        String url = Main.domain + "/worknet/TLogin.asp";
        Main.driver.get(url);
        WebElement loginId = Main.driver.findElement(By.id("strLoginID"));
        loginId.sendKeys(new CharSequence[]{id});
        WebElement loginPwd = Main.driver.findElement(By.id("strLoginPwd"));
        loginPwd.sendKeys(new CharSequence[]{pw});
        WebElement loginButton = Main.driver.findElement(By.xpath("//input[@type='submit']"));
        loginButton.click();
        try {
            WebDriverWait wait = new WebDriverWait(Main.driver, Duration.ofSeconds(5L));
            String targetUrl = Main.domain + "/worknet/Course/CourseList.asp";
            System.out.println("targetUrl: " + targetUrl);
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
        loadCourseList();
    }

    MainInfo loadMainInfo() {
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
                tfSubDomain.setText(mainInfo.getSubDomain());
                String selectedCourseCode = mainInfo.getSelectedCourseCode();
                if( selectedCourseCode != null && !selectedCourseCode.isEmpty() ) {
                  tfCourseCode.setText(selectedCourseCode);
                }
                cbSave.setSelected(true);
                return mainInfo;
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("파일을 읽는 중 오류가 발생했습니다.");
            }
        }  
        else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("저장된 정보가 없습니다.");
            alert.setContentText("서브도메인(???.atosoft.kr), 아이디/비밀번호 를 입력 후 저장해주세요.");
            alert.showAndWait();
            System.err.println("파일이 존재하지 않습니다.");
        } 

        
        return null;

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
      List<WebElement> checkBoxes = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//input[@type='checkbox' and @value='" + value + "']")));
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
      System.out.println(결석);
      System.out.println(지각);
      System.out.println(조퇴);
      String pattern = "결석({0}명) : {1}\n지각({2}명) : {3}\n조퇴({4}명) : {5}";
      new MessageFormat(pattern);
      String result = MessageFormat.format(pattern, 결석.split(" ").length, 결석, 지각.split(" ").length, 지각, 조퇴.split(" ").length, 조퇴);
      if( 결석.equals("0") && 지각.equals("0") && 조퇴.equals("0")) {
         result = "전원출석";
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

   void set도메인() {
    String link = this.tfSubDomain.getText();
    if( link != null && !link.isEmpty() ) {
      Main.subdomain = link;
      Main.domain = "https://{}.atosoft.kr";
      // {} 에 subdomain 넣기
      Main.domain = Main.domain.replace("{}", Main.subdomain);
      System.out.println("도메인 설정: " + Main.domain);
    } else {
      System.out.println("서브도메인이 설정되지 않았습니다.");
    }
   }


  /**
   * 강의목록 불러오기
   * - 과정명, 과정코드 맵으로 반환
   */
  void loadCourseList() {
    // 현재 url 경로 확인
    String currentUrl = Main.driver.getCurrentUrl();
    System.out.println("현재 URL: " + currentUrl);
    // /worknet/Course/CourseList.asp 포함 여부 확인
    if( !currentUrl.contains("/worknet/Course/CourseList.asp") ) {
      // [과정등록] - 교육과정 리스트로 이동
      // domain + "/worknet/Course/CourseList.asp" 
      String url = Main.domain + "/worknet/Course/CourseList.asp";
      Main.driver.get(url);
      WebDriverWait wait = new WebDriverWait(Main.driver, Duration.ofSeconds(10L));
      wait.until(ExpectedConditions.urlToBe(url));
      System.out.println("[과정등록] - 강의목록 페이지로 이동");
    }
    // 강의목록 table id : "mainListTable"
    // 테이블이 로드될 때까지 기다리기
    WebDriverWait wait = new WebDriverWait(Main.driver, Duration.ofSeconds(15));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mainListTable")));
    
    Map<String, String> courseMap = new LinkedHashMap<>();
    
    // 다양한 방법으로 테이블 요소 찾기 시도
    List<WebElement> rows = null;
    try {
      // 방법 1: CSS 선택자 사용
      rows = Main.driver.findElements(By.cssSelector("#mainListTable tbody tr"));
      System.out.println("CSS 선택자로 찾은 행 개수: " + rows.size());
    } catch (Exception e) {
      System.out.println("CSS 선택자 실패: " + e.getMessage());
    }
    
    if (rows == null || rows.isEmpty()) {
      try {
        // 방법 2: XPath 사용
        rows = Main.driver.findElements(By.xpath("//table[@id='mainListTable']//tbody//tr"));
        System.out.println("XPath로 찾은 행 개수: " + rows.size());
      } catch (Exception e) {
        System.out.println("XPath 실패: " + e.getMessage());
      }
    }
    
    if (rows != null && !rows.isEmpty()) {
      System.out.println("총 행 개수: " + rows.size());
      
      // for (int i = 0; i < Math.min(rows.size(), 3); i++) {
      for (int i = 0; i < rows.size(); i++) {
        WebElement row = rows.get(i);
        
        // 각 셀을 다양한 방법으로 찾기
        List<WebElement> cells = row.findElements(By.tagName("td"));
        System.out.println("행 " + (i+1) + " 셀 개수: " + cells.size());
        
        // 모든 셀의 내용을 출력하여 디버깅 (innerText도 시도)
        // for (int j = 0; j < Math.min(cells.size(), 12); j++) {
        //   WebElement cell = cells.get(j);
        //   String cellText = cell.getText().trim();
        //   String cellInnerText = "";
        //   try {
        //     cellInnerText = (String) ((JavascriptExecutor) Main.driver).executeScript("return arguments[0].innerText;", cell);
        //   } catch (Exception e) {
        //     // innerText 실패시 무시
        //   }
        //   // System.out.println("  셀[" + j + "] getText: '" + cellText + "', innerText: '" + cellInnerText + "'");
        // }
        
        if (cells.size() >= 4) {
          String courseName = cells.get(2).getText().trim();
          String courseCode = cells.get(3).getText().trim();
          
          // innerText로도 시도
          if (courseName.isEmpty()) {
            try {
              courseName = (String) ((JavascriptExecutor) Main.driver).executeScript("return arguments[0].innerText;", cells.get(2));
            } catch (Exception e) {
              // 무시
            }
          }
          if (courseCode.isEmpty()) {
            try {
              courseCode = (String) ((JavascriptExecutor) Main.driver).executeScript("return arguments[0].innerText;", cells.get(3));
            } catch (Exception e) {
              // 무시
            }
          }
          
          courseMap.put(courseCode, courseName);
          System.out.println("과정코드: " + courseCode + ", 과정명: " + courseName);
        }
      }
    } else {
      System.out.println("테이블 행을 찾을 수 없습니다.");
    }

    // cBoxCourseList 콤보박스에 과정명 추가
    cBoxCourseList.getItems().clear();
    for (Map.Entry<String, String> entry : courseMap.entrySet()) {
      String courseCode = entry.getKey();
      String courseName = entry.getValue();
      cBoxCourseList.getItems().add(entry);
      System.out.println("콤보박스에 추가: " + courseCode + " - " + courseName);
    }
    if (!courseMap.isEmpty() ) {
      // 첫 번째 과정 선택
      Map.Entry<String, String> firstEntry = courseMap.entrySet().iterator().next();
      cBoxCourseList.getSelectionModel().select(firstEntry);
      // tfCourseCode.setText(firstEntry.getValue());
      System.out.println("첫 번째 과정 선택: " + firstEntry.getKey() + " - " + firstEntry.getValue());
    } else {
      System.out.println("과정 목록이 비어있습니다.");  
      tfCourseCode.clear();
    }

    // 저장된 과정코드가 있으면 선택
    String savedCourseCode = loadMainInfo().getSelectedCourseCode();
    if( savedCourseCode != null && !savedCourseCode.isEmpty() ) {
      for( Map.Entry<String, String> entry : courseMap.entrySet() ) {
        if( entry.getKey().equals(savedCourseCode) ) {
          cBoxCourseList.getSelectionModel().select(entry);
          System.out.println("저장된 과정코드로 선택: " + entry.getKey() + " - " + entry.getValue());
          break;
        }
      }
    }

    // 과정 선택 콤보박스 선택 변경 이벤트 등록
    onChangeCourseList();
  }

  @FXML
  void loadStudentList() {
    String courseCode = tfCourseCode.getText();
    if( courseCode == null || courseCode.isEmpty() ) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "과정코드를 입력해주세요.");
      alert.showAndWait();
      return;
    }
    String url = Main.domain + "/worknet/Student/StudentReport.asp?strCCode=" + courseCode;
    Main.driver.get(url);
    WebDriverWait wait = new WebDriverWait(Main.driver, Duration.ofSeconds(10L));
    wait.until(ExpectedConditions.urlToBe(url));
    System.out.println("[학적부] - 수강생목록 페이지로 이동: " + url);

    // 학적부 table id : "mainListTable"
    // 테이블이 로드될 때까지 기다리기
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mainListTable")));
    // 학생 목록 테이블에서 학생 이름 추출
    // 다양한 방법으로 테이블 요소 찾기 시도
    List<WebElement> rows = null;
    try {
      // 방법 1: CSS 선택자 사용
      rows = Main.driver.findElements(By.cssSelector("#mainListTable tbody tr"));
      System.out.println("CSS 선택자로 찾은 행 개수: " + rows.size());
    } catch (Exception e) {
      System.out.println("CSS 선택자 실패: " + e.getMessage());
    }
    
    if (rows == null || rows.isEmpty()) {
      try {
        // 방법 2: XPath 사용
        rows = Main.driver.findElements(By.xpath("//table[@id='mainListTable']//tbody//tr"));
        System.out.println("XPath로 찾은 행 개수: " + rows.size());
      } catch (Exception e) {
        System.out.println("XPath 실패: " + e.getMessage());
      }
    }

    List<String> studentNames = new ArrayList<String>();
    lvStudentList.getItems().clear();
    for (int i = 0; i < rows.size(); i++) {
      WebElement row = rows.get(i);
      List<WebElement> cells = row.findElements(By.tagName("td"));
      
      if (cells.size() >= 5) {
        String studentName = (String) ((JavascriptExecutor) Main.driver).executeScript("return arguments[0].innerText;", cells.get(4));
        System.out.println("학생 " + (i+1) + ": " + studentName);
        studentNames.add(studentName);
      }
    }
    lvStudentList.getItems().addAll(studentNames);
  }


  public void onSelectListView(ActionEvent event) {
    String selectedNames = String.join(" ", lvStudentList.getSelectionModel().getSelectedItems());
    System.out.println("선택된 학생: " + selectedNames);
    tfA.setText(selectedNames);
  }

  public void select결석() {
    String selectedNames = String.join(" ", lvStudentList.getSelectionModel().getSelectedItems());
    System.out.println("선택된 학생: " + selectedNames);
    tfA.setText(selectedNames);
  }

  public void select지각() {
    String selectedNames = String.join(" ", lvStudentList.getSelectionModel().getSelectedItems());
    System.out.println("선택된 학생: " + selectedNames);
    tfB.setText(selectedNames);
  }

  public void select조퇴() {
    String selectedNames = String.join(" ", lvStudentList.getSelectionModel().getSelectedItems());
    System.out.println("선택된 학생: " + selectedNames);
    tfC.setText(selectedNames);
  }

}
