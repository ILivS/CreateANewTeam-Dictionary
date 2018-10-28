package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import main.DatabaseConnection;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class favoriteController implements Initializable {

    @FXML
    private ListView<String> listview1;
    private Connection connection = DatabaseConnection.getConnection();
    private FilteredList<String> filteredData ;
    private PreparedStatement preparedStatement = null;
    private ResultSet rs = null;


    private void getSelectedItems() {
        listview1.setOnMouseClicked(e -> {

        });
    }

    private void refreshfavListViewItems() {

        ObservableList<String> items = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(items, e->true);
        try {
            String query = "select  word   from  av  where star=\"yes\" ";
            preparedStatement = connection.prepareStatement(query);
            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                items.add(rs.getString(1));
            }
            listview1.setItems(items.sorted());
            preparedStatement.close();
            rs.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshfavListViewItems();
        getSelectedItems();
    }
}