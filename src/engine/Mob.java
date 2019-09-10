package engine;

import objects.Ability;
import objects.Item;
import objects.Modifiers;

import java.util.ArrayList;
import java.lang.Math;
import java.util.Iterator;

//class meant to be extended to specific mobs
public abstract class Mob {
    public class EquippedWeapons {
        private final Item.Weapon defaultWeapon;

        private Item.Weapon right;
        private Item.Weapon left;

        public EquippedWeapons() {
            this(new Item.Weapon(0, Item.Weapon.Type.HEAVY, "fist"));
        }

        public EquippedWeapons(Item.Weapon defaultWeapon) {
            this.defaultWeapon = defaultWeapon;
            right = this.defaultWeapon;
            left = this.defaultWeapon;
        }

        public void add(Item.Weapon weapon) {
            if(right == defaultWeapon) {
                right = weapon;
            } else if(left == defaultWeapon) {
                left = weapon;
            }
        }

        public Item.Weapon getRight() {
            return right;
        }

        public Item.Weapon getLeft() {
            return left;
        }

        public void setRight(Item.Weapon right) {
            if(right == null) {
                this.right = defaultWeapon;
            } else {
                this.right = right;
            }
        }

        public void setLeft(Item.Weapon left) {
            if(left == null) {
                this.left = defaultWeapon;
            } else {
                this.left = left;
            }
        }
    }

    public final String name;

    private int maxHealth;
    private int armor;
    private int speed;
    private int strength;
    private int dexterity;
    //placeholder
    private int magic;

    private int curHealth;
    private int effMaxHealth;
    private int effArmor;
    private int effSpeed;
    private int effStrength;
    private int effDexterity;
    private int effMagic;

    public EquippedWeapons equippedWeapons;

    //TODO: tweak armor constants if necessary
    //damage calc constants
    private static final double MAX_ARMOR_MULTIPLIER = 0.05;
    private static final double ARMOR_SCALAR = 0.015;

    public ArrayList<Item.Armor> equippedArmor;
    public ArrayList<Ability.AbilityInterface> abilityList;
    public ArrayList<Modifiers.StatusEffect> statusEffects;
    //heldItem assumed to be one-time use
    public Item.Consumable heldItem;

    protected Mob(int health, int armor, int strength, int dexterity, int speed, int magic, String name) {
        this(health, armor, strength, dexterity, speed, magic, name, null);
    }

    protected Mob(int health, int armor, int strength, int dexterity, int speed,
                  int magic, String name, Item.Consumable heldItem) {
        this.maxHealth = health;
        this.curHealth = health;
        this.armor = armor;
        this.strength = strength;
        this.dexterity = dexterity;
        this.speed = speed;
        this.magic = magic;
        this.name = name;
        this.heldItem = heldItem;

        equippedArmor = new ArrayList<>();
        equippedWeapons = new EquippedWeapons();
        abilityList = new ArrayList<>();
        statusEffects = new ArrayList<>();

        resetStats();
    }

    public void setStat(Modifiers.Stat stat, int amount) {
        switch(stat) {
            case HEALTH:
                curHealth = amount;
                break;
            case MAX_HEALTH:
                maxHealth = amount;
                effMaxHealth = amount;
                break;
            case ARMOR:
                armor = amount;
                effArmor = amount;
                break;
            case SPEED:
                speed = amount;
                effSpeed = amount;
                break;
            case STRENGTH:
                strength = amount;
                effStrength = amount;
                break;
            case DEXTERITY:
                dexterity = amount;
                effDexterity = amount;
                break;
            case MAGIC:
                magic = amount;
                effMagic = amount;
                break;
        }
    }

    public int getStat(Modifiers.Stat stat) {
        switch(stat) {
            case HEALTH:
                return curHealth;
            case MAX_HEALTH:
                return effMaxHealth;
            case ARMOR:
                return effArmor;
            case SPEED:
                return effSpeed;
            case STRENGTH:
                return effStrength;
            case DEXTERITY:
                return effDexterity;
            case MAGIC:
                return effMagic;
        }

        return -1;
    }

    public int addStat(Modifiers.Stat stat, int amount) {
        switch(stat) {
            case HEALTH:
                if(curHealth + amount > effMaxHealth) {
                    int healedFor = effMaxHealth - curHealth;
                    curHealth = effMaxHealth;
                    return healedFor;
                } else {
                    curHealth += amount;
                    return amount;
                }
            case MAX_HEALTH:
                effMaxHealth += amount;
                if(curHealth > effMaxHealth) {
                    curHealth = effMaxHealth;
                }
                break;
            case ARMOR:
                effArmor += amount;
                break;
            case SPEED:
                effSpeed += amount;
                break;
            case STRENGTH:
                effStrength += amount;
                break;
            case DEXTERITY:
                effDexterity += amount;
                break;
            case MAGIC:
                effMagic += amount;
                break;
        }

        return amount;
    }

    public String addStatusEffect(Modifiers.StatusEffect effect) {
        return effect.activate(this);
    }

    public void removeStatusEffect(Modifiers.StatusEffect effect) {
        effect.remove(this);
        statusEffects.remove(effect);
    }

    //ticks status effects over and returns String array to output to console
    public String[] tickStatus() {
        ArrayList<String> output = new ArrayList<>();
        for(Iterator<Modifiers.StatusEffect> effectIter = statusEffects.iterator(); effectIter.hasNext();) {
            Modifiers.StatusEffect effect = effectIter.next();
            String out = effect.tick(this, effectIter);
            if(out != null) {
                output.add(out);
            }
        }

        return output.toArray(new String[0]);
    }

    //used to reset stats after objects finishes, etc.
    public void resetStats() {
        effMaxHealth = maxHealth;
        effStrength = strength;
        effDexterity = dexterity;
        reCalculateArmor();
        effSpeed = speed;
        effMagic = magic;
        equippedWeapons.getRight().passive(this);
        equippedWeapons.getLeft().passive(this);
    }

    //used for general damage
    public void damage(int amount) {
        curHealth -= amount;
        if(curHealth < 0) {
            curHealth = 0;
        }
    }

    //used for damage from attacks
    //returns amount of damage dealt
    public int hitDamage(int amount) {
        //TODO: add misc. and type damage multipliers
        //armor reduction on exponential curve starting at 1 and approaching MAX_ARMOR_MULTIPLIER
        //intensity of curve handled by ARMOR_SCALAR (bigger = faster curve)
        int damageAmount = (int) Math.round(amount*((1-MAX_ARMOR_MULTIPLIER)/
                                          Math.exp(ARMOR_SCALAR*effArmor) + MAX_ARMOR_MULTIPLIER));
        curHealth -= damageAmount;
        return damageAmount;
    }

    public int attack(Item.Weapon weapon, Mob target) {
        return attack(weapon, target, 1);
    }

    //returns -1 if attack failed
    public int attack(Item.Weapon weapon, Mob target, double hitChance) {
        if(Math.random() < hitChance) {
            int damage = weapon.getDamage();

            switch (weapon.getWeaponType()) {
                case LIGHT:
                    damage += effDexterity;
                    break;
                case HEAVY:
                    damage += effStrength;
                    break;
                case RANGED:
                    damage += effDexterity / 2;
                    break;
            }

            return target.hitDamage(damage);

        } else {
            return -1;
        }
    }

    public boolean checkDead() {
        if(curHealth <= 0) {
            curHealth = 0;
            return true;
        } else {
            return false;
        }
    }

    //resets armor val and calls all armor passives again
    private void reCalculateArmor() {
        effArmor = armor;
        for(Item.Armor armor : equippedArmor) {
            effArmor += armor.getArmorValue();
            armor.passive(this);
        }
    }

    public void equip(Item.Armor armor) {
        equippedArmor.add(armor);
        effArmor += armor.getArmorValue();
        armor.passive(this);
    }

    public void equip(Item.Weapon weapon) {
        equippedWeapons.add(weapon);
        weapon.passive(this);
    }

    public boolean canEquip(Item.Armor.Type armorType) {
        for(Item.Armor armor : equippedArmor) {
            if(armor.getType() == armorType) {
                return false;
            }
        }

        return true;
    }

    public boolean canEquipWeapon() {
        if(equippedWeapons.left == null && equippedWeapons.right == null) {
            return false;
        } else {
            return true;
        }
    }

    //hit chance call for overriding
    public double getHitChance() {
        return 1;
    }

    //call to perform action
    public abstract void doAction(GameClass game);

    //provides a deep copy of mob
    public abstract Mob copy();
}
