package main;

import javafx.scene.control.Alert;

public  class alert {
     public  static  void errorAlert (String text)
     {
         Alert alert = new Alert(Alert.AlertType.ERROR);
         alert.setTitle("Error ");
         alert.setHeaderText(null);
         alert.setContentText(text);
         alert.showAndWait();
     }
    public  static  void infoAlert (String text)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
       alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
    public  static  void warningAlert (String text)
    {
        Alert alert = new Alert(Alert.AlertType.WARNING);
           alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }




}
