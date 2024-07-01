package controller;

import ast.Action;
import cms.util.maybe.Maybe;

class CritterAction {
    private Action.Actions action;
    private Maybe<Integer> num;

    public CritterAction(Action.Actions action, Maybe<Integer> num){
        this.action = action;
        this.num = num;
    }

    public Action.Actions getAction(){ return action; }

    public Maybe<Integer> getNum() { return num; }
}
