package view;

import cms.util.maybe.NoMaybeValue;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import model.ReadOnlyCritter;
import model.ReadOnlyWorld;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class DrawWorld {
    private ReadOnlyWorld world;
    private double angle = Math.PI / 3;
    private double size = 80;
    private ArrayList<ArrayList<Double>> centerHex = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> coorHex = new ArrayList<>();
    private Image foodImage;
    private double maxHeight = bestHeight(45);
    private double maxWidth = bestWidth(45);

    public DrawWorld(){
        try {
            foodImage = new Image(DrawWorld.class.getClassLoader().getResource("food.png").openStream());
        }catch(Exception e){

        }
    }

    /**
     *
     * @param i the index that the points given are in the arraylist as created when drawing
     * @param xCoor the x coordinate at which the user clicked
     * @param yCoor the y coordinate at which the user clicked
     * @param currentX the center x coordinate of the hex
     * @param currentY the center y coordinate of the hex
     * @return null if user did not click within the given hex, otherwise in a String[]:
     * species, info at each memory slot of critter 0-6, critter's rules, and last rule ("NONE" if none), respectively
     *
     */
    private String[] checkInfo(int i, double xCoor, double yCoor, double currentX, double currentY){
        int info = 0;
        if(yCoor>= currentY - size && yCoor<= currentY + size){
            double math = Math.pow(yCoor-currentY,2) + Math.pow(xCoor-currentX,2);
            math = Math.sqrt(math);
            if(math<= Math.sin(angle)*size){
                info = world.getTerrainInfo(coorHex.get(i).get(0),coorHex.get(i).get(1));
            }
        }else{
            return null;
        }
        if(info>0){
            ReadOnlyCritter c = null;
            try {
                c = world.getReadOnlyCritter(coorHex.get(i).get(0),coorHex.get(i).get(1)).get();
            } catch (NoMaybeValue e) {}

            if(c != null){
                String lastRule;
                try {
                    lastRule = c.getLastRuleString().get();
                } catch (NoMaybeValue e) {
                    lastRule = "NONE";
                }
                return new String[]{c.getSpecies(),c.getMemoryAt(0) + "",c.getMemoryAt(1)+"",
                        c.getMemoryAt(2)+"",c.getMemoryAt(3)+"",c.getMemoryAt(4)+"",c.getMemoryAt(5)+""
                        ,c.getMemoryAt(6)+"",c.getProgramString(),lastRule};
            }
        }
        return null;
    }

    /**
     * Returns the information about the critter that is at the hex that contains the given coordinates
     * @param xCoor the x coordinate of the given coordinate
     * @param yCoor the y coordinate of the given coordinate
     * @return null if there is no critter or if is not a valid location, otherwise in a String[]:
     * species, info at each memory slot of critter 0-6, critter's rules, and last rule ("" if none), respectively
     */
    public String[] getInfo(double xCoor, double yCoor){
        for(int i = 0; i<centerHex.size(); i++){
            double currentX = centerHex.get(i).get(0);
            if(xCoor>= currentX - size && xCoor<= currentX + size){
                double currentY = centerHex.get(i).get(1);
                String[] info = checkInfo(i,xCoor,yCoor,currentX,currentY);
                if(info != null){
                    return info;
                }
            }
        }

        return null;
    }

    /**
     * adds a ReadOnlyWorld to class and draws it
     * @param w the ReadOnlyWorld
     * @param node the root node for MainWindow fxml
     * @param zoom the zoom to draw world on (as double)
     */
    public void addWorld(ReadOnlyWorld w, Parent node, double zoom){
        world = w;
        //System.out.println("changed zoom to: " + zoom);
        size = 160*zoom/2;
        //System.out.println(size);
        getScrollPane(node);
    }

    /**
     * Adds canvas to ScrollPane
     */
    private void getScrollPane(Parent node){
        ScrollPane s = (ScrollPane) node.lookup("#canvasScrollPane");
        s.setContent(drawWorld(node));
    }

    /**
     * Draws the ReadOnlyWorld stored in the class.
     */
    private Canvas drawWorld(Parent node){
        Canvas c = (Canvas) node.lookup("#canvas");


        int givenWidth = world.toHexSize()[1];
        int givenHeight = world.toHexSize()[0];

        double[] info = determineBestSize(givenWidth, givenHeight);
        c.setHeight(info[1]);
        c.setWidth(info[0]);

        GraphicsContext shape = c.getGraphicsContext2D();
        shape.clearRect(0, 0, c.getWidth(), c.getHeight());

        double initialX = size;
        double initialY = 0;
//        System.out.println("reached for loop");
        for(int countWidth = 0; countWidth < givenWidth; countWidth++){
            if(countWidth%2 == 0){
                initialY = c.getHeight()- size*Math.sin(angle);
            }else{
                initialY = c.getHeight() - 2*size*Math.sin(angle);
            }
            for( int countHeight = 0;  countHeight<givenHeight; countHeight++){
//                System.out.println("reached inner for");
                if((countHeight%2 == 0 && countWidth%2 == 0) || (countHeight%2 == 1 && countWidth%2 == 1)){
//                    System.out.println("reached if");
                    double[][] coordinates = hexCoordinates(initialX,initialY);

                    coorHex.add(new ArrayList<>(Arrays.asList(countWidth,countHeight)));
                    centerHex.add(new ArrayList<>(Arrays.asList(initialX,initialY)));
                    int valueAtHex = world.getTerrainInfo(countWidth,countHeight);
                    if(valueAtHex < -1){
//                        System.out.println("tried printing food");
                        drawHexagon(coordinates,shape,Color.WHITESMOKE);
//                        System.out.println("made food");
                        shape.drawImage(foodImage, initialX-size*1.2/2, initialY-size/2, size*1.2, size);

                        int foodNumLength = ((valueAtHex*-1 +1) + "").length();
                        double fontSize = 0;
                        if(foodNumLength == 1){
                            fontSize = size/2;
                        }else{
                            fontSize = size/foodNumLength;
                        }

                        shape.setFont(new Font(shape.getFont().getName(), fontSize));
                        shape.strokeText((valueAtHex*-1 - 1) + "", initialX-size/4, initialY+size/5, size);
                    }else if(valueAtHex == -1){
//                        System.out.println("tried printing rock");
                        drawHexagon(coordinates,shape,Color.GRAY);
//                        System.out.println("made rock");
                    }else if(valueAtHex > 0){
//                        System.out.println("tried printing critter");
                        drawHexagon(coordinates,shape,Color.DARKGREEN);
//                        System.out.println("made critter");
                        try {
                            // get critter
                            ReadOnlyCritter critter = world.getReadOnlyCritter(countWidth, countHeight).get();
                            String species = critter.getSpecies();

                            // get random color
                            int speciesId = species.hashCode();
                            double PHI = (1 + Math.sqrt(5))/2;
                            double n = speciesId * PHI - Math.floor(speciesId * PHI);
                            n *= 100000;
                            Random r = new Random();
                            r.setSeed((int)n);
                            Color hue = Color.rgb(r.nextInt(257),r.nextInt(257),r.nextInt(257));
                            hue = hue.desaturate();
                            hue = hue.desaturate();

                            // draw hex at coordinates
                            drawHexagon(coordinates, shape, hue);

                            // shape.strokeText(critter.getDirection() + "", initialX, initialY);

                            // draw critter image with rotated direction
                            double sizeFactor = (double)critter.getMemoryAt(3)/(critter.getMemoryAt(3)+1);
                            ClassLoader loader = DrawWorld.class.getClassLoader();
                            URL ra = loader.getResource("bug.png");
                            Image critterImage = new Image(ra.openStream());
                            ImageView iv = new ImageView(critterImage);
                            int critterDir = critter.getDirection();
                            iv.setRotate(critterDir * 60);
                            SnapshotParameters params = new SnapshotParameters();
                            params.setFill(Color.TRANSPARENT);
                            Image rotatedImage = iv.snapshot(params, null);
                            double critterSize = size*sizeFactor;
                            if(critterDir % 3 != 0){ critterSize *= Math.sqrt(2); }
                            shape.drawImage(rotatedImage, initialX-critterSize/2, initialY-critterSize/2, critterSize, critterSize);
                        } catch (NoMaybeValue e) {
//                            System.out.println("Error drawing critter");
                        }catch(IOException ne){

                        }
                    }
                    else{
//                        System.out.println("normal coordinate");
                        drawHexagon(coordinates, shape,Color.WHITE);
//                        System.out.println("succesful  normal");
//                        System.out.println("width: " + countWidth + "height: " + countHeight);
                    }
                    initialY -= 2*size*Math.sin(angle);
                }

            }
            initialX += size + size*Math.cos(angle);
        }
//        System.out.println("successful exit");
        return c;
    }

    /**
     * Finds the vertices of hexagon given center point.
     */
    private double[][] hexCoordinates(double x, double y){
        double[] xCoor = new double[6];
        double[] yCoor = new double[6];
        for (int i = 0; i < 6; i++) {
            xCoor[i] = (x + size * Math.cos(angle * i));
            yCoor[i] = (y + size * Math.sin(angle * i));
        }
        return new double[][]{xCoor,yCoor};
    }

    /**
     * Draws the hexagon given the vertices.
     */
    private void drawHexagon(double[][] coordinates, GraphicsContext shape, Color color){
        shape.setFill(color);
        shape.setStroke(Color.BLACK);
        shape.fillPolygon(coordinates[0], coordinates[1], 6);
        shape.strokePolygon(coordinates[0], coordinates[1], 6);
        shape.save();
    }

    /**
     * Returns the best width of the canvas with the current size of hexagons given the width of the hex grid
     * @param numHex the number of hexagons in the width of the hex grid
     * @return the best width of the canvas that will fit all hexagons
     */
    private double bestWidth(int numHex){
        double totalWidth = ((double)numHex/2)*3*size + size*Math.cos(angle);
        return totalWidth;
    }

    /**
     * Returns the best height of the canvas with the current size of hexagons given the height of the hex grid
     * @param numHex the number of hexagons in the height of the hex grid
     * @return the best height of the canvas that will fit all hexagons
     */
    private double bestHeight(int numHex){
        double totalHeight;
        if(numHex%2 == 1){
            totalHeight = (numHex/2 + 1) * size * Math.sin(angle) * 2;
        }else{
            totalHeight = (numHex + 1) * size * Math.sin(angle);
        }
        return totalHeight;
    }

    /**
     * Determines the best size of each hexagon and the best width and height of the canvas based on the width and
     * height the hex grid is supposed to have
     *
     * @param numWidth the number of hexagons in the width of the hex grid
     * @param numHeight the number of hexagons in the height of the hex grid
     * @return an array containing the best width, best height, and best size, respectively
     */
    private double[] determineBestSize(int numWidth, int numHeight){
        double minHeightSize = size;
        double minWidthSize = size;
        if(bestHeight(numHeight) > maxHeight){
            minHeightSize = maxHeight;
            if(numHeight%2 == 1){
                minHeightSize= minHeightSize/((numHeight/2 + 1) * Math.sin(angle) * 2);
            }else{
                minHeightSize= minHeightSize/((numHeight + 1) * Math.sin(angle));
            }
        }
        if(bestWidth(numWidth) > maxWidth){
            minWidthSize = maxWidth/(((double)numWidth/2)*3 + Math.cos(angle));
        }
        //System.out.println(maxHeight + "   " + maxWidth);

        if(minHeightSize<=minWidthSize){
            size = minHeightSize;
        }else{
            size = minWidthSize;
        }
        //System.out.println(bestHeight(numHeight) + "   " + bestWidth(numWidth));
        return new double[]{bestWidth(numWidth),bestHeight(numHeight),size};
    }



}
