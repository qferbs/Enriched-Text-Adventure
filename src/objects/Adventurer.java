package objects;

import engine.GameClass;
import engine.Mob;
import engine.StateHandler;

import java.util.ArrayList;

public class Adventurer extends Mob {

    public ArrayList<Item.ItemInterface> equipment;
    private int mapX;
    private int mapY;
    private int viewDistance;

    public Adventurer(int maxHealth, int strength, int dexterity, int speed, int magic) {
        super(maxHealth, 0, strength, dexterity, speed, magic, "Adventurer");
        equipment = new ArrayList<>();

        equip(new Item.Weapon(5, Item.Weapon.Type.HEAVY, "sword"));
        equip(new Item.Armor(25, Item.Armor.Type.CHEST, "Chest-plate"));
        equipment.add(new Item.Consumable(Modifiers.Stat.HEALTH, 5, "Potion"));
        equipment.add(new Item.Trinket(Modifiers.Stat.MAX_HEALTH, -2,
                                      "Trinket", 1, "Weaken"));
        equipment.add(new Item.Weapon(4, Item.Weapon.Type.LIGHT, "dagger"));
        equipment.add(new Item.Armor(10, Item.Armor.Type.HEAD, "helm"));
        abilityList.add(new Ability.Slam());
        viewDistance = 0;
    }

    public void setMapX(int amount) {
        mapX = amount;
    }

    public void setMapY(int amount) {
        mapY = amount;
    }

    public int getMapX() {
        return mapX;
    }

    public int getMapY() {
        return mapY;
    }

    public void setViewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
    }

    public boolean isTileSeen(int x, int y) {
        if(viewDistance != 0) {
            return Math.sqrt(Math.pow(x - this.mapX, 2) + Math.pow(y - this.mapY, 2)) <= viewDistance;
        } else {
            return true;
        }
    }

    //these two should never be called
    @Override
    public void doAction(GameClass game) {}

    @Override
    public Mob copy() {
        return null;
    }

    public StateHandler.State getUpdateState() {
        return new UpdateState();
    }

    public class UpdateState implements StateHandler.State {
        @Override
        public void start(GameClass game) {
            game.consoleOutput.addMultipleLines(tickStatus());
            game.combatLayout.updateWindows(game);
        }

        @Override
        public boolean isComplete() {return true;}

        @Override
        public void end() {}
    }
}
