package controller;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.io.FileNotFoundException;
import  java.io.IOException;
import netscape.javascript.JSObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;

public class oxfordController   implements Initializable {
         private  String word;
         private String res1="";
         private  String res2="";
         @FXML
    TextArea textArea;
         public  void  init(String word)
         {
             this.word=word;
         }
    protected String dictionaryEntries() {
        final String language = "en";
        final String word_id = word.toLowerCase();
        return "https://od-api.oxforddictionaries.com:443/api/v1/entries/" + language + "/" + word_id + "/synonyms;antonyms";
    }
    protected String doInBackground() {
        final String app_id = "77118f7a";
        final String app_key = "4334835be6fd06db228db6666199b937";
        try {
            URL url = new URL(dictionaryEntries());
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

            urlConnection.setRequestProperty("Accept","application/json");
            urlConnection.setRequestProperty("app_id",app_id);
            urlConnection.setRequestProperty("app_key",app_key);
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
            return stringBuilder.toString();
        }
        catch (IOException ex)
        {
            return  "nAn";
        }
        catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }

    }
    protected  void   stringFilter ()
    {

        try {
            Object obj = new JSONParser().parse(doInBackground());
            System.out.println(obj);
            JSONObject js = (JSONObject) obj;
            JSONArray res = (JSONArray) js.get("results");
            for (Object obj1 : res.toArray()) {
                JSONObject p1 = (JSONObject) obj1;
                JSONArray i1 = (JSONArray) p1.get("lexicalEntries");
                for (Object j1 : i1.toArray()) {
                    JSONObject p2 = (JSONObject) j1;
                    JSONArray i2 = (JSONArray) p2.get("entries");
                    for (Object j2 : i2.toArray()) {
                        JSONObject p3 = (JSONObject) j2;
                        JSONArray i3 = (JSONArray) p3.get("senses");
                        for (Object j3 : i3.toArray()) {
                            JSONObject p4 = (JSONObject) j3;
                            JSONArray i4 = (JSONArray) p4.get("synonyms");
                            JSONArray i5 =(JSONArray) p4.get("antonyms");
                           if (i5 != null)
                           {
                               for (Object j4 : i5.toArray()) {
                                   JSONObject p6 = (JSONObject) j4;
                                   String s= (String) p6.get("text");
                                   res2+= s +", ";
                               }
                           }
                            for (Object j4 : i4.toArray()) {
                                JSONObject p5 = (JSONObject) j4;
                                String s= (String) p5.get("text");
                                res1+= s +", ";
                            }
                            textArea.setWrapText(true);
                            textArea.setStyle("-fx-opacity: 1;");
                            String finalRes="";
                             finalRes += res2.isEmpty() ?  ("Synonyms: " + res1  ) :  ( res1+"\r\n" + "Antonyms: " + res2);
                            textArea.setText( finalRes);
                        }
                    }

                }
            }
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources)  {


    }
}
