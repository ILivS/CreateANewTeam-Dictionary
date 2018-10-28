package controller;
import gtranslate.Audio;
import gtranslate.Language;
import gtranslate.text.Text;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;

import gtranslate.GoogleTranslate;
import javazoom.jl.decoder.JavaLayerException;
import main.IsConnected;
import main.alert;

import java.io.InputStream;
import java.util.Map;
import  java.util.HashMap;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GGController implements  Initializable  {
 private   Map<String, String> map = new HashMap<String, String>();
    @FXML
    private  ChoiceBox<String> cb1; private  String rescb1;
    @FXML
     private  ChoiceBox<String> cb2; private  String rescb2;
     @FXML
private  String resTA;
@FXML
private    TextArea textArea;
@FXML
private TextArea textArea1;
@Override
    public void initialize(URL location, ResourceBundle resources) {
      map.put("Vietnamese","vi");
        map.put("English","en");
        map.put("Korean","ko");
        map.put("Japanese","ja");
        map.put("Chinese","zh");
        map.put("Thailand","th");
        ObservableList<String> languages = FXCollections.observableArrayList("Vietnamese","English","Korean","Chinese","Thailand","Japanese");
        cb1.setItems(languages);
        cb2.setItems(languages);
        cb2.setValue("Vietnamese");
        cb1.setValue("English");
    }
    @FXML
    public  void exchange()
    {
        String temp=cb1.getValue();
        cb1.setValue(cb2.getValue());
        cb2.setValue(temp);
    }
  @FXML
    public void instantSearch( )
    {
        textArea.textProperty().addListener((observableValue, oldValue, newValue) -> {
            try {
                if (IsConnected.IsConnecting(100))
                { rescb1=map.get(cb1.getValue());
                    rescb2=map.get(cb2.getValue());
                    resTA=newValue;
                    try
                    {       textArea1.setDisable(true);
                        textArea1.setStyle("-fx-opacity: 1;");
                        textArea1.setText(GoogleTranslate.translate(rescb1, rescb2, resTA));
                    }

                    catch (IOException e) {
                        e.printStackTrace();
                    }}
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    @FXML
    public  void speak() throws  IOException,InterruptedException
    {
        System.out.println(map.get(cb1.getValue()));
        if (IsConnected.IsConnecting(1000) && textArea.getText()!=null ) {
            try {
                InputStream sound = null;
                Audio audio = Audio.getInstance();
                sound = audio.getAudio(textArea.getText(), map.get(cb1.getValue()));
                audio.play(sound);
            } catch (IOException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JavaLayerException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    @FXML
    public  void translate()
    {
          rescb1=map.get(cb1.getValue());
          rescb2=map.get(cb2.getValue());
          resTA=textArea.getText();
          try
          {
              if (IsConnected.IsConnecting(2000) ) {
                  System.out.println(rescb1);
                  textArea1.setDisable(true);
                  textArea1.setStyle("-fx-opacity: 1;");
                  textArea1.setText(GoogleTranslate.translate(rescb1, rescb2, resTA));
              }
              else
              {
                  alert.warningAlert("No internet connection!");
              }
          }
          catch (InterruptedException e) {
              e.printStackTrace();
          }
          catch (IOException e) {
              e.printStackTrace();
          }
    }
}
