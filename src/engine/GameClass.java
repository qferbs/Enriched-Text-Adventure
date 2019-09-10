package engine;

import objects.Adventurer;
import org.newdawn.slick.*;

import java.util.logging.Level;
import java.util.logging.Logger;

//TODO: finish implementation of objects system
//TODO: replace magic with real system
//TODO: implement damage model (hit chance?, random damage?, range?)
//TODO: create level editor
//TODO: implement monster generator
//TODO: implement dungeon map/generator
//TODO: standardize hierarchies if irregularities become a problem

public class GameClass extends BasicGame {

    //all different game phases
    public enum GameState {
        ADVENTURE,
        COMBAT,
        CONVERSATION
    }

    private Input input;

    public Console console;
    public BasicWindow consoleOutput;
    public Adventurer adventurer;
    public Layout.Combat combatLayout;
    public Layout.Adventure adventureLayout;
    public StateHandler stateHandler;
    public EnemyHandler enemyHandler;

    public GameState gameState;

    public GameClass(String gamename) {
        super(gamename);
    }

    @Override
    public void init(GameContainer gc) throws SlickException {
        console = new Console(180,540, 663, 0, Color.green, this);
        consoleOutput = new BasicWindow(180, 500, 663, 160, Color.blue, "", true);

        adventurer = new Adventurer(30, 10, 10, 10, 0);

        //handler constructors
        stateHandler = new StateHandler();
        enemyHandler = new EnemyHandler();

        //set input
        input = new Input(gc.getHeight());
        this.setInput(input);

        switchState(GameState.ADVENTURE);

        //init stateHandler
        stateHandler.init(this);

    }

    @Override
    public void update(GameContainer gc, int delta) throws SlickException {
        console.update(gc, delta);
        stateHandler.update(gc, delta);
        //NOTE: may need consoleOutput update
        if(gameState == GameState.ADVENTURE) {
            adventureLayout.update(gc, delta, input);
        }
    }

    @Override
    public void render(GameContainer gc, Graphics g) throws SlickException {
        switch(gameState) {
            case COMBAT:
                combatLayout.render(g);
                break;
            case ADVENTURE:
            case CONVERSATION:
                adventureLayout.render(g);
                break;
        }

        console.render(g);
        consoleOutput.render(g);
    }

    //inputs

    @Override
    public void keyPressed(int key, char c) {
        console.keyPressed(key, c);
    }

    @Override
    public void mouseWheelMoved(int change) {
        console.MouseWheelMoved(change, input);
        consoleOutput.mouseWheelMoved(change, input);
    }

    public void switchState(GameState gameState) {
        this.gameState = gameState;
        stateHandler.changeGameState(gameState);

        switch(gameState) {
            case COMBAT:
                combatLayout = new Layout.Combat(this);
                break;
            case ADVENTURE:
            case CONVERSATION:
                adventureLayout = new Layout.Adventure(this);
                break;
        }
    }

    public static void main(String[] args) {
        try {
            AppGameContainer appgc;
            appgc = new AppGameContainer(new GameClass("Gamer time"));
            appgc.setDisplayMode(1024, 576, false);
            appgc.setVSync(true);
            appgc.setTargetFrameRate(60);
            appgc.start();
        }
        catch (SlickException ex) {
            Logger.getLogger(GameClass.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
