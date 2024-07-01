package ast;

import java.util.List;
import cms.util.maybe.Maybe;

public abstract class AbstractNode implements Node {
    protected Node parent;

    @Override
    public int size() {
        int size = 1;
        for(Node child: getChildren()){
            size = size+child.size();
        }
        return size;
    }

    // head is 0, then counts children left to right, then children of children by layers
    @Override
    public Node nodeAt(int index) {
        if(index>=size() || index<0){
            throw new IndexOutOfBoundsException();
        }
        List<Node> children = getChildren();
        if(index == 0){
            return this;
        }else if(index <= children.size()){
            return children.get(index-1);
        }else{
            int iterateIndex = children.size();
            for(int i = 0; i< children.size(); i++){
                try{
                    return children.get(i).nodeAt(index-iterateIndex);
                }catch(IndexOutOfBoundsException e){
                    iterateIndex = iterateIndex + children.get(i).size() - 1;
                }
            }
        }
        return null;
    }

    @Override
    public abstract StringBuilder prettyPrint(StringBuilder sb);

    @Override
    public abstract Node clone();

    @Override
    public abstract List<Node> getChildren();

    @Override
    public Maybe<Node> getParent() {
        return Maybe.from(parent);
    }

    @Override
    public void setParent(Node p) {
        parent = p;
    }

    /**
     * @return the String representation of the tree rooted at this {@code
     * Node}.
     */
    public String toString(){
        return prettyPrint(new StringBuilder()).toString();
    }
    @Override
    public Maybe<Node> accept(Mutation v){
        return Maybe.none();
    }

    @Override
    public boolean set(Mutation m, Node n){
        return false;
    }
}
