package main.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import main.bussiness.XmlGenerator;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

public class Controller {
    public Button browseButton;
    public TextField dirField;
    public TextField nameField;
    public TextField numberOfRecordField;
    public Button generateButton;

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

        try {
            Long.parseLong(numberOfRecordField.getText());
            Long recordsCount = Long.valueOf(numberOfRecordField.getText());
            xml.generate(fileName, dirName, recordsCount);
        } catch(Exception e) {
            System.out.println("Number of records should be positive number!");
        }



    }


}
