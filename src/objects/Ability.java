package objects;

import engine.GameClass;
import engine.Mob;

public class Ability {
    public interface AbilityInterface {
        //returns null if target list invalid, otherwise returns ability usage print-out
        String[] use(Mob user, Mob target, GameClass game);

        //returns ability name
        String getName();

        //returns description of ability to be printed
        String[] getHelp();
    }

    public static class Slam implements AbilityInterface{
        public Slam() {

        }

        public String[] use(Mob user, Mob target, GameClass game) {
            String[] output = new String[Monster.monsterList.size() + 1];
            output[0] = "Used Slam!";

            for(int i = 0; i < Monster.monsterList.size(); i++) {
                output[i + 1] = Monster.monsterList.get(i).name
                                + " took "
                                + Monster.monsterList.get(i).hitDamage(user.getStat(Modifiers.Stat.STRENGTH))
                                + " damage.";
            }

            return output;
        }

        public String getName() {
            return "Slam";
        }

        public String[] getHelp() {
            return new String[] {"Slam: Hits all enemies for damage equal to Strength stat."};
        }
    }

    public static class GoblinBuff implements AbilityInterface {
        @Override
        public String[] use(Mob user, Mob target, GameClass game) {
            user.addStatusEffect(new Modifiers.Buff(Modifiers.Stat.ARMOR, 10, 2, "Harden"));
            return new String[] {user.name + " used Goblin Buff.", user.name + "'s skin hardened!"};
        }

        @Override
        public String getName() {
            return "Goblin Buff";
        }

        @Override
        public String[] getHelp() {
            return new String[] {"gobble gobble gobble"};
        }
    }

    public static class SpiderWeb implements AbilityInterface {
        @Override
        public String[] use(Mob user, Mob target, GameClass game) {
            target.addStatusEffect(new Modifiers.Buff(Modifiers.Stat.SPEED, -5, 3, "Webbed"));
            return new String[] {user.name + " shot webs at " + target.name + "!", target.name + " is now slowed!"};
        }

        @Override
        public String getName() {
            return "Spider Web";
        }

        @Override
        public String[] getHelp() {
            return new String[] {"spidy boi"};
        }
    }
}