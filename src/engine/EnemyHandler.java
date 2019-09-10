package engine;

import objects.Modifiers;
import objects.Monster;

import java.util.ArrayList;
import java.util.Iterator;

//handles enemy actions curing objects state
public class EnemyHandler {

    public void enemyTurn1(GameClass game) {
        for(Mob monster : Monster.monsterList) {
            if(monster.getStat(Modifiers.Stat.SPEED) > game.adventurer.getStat(Modifiers.Stat.SPEED)) {
                monster.doAction(game);
            }
        }
    }

    public void enemyTurn2(GameClass game) {
        for(Mob monster : Monster.monsterList) {
            if(monster.getStat(Modifiers.Stat.SPEED) <= game.adventurer.getStat(Modifiers.Stat.SPEED)) {
                monster.doAction(game);
            }
        }
    }

    public String[] enemyTick() {
        ArrayList<String> output = new ArrayList<>();
        for(Mob monster : Monster.monsterList) {
            String [] out = monster.tickStatus();
            if(!out.equals(new String[]{})) {
                for(String string : out) {
                    output.add(string);
                }
            }
        }

        return output.toArray(new String[0]);
    }

    public StateHandler.State getUpdateState() {
        return new UpdateState();
    }

    public StateHandler.State getTurnState1() {
        return new TurnState1();
    }

    public StateHandler.State getTurnState2() {
        return new TurnState2();
    }

    public class UpdateState implements StateHandler.State {
        @Override
        public void start(GameClass game) {
            game.consoleOutput.addMultipleLines(enemyTick());

            //delete any killed monsters and display message
            for (Iterator<Mob> monsterIter = Monster.monsterList.iterator(); monsterIter.hasNext(); ) {
                Mob monster = monsterIter.next();
                if (monster.checkDead()) {
                    game.consoleOutput.addLine(monster.name + " was killed!");
                    monsterIter.remove();
                }
            }

            game.combatLayout.updateWindows(game);

            if(Monster.monsterList.isEmpty()) {
                game.consoleOutput.addLine("You are victorious!");
                game.switchState(GameClass.GameState.ADVENTURE);
            } else if(game.adventurer.getStat(Modifiers.Stat.HEALTH) <= 0) {
                game.consoleOutput.addLine("You lost!");
                //TODO: add end game handling here
            }
        }

        @Override
        public boolean isComplete() {return true;}

        @Override
        public void end() {}
    }

    public class TurnState1 implements StateHandler.State {
        @Override
        public void start(GameClass game) {
            enemyTurn1(game);
            game.combatLayout.updateWindows(game);
        }
        @Override
        public boolean isComplete() {return true;}
        @Override
        public void end() {}
    }

    public class TurnState2 implements StateHandler.State {
        @Override
        public void start(GameClass game) {
            enemyTurn2(game);
            game.combatLayout.updateWindows(game);
        }
        @Override
        public boolean isComplete() {return true;}
        @Override
        public void end() {}
    }
}
