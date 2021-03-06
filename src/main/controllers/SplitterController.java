package main.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import main.bussiness.Splitter;
import main.utils.Checkers;
import main.utils.newDirectory;
import java.io.File;

public class SplitterController {
    public Button browseFile;
    public TextField fileField;
    public Label errorLabel;
    public Button splitButton;
    public TextField bytesField;
    public TextField dirField;
    public Button dirBrowseButton;
    public ProgressIndicator progressCircle;
    public Button cancelButton;
    private File selectedFile;

    public void browseFileSpl(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose xml file to split");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File defaultDirectory = new File("C:/");
        chooser.setInitialDirectory(defaultDirectory);
        selectedFile = chooser.showOpenDialog(browseFile.getScene().getWindow());
        if (fileField != null && selectedFile != null) {
            fileField.setText(String.valueOf(selectedFile)); //directory for splitted files
        }
    }
    public void browseDir(ActionEvent actionEvent) {
        newDirectory.directoryChoose(dirBrowseButton, dirField);
    }

    public void splitsHandler(ActionEvent actionEvent) {
        final Splitter splitter = new Splitter();
        Checkers checkers = new Checkers();
        String pathToFile = fileField.getText();
        String dir = dirField.getText();
        String bytes = bytesField.getText();
        if (pathToFile != null) {
            if (!dir.equals("")) {
                if (checkers.checkNumber(bytes)) {
                    Long userBytes = Long.valueOf(bytesField.getText());
                    String fileName = selectedFile.getName().replaceFirst("[.][^.]+$", "");
                    final Task split = splitter.split(fileName, pathToFile, dir, userBytes);
                    progressCircle.progressProperty().unbind();
                    progressCircle.progressProperty().bind(split.progressProperty());
                    errorLabel.textProperty().bind(split.messageProperty());
                    splitButton.setDisable(true);
                    cancelButton.setDisable(false);
                    split.messageProperty().addListener(new ChangeListener<String>() {
                        public void changed(ObservableValue<? extends String> observable,
                                            String oldValue, String newValue) {

                        }
                    });
                    new Thread(split).start();
                    GeneratorController gc = new GeneratorController();
                    gc.onSucceeded(split, splitButton, cancelButton);
                    gc.onCancel(split, cancelButton, splitButton, progressCircle);
                } else {
                    errorLabel.setText("Number of records should be positive number!");
                }
            } else {
                errorLabel.setText("Please choose directory");
            }
        } else {
            errorLabel.setText("Please choose correct file name");
        }
    }
}
