package main;

import ast.Mutation;
import ast.MutationFactory;
import ast.Node;
import ast.Program;
import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;
import exceptions.SyntaxError;
import parse.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Random;

public class ParseAndMutateApp {

    public static void main(String[] args) {
        int n = 0;
        String file = null;
        try {
            if (args.length == 1) {
                file = args[0];
            } else if (args.length == 3 && args[0].equals("--mutate")) {
                n = Integer.parseInt(args[1]);
                if (n < 0) throw new IllegalArgumentException();
                file = args[2];
            } else {
                throw new IllegalArgumentException();
            }

            BufferedReader br = new BufferedReader(new FileReader(file));
            Parser p = ParserFactory.getParser();
            Program prog = p.parse(br);
            Random r = new Random();

            if(args.length == 1){
                System.out.println(prog);
            }

            int mutationCnt = 0;
            while(mutationCnt < n){
                Mutation cur;
                int mutation = r.nextInt(6);
                switch(mutation){
                    case 0:
                        cur = MutationFactory.getRemove();
                        break;
                    case 1:
                        cur = MutationFactory.getSwap();
                        break;
                    case 2:
                        cur = MutationFactory.getReplace();
                        break;
                    case 3:
                        cur = MutationFactory.getTransform();
                        break;
                    case 4:
                        cur = MutationFactory.getInsert();
                        break;
                    default:
                        cur = MutationFactory.getDuplicate();
                }

                Maybe<Program> mutatedNode = cur.apply(prog, prog.nodeAt(r.nextInt(prog.size())));

                try{
                    prog = mutatedNode.get();
                    mutationCnt++;
                    System.out.println("--- MUTATION " + mutationCnt + ": " + cur.getType() + " ---");
                    System.out.println("MUTATED PROGRAM");
                    System.out.println(prog);
                } catch (NoMaybeValue e){}
            }

        } catch (IllegalArgumentException e) {
            System.out.println(e);
            System.out.println("Usage:\n  <input_file>\n  " +
                               "--mutate <n> <input_file>");
        } catch (FileNotFoundException e) {
            System.out.println("File " + file + " not found.");
        } catch (SyntaxError e) {
            System.out.println(e);
        }
    }
}
