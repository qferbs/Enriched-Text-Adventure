package objects;

import engine.Mob;

import java.util.Iterator;

public class Modifiers {

    public enum Stat {
        STRENGTH,
        DEXTERITY,
        ARMOR,
        SPEED,
        MAGIC,
        HEALTH,
        MAX_HEALTH
    }

    public interface StatusEffect {
        //called on first application of effect and augments stats
        //returns string to write to console
        String activate(Mob target);

        //called on tick over of effect
        //returns string to write to console
        String tick(Mob target, Iterator<StatusEffect> iter);

        //reverts any stats changed by the effect
        void remove(Mob target);

        //returns name of effect or null if no name exists
        String getName();

        //resets effect (eg reset duration)
        void reset();

        //returns how many turns left until the effect wears off and 0 if infinite
        int getTurnsLeft();

        //returns a copy of the effect
        StatusEffect copy();

        //returns string which explains status effect for printing
        String[] getInfo();
    }

    public static class Buff implements StatusEffect {
        private final String name;
        private final Stat stat;
        private final int amount;
        private final int duration;
        private int age;

        //for copying buffs
        public Buff(Buff source) {
            this(source.stat, source.amount, source.duration, source.name);
        }

        public Buff(Stat stat, int amount, int duration, String name) {
            this.stat = stat;
            this.amount = amount;
            this.duration = duration;
            this.name = name;
            this.age = 0;
        }

        @Override
        public String activate(Mob target) {
            StatusEffect dupeEffect = null;
            for(StatusEffect effect : target.statusEffects) {
                if (effect.getName() != null && effect.getName() == name) {
                    dupeEffect = effect;
                }
            }

            if((dupeEffect != null)) {
                if((dupeEffect.getTurnsLeft() != 0)
                        && ((dupeEffect.getTurnsLeft() < this.getTurnsLeft())
                            || (this.getTurnsLeft() == 0))) {
                    target.removeStatusEffect(dupeEffect);
                } else {
                    return name + " already applied to " + target.name;
                }
            }

            target.addStat(stat, amount);
            target.statusEffects.add(this.copy());

            return name + " applied to " + target.name + ".";
        }

        @Override
        public String tick(Mob target, Iterator<StatusEffect> iter) {
            if(getTurnsLeft() == 1) {
                this.remove(target);
                iter.remove();
                return name + " has worn off of " + target.name + ".";
            } else {
                age += 1;
                return null;
            }
        }

        @Override
        public void remove(Mob target) {
            target.addStat(stat, -amount);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void reset() {
            age = 0;
        }

        @Override
        public int getTurnsLeft() {
            if(duration == 0) {
                return 0;
            } else {
                return duration + 1 - age;
            }
        }

        public StatusEffect copy() {
            return new Buff(this);
        }

        public String[] getInfo() {
            return new String[] {
                    name + ":",
                    "gives target " + amount + " " + stat + " for " + duration + " turns."
            };
        }
    }
}
