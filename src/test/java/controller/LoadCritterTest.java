package controller;

import org.junit.jupiter.api.*;

import java.io.*;

public class LoadCritterTest {
    ControllerImpl a = new ControllerImpl();

    @Test
    void loadCritters(){
        a.newWorld();
        a.loadCritters("src/test/resources/A5files/critter1.txt",2);
        a.printWorld(System.out);
    }

    @Test
    // load space critter
    void loadCritter(){
        System.out.println(a.loadCritter("src/test/resources/A5files/space_critter.txt"));
    }

    @Test
    // load critter with comments and whitespace
    void loadCritter1(){
        try {
            FileWriter myWriter = new FileWriter("./temp.txt");
            myWriter.write("species: small species name\n" +
                    "memsize: 7\n" +
                    "// hi this is a cool comment \n" +
                    "defense: 1\n" +
                    "offense: 1\n\n\r\n" +
                    "size: 1\n" +
                    "energy: 500\n" +
                    "posture: 0\n" +
                    "1 = 1 --> wait;");
            myWriter.close();
            System.out.println(a.loadCritter("./temp.txt"));
        } catch (IOException e) {
            System.out.println("Error creating file.");
        }
    }

    @Test
    // load critter with incomplete/invalid parameters
    void loadCritter2(){
        try {
            FileWriter myWriter = new FileWriter("./temp.txt");
            myWriter.write("species: small species name\n" +
                    "memsize: 7\n" +
                    "memsize: 10\n" +
                    "size: -1\n" +
                    "POSTURE: 999\n" +
                    "1 = 1 --> wait; 2 > 3 --> mem[3] := 0; ");
            myWriter.close();
            System.out.println(a.loadCritter("./temp.txt"));
        } catch (IOException e) {
            System.out.println("Error creating file.");
        }
    }

    @Test
    // load critter with incomplete parameters
    void loadCritter3(){
        try {
            FileWriter myWriter = new FileWriter("./temp.txt");
            myWriter.write("species: large species\n" +
                    "1 = 1 --> wait; 2 > 3 --> mem[3] := 0;");
            myWriter.close();
            System.out.println(a.loadCritter("./temp.txt"));
        } catch (IOException e) {
            System.out.println("Error creating file.");
        }
    }

}
