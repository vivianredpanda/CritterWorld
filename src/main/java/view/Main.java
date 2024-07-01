package view;

import controller.Controller;
import controller.ControllerImpl;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
public class Main extends Application implements Initializable {

    private Controller controller = new ControllerImpl();
    private DrawWorld draw = new DrawWorld();
    private Parent node;
    private Text zoomPercentage;
    private Runnable drawWld;
    public static void main(String[] args){
        Application.launch(args);
    }

    @Override
    public void start(final Stage stage) {
        try{
            ClassLoader loader = Main.class.getClassLoader();
            URL r = loader.getResource("fxml/MainWindow.fxml");
            if (r == null) {
                System.out.println("No FXML resource found.");
                try {
                    stop();
                } catch (final Exception e) {
                }
            }

            node = FXMLLoader.load(r);
            final Scene scene = new Scene(node);
            stage.setTitle("Main window");
            stage.setScene(scene);
            stage.sizeToScene();
            stage.show();

            controller.newWorld();
            zoomPercentage = (Text) node.lookup("#zoomPercent");
            drawWld = new Runnable() {
                @Override
                public void run() {
                    int zoom = Integer.parseInt(zoomPercentage.getText().substring(0,zoomPercentage.getText().length()-1));
                    draw.addWorld(controller.getReadOnlyWorld(), node, (double)zoom/100);
                }
            };

            drawWld.run();

            TextArea n = (TextArea) node.lookup("#stepSize");
            n.setText("1");
            Runnable stepRun = new Runnable() {
                @Override
                public void run() {
                    step(1);
                }
            };
            // load world
            Button loadWorldButton = (Button) node.lookup("#loadWorldButton");
            loadWorldButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Choose World File");
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        controller.loadWorld(file.getAbsolutePath(),true,false);
                    }
                    try {
                        Label l = (Label) node.lookup("#stateLabel");
                        int aliveCrit = controller.getReadOnlyWorld().getNumberOfAliveCritters();
                        StringBuilder s = new StringBuilder("Step: 0");
                        s.append("\nAlive Critters: ");
                        s.append(aliveCrit);
                        l.setText (s.toString());
                        drawWld.run();
                    }catch(Exception ne){

                    }
                }
            });

            // load critter
            Button loadCritterButton = (Button) node.lookup("#loadCritterButton");
            loadCritterButton.setOnAction(event -> {
                int zoom = Integer.parseInt(zoomPercentage.getText().substring(0,zoomPercentage.getText().length()-1));
                LoadCritter lc = new LoadCritter(controller, draw, node);
                lc.start(new Stage());
            });

            // Play button
            Button play = (Button) node.lookup("#playButton");
            Label l = (Label) node.lookup("#stateLabel");

            play.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        Timer tmr = new Timer();
                        TimerTask tt = new TimerTask(){
                            @Override
                            public void run() {
                                if(play.getText().equals("Stop")) {
                                    Platform.runLater(stepRun);
                                }
                            }
                        };
                        if(play.getText().equals("Play")){
                            play.setText("Stop");
                            System.out.println(n.getText());
                            tmr.schedule(tt,1,(int)(600 + (double) 2000 / Integer.parseInt(n.getText())));
                            Platform.runLater(tt);
                        }else{
                            play.setText("Play");
                            tt.cancel();
                        }
                    }
                });

                // zoom
                Button zoomIn = (Button) node.lookup("#zoomIn");
                Button zoomOut = (Button) node.lookup("#zoomOut");
                zoomIn.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        try{
                            int zoom = Integer.parseInt(zoomPercentage.getText().substring(0,zoomPercentage.getText().length()-1));
                            if(zoom < 200){
                                zoom += 25;
                                zoomPercentage.setText(new StringBuilder().append(zoom).append("%").toString());
                                drawWld.run();
                            }
                        }catch (Exception ne){

                        }
                    }
                });

            zoomOut.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        try {
                                            int zoom = Integer.parseInt(zoomPercentage.getText().substring(0, zoomPercentage.getText().length() - 1));
                                            if (zoom > 25) {
                                                zoom -= 25;
                                                zoomPercentage.setText(new StringBuilder().append(zoom).append("%").toString());
                                                //Platform.runLater(drawWld);
                                                drawWld.run();
                                            }
                                        } catch (Exception ne) {
                                        }
                                    }
                                });

            // Step button
            Button stepButton = (Button) node.lookup("#stepButton");
            stepButton.setOnAction(event -> {
                stepRun.run();
            });

            // canvas click
            Canvas c = (Canvas) node.lookup("#canvas");
            final boolean[] clicked = {false};
            c.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    MouseButton btn = event.getButton();
                    AnchorPane back = (AnchorPane) node.lookup("#anchor");
                    if(btn == MouseButton.PRIMARY){
                        back.getChildren().remove(node.lookup("#textBox"));
                        clicked[0] = false;
                    }
                    if(btn == MouseButton.SECONDARY){
                        if(!clicked[0]) {
                            String[] s = draw.getInfo(event.getX(), event.getY());
                            if(s != null) {
                                Label tf = new Label();
                                StringBuilder sb = new StringBuilder();
                                sb.append(" Species: ");
                                sb.append(s[0]).append(" \n");
                                sb.append(" Mem size: ");
                                sb.append(s[1]).append(" \n");
                                sb.append(" Defense: ");
                                sb.append(s[2]).append(" \n");
                                sb.append(" Offense: ");
                                sb.append(s[3]).append(" \n");
                                sb.append(" Size: ");
                                sb.append(s[4]).append(" \n");
                                sb.append(" Energy: ");
                                sb.append(s[5]).append(" \n");
                                sb.append(" Pass: ");
                                sb.append(s[6]).append(" \n");
                                sb.append(" Posture: ");
                                sb.append(s[7]).append(" \n");
                                sb.append(" Program: ");
                                sb.append(s[8]).append(" \n");
                                sb.append(" Last rule: ");
                                sb.append(s[9]).append(" ");
                                tf.setText(sb.toString());
                                tf.setStyle("-fx-background-color:#E3FAE3; -fx-border: 1px solid; -fx-border-color:black;");
                                tf.setLayoutX(event.getSceneX());
                                tf.setLayoutY(event.getSceneY());
                                tf.setId("textBox");
                                back.getChildren().add(tf);
                            }
                        }else{
                            back.getChildren().remove(node.lookup("#textBox"));
                        }
                        clicked[0] = !clicked[0];
                    }
                }
            });
        } catch(Exception ie){
            ie.printStackTrace();
            try {
                stop();
            } catch (final Exception e) {}
        }
    }

    /**
     * Steps ahead the world simulation by n steps
     * @param n the step to step ahead
     */
    public synchronized void step(int n){
        controller.advanceTime(n);
        try {
            int zoom = Integer.parseInt(zoomPercentage.getText().substring(0, zoomPercentage.getText().length() - 1));
            draw.addWorld(controller.getReadOnlyWorld(), node, (double)zoom/100);
        }catch(Exception ne){};

        Label l = (Label) node.lookup("#stateLabel");
        try{
            int stepCurr = controller.getReadOnlyWorld().getSteps();
            int aliveCrit = controller.getReadOnlyWorld().getNumberOfAliveCritters();
            StringBuilder s = new StringBuilder("Step: ");
            s.append(stepCurr);
            s.append("\nAlive Critters: ");
            s.append(aliveCrit);
            l.setText (s.toString());
        }catch(Exception e){
        }
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
    }
}
