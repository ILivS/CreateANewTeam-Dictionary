package controller;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import main.Main;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
public class addController  implements  Initializable {
    @FXML
    TextField tf1;
    @FXML
    HTMLEditor ta;
      @FXML
      Button addWordd;

    @FXML
    public void close() {
        Stage stage =  (Stage)addWordd.getScene().getWindow();
        stage.close();
    }
    @FXML

    public  void addWord()
    {
       MainController m= Main.getLoader().getController();
        String text1 = tf1.getText();
        String text2 = ta.getHtmlText();
        m.addWord(text1, text2);
        Stage stage =  (Stage)addWordd.getScene().getWindow();
        stage.close();
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
