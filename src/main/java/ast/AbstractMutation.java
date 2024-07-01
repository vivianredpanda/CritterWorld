package ast;

import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;

import java.util.List;
import java.util.Random;

public abstract class AbstractMutation implements Mutation {
    protected Random r = new Random();
    protected ProgramImpl root;

    public boolean canApply(Node n) {
        try{
            n.accept(this).get();
        }catch(NoMaybeValue e){
            return false;
        }
        return true;
    }

    public boolean equals(Mutation m) {
        return m.getType().equals(this.getType());
    }

    public Maybe<Program> apply(Program program, Node node){
        //set doesn't use node category, it uses the specific instance --> leads to issues in replace
        try {
            Node n = node.accept(this).get();
            for(int i=0;i<program.size();i++){
                if(program.nodeAt(i) == node){
                    program.nodeAt(i).set(this,n);
                    break;
                }
            }
            return Maybe.from(program);
        }catch(NoMaybeValue e){
            return Maybe.none();
        }
    }

    public void traceRoot(Node n){
        if(n.getCategory() == NodeCategory.PROGRAM){
            root = (ProgramImpl) n;
            return;
        } else {
            while (n.getCategory() != NodeCategory.PROGRAM) {
                try {
                    n = n.getParent().get();
                    traceRoot(n);
                } catch (NoMaybeValue e) {
                    return;
                }
            }
        }
    }

    /**
     * checks if c1 can be replaced by c2
     */
    private boolean isReplaceable(NodeCategory c1, NodeCategory c2){
        if(c1 == c2){
            return true;
        }
        if(c1 == NodeCategory.COMMAND && (c2==NodeCategory.UPDATE || c2 == NodeCategory.ACTION)){
            return true;
        }
        if(c1 == NodeCategory.EXPRESSION && c2 == NodeCategory.EXPRESSION){
            return true;
        }
        if(c1 == NodeCategory.CONDITION && (c2 == NodeCategory.REL)){
            return true;
        }
        if((c1 == NodeCategory.UPDATE && c2 == NodeCategory.ACTION) || (c1 == NodeCategory.ACTION && c2 == NodeCategory.UPDATE)){
            return true;
        }
        return false;
    }
    public boolean subTreeExist(Node root, Node node){
        if((node.getCategory() == NodeCategory.UPDATE || node.getCategory() == NodeCategory.ACTION)  && node != root){
            if(isReplaceable(node.getCategory(), root.getCategory())){
                if(node.getCategory() == NodeCategory.UPDATE && root.getCategory() == NodeCategory.ACTION){
                    try{
                        Node parent = node.getParent().get();
                        if(parent.getChildren().indexOf(node) == parent.size()-1){
                            return true;
                        }
                    } catch (NoMaybeValue e) {
                    }
                }else{
                    return true;
                }
            }
        }else{
            if((isReplaceable(root.getCategory(), node.getCategory()) || isReplaceable(node.getCategory(), root.getCategory())) && node != root){
                return true;
            }
        }
        List<Node> children = root.getChildren();
        for(Node i: children){
            boolean a = subTreeExist(i, node);
            if(a){
                return true;
            }
        }
        return false;
    }
    public Maybe<Node> subTree (Node root, Node node){
        if(!subTreeExist(root, node)){
            return Maybe.none();
        }
        Random r = new Random();
        int nodeInd = 0;
        while(true){
            nodeInd = r.nextInt(root.size());
            if((node.getCategory() == NodeCategory.UPDATE || node.getCategory() == NodeCategory.ACTION)  && node != root.nodeAt(nodeInd)){
                if(isReplaceable(node.getCategory(), root.nodeAt(nodeInd).getCategory())){
                    if(node.getCategory() == NodeCategory.UPDATE && root.nodeAt(nodeInd).getCategory() == NodeCategory.ACTION){
                        try{
                            Node parent = node.getParent().get();
                            if(parent.getChildren().indexOf(node) == parent.size()-1){
                                break;
                            }
                        } catch (NoMaybeValue e) {
                        }
                    }else{
                        break;
                    }
                }
            }else{
                if((isReplaceable(root.nodeAt(nodeInd).getCategory(), node.getCategory()) || isReplaceable(node.getCategory(), root.nodeAt(nodeInd).getCategory())) && node != root.nodeAt(nodeInd)){
                    break;
                }
            }
        }
        return Maybe.from(root.nodeAt(nodeInd));
    }

}
