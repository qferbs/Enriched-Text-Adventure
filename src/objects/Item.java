package objects;

import engine.Mob;

public class Item {

    public interface ItemInterface {

        //function call when item used from equipment list
        String[] use(Mob target, String user);

        //function call when passive evaluated at equip/un-equip
        void passive(Mob target);

        //function call to return item's name
        String getName();

        //returns array of strings of the item's info to print
        String[] getInfo();
    }

    public static class Consumable implements ItemInterface {

        private final String name;
        private final Modifiers.Stat stat;
        private final int amount;

        public Consumable(Modifiers.Stat stat, int amount, String name) {
            this.name = name;
            this.amount = amount;
            this.stat = stat;
        }

        public String[] use(Mob target, String user) {
            if(stat == Modifiers.Stat.HEALTH) {
                if(amount < 0) {
                    target.damage(-amount);
                    return new String[]{user + " used " + name + " to do " + amount + " damage to " + target.name + "."};
                } else {
                    return new String[]{user + " used " + name + " to heal " + target.name
                            + " for " + target.addStat(Modifiers.Stat.HEALTH, amount) + " health."};
                }
            } else {
                target.addStat(stat, amount);
                return new String[]{user + " used " +name + " to give "
                                    + amount + " " + stat + " to" + target.name + "."};
            }
        }

        public void passive(Mob target) {

        }

        public String getName() {
            return name;
        }

        public String [] getInfo() {
            return new String[] {
                    name + ":",
                    "adds " + amount + " " + stat + " to target."
            };
        }

        public Modifiers.Stat getStat() {
            return stat;
        }
    }

    public static class Trinket implements ItemInterface {

        private final String name;
        private final Modifiers.StatusEffect effect;

        public Trinket(Modifiers.Stat stat, int amount, String name) {
            this(stat, amount, name, 0);
        }

        public Trinket(Modifiers.Stat stat, int amount, String name, int duration) {
            this(stat, amount, name, duration, name);
        }

        public Trinket(Modifiers.Stat stat, int amount, String name, int duration, String buffName) {
            this.effect = new Modifiers.Buff(stat, amount, duration, buffName);
            this.name = name;
        }

        public String[] use(Mob target, String user) {
            return new String[]{user + " used " + name + " on " + target.name + ".",
                                target.addStatusEffect(effect)};
        }

        public void passive(Mob target) {

        }

        public String getName() {
            return name;
        }

        public String [] getInfo() {
            String[] lines = new String[effect.getInfo().length + 2];
            lines[0] = name + ":";
            lines[1] = "adds " + effect.getName() + " to target.";

            for(int i = 2; i < lines.length; i++) {
                lines[i] = effect.getInfo()[i-2];
            }

            return lines;
        }
    }

    public static class Weapon implements ItemInterface {

        public enum Type {
            HEAVY,
            LIGHT,
            RANGED
        }

        private int damage;
        private int damageDeviation;
        private final Type weaponType;
        private final String name;

        public Weapon(int damage, Type weaponType) {
            this(damage, weaponType, "Weapon", 0);
        }

        public Weapon(int damage, Type weaponType, String name) {
            this(damage, weaponType, name, 0);
        }

        public Weapon(int damage, Type weaponType, String name, int damageDeviation) {
            this.damage = damage;
            this.damageDeviation = damageDeviation;
            this.weaponType = weaponType;
            this.name = name;
        }

        public String[] use(Mob target, String user) {
            if(target.canEquipWeapon()) {
                target.equip(this);
                return new String[]{"Equipped " + name + "!"};
            } else {
                return new String[]{"Cannot equip " + name + ", both hands are full!"};
            }
        }

        public void passive(Mob target) {}

        public int getDamage() {
            return (int)(damage - damageDeviation + Math.random()*damageDeviation*2 + 0.5);
        }

        public int getDamageStat() {
            return damage;
        }

        public int getDamageDeviation() {
            return damageDeviation;
        }

        public String getName() {
            return name;
        }

        public String [] getInfo() {
            return new String[] {
                    name + ":",
                    "DAMAGE: " + (damage - damageDeviation) + "-" + (damage + damageDeviation),
                    "TYPE: " + weaponType
            };
        }

        public Type getWeaponType() {
            return weaponType;
        }
    }

    public static class Armor implements ItemInterface {

        public enum Type {
            HEAD,
            CHEST,
            HANDS,
            LEGS
        }

        private Type armorType;
        private int armorValue;
        private final String name;

        public Armor(int armorValue, Type armorType) {
            this(armorValue, armorType, "Armor");
        }

        public Armor(int armorValue, Type armorType, String name) {
            this.armorValue = armorValue;
            this.armorType = armorType;
            this.name = name;
        }

        public String[] use(Mob target, String user) {
            if(target.canEquip(armorType)) {
                target.equip(this);
                return new String[]{"Equipped " + name + "!"};
            } else {
                return new String[]{"Cannot equip" + name + ", armor slot " + armorType + "full!"};
            }
        }

        public void passive(Mob target) {

        }

        public String getName() {
            return name;
        }

        public String [] getInfo() {
            return new String[] {
                    name + ":",
                    "ARMOR: " + armorValue,
                    "TYPE: " + armorType
            };
        }

        public int getArmorValue() {
            return armorValue;
        }

        public Type getType() {
            return armorType;
        }
    }
}
