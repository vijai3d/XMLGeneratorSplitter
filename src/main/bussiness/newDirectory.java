package main.bussiness;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;


import java.io.File;

public class newDirectory {

    public static void directoryChoose(Button browseButton, TextField dirField) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose directory");
        File defaultDirectory = new File("C:/");
        chooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = chooser.showDialog(browseButton.getScene().getWindow());
        if (dirField != null && selectedDirectory != null && selectedDirectory.isDirectory()
                && selectedDirectory.exists()) {
            dirField.setText(String.valueOf(selectedDirectory)); //directory for generated file
        }
    }
}
