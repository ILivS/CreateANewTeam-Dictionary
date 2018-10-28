package controller;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import main.Main;
import java.net.URL;
import java.util.ResourceBundle;
public class updateController  implements Initializable {
    @FXML
    TextField tf1;
    @FXML
    HTMLEditor ta;
    @FXML
Button updateWordd;
    private String word;
    void initData(String word, String html )
    {
        this.word=word;
        tf1.setText(word);
        ta.setHtmlText(html);
    }
    @FXML
    public void close() {
        Stage stage =  (Stage)updateWordd.getScene().getWindow();
        stage.close();
    }
    @FXML
    public void updateWord()  {
        MainController m= Main.getLoader().getController();
        String text1 = tf1.getText();
        String text2 = ta.getHtmlText();
         m.updateWord(text1, text2,word);
        Stage stage =  (Stage)updateWordd.getScene().getWindow();
        stage.close();
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
