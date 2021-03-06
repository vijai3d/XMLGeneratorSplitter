package main.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import main.bussiness.Generator;
import main.utils.Checkers;
import main.utils.newDirectory;
import org.xml.sax.SAXException;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

public class GeneratorController {
    public Button browseButton;
    public TextField dirField;
    public TextField nameField;
    public TextField numberOfRecordField;
    public Button generateButton;
    public Label errorLable;
    public ProgressIndicator progressCircle;
    public Button cancelButton;

    public void browseDir(ActionEvent actionEvent) {
        newDirectory.directoryChoose(browseButton, dirField);
    }

    public void generateXML(ActionEvent actionEvent) throws JAXBException, IOException, SAXException {
        Generator generator = new Generator();
        Checkers checkers = new Checkers();
        String fileName = nameField.getText();
        String dirName = dirField.getText();
        String recordNumberInput = numberOfRecordField.getText();
        if (checkers.checkFilename(fileName)) {
            if (!dirName.equals("")) {
                if (checkers.checkNumber(recordNumberInput)) {
                    Long recordsCount = Long.valueOf(numberOfRecordField.getText());
                    final Task generate = generator.runGenerator(fileName, dirName, recordsCount);
                    progressCircle.progressProperty().unbind();
                    progressCircle.progressProperty().bind(generate.progressProperty());
                    errorLable.textProperty().bind(generate.messageProperty());
                    generateButton.setDisable(true);
                    cancelButton.setDisable(false);
                    generate.messageProperty().addListener(new ChangeListener<String>() {
                        public void changed(ObservableValue<? extends String> observable,
                                            String oldValue, String newValue) {
                        }
                    });
                    new Thread(generate).start();
                    onSucceeded(generate, generateButton, cancelButton);
                    onCancel(generate, cancelButton, generateButton, progressCircle);
                } else { errorLable.setText("Number of records should be positive number!"); }
            } else { errorLable.setText("Please choose directory"); }
        } else { errorLable.setText("Please choose correct file name"); }
    }

     void onSucceeded(Task generate, final Button generateButton, final Button cancelButton) {
        generate.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t)
            {
                generateButton.setDisable(false);
                cancelButton.setDisable(true);
            }
        });
    }

     void onCancel(final Task generate, final Button cancelButton, final Button generateButton, final ProgressIndicator progressCircle) {
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                generateButton.setDisable(false);
                cancelButton.setDisable(true);
                generate.cancel(true);
                progressCircle.progressProperty().unbind();
                progressCircle.setProgress(0);
            }
        });

    }
}
