package controller;

import ast.Action;
import ast.Program;
import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;
import exceptions.SyntaxError;
import model.Constants;
import model.Critter;
import model.ReadOnlyWorld;
import model.World;
import parse.Parser;
import parse.ParserFactory;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static model.Constants.ENERGY_PER_SIZE;
import static model.Constants.MIN_MEMORY;

public class ControllerImpl implements Controller{

    private World world;
    private List<Critter> critters = new ArrayList<Critter>();
    private boolean enableMutation = false;
    private Parser parser = ParserFactory.getParser();
    private Interpreter interpreter = new InterpreterImpl();

    @Override
    public ReadOnlyWorld getReadOnlyWorld() {
        return world;
    }

    @Override
    public void newWorld() {
        world = new World();
    }

    /**
     * Returns Maybe of an array of the capture groups given by the regex, or Maybe.none() if str does not match the regex
     */
    private Maybe<String[]> regexMatch(String str, String regex){
        Pattern r = Pattern.compile(regex);
        Matcher m = r.matcher(str);

        if (m.find()){
            String[] out = new String[m.groupCount()];
            for(int i=1;i<= m.groupCount();i++){
                out[i-1] = m.group(i);
            }
            return Maybe.from(out);
        }else{
            return Maybe.none();
        }
    }

    /**
     * Convert string array[start..end(exclusive)] to int array. Throws exception when string array is not actually an int
     */
    private int[] strToint (String[] arr, int start, int end) throws NumberFormatException{
        int[] intArgs = new int[end-start];
        for(int i=start;i<end;i++){
            intArgs[i-start] = Integer.parseInt(arr[i]);
        }
        return intArgs;
    }
    @Override
    public boolean loadWorld(String filename, boolean enableManna, boolean enableForcedMutation) {
        this.enableMutation = enableForcedMutation;
        // make a default world so simulation continues even if inputs are wrong
        newWorld();
        boolean out = true;
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            int numValidLines = 0;
            String data = br.readLine();
            while(data != null){
                // checks comments & blank lines
                if(data.matches("^\\s*$|^\\/\\/.*$")){
                    data = br.readLine();
                    continue;
                }else{
                    // checks world
                    if(numValidLines == 0){
                        try {
                            String[] args = regexMatch(data, "^name (.*)$").get();
                            world.setName(args[0]);
                            System.out.println("Name: " + world.getName());
                            numValidLines++;
                        }catch(NoMaybeValue e){
                            System.out.println("Wrong format in name definition. The first line should be \"name <world name>\"");
                            out = false;
                            break;
                        }
                    }
                    // checks size
                    else if (numValidLines == 1){
                        try {
                            String[] args = regexMatch(data, "^size ([0-9]+) ([0-9]+).*$").get();
                            int[] intArgs = strToint(args,0,2);
                            world = new World(intArgs[0],intArgs[1]);

                            numValidLines++;
                        }catch(NoMaybeValue e){
                            System.out.println("Wrong format in size definition. The second line should be \"size <width> <height>\"");
                            out = false;
                            break;
                        }catch(NumberFormatException e){
                            System.out.println("Wrong format in size definition. The second line should be \"size <width> <height>\"");
                            out = false;
                            break;
                        }
                    }else{
                        try {
                            // handles out of order
                            Maybe<String[]> args = regexMatch(data, "^rock ([0-9]+) ([0-9]+).*$");
                            if(!args.isPresent()){
                                args = regexMatch(data, "^food ([0-9]+) ([0-9]+) ([0-9]+).*$");
                            }
                            if(!args.isPresent()){
                                args = regexMatch(data, "^critter (.*) ([0-9]+) ([0-9]+) ([0-9]+).*$");
                            }
                            String[] realArgs = args.get();

                            // rocks
                            if(realArgs.length == 2){
                                int[] intArgs = strToint(realArgs,0,2);
                                if(!world.addRock(intArgs[0], intArgs[1])){
                                    System.out.println("Invalid input for rock definition. Check to make sure all the arguments you provided are within bound / you are not placing things at positions that are already occupied.");
                                    out = false;
                                }
                            }

                            // food
                            else if(realArgs.length==3){
                                int[] intArgs = strToint(realArgs,0,3);
                                if(!world.addFood(intArgs[0], intArgs[1], intArgs[2])){
                                    System.out.println("Invalid input for food definition. Check to make sure all the arguments you provided are within bound / you are not placing things at positions that are already occupied.");
                                    out = false;
                                }
                            }

                            // load critters
                            else if(realArgs.length==4) {
                                File f = new File(filename);
                                File parent = new File(f.getParent());
                                Critter c = loadCritter(parent.getAbsolutePath() + "/" + realArgs[0]);

                                if (c == null) {
                                    System.out.println("Invalid input for critter definition. Check to make sure all the information in critter file is correct.");
                                    out = false;
                                } else {
                                    int[] intArgs = strToint(realArgs, 1, 4);
                                    if (c.setDirection(intArgs[2]) && world.addCritter(c, intArgs[0], intArgs[1], true)) {
                                        c.setPosition(intArgs[0], intArgs[1]);
                                        c.setWorld(world);
                                        critters.add(c);
                                    } else {
                                        System.out.println("Invalid input for critter definition. Check to make sure all the arguments you provided are within bound / you are not placing things at positions that are already occupied.");
                                        out = false;
                                    }
                                }
                            }
                        }catch(NoMaybeValue e){
                            System.out.println("Wrong format in world property definition. Should be one of \"rock <column> <row>\", \"food <column> <row> <amount>\", \"critter <critter file> <column> <row> <direction>\"");
                            out = false;
                        }catch(NumberFormatException e){
                            System.out.println("Wrong format in world property definition. Should be one of \"rock <column> <row>\", \"food <column> <row> <amount>\", \"critter <critter file> <column> <row> <direction>\"");
                            out = false;
                        }
                    }
                }
                data = br.readLine();
            }
        }catch(FileNotFoundException e){
            System.out.println("The file you entered is not valid!");
            out = false;
        } catch (IOException e) {
            System.out.println("IO exception encountered while loading world.");
            out = false;
        }

        world.setEnableManna(enableManna);
        return out;
    }

    @Override
    public boolean loadCritters(String filename, int n) {
        if(world == null){
            System.out.println("Needs world to load critters");
            return false;
        }
        Critter c = loadCritter(filename);
        System.out.println(c);
        if(c == null){
            System.out.println("Error loading critter from file: " + filename);
            return false;
        }
        List<int[]> validPositions = world.getValidPosition();
        for(int i = 0; i < n; i++){
            // check if there are any valid positions in the world
            if(validPositions.size() < 1){
                System.out.println("No valid positions remaining for loading Critter " + i);
                return false;
            }

            // get random position for the critter
            Random r = new Random();
            int idx = r.nextInt(validPositions.size());
            int[] pos = validPositions.get(idx);
            // create new critter
            Critter cur = c.clone();
            System.out.println("Setting position to: " + pos[0] + " " + pos[1]);

            cur.setPosition(pos[0], pos[1]);
            cur.setWorld(world);

           if (world.addCritter(cur, pos[0], pos[1], true)){
               critters.add(cur);
           }

            validPositions.remove(idx);
        }

        return true;
    }

    /**
     * Loads a critter from the given file
     *
     * @param filename the file to load the critter from
     * @return the critter created according to the file specifications, or null if Critter did not contain a valid program
     */
    public Critter loadCritter(String filename){
        // initialize default memory values
        String species = "default";
        int[] memory = new int[MIN_MEMORY];
        memory[0] = MIN_MEMORY;
        memory[1] = 1;
        memory[2] = 1;
        memory[3] = 1;
        memory[4] = 500;
        Program root = null;

        // create map that associates String with its index in memory
        Map<String, Integer> memIdx = new HashMap<>();
        memIdx.put("DEFENSE:", 1);
        memIdx.put("OFFENSE:", 2);
        memIdx.put("SIZE:", 3);
        memIdx.put("ENERGY:", 4);
        memIdx.put("POSTURE:", 6);

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            br.mark(1000);
            String data = br.readLine();

            while(data != null){
                if(!data.matches("^\\s*$|^\\/\\/.*$")) {
                    // start parsing Program
                    if(data.contains("-->")){
                        br.reset();
                        break;
                    }
                    // see if the first token matches a keyword: if it does, check input valid and update
                    String[] words = data.split(" ");
                    if(words.length >= 2) { // must have at least two arguments
                        words[0] = words[0].toUpperCase();
                        if(words[0].equals("SPECIES:")){ // assign species
                            StringBuilder sb = new StringBuilder();
                            for(int i = 1; i < words.length; i++){
                                sb.append(words[i]);
                                if(i != words.length-1){ sb.append(" "); }
                            }
                            species = sb.toString();
                        } else if(words[0].equals("MEMSIZE:")){ // assign memsize
                            try {
                                int num = Integer.parseInt(words[1]);
                                if(num < MIN_MEMORY){
                                    System.out.println("Memory size must be greater than or equal to " + MIN_MEMORY + ".");
                                } else{
                                    // copy current values into new memory array
                                    int[] temp = new int[num];
                                    System.arraycopy(memory, 0, temp, 0, 7);
                                    temp[0] = num;
                                    memory = temp;
                                }
                            } catch(NumberFormatException e) {
                                System.out.println("Memory size must be an integer.");
                            }
                        } else if(memIdx.containsKey(words[0])){ // assign other mem
                            try {
                                int num = Integer.parseInt(words[1]);
                                if(num < 0){
                                    System.out.println("Error loading parameter for " + words[0] + " must be greater than or equal to 0.");
                                } else if(num > 99 && words[0].equals("POSTURE:")) {
                                    System.out.println("Error loading parameter for POSTURE: must be less than or equal to 99.");
                                } else{
                                    memory[memIdx.get(words[0])] = num;
                                }
                            } catch(NumberFormatException e) {
                                System.out.println("Memory size must be an integer.");
                            }
                        } else{
                            System.out.println("Line skipped while loading critter: " + data);
                        }
                    } else{
                        System.out.println("Each line in critter file must have at least 2 arguments.");
                    }
                }
                br.mark(1000);
                data = br.readLine();
            }
            root = parser.parse(br);
        } catch (FileNotFoundException e) {
            System.out.println("The file you entered is not valid!");
        } catch (IOException e) {
            System.out.println("IO exception encountered while loading world.");
        } catch (SyntaxError e) {
            System.out.println("Syntax error encountered: " + e);
        }

        int maxEnergy = memory[3] * ENERGY_PER_SIZE;
        if(memory[4] > maxEnergy){
            System.out.println("Energy cannot be greater than size * " + ENERGY_PER_SIZE + ", setting to " + maxEnergy);
            memory[4] = maxEnergy;
        }

        // return new Critter if valid program found
        return root == null? null: new Critter(species, memory, root);
    }

    public boolean loadCritterAtPos(String filename, int r, int c){
        Critter crit = loadCritter(filename);
        if(world.addCritter(crit,r,c, true)) {
            crit.setPosition(r, c);
            return true;
        }
        return false;
    }

    @Override
    public boolean advanceTime(int n) {
        if(world == null || n < 0){ return false; }

        for(int i=0;i<n;i++) {
            world.advanceStep();
            HashMap<Critter, Critter> mateCritter = new HashMap<Critter, Critter>();

            int lastCritterIdx = critters.size();
            for(int j=0;j<lastCritterIdx;j++) {
                Critter crit = critters.get(j);

                // gets action
                CritterAction ca = interpreter.getNextAction(crit);
                Maybe<Integer> num = ca.getNum();
                Action.Actions act = ca.getAction();

                switch(act){
                    case WAIT:
                        crit.waitAction();
                        break;
                    case FORWARD:
                        int[] pos = crit.moveAction(1);
                        if(world.move(crit, pos[0], pos[1])){
                            crit.setPosition(pos[0],pos[1]);
                        }
                        break;
                    case BACKWARD:
                        pos = crit.moveAction(-1);
                        if(world.move(crit, pos[0], pos[1])){
                            crit.setPosition(pos[0], pos[1]);
                        }
                        break;
                    case LEFT:
                        crit.turnAction(-1);
                        break;
                    case RIGHT:
                        crit.turnAction(1);
                        break;
                    case EAT:
                        pos = crit.checkMove(1);
                        int food = world.getTerrainInfo(pos[0], pos[1]);
                        if(food < -1){
                            int f = (food+1)*-1;
                            int leftover = crit.eat(f);
                            world.setFood(pos[0], pos[1], leftover);
                        }else{
                            crit.eat(0);
                        }
                        break;
                    case ATTACK:
                        pos = crit.checkMove(1);
                        int[] info = crit.attack();
                        if(world.getTerrainInfo(pos[0], pos[1]) > 0){
                            Critter beingAttacked;
                            for(int k = 0;k<critters.size();k++){
                                if(Arrays.equals(critters.get(k).getPosition(), pos)){
                                    beingAttacked = critters.get(k);
                                    beingAttacked.beingAttacked(info);
                                    if(beingAttacked.isDead()){
                                        handleDead(beingAttacked);
                                        critters.remove(k);
                                        if(k < j){ j--; }
                                        lastCritterIdx--;
                                        continue;
                                    }
                                    break;
                                }
                            }
                        }
                        break;
                    case GROW:
                        crit.grow();
                        break;
                    case BUD:
                        pos = crit.checkMove(-1);
                        Maybe<Critter> buddy = crit.bud();
                        if(world.getTerrainInfo(pos[0], pos[1]) == 0){
                            try{
                                Critter newCritter = buddy.get();
                                newCritter.setPosition(pos[0], pos[1]);
                                critters.add(newCritter);
                                world.addCritter(buddy.get(),pos[0], pos[1],true);
                            }catch(NoMaybeValue e){
                                break;
                            }
                        }
                        break;
                    case MATE:
                        pos = crit.checkMove(1);
                        if (world.getTerrainInfo(pos[0],pos[1]) > 0){
                            Critter crit2;
                            boolean added = false;
                            for(int k=0;k<critters.size();k++){
                                if(Arrays.equals(critters.get(k).getPosition(),pos)){
                                    crit2 = critters.get(k);
                                    if(Arrays.equals(crit2.checkMove(1), crit.getPosition())){
                                        if(mateCritter.containsKey(crit2)){
                                            mateCritter.put(crit2, crit);
                                            added = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if(!added){
                                mateCritter.put(crit, null);
                            }
                        }else{
                            mateCritter.put(crit, null);
                        }
                        break;
                    case SERVE:
                        pos = crit.checkMove(1);
                        if(world.getTerrainInfo(pos[0], pos[1]) == 0 || world.getTerrainInfo(pos[0], pos[1]) < -1){
                            try {
                                info = crit.serve(num.get());
                                world.addFood(info[0],info[1],info[2]);
                            }catch(NoMaybeValue e){
                                crit.serve(0);
                            }
                        }else {
                            crit.serve(0);
                        }
                        break;
                }

                // remove current critter if died in this time step
                if(crit.isDead()){
                    System.out.println("Critter: " + crit.getSpecies() + " died");
                    handleDead(crit);
                    critters.remove(j);
                    j--;
                    lastCritterIdx--;
                }
            }

            // mate
            Set<Critter> mater = mateCritter.keySet();
            Iterator<Critter> iter = mater.iterator();
            while(iter.hasNext()) {
                Critter matern = iter.next();
                if (mateCritter.get(matern) != null) {
                    int[] pos1 = matern.checkMove(-1);
                    int[] pos2 = mateCritter.get(matern).checkMove(-1);
                    int[] childPos = new int[2];
                    Maybe<Critter> child = Maybe.none();
                    if (world.getTerrainInfo(pos1[0],pos1[1]) == 0 && world.getTerrainInfo(pos2[0],pos2[1]) == 0) {
                        Random r = new Random();
                        if (r.nextBoolean()) {
                            childPos = pos1;
                        } else {
                            childPos = pos2;
                        }
                        child = matern.mate(Maybe.from(mateCritter.get(matern)), true);
                    } else if (world.getTerrainInfo(pos1[0],pos1[1]) == 0) {
                        childPos = pos1;
                        child = matern.mate(Maybe.from(mateCritter.get(matern)), true);
                    } else if (world.getTerrainInfo(pos2[0], pos2[1]) == 0) {
                        childPos = pos2;
                        child = matern.mate(Maybe.from(mateCritter.get(matern)), true);
                    } else {
                        matern.mate(Maybe.from(mateCritter.get(matern)), false);
                    }
                    try {
                        Critter chil = child.get();
                        world.addCritter(chil, childPos[0], childPos[1], true);
                        chil.setPosition(childPos[0], childPos[1]);
                        chil.setWorld(world);
                        critters.add(chil);
                    } catch (NoMaybeValue ignored) {}
                } else {
                    matern.mate(Maybe.none(), false);
                }
            }

            world.dropManna();
        }
        return true;
    }

    // removes a dead Critter c from the simulation
    private void handleDead(Critter c){
        int amount = Constants.FOOD_PER_SIZE * c.getMemoryAt(3);
        int[] pos = c.getPosition();
        world.removeCritter(c, true);
        world.addFood(pos[0], pos[1], amount);
    }

    @Override
    public void printWorld(PrintStream out) {
        out.println(world);
    }

}
