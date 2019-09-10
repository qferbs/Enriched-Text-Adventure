package objects;

import engine.GameClass;
import engine.Mob;

import java.util.ArrayList;

//TODO: add more monster types

public class Monster {

    public static ArrayList<Mob> monsterList = new ArrayList<>();

    public static class Goblin extends Mob {

        private final static int SPEED = 5;
        private final static double hitChance = 0.5;

        public Goblin(int health, int strength, int dexterity) {
            this(health, strength, dexterity, "goblin");
        }

        public Goblin(int health, int strength, int dexterity, String name) {
            this(health, strength, dexterity, name, null);
        }

        public Goblin(int health, int strength, int dexterity, String name, Item.Consumable item) {

            super(health, 0, strength, dexterity, SPEED, 0, name, item);

            Item.Armor goblinArmor = new Item.Armor(5, Item.Armor.Type.CHEST, "Goblin Armor");
            Item.Weapon goblinWeapon = new Item.Weapon(5,
                    Item.Weapon.Type.LIGHT, "Goblin Sword", 2);

            this.equip(goblinArmor);
            this.equip(goblinWeapon);
            this.abilityList.add(new Ability.GoblinBuff());
        }

        @Override
        public double getHitChance() {
            return hitChance;
        }

        @Override
        public void doAction(GameClass game) {
            //if statements controlling goblin's decisions
            //priorities: final blow > heal if low > ability (if available) || attack (50/50 split)
            if(game.adventurer.getStat(Modifiers.Stat.HEALTH)
                    < equippedWeapons.getRight().getDamageStat() + getStat(Modifiers.Stat.DEXTERITY)) {

                int damage = this.attack(equippedWeapons.getRight(), game.adventurer, hitChance);
                if(damage == -1) {
                    game.consoleOutput.addLine(name + " missed!");
                } else {
                    game.consoleOutput.addLine(name + " did " + damage + " damage.");
                }

            } else if((getStat(Modifiers.Stat.HEALTH) < getStat(Modifiers.Stat.MAX_HEALTH)*0.4)
                      && (heldItem != null && heldItem.getStat() == Modifiers.Stat.HEALTH)) {

                game.consoleOutput.addMultipleLines(heldItem.use(this, name));
                heldItem = null;

            } else if(abilityList.size() > 0 && Math.random() > 0.5) {

                game.consoleOutput.addMultipleLines(
                        abilityList.get((int)Math.random()*abilityList.size()).use(this, game.adventurer, game));

            } else {

                int damage = this.attack(equippedWeapons.getRight(), game.adventurer, hitChance);

                if(damage == -1) {
                    game.consoleOutput.addLine(name + " missed!");
                } else {
                    game.consoleOutput.addLine(name + " did " + damage + " damage.");
                }
            }
        }

        @Override
        public Mob copy() {
            return new Goblin(getStat(Modifiers.Stat.HEALTH),
                              getStat(Modifiers.Stat.STRENGTH),
                              getStat(Modifiers.Stat.DEXTERITY),
                              name,
                              heldItem
                              );
        }
    }

    public static class Spider extends Mob {

        private final static double HIT_CHANCE = 0.6;

        public Spider(int health, int armor, String name) {
            super(health, armor, 5, 5, 20, 0, name);

            int speed = 20 - armor*armor;
            if(speed > 5) {
                setStat(Modifiers.Stat.SPEED, speed);
            } else {
                setStat(Modifiers.Stat.SPEED, 5);
            }

            //spider damage equation based on health
            this.equip(new Item.Weapon((int) (health/3.0 + 6.66 + 0.5) - 5, Item.Weapon.Type.LIGHT, "Spider Claw", 3));
            this.abilityList.add(new Ability.SpiderWeb());
        }

        @Override
        public double getHitChance() {
            return HIT_CHANCE;
        }

        @Override
        public void doAction(GameClass game) {
            if(game.adventurer.getStat(Modifiers.Stat.HEALTH)
                    < equippedWeapons.getRight().getDamageStat() + getStat(Modifiers.Stat.DEXTERITY)) {

                int damage = this.attack(equippedWeapons.getRight(), game.adventurer, HIT_CHANCE);
                if(damage == -1) {
                    game.consoleOutput.addLine(name + " missed!");
                } else {
                    game.consoleOutput.addLine(name + " did " + damage + " damage.");
                }
            } else if(abilityList.size() > 0 && Math.random() > 0.7) {

                game.consoleOutput.addMultipleLines(
                        abilityList.get((int)Math.random()*abilityList.size()).use(this, game.adventurer, game));

            } else {
                int damage = this.attack(equippedWeapons.getRight(), game.adventurer, HIT_CHANCE);

                if(damage == -1) {
                    game.consoleOutput.addLine(name + " missed!");
                } else {
                    game.consoleOutput.addLine(name + " did " + damage + " damage.");
                }
            }
        }

        @Override
        public Mob copy() {
            return new Spider(getStat(Modifiers.Stat.MAX_HEALTH),
                    getStat(Modifiers.Stat.ARMOR),
                    name);
        }
    }
}