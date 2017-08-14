package main.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import java.io.File;

public class SplitterController {
    public Button browseDir;
    public TextField dirField;
    public Label errorLable;

    public void browseDirSpl(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose xml file to splitt");
       chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File defaultDirectory = new File("C:/");
        chooser.setInitialDirectory(defaultDirectory);
        File selectedFile = chooser.showOpenDialog(browseDir.getScene().getWindow());
        if (dirField != null && selectedFile != null) {
            dirField.setText(String.valueOf(selectedFile)); //directory for splitted files
        }
    }

}
