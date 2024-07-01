package model;

import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;
import controller.ControllerImpl;

import java.util.*;

public class World implements ReadOnlyWorld{
    private Random r = new Random();
    private int[][] map;
    private String name = "A Whole New World";
    private int step = 0;

    private List<ReadOnlyCritter> critters = new ArrayList<ReadOnlyCritter>();

    private boolean enableManna = true;

    // The size of the 2D array.
    // This is done so that we don't have to check map and map[0] is not empty every time
    // mapSize is in the form of (num rows, cols in even rows, cols in odd rows) since odd and even rows may have different number of elements in them
    private int[] mapSize = {0,0,0};

    /**
     * default constructor with random rocks and default size
     */
    public World(){
        initializeWorld(Constants.HEIGHT, Constants.WIDTH);


        // 50% chance for something to be a rock
        for(int i=0;i<map.length;i++){
            for(int j = 0;j < map[i].length;j++){
                if(r.nextBoolean()){
                    map[i][j] = Constants.ROCK_VALUE;
                }
            }
        }

    }

    /**
     * Refactored repeated code for initializing the world
     */
    private void initializeWorld(int x, int y){
        int[] size = {x,y};
        size = toCoorSize(size);
        mapSize[0] = size[0];
        mapSize[1] = size[1];
        mapSize[2] = mapSize[1]-y%2;
        if(y%2==1){
            map = new int[mapSize[0]][];
            for(int i=0;i<mapSize[0];i++){
                map[i] = new int[mapSize[i%2+1]];
            }
        }else {
            map = new int[mapSize[0]][mapSize[1]];
        }
     }

    /**
     * Construct a world with given sizes
     * @param width the width of the world
     * @param height the height of the world
     */
    public World(int width, int height){
        initializeWorld(height,width);
    }

    /**
     * Adds a rock to the map
     * @param x The x coordinate to add rock
     * @param y The y coordinate to add rock
     * @return true if successfully add, false if not
     */
    public boolean addRock(int x, int y){
        int[] pos = new int[]{y,x};
        if(isValidPosition(pos)) {
            map[pos[0]][pos[1]] =  Constants.ROCK_VALUE;
            return true;
        }
        return false;
    }

    public boolean setFood(int x, int y, int amount){
        int[] pos = new int[]{y,x};
        if(isValidPosition(pos) || getTerrainInfo(x,y) <  -1){
            if(-1-amount == -1){
                map[pos[0]][pos[1]] = 0;
            }else {
                map[pos[0]][pos[1]] = -1 - amount;
            }
            return true;
        }
        return false;
    }
    /**
     * Adds a food to the map
     * @param x The x coordinate to add food
     * @param y The y coordinate to add food
     * @param amount The amount of the food
     * @return true if successfully add, false if not
     */

    public boolean addFood(int x, int y, int amount){
        int[] pos = new int[]{y,x};
        if(isValidPosition(pos)) {
            map[pos[0]][pos[1]] = -1-amount;
            return true;
        }else if(getTerrainInfo(x,y) <  -1){
            map[pos[0]][pos[1]]+=-amount;
            return true;
        }
        return false;
    }

    /**
     * Returns the hex size of the map
     */
    public int[] toHexSize(){
        if(mapSize[1]==mapSize[2]){
            return new int[] {mapSize[0], mapSize[1]*2};
        }else {
            return new int[]{mapSize[0], mapSize[1] * 2 - 1};
        }
    }

    /**
     * Returns the 2D array size of the map given hex size of map.
     */
    private int[] toCoorSize(int[] hex){
        if(hex[1]%2==1) {
            return new int[]{hex[0], (int) Math.ceil((hex[1] + hex[0] % 2) / 2.0)};
        }else{
            return new int[]{hex[0], (int) Math.floor((hex[1] + hex[0] % 2) / 2.0)};
        }
    }
    /**
     * Converts a hex coordinate to a 2D array coordinate
     * @postcond the hex array contains the converted 2D array coordinates
     * @param coor the hex coordinate to be converted
     */
    private void coorToHex(int[] coor){
        coor[1] = coor[0]%2+coor[1]*2;
        coor[0] = mapSize[0]-coor[0]-1;
    }

    /**
     * Converts a hex coordinate to a 2D array coordinate
     * @postcond the hex array contains the converted 2D array coordinates
     * @param hex the hex coordinate to be converted
     * @return true if the converted coordinate is valid, false if not
     */
    private boolean hexToCoor(int[] hex){
        double d = (hex[1]- hex[0]%2) / 2.0;
        if((int)(d*10)%10 == 0){
            hex[1] = (int)d;
            //hex[0] = mapSize[0]*2+hex[0]%2-hex[0]-1;
        }else{
            return false;
        }
        try{
            int a = map[hex[0]][hex[1]];
            return true;
        }catch(Exception e){
            return false;
        }
    }

    /**
     * Sets the world's name.
     * @param n The name to set to
     */

    public void setName(String n){
        name = n;
    }

    /**
     * Gets the world's name
     * @return the world's name
     */
    public String getName(){
        return name;
    }

    @Override
    public int getSteps() {
        return step;
    }

    /**
     * Set the time step to n
     * @return true if successful, false if not
     */
    public void advanceStep(){
        step++;
    }

    /**
     * Drops Manna onto the word
     */
    public boolean dropManna(){
        if(enableManna){
            // 1/N possibility of dropping food
            int aliveCritter = getNumberOfAliveCritters();
            try {
                int rand = r.nextInt(aliveCritter);
                if (rand == 0) {
                    // calculates size
                    int size;
                    if (mapSize[0] % 2 == 0) {
                        size = mapSize[0] * mapSize[1];
                    } else {
                        size = ((mapSize[0] / 2) + 1) * mapSize[1] + (mapSize[0] / 2) * mapSize[2];
                    }

                    // add food at random positions
                    int numFood = Constants.MANNA_COUNT * size / 1000;
                    for (int i = 0; i < numFood; i++) {
                        int x = r.nextInt(mapSize[0]);
                        int y = r.nextInt(map[x].length);
                        if(map[x][y] == 0){
                            map[x][y] = -1 - Constants.MANNA_AMOUNT;
                        }
                    }
                }
                return true;
            }catch(IllegalArgumentException e){
                return false;
            }
        }
        return false;
    }

    /**
     * Sets enableManna
     * @param manna The boolean to set enableManna to.
     */
    public void setEnableManna(boolean manna) {
        this.enableManna = manna;
    }

    @Override
    public int getNumberOfAliveCritters() {
        return critters.size();
    }

    @Override
    public Maybe<ReadOnlyCritter> getReadOnlyCritter(int c, int r) {
            for(int i=0;i<critters.size();i++){
                if(Arrays.equals(critters.get(i).getPosition(), new int[]{c,r})){
                    return Maybe.from(critters.get(i));
                }
            }
            return Maybe.none();
    }

    @Override
    public int getTerrainInfo(int c, int r) {
        int[] pos = new int[] {r,c};
        if(hexToCoor(pos)){
            try{
                ReadOnlyCritter cr = getReadOnlyCritter(c,r).get();
                return cr.getDirection()+1;
            }catch(NoMaybeValue e){
                return map[pos[0]][pos[1]];
            }
        }
        return  Constants.ROCK_VALUE;
    }

    /**
     * Returns a list of valid positions.
     * @return a list of valid positions in the world hex coordinate
     */
    public List<int[]> getValidPosition(){
        List<int[]> out = new ArrayList<int[]> ();
        for(int i=0;i<map.length;i++){
            for(int j=0;j<map[i].length;j++){
                if(map[i][j] == 0){
                    int[] coor = new int[] {i,j};
                    coorToHex(coor);
                    coor[0] = mapSize[0]-coor[0]-1;
                    out.add(new int[] {coor[1],coor[0]});
                }
            }
        }
        return out;
    }

    /**
     * Checks if a coordinate is a valid position
     * @param hex row, col of position to look up.
     * @return true if the position is empty, false otherwise
     */
    private boolean isValidPosition(int[] hex){
        if (hexToCoor(hex)){
            return map[hex[0]][hex[1]] == 0;
        }
        return false;
    }


    public String toString(){
        // simulating a hex world (even though it's slower) so we can test coorToHex in the process too

        int[] size = toHexSize();
        StringBuilder out = new StringBuilder();

        String[][]hexMap = new String[size[0]][size[1]];

        for(int i=0;i<size[0];i++){
            for(int j=0;j<size[1];j++){
                hexMap[i][j] = " ";
            }
        }
        // initialize char array
        for(int i=0;i<map.length;i++){
            for(int j=0;j<map[i].length;j++){
                int[] coor = {i,j};
                coorToHex(coor);
                int[] critCor = Arrays.copyOf(coor, 2);
                critCor[0] = mapSize[0]-coor[0]-1;
                String c = " ";
                if(map[i][j] == 0){
                    c = "-";
                }else if(map[i][j] > 0){
                    try {
                        ReadOnlyCritter cr = getReadOnlyCritter(critCor[1], critCor[0]).get();
                        c = "" + cr.getDirection();
                    }catch(NoMaybeValue e){
                        System.out.println("Error In World toString: " + i + " " + j);
                    }

                }else if(map[i][j]== Constants.ROCK_VALUE){
                    c = "#";
                }else if(map[i][j] < -1){
                    c = "F";
                }
                hexMap[coor[0]][coor[1]] = c;
            }
        }

        for(int i=0;i<size[0];i++){
            for(int j=0;j<size[1];j++){
                out.append(hexMap[i][j]);
            }
            out.append('\n');
        }
        return out.toString();
    }


    /**
     * Adds a critter to the world
     * @param c The critter to add
     * @param x The col to add
     * @param y The row to add
     * @param newCrit whether the critter should be added to the queue or not
     * @return true if successfully added, false if space is already occupied / coordinate not exist
     */
    public boolean addCritter(ReadOnlyCritter c, int x, int y, boolean newCrit){
        int[] pos = new int[]{y,x};
        if(isValidPosition(pos)){
            map[pos[0]][pos[1]] = 1;
            if(newCrit) {
                critters.add(c);
            }
            return true;
        }else{
            return false;
        }
    }

    /**
     * Removes a critter at a location
     * @param c the critter to remove
     * @param deadCrit whether the critter should be added to the queue or not
     * @return true if successfully removed, false if coordinate is not valid
     */
    public boolean removeCritter(ReadOnlyCritter c, boolean deadCrit){
        int[] pos = new int[] {c.getPosition()[1], c.getPosition()[0]};
        if(hexToCoor(pos)) {
            map[pos[0]][pos[1]] = 0;
            if(deadCrit) {
                critters.remove(c);
            }
            return true;
        }
        return false;
    }

    /**
     * Moves a critter to a new position
     * @param c Critter to move
     * @param x new col position
     * @param y new row position
     * @precondition c is on the map.
     * @return true if successful, false if not
     */
    public boolean move(ReadOnlyCritter c, int x, int y){
        return addCritter(c,x,y,false) && removeCritter(c,false);
    }
}
