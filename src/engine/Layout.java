package engine;

import objects.*;
import org.newdawn.slick.*;

import java.util.ArrayList;

public class Layout {
    public static class Combat {
        private BasicWindow itemWindow;
        private BasicWindow statusWindow;
        private BasicWindow abilityWindow;
        private BasicWindow magicWindow;
        //TODO: implement enemyWindow (using sprite graphics)
        private BasicWindow enemyWindow;

        protected Combat(GameClass game) {
            itemWindow = new BasicWindow(30, 270, 120, 240,
                                         Color.orange, "Items", true);
            statusWindow = new BasicWindow(30, 540, 120, 240, Color.red, "Status");
            abilityWindow = new BasicWindow( 874, 270, 120, 240,
                                            Color.pink, "Abilities", true);
            magicWindow = new BasicWindow(874, 540, 120, 240,
                                          Color.magenta, "Magic", true);
            //temp
            enemyWindow = new BasicWindow(180, 320, 663, 290, Color.lightGray, "Enemies");

            updateWindows(game);

            //move console to position
            game.console.move(180,540, 663);
            game.consoleOutput.move(180, 500, 663, 160);
        }

        public void render(Graphics g) {
            itemWindow.render(g);
            statusWindow.render(g);
            abilityWindow.render(g);
            magicWindow.render(g);
            enemyWindow.render(g);
        }

        public void updateWindows(GameClass game) {
            updateItems(game.adventurer.equipment);
            updateStatus(game.adventurer);
            updateAbilities(game.adventurer.abilityList);
            updateEnemies();
        }

        private void updateItems(ArrayList<Item.ItemInterface> equipment) {
            itemWindow.clear();
            for(Item.ItemInterface item : equipment) {
                itemWindow.addLine(item.getName());
            }
        }

        private void updateStatus(Adventurer adventurer) {
            statusWindow.clear();
            statusWindow.addLine("|" + adventurer.name + "|");

            statusWindow.addLine("EQUIP:");
            if(adventurer.equippedWeapons.getRight() != null) {
                statusWindow.addLine("  R: " + adventurer.equippedWeapons.getRight().getName());
            } else {
                statusWindow.addLine("");
            }
            if(adventurer.equippedWeapons.getLeft() != null) {
                statusWindow.addLine("  L: " + adventurer.equippedWeapons.getLeft().getName());
            } else {
                statusWindow.addLine("");
            }

            statusWindow.addLine("HP: " + adventurer.getStat(Modifiers.Stat.HEALTH)
                                 + "/" + adventurer.getStat(Modifiers.Stat.MAX_HEALTH));
            statusWindow.addLine("ARM: " + adventurer.getStat(Modifiers.Stat.ARMOR));
            statusWindow.addLine("STR: " + adventurer.getStat(Modifiers.Stat.STRENGTH));
            statusWindow.addLine("DEX: " + adventurer.getStat(Modifiers.Stat.DEXTERITY));
            statusWindow.addLine("SPD: " + adventurer.getStat(Modifiers.Stat.SPEED));
            statusWindow.addLine("MAG: " + adventurer.getStat(Modifiers.Stat.MAGIC));
        }

        private void updateAbilities(ArrayList<Ability.AbilityInterface> abilityList) {
            abilityWindow.clear();
            for(Ability.AbilityInterface ability : abilityList) {
                abilityWindow.addLine(ability.getName());
            }
        }

        private void updateEnemies() {
            enemyWindow.clear();
            for(int i = 0; i < Monster.monsterList.size(); i++) {
                Mob enemy = Monster.monsterList.get(i);
                enemyWindow.addLine((i + 1) + ": " + enemy.name + "(" + enemy.getStat(Modifiers.Stat.HEALTH)
                                    + "/" + enemy.getStat(Modifiers.Stat.MAX_HEALTH) + ")");
            }
        }
    }

    public static class Adventure {
        private BasicWindow mapWindow;
        private Map map;
        private BasicWindow statusWindow;
        private BasicWindow equipmentWindow;

        public Adventure(GameClass game) {
            statusWindow = new BasicWindow(30, 540, 120, 200, Color.red, "Status");
            equipmentWindow = new BasicWindow(864, 540, 140, 510,
                    Color.yellow, "Equipment", true);
            //TODO: Display map using sprite graphics
            mapWindow = new BasicWindow(30, 320, 813, 290, Color.gray, "Map", false);
            map = new Map(30, 320 + 20, 813, 280, game.adventurer, "map.txt");

            //move console to position
            game.console.move(170,540, 673);
            game.consoleOutput.move(170, 500, 673, 160);

            updateWindows(game);
        }

        public void update(GameContainer gc, int delta, Input input) {
            map.update(gc, delta, input);
        }

        public void render(Graphics g) {
            statusWindow.render(g);
            equipmentWindow.render(g);
            mapWindow.render(g);
            map.render(g);
        }

        public Map getMap() {
            return map;
        }

        public void updateWindows(GameClass game) {
            updateStatusWindow(game.adventurer);
            updateEquipmentWindow(game.adventurer);

        }

        private void updateStatusWindow(Adventurer adventurer) {
            statusWindow.clear();
            String[] lines = {
                    "|" + adventurer.name + "|",
                    "HP: " + adventurer.getStat(Modifiers.Stat.HEALTH) + "/"
                            + adventurer.getStat(Modifiers.Stat.MAX_HEALTH),
                    "ARM: " + adventurer.getStat(Modifiers.Stat.ARMOR),
                    "STR: " + adventurer.getStat(Modifiers.Stat.STRENGTH),
                    "DEX: " + adventurer.getStat(Modifiers.Stat.DEXTERITY),
                    "SPD: " + adventurer.getStat(Modifiers.Stat.SPEED),
                    "MAG: " + adventurer.getStat(Modifiers.Stat.MAGIC)
            };

            statusWindow.addMultipleLines(lines);
        }

        private void updateEquipmentWindow(Adventurer adventurer) {
            equipmentWindow.clear();
            equipmentWindow.addLine("ITEMS:");
            for(Item.ItemInterface item : adventurer.equipment) {
                equipmentWindow.addLine(" " + item.getName());
            }

            equipmentWindow.addLine("EQUIP:");
            if(adventurer.equippedWeapons.getRight() != null) {
                equipmentWindow.addLine(" R: " + adventurer.equippedWeapons.getRight().getName());
            } else equipmentWindow.addLine("");
            if(adventurer.equippedWeapons.getLeft() != null) {
                equipmentWindow.addLine(" L: " + adventurer.equippedWeapons.getLeft().getName());
            } else equipmentWindow.addLine("");

            equipmentWindow.addLine("ARMOR:");
            for(Item.Armor armor : adventurer.equippedArmor) {
                equipmentWindow.addLine(" " + armor.getType().toString() + ":");
                equipmentWindow.addLine("  " + armor.getName());
            }
        }
    }
}
