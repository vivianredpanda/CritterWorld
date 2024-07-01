package model;


import ast.Program;
import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;
import exceptions.SyntaxError;
import org.junit.jupiter.api.Test;
import parse.Parser;
import parse.ParserFactory;

import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;
import java.util.*;

public class WorldTest {

    @Test
    void dropManna(){
        World a = new World();
        a.setEnableManna(true);
        CritterTest critter = new CritterTest();
        try {
            a.addCritter(critter.critterExample(), 0,0, true);
        } catch (SyntaxError e) {
            throw new RuntimeException(e);
        }
        a.dropManna();
        System.out.println(a);
    }

    @Test
    void addStuffWorld(){
        World a = new World();
        CritterTest critter = new CritterTest();
        a.addRock(0,0);
        try {
            Critter c = critter.critterExample();
            List<Critter> clist = new ArrayList<Critter>();
            for(int i=0;i<10;i++){
                Critter ctemp = c.clone();
                clist.add(ctemp);
                a.addCritter(ctemp,i*2,0,true);
                ctemp.setPosition(i*2,0);
            }



            a.removeCritter(clist.get(0), true);
            a.removeCritter(clist.get(1), true);
            a.move(clist.get(2), 4, 0);
            clist.get(2).setPosition(4,0);
            a.addRock(0,0);
            a.addFood(4,8,10);
            a.addRock(3,5);
            clist.get(3).setDirection(0);
            System.out.println(a);
            //a.addRock()
            int[] pos = clist.get(3).moveAction(1);
            if(a.move(clist.get(3), pos[0], pos[1])){
                System.out.println("forward");
                clist.get(3).setPosition(pos[0],pos[1]);
            }else{
                System.out.println("no forward");
            }
            System.out.println(a.getValidPosition());

            System.out.println(a);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
