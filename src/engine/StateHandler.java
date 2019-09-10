package engine;

import org.newdawn.slick.GameContainer;

import java.util.ArrayList;

//handles core gameplay loops for each game state
public class StateHandler {

    //interface to handle different game states (ie waiting for player to successfully execute a command
    public interface State {
        void start(GameClass game);
        boolean isComplete();
        void end();
    }

    //dummy class to signify end of stateList
    public class EndStateList implements State {
        public void start(GameClass game) {}
        public boolean isComplete() {return true;}
        public void end() {}
    }

    private boolean stateActive = false;
    private GameClass game;
    private ArrayList<State> stateList = new ArrayList<>();
    private State currentState = null;
    private int stateNum = 0;

    private final ArrayList<State> combatStateList = new ArrayList<>();
    private final ArrayList<State> adventureStateList = new ArrayList<>();
    private final ArrayList<State> conversationStateList = new ArrayList<>();

    public void init(GameClass game) {
        this.game = game;

        //sets objects list
        State[] combatList = {
                game.enemyHandler.getTurnState1(),
                game.adventurer.getUpdateState(),
                game.console.getInputState(),
                game.enemyHandler.getUpdateState(),
                game.enemyHandler.getTurnState2(),
                new EndStateList()
        };

        for(State state : combatList) {
            combatStateList.add(state);
        }

        //sets adventure list
        State[] adventureList = {
                game.console.getInputState(),
                game.adventureLayout.getMap().getTileEventState(),
                new EndStateList()
        };

        for(State state : adventureList) {
            adventureStateList.add(state);
        }

        //sets conversation list
        State[] conversationList = {
                game.console.getInputState(),
                new EndStateList()
        };

        for(State state : conversationList) {
            conversationStateList.add(state);
        }
    }

    public void update(GameContainer gc, int delta) {
        if(stateList.size() != 0) {
            if (!stateActive) {
                currentState = getNextState();
                currentState.start(game);
                stateActive = true;
            } else if (currentState.isComplete()) {
                currentState.end();
                stateActive = false;
            }
        }
    }

    private State getNextState() {
        if(stateList.size() == 0) {
            return null;
        } else if(stateList.get(stateNum).getClass() == EndStateList.class) {
            stateNum = 0;
        }

        return stateList.get(stateNum++);
    }

    public void changeGameState(GameClass.GameState gameState) {
        if(currentState != null) {
            currentState.end();
        }

        stateActive = false;
        stateNum = 0;

        switch(gameState) {
            case COMBAT:
                stateList = combatStateList;
                break;
            case ADVENTURE:
                stateList = adventureStateList;
                break;
            case CONVERSATION:
                stateList = conversationStateList;
                break;
        }
    }
}
