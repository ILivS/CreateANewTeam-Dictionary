package main;

import SymSpell.SymSpell;
import animatefx.animation.FadeIn;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.FileNotFoundException;

public class Main extends Application {
    private  double x,y;
    private  static  SymSpell symSpell;
    private  static  FXMLLoader mLoader;
    public  static  FXMLLoader getLoader()
    {
        return  mLoader;
    }
    public  static  void  setLoader(FXMLLoader tempLoader)
    {
        mLoader=tempLoader;
    }
    public static  SymSpell getSymSpell ()
    {
          return  symSpell;
    }
    @Override
    public void start(Stage primaryStage) throws Exception{
        symSpell = new SymSpell(-1, 3, -1, 10);
        int termIndex = 0;
        int countIndex = 1;
        String path = "src/SymSpell/data/frequency_dictionary_en_82_765.txt";
        if (!symSpell.loadDictionary(path, termIndex, countIndex)) try {
            throw new FileNotFoundException("File not found");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        FXMLLoader loader= new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/Main.fxml"));
        loader.load();
        mLoader=loader;
        Parent root= loader.getRoot();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Dictionary");
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        root.getScene().setFill(Color.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);
        root.getStylesheets().add(getClass().getResource("../style/style.css").toString());
        root.setOnMousePressed(event -> {
            x = event.getSceneX();
            y = event.getSceneY();
        });
        root.setOnMouseDragged(mouseEvent -> {
            primaryStage.setX(mouseEvent.getScreenX() -x);
            primaryStage.setY(mouseEvent.getScreenY() -y);
        });
        primaryStage.show();
        new FadeIn(root).play();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
