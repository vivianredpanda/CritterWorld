package model;

import ast.Program;
import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;
import exceptions.SyntaxError;
import org.junit.jupiter.api.Test;
import parse.Parser;
import parse.ParserFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CritterTest {

    public Critter critterExample() throws SyntaxError {
        int[] potential = new int[]{9,2,3,1,420,17};
        int[] mem = Critter.CREATE_MEM(potential[0],potential[1],potential[2],potential[3],potential[4],potential[5]);

        Reader br = writeToFile("mem[4] > 2500 --> bud;\n"
                + "mem[3*(1+2)] != 17 --> mem[6] := 17;//hi this is a test comment\n" +
                "{mem[4] > mem[3] * 400 and mem[3] < 7} --> grow;\n" +
                "ahead[1] < -1 and mem[4] < 500 * mem[3] --> eat;\n" +
                "(ahead[1] / 10 mod 100) != 17 and ahead[1] > 0 --> attack;\n" +
                "{ahead[2] < 10 or random[20] = 0} and ahead[1] = 0 --> forward;\n") ;
        Parser p = ParserFactory.getParser();
        Program root = p.parse(br);
        return new Critter("example", potential, root);
    }

    public Critter critterExample2() throws SyntaxError {
        int[] potential = new int[]{9,2,3,1,420,17};
        int[] mem = Critter.CREATE_MEM(potential[0],potential[1],potential[2],potential[3],potential[4],potential[5]);

        Reader br = writeToFile("mem[4] > 2500 --> bud;\n"
                + "mem[3*(1+2)] != 17 --> mem[6] := 17;//hi this is a test comment\n" +
                "{mem[4] > mem[3] * 400 and mem[3] < 7} --> grow;\n" +
                "ahead[1] < -1 and mem[4] < 500 * mem[3] --> eat;\n" +
                "(ahead[1] / 10 mod 100) != 17 and ahead[1] > 0 --> attack;\n") ;
        Parser p = ParserFactory.getParser();
        Program root = p.parse(br);
        return new Critter("example", potential, root);
    }
    @Test
    void createMem() {
        int[] potential = new int[]{7,30,20,2,3,20};
        int[] mem = Critter.CREATE_MEM(potential[0],potential[1],potential[2],potential[3],potential[4],potential[5]);
        assertEquals(potential[0],mem[0]);
        assertEquals(potential[1],mem[1]);
        assertEquals(potential[2],mem[2]);
        assertEquals(1,mem[3]);
        assertEquals(potential[4],mem[4]);
        assertEquals(1,mem[5]);
        assertEquals(potential[5],mem[6]);

        potential = new int[]{10234,-3,-20,1,1,1};
        mem = Critter.CREATE_MEM(potential[0],potential[1],potential[2],potential[3],potential[4],potential[5]);
        assertEquals(potential[0],mem[0]);
        assertEquals(1,mem[1]);
        assertEquals(1,mem[2]);
        assertEquals(potential[3],mem[3]);
        assertEquals(potential[4],mem[4]);
        assertEquals(1,mem[5]);
        assertEquals(potential[5],mem[6]);
    }

    @Test
    void setMemoryAt() throws SyntaxError {
        Critter one = critterExample();
        for(int i = 0; i<one.getMemory().length; i++){
            if(i == 6){
                one.setMemoryAt(i,80);
                assertEquals(80,one.getMemoryAt(i));
                one.setMemoryAt(i,99);
                assertEquals(99,one.getMemoryAt(i));
                one.setMemoryAt(i,0);
                assertEquals(0,one.getMemoryAt(i));
                one.setMemoryAt(i,-1);
                assertEquals(0,one.getMemoryAt(i));
                one.setMemoryAt(i,100);
                assertEquals(0,one.getMemoryAt(i));
                one.setMemoryAt(i,Integer.MIN_VALUE);
                assertEquals(0,one.getMemoryAt(i));
                one.setMemoryAt(i,Integer.MAX_VALUE);
                assertEquals(0,one.getMemoryAt(i));
            }else if(i>6){
                Random r = new Random();
                int sign = (int) (((int)Math.round(Math.random()) - 0.5) * 2);
                int random = r.nextInt(10000) * sign;
                one.setMemoryAt(i,random);
                assertEquals(random,one.getMemoryAt(i));
            }
            int original = one.getMemoryAt(i);
            one.setMemoryAt(i,12);
            assertEquals(original, one.getMemoryAt(i));
        }

    }

    @Test
    void waitAction() throws SyntaxError {
        Critter one = critterExample();
        for(int i = 0; i<100; i++){
            int[] originalMem = one.getMemory();
            one.waitAction();
            originalMem[4] = originalMem[4] + originalMem[3]*Constants.SOLAR_FLUX;
            if(originalMem[4] > 500){
                originalMem[4] = 500;
            }
            assertEquals(true, Arrays.equals(originalMem,one.getMemory()));
            try {
                assertEquals("WAIT",one.getLastRuleString().get());
            } catch (NoMaybeValue e) {
                assertFalse(true);
            }
        }
    }

    @Test
    void turnAction() throws SyntaxError {
        Critter one = critterExample();
        for(int i = 0; i<500; i++){
            int r = (int) (((int)Math.round(Math.random()) - 0.5) * 2);

            int original = one.getDirection();
            int[] originalMem = one.getMemory();
            one.turnAction(r);
            originalMem[4] = originalMem[4] - originalMem[3];
            if(originalMem[4]<0){
                originalMem[4] = 0;
            }
            original = original+r;
            if (original > 5) {
                original = 0;
            }else if(original<0){
                original = 5;
            }

            System.out.println("original: " + Arrays.toString(originalMem) + " new: " + Arrays.toString(one.getMemory()));
            assertEquals(true, Arrays.equals(originalMem, one.getMemory()));
            assertEquals(original, one.getDirection());
        }
    }

    @Test
    void checkMove() throws SyntaxError {
        Critter one = critterExample();
        for(int i  = 0; i<500; i++){
            Random r = new Random();
            one.setPosition(r.nextInt(50000),r.nextInt(50000));
            int[] position = one.getPosition();

            int direction = (int) (((int)Math.round(Math.random()) - 0.5) * 2);
            int dir = one.getDirection();
            int[] modified = one.checkMove(direction);
            if(direction<0){ dir = (dir+3)%6;}
            switch(dir){
                case 0: assertEquals(position[0],modified[0]);
                    assertEquals(position[1] + 2,modified[1]);
                    break;
                case 1: assertEquals(position[0] + 1,modified[0]);
                    assertEquals(position[1] + 1,modified[1]);
                    break;
                case 2: assertEquals(position[0] + 1,modified[0]);
                    assertEquals(position[1] - 1,modified[1]);
                    break;
                case 3: assertEquals(position[0],modified[0]);
                    assertEquals(position[1] - 2,modified[1]);
                    break;
                case 4: assertEquals(position[0] - 1,modified[0]);
                    assertEquals(position[1] - 1,modified[1]);
                    break;
                case 5: assertEquals(position[0] - 1,modified[0]);
                    assertEquals(position[1] + 1,modified[1]);
                    break;
            }
        }
    }

    @Test
    void moveAction() throws SyntaxError {
        Critter one = critterExample();
        for(int i = 0; i<10; i++){
            int direction = (int) (((int) Math.round(Math.random()) - 0.5) * 2);
            int original = one.getMemoryAt(4);
            one.moveAction(direction);
            assertEquals(original - one.getMemoryAt(3) * Constants.MOVE_COST, one.getMemoryAt(4));
        }
    }

    @Test
    void eat() throws SyntaxError {
        Critter one = critterExample();
        int original = one.getMemoryAt(4);
        int remain = one.eat(2);
        assertEquals(original + 1,one.getMemoryAt(4));
        assertEquals(0,remain);

        remain = one.eat(81);
        assertEquals(Constants.ENERGY_PER_SIZE,one.getMemoryAt(4));
        assertEquals(1,remain);
    }

    @Test
    void serve() throws SyntaxError {
        Critter one = critterExample();
        int[] info = one.serve(20);
        assertEquals(20,info[2]);
        assertEquals(400 - one.getMemoryAt(3),one.getMemoryAt(4));
        info = one.serve(100);
        assertEquals(100,info[2]);
        assertEquals(300 - 2*one.getMemoryAt(3),one.getMemoryAt(4));
        info = one.serve(297);
        assertEquals(297,info[2]);
        assertEquals(3-3*one.getMemoryAt(3),one.getMemoryAt(4));
        info = one.serve(1);
        assertEquals(0,info[2]);
        assertEquals(0,one.getMemoryAt(4));

        one = critterExample();
        info = one.serve(500);
        assertEquals(419,info[2]);
        assertEquals(0,one.getMemoryAt(4));
    }

    @Test
    void attack() throws SyntaxError {
        Critter one = critterExample();
        Critter two = critterExample();
        int original = one.getMemoryAt(4);
        int[] information = one.attack();
        assertEquals(original - one.getMemoryAt(3)*Constants.ATTACK_COST, one.getMemoryAt(4));
        two.beingAttacked(information);
        //someone check my math and for get complexity

    }

    @Test
    void grow() throws SyntaxError {
        Critter one = critterExample();
        int original = one.getMemoryAt(4);
        one.grow();
        assertEquals(2,one.getMemoryAt(3));
        assertEquals(original-1*one.getComplexity()*Constants.GROW_COST, one.getMemoryAt(4));
        for(int i = 0; i<500; i++){
            one.waitAction();
        }
        assertEquals(true, one.getMemoryAt(4) > 500);

    }

    @Test
    void bud() throws SyntaxError {
        //with no mutation testing
        Critter one = critterExample();
        for(int i = 0; i<1500; i++){
            one.waitAction();
            if(i%1000 == 0){
                one.grow();
            }
        }
        int[] original = one.getMemory();
        int oComplexity = one.getComplexity();
        Critter copy = null;
        try {
            copy = one.bud().get();
        } catch (NoMaybeValue e) {
            assertFalse(true);
        }
        assertEquals(original.length,copy.getMemory().length);
        assertEquals(original[0],copy.getMemoryAt(0));
        assertEquals(original[1],copy.getMemoryAt(1));
        assertEquals(original[2],copy.getMemoryAt(2));
        assertEquals(1,copy.getMemoryAt(3));
        assertEquals(250,copy.getMemoryAt(4));
        assertEquals(original[5],copy.getMemoryAt(5));
        for(int i = 6; i<original.length; i++){
            assertEquals(0,copy.getMemoryAt(0));
        }
        assertEquals( original[4] - oComplexity*Constants.BUD_COST,one.getMemoryAt(4));

        copy = null;
        try {
            copy = one.bud().get();
        } catch (NoMaybeValue e) {
            assertTrue(true);
        }
        assertEquals(copy,null);
    }

    @Test
    void mate() throws SyntaxError {
        //does not test different parents mate

        //critter one wants to mate, but critter two did not want to mate
        Critter one = critterExample();
        Critter two = critterExample();
        int oneOriginal = one.getMemoryAt(4);
        int twoOriginal = two.getMemoryAt(4);
        Maybe<Critter> child = one.mate(null, false);
        assertEquals(oneOriginal - one.getMemoryAt(3),one.getMemoryAt(4));
        assertEquals(twoOriginal, two.getMemoryAt(4));
        boolean childExists = true;
        try {
            child.get();
        } catch (NoMaybeValue e) {
           childExists = false;
        }
        assertEquals(false,childExists);

        //critter one and two both wanted to mate, but they were not oriented correctly or other issues that don't allow
        //them to
        oneOriginal = one.getMemoryAt(4);
        twoOriginal = two.getMemoryAt(4);
        child = one.mate(Maybe.from(two), false);
        assertEquals(oneOriginal - one.getMemoryAt(3),one.getMemoryAt(4));
        assertEquals(twoOriginal - two.getMemoryAt(3), two.getMemoryAt(4));
        childExists = true;
        try {
            child.get();
        } catch (NoMaybeValue e) {
            childExists = false;
        }
        assertEquals(false,childExists);


        //critter one and two both want to mate and are able to according to their surroundings, but do not have enough
        //energy (only uses up turn???)
        oneOriginal = one.getMemoryAt(4);
        twoOriginal = two.getMemoryAt(4);
        child = one.mate(Maybe.from(two), true);
        assertEquals(oneOriginal - one.getMemoryAt(3),one.getMemoryAt(4));
        assertEquals(twoOriginal - two.getMemoryAt(3), two.getMemoryAt(4));
        childExists = true;
        try {
            Critter existingChild = child.get();
        } catch (NoMaybeValue e) {
            childExists = false;
        }
        assertEquals(false,childExists);


        for(int i = 0; i<1500; i++){
            one.waitAction();
            two.waitAction();
            if(i%1000 == 0){
                one.grow();
                two.grow();
            }
        }

        //critter one and two are identical critters that both want to mate and can successfully mate
        int[] oneArr = one.getMemory();
        oneOriginal = one.getMemoryAt(4);
        twoOriginal = two.getMemoryAt(4);
        int oneComplexity = one.getComplexity();
        int twoComplexity = two.getComplexity();
        child = one.mate(Maybe.from(two),true);
        assertEquals(oneOriginal - Constants.MATE_COST*oneComplexity,one.getMemoryAt(4));
        assertEquals(twoOriginal - Constants.MATE_COST*twoComplexity, two.getMemoryAt(4));

        childExists = true;
        try {
            Critter existingChild = child.get();
            assertEquals(oneArr.length,existingChild.getMemory().length);
            assertEquals(oneArr[0],existingChild.getMemoryAt(0));
            assertEquals(oneArr[1],existingChild.getMemoryAt(1));
            assertEquals(oneArr[2],existingChild.getMemoryAt(2));
            assertEquals(1,existingChild.getMemoryAt(3));
            assertEquals(250,existingChild.getMemoryAt(4));
            assertEquals(oneArr[5],existingChild.getMemoryAt(5));
            for(int i = 6; i<oneArr.length; i++){
                assertEquals(0,existingChild.getMemoryAt(0));
            }
            assertEquals(one.getProgramString(),existingChild.getProgramString());
        } catch (NoMaybeValue e) {
            childExists = false;
        }
        assertEquals(true,childExists);

        one = critterExample();
        two = critterExample2();

        for(int i = 0; i<1500; i++){
            one.waitAction();
            two.waitAction();
            if(i%1000 == 0){
                one.grow();
                two.grow();
            }
        }

        oneArr = one.getMemory();
        oneOriginal = one.getMemoryAt(4);
        twoOriginal = two.getMemoryAt(4);
        oneComplexity = one.getComplexity();
        twoComplexity = two.getComplexity();
        child = one.mate(Maybe.from(two),true);
        assertEquals(oneOriginal - Constants.MATE_COST*oneComplexity,one.getMemoryAt(4));
        assertEquals(twoOriginal - Constants.MATE_COST*twoComplexity, two.getMemoryAt(4));

        childExists = true;
        try {
            Critter existingChild = child.get();
            assertEquals(oneArr.length,existingChild.getMemory().length);
            assertEquals(oneArr[0],existingChild.getMemoryAt(0));
            assertEquals(oneArr[1],existingChild.getMemoryAt(1));
            assertEquals(oneArr[2],existingChild.getMemoryAt(2));
            assertEquals(1,existingChild.getMemoryAt(3));
            assertEquals(250,existingChild.getMemoryAt(4));
            assertEquals(oneArr[5],existingChild.getMemoryAt(5));
            for(int i = 6; i<oneArr.length; i++){
                assertEquals(0,existingChild.getMemoryAt(0));
            }
            assertEquals(one.getProgramString(),existingChild.getProgramString());
        } catch (NoMaybeValue e) {
            childExists = false;
        }
        assertEquals(true,childExists);
    }

    public Reader writeToFile(String s){
        try {
            FileWriter myWriter = new FileWriter("./temp.txt");
            myWriter.write(s);
            myWriter.close();
            return new BufferedReader(new FileReader("./temp.txt"));
        } catch (IOException e) {
            System.out.println("Error creating file.");
        }
        return null;
    }

}