package model;

import ast.*;
import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;

import java.util.*;

import static cms.util.maybe.Maybe.none;
import static model.Constants.MAX_SMELL_DISTANCE;
import static model.Constants.MIN_MEMORY;

public class Critter implements ReadOnlyCritter{
    //arrSize, defense, offense, size, energy, pass posture
    private int[] memory;
    private String species; // maybe we should make this an enum? but idk and maybe also final?
    private Program root;
    private int direction;
    private Action.Actions lastAction; //maybe enum/different type
    private final int[] position = new int[2]; //prob has invariant?

    private ReadOnlyWorld world;

    /**
     * Creates a Critter of a specific species with a valid mem[] and a Critter grammar
     * @param species given species of Critter
     * @param mem given mem[] representing memSize, defense, offense, size, energy, pass, and posture
     * @param root given root of the AST of this Critter's grammar
     */
    public Critter(String species, int[] mem, Program root){
        this.species = species;
        memory = mem;
        this.root = root;
        Random r = new Random();
        direction = r.nextInt(6);
    }

    /**
     * Creates a valid memory array for Critters based on the provided values that may not be valid
     * @param arrSize the size of the memory array
     * @param defense the defense value of the Critter
     * @param offense the offense value of the Critter
     * @param size the initial size of the Critter
     * @param energy the initial energy amount of the Critter
     * @param posture the initial posture of the Critter
     * @return the valid memory array created
     */
    public static int[] CREATE_MEM(int arrSize, int defense, int offense, int size, int energy, int posture){
        int[] temp = new int[]{defense, offense, size, energy,1, posture};
        String[] tempName = new String[] {"defense", "offense", "size","energy","pass", "posture"};
        int[] mem;
        if(arrSize< MIN_MEMORY){
            System.out.println("Invalid memory size");
            mem = new int[MIN_MEMORY];
        }else{
            mem = new int[arrSize];
        }
        mem[0] = mem.length;

        for(int i =0; i<temp.length; i++){
            if(temp[i]<1 && i!=5){
                System.out.println( tempName[i] + " must be at least 1");
                mem[i+1] = 1;
            }else if(i==5 && (temp[i]<0 || temp[i]>99)){
                System.out.println(tempName[i] + " must be between 0 and 99");
                mem[i+1] = 0;
            }else if(i==2 && temp[2] > 1){
                System.out.println(tempName[i] + " must be equal to 1");
                mem[i+1] = 1;
            }else if(i==3 && temp[3]>Constants.ENERGY_PER_SIZE){
                System.out.println(tempName[i] + "");
                mem[i+1] = 500;
            }else{
                mem[i+1] = temp[i];
            }
        }
        return mem;
    }

    @Override
    public String getSpecies() {
        return species;
    }

    @Override
    public int[] getMemory() {
        return Arrays.copyOf(memory,memory.length);
    }

    @Override
    public int getMemoryAt(int idx) {
        try {
            return memory[idx];
        } catch (IndexOutOfBoundsException ignored) {}
        return 0;
    }

    @Override
    public String getProgramString() {
        return root.toString();
    }

    @Override
    public Maybe<String> getLastRuleString() {
        if(lastAction == null){
            return none();
        }
        return Maybe.some(lastAction.toString());
    }

    @Override
    public Program getProgramImpl() {
        //should we return copy instead of actual
        return root;
    }

    public int getAppearance() {
        return memory[3]*1000 + memory[6]*10 + direction;
    }

    public void setWorld(ReadOnlyWorld world){ this.world = world; }

    public int getDirection(){
        return direction;
    }

    /**
     * Sets the value at memory[idx] to provided value
     * Has no effect if index is out of bounds or change is invalid: setting mem[6] < 0, mem[6] > 99, modifying mem[0-5]
     *
     * @param idx the index of memory to modify
     * @param val the new value for memory[idx]
     */
    public void setMemoryAt(int idx, int val){
        boolean invalid = idx < 6 || idx >= memory.length || idx == 6 && (val < 0 || val > 99);
        if(!invalid){
            memory[idx] = val;
        } else{
            System.out.println("Unable to update memory at index " + idx + " to value " + val);
        }
    }

    /**
     * Sets the direction of a critter.
     * @param dir the direction to set to
     * @return whether the set is successful
     */
    public boolean setDirection(int dir){
        if(dir>=0 && dir < 6){
            direction = dir;
            return true;
        }
        return false;
    }

    /**
     * Resets the value of PASS to represent new cycle
     */
    public void resetPass(){ memory[5] = 1; }

    /**
     * Increments the value of PASS
     */
    public void incrementPass(){ memory[5] += 1; }

    /**
     * Returns the contents of the hex in direction dir relative to the direction of the critter
     * 0 denotes an empty hex, n > 0 denotes critter with appearance n, n < -1 denotes food with energy (-n)-1, -1 denotes rock
     *
     * @param dir the direction of hex to check contents of relative to the critter's direction
     * @return contents of hex in direction dir
     */
    public int getNearby(int dir){
        dir = (direction + dir % 6 + 6) % 6;

        // get the coordinates to check
        int[] checkPos = getCoordinatesInDir(position, dir, 1);
        return getTerrainInfo(checkPos);
    }

    // gets terrain info at checkPos, or returns appearance of critter at checkPos
    private int getTerrainInfo(int[] checkPos) {
        int x = checkPos[0];
        int y = checkPos[1];

        int terrain = world.getTerrainInfo(x, y);
        if(terrain <= 0){ return terrain; }
        try {
            ReadOnlyCritter c = world.getReadOnlyCritter(x, y).get();
            return c.getAppearance();
        } catch (NoMaybeValue e) {
            return -1;
        }
    }

    // returns the world's terrain info at direction dir relative to the critter (regardless of the direction the critter is facing) at distance n
    private int[] getCoordinatesInDir(int[] start, int dir, int n){
        int x = start[0];
        int y = start[1];

        switch(dir){
            case 0: {
                y += n*2;
                break;
            }
            case 1: {
                x += n;
                y += n;
                break;
            }
            case 2: {
                x += n;
                y -= n;
                break;
            }
            case 3: {
                y -= 2*n;
                break;
            }
            case 4: {
                x -= n;
                y -= n;
                break;
            }
            default: {
                x -= n;
                y += n;
                break;
            }
        }

        return new int[]{x, y};
    }


    /**
     * Returns the contents of the hex at distance dist ahead of the critter
     *
     * @param dist the distance ahead to check contents of
     * @return the contents of hex at distance dist ahead of the critter
     */
    public int getAhead(int dist){
        if(dist < 0){ dist = 0; }

        // get the coordinates to check
        int[] checkPos = getCoordinatesInDir(position, direction, dist);
        //System.out.println("In ahead: " + getTerrainInfo(checkPos));
        return getTerrainInfo(checkPos);
    }

    /**
     * Returns direction and distance of closest food to the critter up to MAX_SMELL_DISTANCE hexes away
     * Uses expression of the form 1000 * distance + direction
     * Result is 1000000 if no food within MAX_SMELL_DISTANCE
     *
     * @return direction and distance of closest food to the critter
     */
    public int getSmell(){
        int temp = getSmellDijkstra();
       // System.out.println("Smell: " + temp);
        return temp;
        /*
        // bfs to get coordinates of closest food
        int[] closestFood = getSmellBFS();

        // check if food found within MAX_SMELL_DISTANCE
        if(closestFood == null){ return 1000000; }

        // get Manhattan distance
        int x1 = position[0];
        int y1 = position[1];
        int x2 = closestFood[0];
        int y2 = closestFood[1];
        int dist = Math.max(Math.abs(x1-x2), Math.max((Math.abs(x2-x1+y2-y1))/2,(Math.abs(x2-x1-y2+y1))/2));

        // get closest direction
        double angle = Math.atan2(x2-x1, y2-y1);
        angle = ((Math.toDegrees(angle) % 360) + 360) % 360; // angle in degrees in range [0, 360)
        int dir = (int)((angle+30)/60) % 6;

        if(dir < 0 || dir > 5){
            System.out.println("ERROR: invalid direction in getSmell().");
            return 1000000;
        }

        // convert direction to be relative to the critter's direction
        int relDir = (dir - direction + 6) % 6;

        return 1000 * dist + relDir;
         */
    }

    // returns position of the food closest to the critter
    private int[] getSmellBFS(){
        List<int[]> hexToCheck  = new ArrayList<>(); // store arrays containing x, y, dist
        hexToCheck.add(new int[]{position[0], position[1], 0}); // add starting node

        int curIdx = 0;
        while(curIdx < hexToCheck.size()){
            // check current node
            int[] cur = hexToCheck.get(curIdx);
            if(world == null){
                System.out.println("ERROR: World is null in getSmellBFS");
            }
            if(world.getTerrainInfo(cur[0], cur[1]) < -1){
                return cur;
            }

            // check current distance
            if(cur[2] < MAX_SMELL_DISTANCE){
                // add unvisited neighbors
                for(int i = 0; i < 6; i++){
                    int[] temp = getCoordinatesInDir(new int[]{cur[0], cur[1]},i,1);
                    if(hexToCheck.stream().noneMatch(a -> a[0]==temp[0] && a[1]==temp[1])){
                        hexToCheck.add(new int[]{temp[0],temp[1],cur[2]+1});
                    }
                }
            }

            curIdx++;
        }

        // no food found
        return null;
    }

    /**
     * Returns direction and distance of closest food to the critter up to MAX_SMELL_DISTANCE steps away
     * Direction is the direction of the first hex the critter moves to, or direction of food relative to it (if food in neighboring hex)
     * Uses expression of the form distance * 100 + direction
     * Result is 1000000 if no food within MAX_SMELL_DISTANCE
     *
     * @return direction and distance of closest food to the critter
     */
    private int getSmellDijkstra(){
        // priority queue containing arrays of length 5 of the form [col, row, critter direction at this location, current distance, direction of first step]
        PriorityQueue<int[]> p = new PriorityQueue<>(10, Comparator.comparingInt(a -> a[3]));
        p.add(new int[]{position[0], position[1], direction, 0, -1});
        List<int[]> visited = new ArrayList<>();

        while(!p.isEmpty()){
            int[] cur = p.poll();
            visited.add(cur);

            if(world.getTerrainInfo(cur[0], cur[1]) < -1){
                // return distance * 100 + direction, subtract one since only need to be in position to eat
                return (cur[3]-1)*100+cur[4];
            }

            // check current distance
            if(cur[3] < MAX_SMELL_DISTANCE){
                for(int i = 0; i < 6; i++){
                    int[] temp = getCoordinatesInDir(new int[]{cur[0], cur[1]},i,1);

                    // skip if rock or another critter
                    if(world.getTerrainInfo(temp[0],temp[1]) == -1 || world.getTerrainInfo(temp[0],temp[1]) > 0) { continue; }

                    // get direction relative to critter's direction
                    int relDir = (i - cur[2] + 6) % 6;
                    int numTurns = Math.min(relDir,6-relDir);

                    // track if coordinate currently in priority queue
                    boolean alreadyVisited = false;

                    // distance traversed to arrive at this coordinate
                    int newDist = cur[3] + numTurns + 1;

                    // check if in priority queue
                    for(int[] old : p) {
                        if(old[0]==temp[0] && old[1] == temp[1]){
                            // update values if newDist is less than current distance
                            if(newDist < old[3]){
                                old[3] = newDist;
                                old[2] = i;
                                old[4] = cur[4];
                            }

                            alreadyVisited = true;
                        }
                    }

                    // not yet visited
                    if(!alreadyVisited && visited.stream().noneMatch(a -> a[0]==temp[0] && a[1]==temp[1])){
                        if(newDist <= MAX_SMELL_DISTANCE){
                            // set direction of first step to relative direction to first hex if neighboring first hex
                            int firstStep = (cur[4] == -1)? relDir: cur[4];

                            p.add(new int[]{temp[0],temp[1],i,newDist,firstStep});
                        }
                    }
                }
            }
        }

        // no food within MAX_SMELL_DISTANCE
        return 1000000;
    }

    /**
     * Returns a random integer in the range [0,n)
     *
     * @param n upper bound (exclusive) for the random number
     * @return random integer in the range [0,n)
     */
    public int getRandom(int n){
        Random r = new Random();
        return r.nextInt(n);
    }

    public int[] getPosition(){
        return Arrays.copyOf(position,position.length);
    }

    /**
     * Sets the position of the Critter based on the parameters
     * @param xPos the x-coordinate of the Critter's position
     * @param yPos the y-coordinate of the Critter's position
     */
    public void setPosition(int xPos, int yPos){
        position[0] = xPos;
        position[1] = yPos;
    }

    /**
     * Checks if the Critter has died
     * @return true if Critter's energy is 0, false if greater than 0
     */
    public boolean isDead(){
        return memory[4] <= 0;
    }

    /**
     * The Critter waits and absorbs energy equivalent to its size*SolarFlux
     */
    public void waitAction(){
        memory[4] += memory[3] * Constants.SOLAR_FLUX;
        checkEnergyLimit();
        lastAction = Action.Actions.WAIT;
    }

    private void checkEnergyLimit(){
        if(memory[4]>memory[3]*Constants.ENERGY_PER_SIZE){
            memory[4] = memory[3]*Constants.ENERGY_PER_SIZE;
        }else if(memory[4]<0){
            memory[4] = 0;
        }
    }

    /**
     * The Critter either turns right and left by expending its size worth of energy
     * @param dir +1 for turn right, -1 for turn left
     */
    public void turnAction(int dir){
        memory[4] -= memory[3];
        checkEnergyLimit();
        if(dir<0){
            lastAction = Action.Actions.LEFT;
        }else{
            lastAction = Action.Actions.RIGHT;
        }
        direction = (direction + dir + 6) % 6;
    }

    /**
     * Returns the coordinates of the cell in front (forward) or behind (backward) of the Critter
     *
     * @param dir +1 for forward, -1 for backward
     * @return the int[][] containing the row and column representing the coordinates of the cell found
     */
    public int[] checkMove(int dir){
        int[] newPosition;
        int toMove = direction;
        if(dir<0){
            toMove = (direction+3) %6;
        }

        if(toMove%3 == 0){
            int yValue = -2;
            if(toMove == 0){
                yValue = 2;
            }
            newPosition = new int[]{position[0] + 0,position[1] + yValue};
        }else if(toMove%2 == 1){
            int xValue = -1;
            if(toMove == 1){
                xValue = 1;
            }
            newPosition = new int[]{position[0] + xValue,position[1] + 1};
        }else{
            newPosition = new int[]{position[0] + toMove%4 - 1,position[1]-1};
        }
        return newPosition;
    }

    /**
     * Plays out a theoretical move of the Critter forward or backward assuming the board is
     * completely empty and infinite
     * To actually move the Critter, must call setPosition()
     *
     * @param dir +1 for forward, -1 for backward
     * @return the int[][] containing the row and column where the Critter would move to
     */
    public int[] moveAction(int dir){
        if(dir<0){
            lastAction = Action.Actions.BACKWARD;
        }else{
            lastAction=Action.Actions.FORWARD;
        }
        memory[4] -= memory[3]*Constants.MOVE_COST;
        checkEnergyLimit();
        return checkMove(dir);
    }

    /**
     * The Critter eats food located in the cell in front of it up to its maximum energy capacity and returns the
     * value of the remaining food uneaten
     *
     * @param food the energy value of the food in the cell to be absorbed (must be greater than or equal to 0)
     * @return the remaining energy value of food unable to be absorbed
     */
    public int eat(int food){
        lastAction = Action.Actions.EAT;
        memory[4] -= memory[3];
        if(memory[4]<0){
            checkEnergyLimit();
            return food;
        }
        if(memory[4] + food <= memory[3]*Constants.ENERGY_PER_SIZE){
            memory[4] += food;
            return 0;
        }else{
            int remaining = food + memory[4] - memory[3]*Constants.ENERGY_PER_SIZE;
            memory[4] = memory[3]*Constants.ENERGY_PER_SIZE;
            return remaining;
        }
    }


    /**
     * Critter spends some of its energy to convert into food to be served in the hex in front of it
     * @param energy
     */
    public int[] serve(int energy){
        //first have to checkMove somehow
        int[] info = new int[3];
        int[] position = checkMove(1);
        info[0] = position[0];
        info[1] = position[1];

        if(energy > memory[4] - memory[3]){
            if(memory[4]>=memory[3]){
                info[2] = memory[4] - memory[3];
            }else{
                info[2] = 0;
            }
            memory[4] = 0;
        }else if(energy>=0){
            info[2] = energy;
            memory[4] = memory[4] - energy - memory[3];
        }else{
            info[2] = 0;
        }
        checkEnergyLimit();
        lastAction = Action.Actions.SERVE;
        return info;
    }

    /**
     * Critter expends energy equivalent to size times ATTACK_COST in order to attack
     *
     * @return array containing information about this Critter's size at index 0 and offense ability at index 1
     */
    public int[] attack(){
        memory[4] -= memory[3]*Constants.ATTACK_COST;
        checkEnergyLimit();
        int[] info = {memory[3],memory[2]};
        lastAction = Action.Actions.ATTACK;
        return info;
    }

    /**
     * Calculates and removes energy from Critter that has been attacked
     *
     * @param info int[] containing information about attacking Critter's size at index 0 and offense ability at index 1
     */
    public void beingAttacked(int[] info){
        double x = Constants.DAMAGE_INC * ((info[0]*info[1]) - (memory[3]*memory[1]));
        double p = 1/(1+Math.pow(Math.E,-1*x));
        int energyRemoved = (int) Math.round(Constants.BASE_DAMAGE * info[0] * p);
        memory[4] -= energyRemoved;
        checkEnergyLimit();
    }

    /**
     * The Critter grows by one unit by using energy based on its size, number of rules,
     * offense ability, and defense ability
     */
    public void grow(){
        memory[4] -= memory[3]*getComplexity()*Constants.GROW_COST;
        checkEnergyLimit();
        if(memory[4] > 0){ memory[3]++; }
        lastAction = Action.Actions.GROW;
    }

    /**
     * The Critter buds a new Critter which contains the same rules modulo mutation, the same memory except for energy,
     * size, posture, and all additional values after index 6 in the memory
     *
     * @return a new Critter budded by this Critter
     */
    public Maybe<Critter> bud(){
        lastAction = Action.Actions.BUD;
        memory[4] -= getComplexity()*Constants.BUD_COST;

        Maybe<Critter> newCritter;
        if(memory[4] < 0){
            newCritter = Maybe.none();
        } else {
            Program rootCopy = mutation(1,4);
            int[] memCopy = getMemory();
            memCopy[4] = Constants.INITIAL_ENERGY;
            memCopy[3] = 1;
            for(int i = 6; i<memCopy.length; i++){
                memCopy[i] = 0;
            }

            Critter budCritter = new Critter(species, memCopy, rootCopy);
            budCritter.setWorld(world);
            newCritter = Maybe.from(budCritter);
        }
        checkEnergyLimit();

        return newCritter;
    }


    public Critter clone(){
        return new Critter(species, getMemory(), (Program) root.clone());
    }

    /**
     * Calculates the complexity of the critter based on its rules
     * @return int represents the complexity of the critter
     */
    public int getComplexity(){
        int numRules = root.getChildren().size();
        return (numRules*Constants.RULE_COST) + ((memory[2] + memory[1])*Constants.ABILITY_COST);
    }

    /**
     * This Critter mates with Critter m and produces a new Critter with memory and rule set produced
     * randomly from both Critters by using up energy proportional to complexity if mating is successful,
     * else uses up energy equivalent to turn
     *
     * @param m the Critter for this to mate with
     * @param successful if mating is successful
     * @return null if unsuccssful mating or the new Critter produced if successful
     */
    public Maybe<Critter> mate(Maybe<Critter> m, boolean successful){
        lastAction = Action.Actions.MATE;
        if(!successful){
            try {
                m.get().lastAction = Action.Actions.MATE;
                m.get().memory[4] = m.get().memory[4] - m.get().memory[3];
            } catch (NoMaybeValue e) {
            }finally{
                memory[4] = memory[4] - memory[3];
                return none();
            }
        }
        try {
            Critter other = m.get();
            other.lastAction = Action.Actions.MATE;
            other.memory[4] = other.memory[4] - Constants.MATE_COST * other.getComplexity();
            memory[4] = memory[4] - Constants.MATE_COST * getComplexity();
            if(memory[4]<0 || other.memory[4]<0){
                memory[4] += Constants.MATE_COST*getComplexity() - memory[3];
                other.memory[4] += Constants.MATE_COST*other.getComplexity() - other.memory[3];
                return none();
            }
            int[] memArr = getMemory();
            for (int i = 0; i < 3; i++) {
                int r = (int) Math.round(Math.random());
                if (r == 0) {
                    memArr[i] = other.getMemoryAt(i);
                } else {
                    memArr[i] = memory[i];
                }
            }

            memArr[4] = Constants.INITIAL_ENERGY;
            memArr[3] = 1;
            for (int i = 6; i < memArr.length; i++) {
                memArr[i] = 0;
            }

            Program thisMutated = mutation(1,4);
            Program otherMutated = other.mutation(1,4);

            Program sizeProgram = (Program) getRandom(thisMutated,otherMutated);
            int size = sizeProgram.getChildren().size();
            String childSpecies = ((Critter) getRandom(this,other)).getSpecies();

            List<Node> rules = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                Program toUse = (Program) getRandom(thisMutated,otherMutated);
                if(toUse.getChildren().size() <= i){
                    rules.add(sizeProgram.getChildren().get(i));
                }else{
                    rules.add(toUse.getChildren().get(i));
                }
            }
            return Maybe.from(new Critter(childSpecies, memArr, new ProgramImpl(rules)));
        }catch (NoMaybeValue e){
            return none();
        }
    }

    /**
     * Returns a random object between one and two
     *
     * @param one one object option to choose from
     * @param two a different object option to choose from
     * @return the random object chosen
     */
    private Object getRandom(Object one, Object two){
        int r = (int) Math.round(Math.random());
        if (r == 0) {
            return two;
        } else {
            return one;
        }
    }

    private Program mutation(int numChance, int denomChance){
        Random r = new Random();
        int num = r.nextInt(denomChance);
        if(num<numChance){
            Program original = root;
            root = root.mutate();
            if(root == null){
                root = original;
            }
            root = mutation(numChance,denomChance);
        }
        return root;
    }

    public void forcedMutation(){
        root = mutation(1,1);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("--- CRITTER ---");
        sb.append("\nSpecies: ").append(species);
        sb.append("\nDirection: ").append(direction);
        sb.append("\nLast Action: ").append(lastAction);
        sb.append("\nMemory: ").append(Arrays.toString(memory));
        sb.append("\nProgram: \n").append(root.toString());
        return sb.toString();
    }
}
