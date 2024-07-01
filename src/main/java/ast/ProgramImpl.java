package ast;

import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** A data structure representing a critter program. */
public class ProgramImpl extends AbstractNode implements Program {

    private List<Node> rules;

    public ProgramImpl(List<Node> rules){
        this.rules = rules;
    }

    @Override
    public Program mutate() {
        Random r = new Random();
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

        Maybe<Program> mutatedNode = mutate(r.nextInt(this.size()), cur);
        try {
            return mutatedNode.get();
        } catch (NoMaybeValue e){
            return null;
        }
    }

    @Override
    public Maybe<Program> mutate(int index, Mutation m) {
        return m.apply(this, this.nodeAt(index));
    }

    @Override
    public Maybe<Node> findNodeOfType(NodeCategory type) {
        // loop through all nodes, add all the nodes of category c to lst
        List<Node> options = new ArrayList<>();
        for(int i = 0; i < rules.size(); i++){
            Node cur = rules.get(i);
            if(cur.getCategory() == type){ options.add(cur); }
        }

        // if size is 0, return Maybe.none() --> no node of category c rooted at n
        if(options.size()==0){ return Maybe.none(); }

        // otherwise, return a random element of lst
        Random r = new Random();
        return Maybe.from(options.get(r.nextInt(options.size())));
    }

    @Override
    public NodeCategory getCategory() {
        return NodeCategory.PROGRAM;
    }

    @Override
    public List<Node> getChildren() {
        return rules;
    }

    @Override
    public StringBuilder prettyPrint(StringBuilder sb){
        for(int i=0;i<rules.size();i++){
            rules.get(i).prettyPrint(sb);
            sb.append("\n");
        }
        return sb;
    }

    @Override
    public Node clone() {
        List<Node> lst = new ArrayList<>();
        for(Node c: getChildren()){
            lst.add(c.clone());
        }
        Node copy = new ProgramImpl(lst);
        for(Node c: lst){
            c.setParent(copy);
        }
        return copy;
    }

    public boolean classInv() {
        assert(rules.size() > 0);
        for(Node c: rules){
            assert(c.classInv());
            assert(c instanceof Rule);
        }
        return true;
    }

    public Maybe<Node> accept(Mutation v){
        return v.visit(this);
    }

    public boolean set(Mutation m, Node n){
        if(m != null && n instanceof ProgramImpl) {
            rules = ((ProgramImpl)(n)).rules;
            return true;
        }
        return false;
    }
}
