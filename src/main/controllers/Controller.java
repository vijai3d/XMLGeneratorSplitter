package main.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import main.bussiness.XmlGenerator;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {
    public Button browseButton;
    public TextField dirField;
    public TextField nameField;
    public TextField numberOfRecordField;
    public Button generateButton;
    public Label errorLable;

    public void initialize() {
        errorLable.setText("");
    }
    public void browseDir(ActionEvent actionEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("JavaFX Projects");
        File defaultDirectory = new File("c:/");
        chooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = chooser.showDialog(browseButton.getScene().getWindow());
        dirField.setText(String.valueOf(selectedDirectory)); //directory for generated file
    }

    public void generateXML(ActionEvent actionEvent) throws JAXBException, IOException, SAXException {
        XmlGenerator xml = new XmlGenerator();
        String fileName = nameField.getText();
        String dirName = dirField.getText();

        if (containsIllegals(fileName)) {
            if (!dirName.equals("")) {
                if (Long.parseLong(numberOfRecordField.getText()) >0) {
                    Long recordsCount = Long.valueOf(numberOfRecordField.getText());
                    xml.generate(fileName, dirName, recordsCount);
                    initialize();
                } else {
                    errorLable.setText("Number of records should be positive number!");
                }
            } else {
                errorLable.setText("Please choose directory");
            }
        } else {
            errorLable.setText("Please choose correct file name");
        }


    }

    public boolean containsIllegals(String toExamine) {
        Pattern pattern = Pattern.compile("[\\w,\\s-]");
        Matcher matcher = pattern.matcher(toExamine);
        return matcher.find();
    }
}
