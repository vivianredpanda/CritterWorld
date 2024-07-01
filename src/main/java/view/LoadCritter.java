package view;

import controller.Controller;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class LoadCritter extends Application implements Initializable{

    private Button selectFile;

    private Button uploadCritter;

    private Controller controller;

    private DrawWorld draw;

    private Parent parent;

    public LoadCritter(Controller c, DrawWorld dw, Parent nd){
        controller = c;
        draw = dw;
        parent = nd;
    }

    public void start(final Stage stage){
        ClassLoader loader = LoadCritter.class.getClassLoader();
        URL r = loader.getResource("fxml/loadCritter.fxml");
        if (r == null) {
            System.out.println("No FXML resource found for load critter.");
            try {
                stop();
            } catch (Exception e) {}
        }

        try {
            Parent node = FXMLLoader.load(r);
            Scene scene = new Scene(node);
            stage.setTitle("Load critter");
            stage.setScene(scene);
            stage.sizeToScene();
            stage.show();

            // make sure only one radio button is selected
            ToggleGroup group = new ToggleGroup();
            RadioButton rb1 = (RadioButton) node.lookup("#radio1");
            RadioButton rb2 = (RadioButton) node.lookup("#radio2");
            rb1.setToggleGroup(group);
            rb2.setToggleGroup(group);

            // select file button
            selectFile = (Button) node.lookup("#selectFile");

            // an array just so can store things in event handler
            File[] fileChosen = {null};

            selectFile.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Choose World File");
                    fileChosen[0] = fileChooser.showOpenDialog(stage);
                }
            });

            // upload Critter button
            uploadCritter = (Button) node.lookup("#uploadCritter");
            uploadCritter.setOnAction(event -> {
                if(rb1.isSelected()){
                    try {
                        TextArea tx = (TextArea) node.lookup("#numCritter");
                        int numCrit = Integer.parseInt(tx.getText());
                        controller.loadCritters(fileChosen[0].getAbsolutePath(), numCrit);

                        //draw.addWorld(controller.getReadOnlyWorld(), node, zoom);
                        Text zoomPercentage = (Text) parent.lookup("#zoomPercent");
                        int zoom = Integer.parseInt(zoomPercentage.getText().substring(0,zoomPercentage.getText().length()-1));
                        draw.addWorld(controller.getReadOnlyWorld(), parent, zoom);
                        Label l = (Label) parent.lookup("#stateLabel");
                        int stepCurr = controller.getReadOnlyWorld().getSteps();
                        int aliveCrit = controller.getReadOnlyWorld().getNumberOfAliveCritters();
                        StringBuilder s = new StringBuilder("Step: ");
                        s.append(stepCurr);
                        s.append("\nAlive Critters: ");
                        s.append(aliveCrit);
                        l.setText (s.toString());
                    }catch(Exception ne){
                        System.out.println("Unsuccessful critter loading");
                    }
                }else if(rb2.isSelected()){
                    TextArea col = (TextArea) node.lookup("#column");
                    TextArea row = (TextArea) node.lookup("#row");
                    try{
                        int colInt = Integer.parseInt(col.getText());
                        int rowInt = Integer.parseInt(row.getText());
                        controller.loadCritterAtPos(fileChosen[0].getAbsolutePath(), colInt, rowInt);
                        Text zoomPercentage = (Text) parent.lookup("#zoomPercent");
                        int zoom = Integer.parseInt(zoomPercentage.getText().substring(0,zoomPercentage.getText().length()-1));
                        draw.addWorld(controller.getReadOnlyWorld(), parent, zoom);
                        Label l = (Label) parent.lookup("#stateLabel");
                        int stepCurr = controller.getReadOnlyWorld().getSteps();
                        int aliveCrit = controller.getReadOnlyWorld().getNumberOfAliveCritters();
                        StringBuilder s = new StringBuilder("Step: ");
                        s.append(stepCurr);
                        s.append("\nAlive Critters: ");
                        s.append(aliveCrit);
                        l.setText (s.toString());
                    }catch(Exception ne){
                        System.out.println("Unsuccessful critter loading");
                    }
                }else{
                    System.out.println("Need to select a choice to load critter");
                }
            });
        } catch (Exception e) {}
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}

