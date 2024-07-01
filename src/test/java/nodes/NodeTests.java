package nodes;
import ast.*;
import ast.Number;
import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NodeTests {
    @Test
    public void ruleNode(){
        Expr a = new Number(1);
        Expr b = new Number(2);
        Update u = new Update(a,b);
        Maybe<Expr> m = Maybe.none();
        Action ac = new Action(Action.Actions.ATTACK,m);
        assertEquals(1,a.size());
        assertEquals(NodeCategory.EXPRESSION,a.getCategory());
        assertEquals(3,u.size());
        assertEquals(NodeCategory.UPDATE,u.getCategory());
        assertEquals(1,ac.size());
        assertEquals(NodeCategory.ACTION,ac.getCategory());

        m = Maybe.some(b);
        ac = new Action(Action.Actions.BACKWARD,m);
        assertEquals(2,ac.size());
        assertEquals(NodeCategory.ACTION,ac.getCategory());
        List<Node> ua = new ArrayList<>(Arrays.asList(u,ac));
        Command co = new Command(ua);
        assertEquals(6,co.size());
        assertEquals(NodeCategory.COMMAND,co.getCategory());
        Expr c = new Number(3);
        Expr d = new Number(4);
        Rel r1 = new Rel(c, Rel.RelOperator.LT,d);
        Expr e=new Number(5);
        Expr f = new Number(6);
        Rel r2 = new Rel(e,Rel.RelOperator.LE,f);
        assertEquals(NodeCategory.REL,r2.getCategory());
        Condition amp = new AndCondition(r1, BinaryCondition.Operator.AND,r2);
        assertEquals(NodeCategory.CONDITION,amp.getCategory());
        assertEquals(7,amp.size());
        Rule r = new Rule(amp,co);
        assertEquals(14,r.size());
        assertEquals(NodeCategory.RULE,r.getCategory());
        Rel r3 = new Rel(a, Rel.RelOperator.GE,b);
        Rel r4 = new Rel(b,Rel.RelOperator.NE, b);
        Condition amp1 = new OrCondition(r3, BinaryCondition.Operator.OR,r4);
        amp = new AndCondition(r1,BinaryCondition.Operator.AND, amp1);
        r = new Rule(amp,co);
        Node cloned = r.clone();

        System.out.println(cloned.toString());

        for(int i = 0; i<cloned.size(); i++){
            System.out.println(cloned.nodeAt(i).getCategory());
            try {
                System.out.println("parent is: "+ cloned.nodeAt(i).getParent().get());
            } catch (NoMaybeValue ex) {
            }
            System.out.println(cloned.nodeAt(i));

        }



    }
}
