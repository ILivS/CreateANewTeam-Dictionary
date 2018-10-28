package controller;

import SymSpell.SymSpellTrigger;
import animatefx.animation.*;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import gtranslate.Audio;
import gtranslate.Language;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javazoom.jl.decoder.JavaLayerException;
import main.DatabaseConnection;
import main.IsConnected;
import main.Main;
import main.alert;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController implements Initializable {
    private String missspellWord;
    private Connection connection = DatabaseConnection.getConnection();
    private ObservableList<String> history = FXCollections.observableArrayList();
    private ObservableList<String> items = FXCollections.observableArrayList();
    private FilteredList<String> filteredData;
    private PreparedStatement preparedStatement = null;
    private ResultSet rs = null;
    private FXMLLoader loaderDialog = new FXMLLoader();
    private FXMLLoader loaderFXML = new FXMLLoader();
    @FXML
    FontAwesomeIconView favor;
    private String html;
    private String star;
    @FXML
    private ListView<String> listview1;
    @FXML
    HBox fav;
    @FXML
    HBox homepage;
    @FXML
    HBox gg;
    @FXML
    BorderPane pane1;
    @FXML
    Button dlt;
    @FXML
    Button edit;
    @FXML
    Button add;
    @FXML
    TextField search;
    @FXML
    AnchorPane apane;
    @FXML
    private WebView webview1;
    private WebEngine webEngine;

    @FXML
    public void close() {

        System.exit(0);
    }

    @FXML
    void updateWord(String word, String html, String old) {
        String query = "update av set word=?,html=? where word='" + old + "' ";
        executeQuery(query, word, html);
        webEngine.loadContent(getHtml());
    }

    void addWord(String word, String html) {
        if (items.contains(word))
        {
            updateWord(word,html,word);
        }
            else
        {
            String query = "INSERT INTO av (word, html) VALUES(?,?)";
            executeQuery(query, word, html);
        }

    }

    @FXML
    public void dltWord() {
        if (getSelecting() != null) {
            if (history.contains(getSelecting())) {
                String queryHistory = "delete from av  where history=? ";
                executeQuery(queryHistory, getSelecting(), null);
                history.remove(getSelecting());
            }
            String query = "delete from av  where word=? and html=? ";
            executeQuery(query, getSelecting(), getHtml());
            webEngine.loadContent("");
            listview1.getSelectionModel().clearSelection();
        }
    }

    public void activateButt(HBox button) {
        button.setDisable(true);
        button.getStyleClass().add("tag");
        Node icon = button.getChildren().get(0);
        Node text = button.getChildren().get(1);
        icon.setStyle("-fx-fill: #039BE5;-fx-font-family: FontAwesome; -fx-font-size: 24px;");
        text.setStyle("-fx-fill: #039BE5;");
        FadeInRight fadeInRight = new FadeInRight(button);
        fadeInRight.setSpeed(2);
        fadeInRight.play();

    }

    public void deactivateButt(HBox button) {
        button.setDisable(false);
        button.getStyleClass().remove("tag");
        button.getStyleClass().add("left_button");
        Node icon = button.getChildren().get(0);
        Node text = button.getChildren().get(1);
        icon.setStyle("-fx-fill: #fff;-fx-font-family: FontAwesome; -fx-font-size: 24px;");
        text.setStyle("-fx-fill: #fff;");
    }

    @FXML
    public void loadMain() throws IOException {
        activateButt(homepage);
        deactivateButt(fav);
        deactivateButt(gg);
        loadFXML("/fxml/mainCenter.fxml");
        Main.setLoader(loaderFXML);
    }

    @FXML
    public void loadGoogleTranslate() throws IOException {
        activateButt(gg);
        deactivateButt(fav);
        deactivateButt(homepage);
        loadFXML("/fxml/GoogleTranslate.fxml");
    }
    @FXML
    public void loadFavorite() throws  IOException  {
        activateButt(fav);
        deactivateButt(gg);
        deactivateButt(homepage);
        loadFXML("/fxml/favorite.fxml");
    }
    @FXML
    private void suggestionSearch(String suggest) {
        listview1.setItems(SymSpellTrigger.lookUp(suggest, items));
    }
    private void executeQuery2(String query, String word)  {
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, word);
            preparedStatement.execute();
            preparedStatement.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        refreshListViewItems();
    }
    private String getStar() {
        try {
            String query = " SELECT * FROM  av WHERE word= ? ";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, getSelecting());
            rs = preparedStatement.executeQuery();
            star = rs.getString("star");
            preparedStatement.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return star;
    }
    @FXML
    private void getSelectedItems() {
        listview1.setOnMouseClicked(e -> {
            if(getStar()!=null) favor.setStyle("-fx-fill: #f39c12; -fx-font-size: 25px;");
            else favor.setStyle("-fx-fill: #d1d1d1; -fx-font-size: 25px;");
            webEngine.loadContent(getHtml());
            if (!history.contains(getSelecting())) {
                history.add(0, getSelecting());
                String query = "INSERT INTO av  (history) VALUES(?)";
                executeQuery(query, getSelecting(), null);
            }
            listview1.setVisible(false);
        });

    }

    private String getSelecting() {

        return listview1.getSelectionModel().getSelectedItem();
    }
    @FXML
    public void loadaddingDialog() throws IOException {
        loadDialog("/fxml/addDialog.fxml");
    }

    @FXML
    public void loadeditingDialog() throws IOException {
        if (getSelecting() != null) {
            loadDialog("/fxml/updateDialog.fxml");
            updateController ud = loaderDialog.getController();
            ud.initData(getSelecting(), getHtml());
        } else {
            alert.errorAlert("No word selected!");
        }
    }
    private void executeQuery(String query, String word, String html) {
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, word);
            if (html != null)
                preparedStatement.setString(2, html);
            preparedStatement.execute();
            preparedStatement.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        refreshListViewItems();
    }


    @FXML
    void setFav()  {
        if (getSelecting() != null) {
            if(getStar()==null){
                String query = "update av set star=\"yes\" where word=? ";
                executeQuery2(query, getSelecting());
                favor.setStyle("-fx-fill: #f39c12; -fx-font-size: 25px;");
            }
            else{
                String query = "update av set star= null where word=? ";
                executeQuery2(query, getSelecting());
                favor.setStyle("-fx-fill: #d1d1d1; -fx-font-size: 25px;");
            }
        } else {
            alert.errorAlert("No word selected!");
        }
    }

    @FXML
    public void getOxford() throws IOException, InterruptedException {
        if (getSelecting() != null && IsConnected.IsConnecting(500)) {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/oxford.fxml"));
            loader.load();
            oxfordController o = loader.getController();
            o.init(getSelecting());
            if (!o.doInBackground().equals("nAn")) {
                o.stringFilter();
                Parent p = loader.getRoot();
                Stage stage = new Stage();
                stage.setScene(new Scene(p));
                stage.show();
            }
        }
    }

    @FXML
    public void searchWord() {
        listview1.getSelectionModel().clearSelection();
        webEngine.loadContent("");
        listview1.setVisible(true);
        listview1.setItems(items);
        FadeIn fadeIn = new FadeIn(listview1);
        fadeIn.setSpeed(4);
        fadeIn.play();
        search.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if ( newValue == null || newValue.isEmpty())
            {
                listview1.setItems(history);
            }
            else
            {
                filteredData.setPredicate(word -> {
                    String lowerCaseFilter = newValue.toLowerCase();
                    return word.toLowerCase().startsWith(lowerCaseFilter);
                });
                if (filteredData.isEmpty() ) {
                    missspellWord = newValue;
                    suggestionSearch(missspellWord);
                } else
                    listview1.setItems(filteredData);
            }
        });
    }

    @FXML
    public void speak() throws IOException, InterruptedException {
        if (IsConnected.IsConnecting(1000) && getSelecting() != null) try {
            InputStream sound;
            Audio audio = Audio.getInstance();
            sound = audio.getAudio(getSelecting(), Language.ENGLISH);
            audio.play(sound);
        } catch (IOException | JavaLayerException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        else if (getSelecting() != null && !IsConnected.IsConnecting(1000)) {
            Media pick = new Media(this.getClass().getResource("/music/error.mp3").toString());
            MediaPlayer player = new MediaPlayer(pick);
            player.play();
        }
    }

    private String getHtml() {
        try {
            String query = " SELECT * FROM  av  WHERE word= ? ";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, getSelecting());
            rs = preparedStatement.executeQuery();
            while (rs.next())
                html = rs.getString("html");
            preparedStatement.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return html;
    }

    private void loadFXML(String link) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(link));
        loader.load();
        loaderFXML = loader;
        Parent root = loader.getRoot();
        new FadeIn(root).play();
        pane1.setCenter(root);
    }

    private void loadDialog(String link) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(link));
        loader.load();
        loaderDialog = loader;
        Parent p = loader.getRoot();
        Stage stage = new Stage();
        Scene scene = new Scene(p);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.initStyle(StageStyle.TRANSPARENT);
        p.getScene().setFill(Color.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);
        Stage currWindow = (Stage) edit.getScene().getWindow();
        stage.initOwner(currWindow);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.show();


    }

    private ObservableList<String> getItems(String query) {
        ObservableList<String> items = FXCollections.observableArrayList();
        try {
            preparedStatement = connection.prepareStatement(query);
            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                items.add(rs.getString(1));
            }

            preparedStatement.close();
            rs.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return items;


    }

    private void refreshListViewItems() {
        ObservableList<String> itemsCopy;
        String query = "select  word   from  av   where word  IS NOT NULL  ";
        itemsCopy = getItems(query);
        items = itemsCopy;
        filteredData = new FilteredList<>(itemsCopy, e -> true);
        itemsCopy.sorted();
    }

    private void loadHistory() {

        String query = "select  history  from  av  where history IS NOT NULL ";
        history = getItems(query);
        Collections.reverse(history);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webEngine = webview1.getEngine();
        webEngine.setUserStyleSheetLocation(getClass().getResource("/style/webStyle.css").toString());
        refreshListViewItems();
        loadHistory();
        listview1.setVisible(true);
        listview1.setItems(history);
        getSelectedItems();

    }
}
