package engine;

import objects.Ability;
import objects.Item;
import objects.Monster;

import java.util.Iterator;

public class AdventureCommands {

    static boolean runCommand(String command, GameClass game) {
        String[] commandArray = command.split(" ");
        boolean isSuccess = false;

        //check for current game state
        if(game.gameState == GameClass.GameState.ADVENTURE) {

            //execute correct command and record successful execution
            switch (commandArray[0]) {
                case "move":
                    isSuccess = move(commandArray, game);
                    break;
                case "view":
                    isSuccess = false;
                    view(commandArray, game);
                    break;
                case "equip":
                    isSuccess = false;
                    equip(commandArray, game);
                    game.adventurer.resetStats();
                    game.adventureLayout.updateWindows(game);
                    break;
                case "unequip":
                    isSuccess = false;
                    unequip(commandArray, game);
                    game.adventurer.resetStats();
                    game.adventureLayout.updateWindows(game);
                    break;
                //debug command
                case "_switchState":
                    game.switchState(GameClass.GameState.COMBAT);
                    game.consoleOutput.addLine("DEBUG: switched state to combat");
                    isSuccess = true;
                    break;
                default:
                    game.consoleOutput.addLine("Command not recognized!");
                    isSuccess = false;
            }

            //ran once successful action completed
            if(isSuccess) {
                game.adventureLayout.updateWindows(game);
            }
        } else if(game.gameState == GameClass.GameState.COMBAT){

            switch (commandArray[0]) {
                case "attack":
                    isSuccess = attack(commandArray, game);
                    break;
                case "ability":
                    isSuccess = ability(commandArray, game);
                    break;
                case "item":
                    isSuccess = item(commandArray, game);
                    break;
                case "magic":
                    isSuccess = magic(commandArray, game);
                    break;
                    //debug command
                case "_switchState":
                    game.switchState(GameClass.GameState.ADVENTURE);
                    game.consoleOutput.addLine("DEBUG: switched mode to adventure");
                    isSuccess = true;
                    break;
                    default:
                        game.consoleOutput.addLine("Command not recognized!");
                        isSuccess = false;
            }

            //true once objects action successfully completed
            if(isSuccess) {
                //update game state after command completed
                game.combatLayout.updateWindows(game);
            }
        }
        return isSuccess;
    }

    //all commands return true if a game action has successfully occurred and false otherwise
    //(ie invalid command)

    //adventure commands
    private static void equip(String[] commandArray, GameClass game) {
        Item.ItemInterface item = null;
        if(commandArray.length < 2 || commandArray[1].equalsIgnoreCase("?")) {
            game.consoleOutput.addLine("Syntax: equip [item]");
            return;
        } else if(commandArray.length > 3) {
            game.consoleOutput.addLine("Too many arguments!");
            return;
        }

        for (Item.ItemInterface curItem : game.adventurer.equipment) {
            if (curItem.getName().equalsIgnoreCase(commandArray[1])) {
                item = curItem;
                break;
            }
        }

        if(item == null) {
            game.consoleOutput.addLine("No such item!");
            return;
        }

        if(item.getClass() == Item.Weapon.class) {
            if(commandArray.length == 3 && commandArray[2].equalsIgnoreCase("right")) {
                Item.Weapon curWeapon = game.adventurer.equippedWeapons.getRight();
                if(!curWeapon.getName().equalsIgnoreCase("fist")) {
                    game.adventurer.equipment.add(curWeapon);
                }

                game.consoleOutput.addLine("Equipped " + item.getName() + " to right hand.");
                game.adventurer.equippedWeapons.setRight((Item.Weapon) item);
                game.adventurer.equipment.remove(item);
            }else if(commandArray.length == 3 && commandArray[2].equalsIgnoreCase("left")) {
                Item.Weapon curWeapon = game.adventurer.equippedWeapons.getLeft();
                if(!curWeapon.getName().equalsIgnoreCase("fist")) {
                    game.adventurer.equipment.add(curWeapon);
                }

                game.consoleOutput.addLine("Equipped " + item.getName() + " to left hand.");
                game.adventurer.equippedWeapons.setLeft((Item.Weapon) item);
                game.adventurer.equipment.remove(item);
            } else if(game.adventurer.canEquipWeapon()) {
                game.consoleOutput.addLine("Equipped " + item.getName());
                game.adventurer.equip((Item.Weapon) item);
                game.adventurer.equipment.remove(item);
            } else {
                game.consoleOutput.addLine("Cannot equip " + item.getName() + "!");
            }

        } else if(item.getClass() == Item.Armor.class) {
            Item.Armor itemArmor = (Item.Armor) item;
            if (!game.adventurer.canEquip(itemArmor.getType())) {
                for(Iterator<Item.Armor> armorIter = game.adventurer.equippedArmor.iterator();
                    armorIter.hasNext();) {
                    Item.Armor armor = armorIter.next();
                    if(armor.getType() == itemArmor.getType()) {
                        game.consoleOutput.addLine("Unequipped " + armor.getName() + ".");
                        game.adventurer.equipment.add(armor);
                        armorIter.remove();
                        break;
                    }
                }
            }
            game.consoleOutput.addLine("Equipped " + item.getName() + "!");
            game.adventurer.equip(itemArmor);
            game.adventurer.equipment.remove(item);
        }
    }

    private static void unequip(String[] commandArray, GameClass game) {
        Item.Armor armor = null;
        if(commandArray.length < 2 || commandArray[1].equalsIgnoreCase("?")) {
            game.consoleOutput.addLine("Syntax: unequip [item]");
            return;
        } else if(commandArray.length > 2) {
            game.consoleOutput.addLine("Too many arguments!");
            return;
        }

        for (Item.Armor curArmor : game.adventurer.equippedArmor) {
            if (curArmor.getName().equalsIgnoreCase(commandArray[1])) {
                armor = curArmor;
                break;
            }
        }

        if(armor != null) {
            game.consoleOutput.addLine("Unequipped " + armor.getName() + ".");
            game.adventurer.equipment.add(armor);
            game.adventurer.equippedArmor.remove(armor);
            return;
        }

        if(commandArray[1].equalsIgnoreCase("fist")) {
            game.consoleOutput.addLine("Cannot unequip your own hand!");
            return;
        }

        if(game.adventurer.equippedWeapons.getRight().getName().equalsIgnoreCase(commandArray[1])) {
            game.consoleOutput.addLine("Unequipped " + game.adventurer.equippedWeapons.getRight().getName() + ".");
            game.adventurer.equipment.add(game.adventurer.equippedWeapons.getRight());
            game.adventurer.equippedWeapons.setRight(null);
            return;
        } else if(game.adventurer.equippedWeapons.getLeft().getName().equalsIgnoreCase(commandArray[1])) {
            game.consoleOutput.addLine("Unequipped " + game.adventurer.equippedWeapons.getLeft().getName() + ".");
            game.adventurer.equipment.add(game.adventurer.equippedWeapons.getLeft());
            game.adventurer.equippedWeapons.setLeft(null);
            return;
        }

        game.consoleOutput.addLine("No such item equipped!");
    }

    private static void view(String[] commandArray, GameClass game) {

        if(commandArray.length < 2 || commandArray[1].equalsIgnoreCase("?")) {
            game.consoleOutput.addLine("Syntax: view [item]");
        } else if(commandArray.length > 2) {
            game.consoleOutput.addLine("Too many arguments!");
        } else {
            for(Item.ItemInterface item : game.adventurer.equipment) {
                if(item.getName().equalsIgnoreCase(commandArray[1])) {
                    game.consoleOutput.addMultipleLines(item.getInfo());
                    return;
                }
            }

            for(Item.ItemInterface item : game.adventurer.equippedArmor) {
                if(item.getName().equalsIgnoreCase(commandArray[1])) {
                    game.consoleOutput.addMultipleLines(item.getInfo());
                    return;
                }
            }

            if(game.adventurer.equippedWeapons.getRight().getName().equalsIgnoreCase(commandArray[1])) {
                game.consoleOutput.addMultipleLines(game.adventurer.equippedWeapons.getRight().getInfo());
                return;
            } else if(game.adventurer.equippedWeapons.getLeft().getName() == commandArray[1]) {
                game.consoleOutput.addMultipleLines(game.adventurer.equippedWeapons.getLeft().getInfo());
                return;
            }

            game.consoleOutput.addLine("No such item!");
        }
    }

    private static boolean move(String[] commandArray, GameClass game) {
        int tempX = game.adventurer.getMapX();
        int tempY = game.adventurer.getMapY();

        //check arg number correct
        if(commandArray.length < 2) {
            game.consoleOutput.addLine("Syntax: move [direction]");
            return false;
        } else if(commandArray.length > 2) {
            game.consoleOutput.addLine("Too many arguments!");
            return false;
        }

        switch(commandArray[1].toLowerCase()) {
            case "up":
                tempY += 1;
                break;
            case "down":
                tempY -= 1;
                break;
            case "right":
                tempX += 1;
                break;
            case "left":
                tempX -= 1;
                break;
            default:
                game.consoleOutput.addLine("No such direction!");
                return false;
        }

        if(game.adventureLayout.getMap().canMove(tempX, tempY)) {
            game.consoleOutput.addLine("Moved " + commandArray[1].toLowerCase() + ".");
            game.adventurer.setMapX(tempX);
            game.adventurer.setMapY(tempY);
            return true;
        } else {
            game.consoleOutput.addLine("Cannot move " + commandArray[1].toLowerCase() + ".");
            return false;
        }
    }

    //TODO: add magic command
    //objects commands
    private static boolean attack(String[] commandArray, GameClass game) {
        Mob targetMonster = null;
        Item.Weapon chosenWeapon = null;

        //checks if not enough elemaents input
        if(commandArray.length < 2 || commandArray[1].equals("?")) {
             game.consoleOutput.addLine("Syntax: attack [target] [weapon]");
            return false;
        }

        //finds target referred to (note: can use 1, 2, 3, etc.)
        for(int i = 0; i < Monster.monsterList.size(); i++) {
            Mob monster = Monster.monsterList.get(i);
            if(monster.name.equalsIgnoreCase(commandArray[1]) || commandArray[1].equals(String.valueOf(i + 1))) {
                targetMonster = monster;
                break;
            }
        }

        //cluster-fuck which ends up assigning inputted weapon to chosenWeapon,
        //or the weapon in the right hand by default
        //note: can use "right" or "left" to choose weapon
        if(commandArray.length < 3) {
            chosenWeapon = game.adventurer.equippedWeapons.getRight();
        } else {
            if (game.adventurer.equippedWeapons.getRight().getName().equalsIgnoreCase(commandArray[2])
                    || commandArray[2].equalsIgnoreCase("right")) {
                chosenWeapon = game.adventurer.equippedWeapons.getRight();
            } else if(game.adventurer.equippedWeapons.getLeft().getName().equalsIgnoreCase(commandArray[2])
                    || commandArray[2].equalsIgnoreCase("left")) {
                chosenWeapon = game.adventurer.equippedWeapons.getLeft();
            }
        }

        if(targetMonster == null) {
            game.consoleOutput.addLine("No such monster!");
            return false;
        } else if(chosenWeapon == null) {
            game.consoleOutput.addLine("No such weapon!");
            return false;
        }

        game.consoleOutput.addLine("Attacked " + targetMonster.name + " with " + chosenWeapon.getName() + ".");

        int damage = game.adventurer.attack(chosenWeapon, targetMonster);
        if(damage == -1) {
            game.consoleOutput.addLine("Attack missed!");
        } else {
            game.consoleOutput.addLine("Did " + damage + " damage to " + targetMonster.name + ".");
        }

        return true;
    }

    private static boolean ability(String[] commandArray, GameClass game) {
        Mob target = null;
        Ability.AbilityInterface ability = null;

        if(commandArray.length < 2 || commandArray[1].equals("?")) {
            game.consoleOutput.addLine("Syntax: ability [ability] {target}");
            return false;
        } else if(commandArray.length > 3) {
            game.consoleOutput.addLine("Too many arguments!");
            return false;
        } else if(commandArray.length == 3) {
            for(int i = 0; i < Monster.monsterList.size(); i++) {
                Mob enemy = Monster.monsterList.get(i);
                if (enemy.name.equalsIgnoreCase(commandArray[2]) || commandArray[2].equals(String.valueOf(i + 1))) {
                    target = enemy;
                    break;
                }
            }
        }

        for(Ability.AbilityInterface a : game.adventurer.abilityList) {
            if(commandArray[1].equalsIgnoreCase(a.getName())) {
                ability = a;
                break;
            }
        }

        if(ability == null) {
            game.consoleOutput.addLine("No such ability!");
            return false;
        }

        if (commandArray.length == 3 && commandArray[2].equals("?")) {
            game.consoleOutput.addMultipleLines(ability.getHelp());
            return false;
        } else {
            game.consoleOutput.addMultipleLines(ability.use(game.adventurer, target, game));
            return true;
        }
    }

    private static boolean item(String[] commandArray, GameClass game) {
        Mob target = null;
        Item.ItemInterface item = null;

        if(commandArray.length < 2 || commandArray[1].equals("?")) {
            game.consoleOutput.addLine("Syntax: item [item] {target}");
            return false;
        } else if(commandArray.length > 3) {
            game.consoleOutput.addLine("Too many arguments!");
            return false;
        } else if(commandArray.length == 3) {
            for(int i = 0; i < Monster.monsterList.size(); i++) {
                Mob enemy = Monster.monsterList.get(i);
                if(enemy.name.equalsIgnoreCase(commandArray[2]) || commandArray[2].equals(String.valueOf(i + 1))) {
                    target = enemy;
                    break;
                }
            }

            if(target == null) {
                if(game.adventurer.name.equalsIgnoreCase(commandArray[2])) {
                    target = game.adventurer;
                } else {
                    game.consoleOutput.addLine("Not a valid target!");
                    return false;
                }
            }
        } else {
            target = game.adventurer;
        }

        for(Item.ItemInterface i : game.adventurer.equipment) {
            if(i.getName().equalsIgnoreCase(commandArray[1])) {
                item = i;
                break;
            }
        }

        if(item == null || item.getClass() == Item.Weapon.class || item.getClass() == Item.Armor.class) {
            game.consoleOutput.addLine("Not a valid item!");
            return false;
        } else {
            game.consoleOutput.addMultipleLines(item.use(target, game.adventurer.name));
            if(item.getClass() == Item.Consumable.class) {
                game.adventurer.equipment.remove(item);
            }

            return true;
        }
    }

    private static boolean magic(String[] commandArray, GameClass game) {

        return true;
    }
}
