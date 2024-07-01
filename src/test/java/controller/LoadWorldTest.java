package controller;

import org.junit.jupiter.api.*;

public class LoadWorldTest {
    @Test
    void loadWorldFile(){
        ControllerImpl a = new ControllerImpl();
        a.loadWorld("src/test/resources/A5files/empty.wld",true, false);
        a.printWorld(System.out);
    }
    @Test
    void newWorld(){
        ControllerImpl a = new ControllerImpl();
        a.newWorld();
        a.printWorld(System.out);
    }
}
