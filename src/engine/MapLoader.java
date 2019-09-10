package engine;

import objects.Monster;
import objects.TileEvent;
import org.newdawn.slick.Color;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

//TODO: make more flexible file type for storing maps
//TODO: add binary version of map file type for speed (using Serializable?)
//class for loading in map data
public class MapLoader {

    private HashMap<Character, Map.Tile> tileRegistry = new HashMap<>();

    //returns 2D array of map tiles
    public ArrayList<ArrayList<Map.Tile>> load(String fileName) {
        ArrayList<ArrayList<Map.Tile>> tileMap = new ArrayList<>();
        Path path = Paths.get("src//basicgame//" + fileName);
        try {
            LineNumberReader fileReader = new LineNumberReader(new FileReader(path.toAbsolutePath().toString()));
            char[] lineChars;

            parseHeader(fileReader);

            //map loading: runs until file finished
            String line = fileReader.readLine();

            for (int i = 0; i < line.length(); i++) {
                tileMap.add(new ArrayList<>());
            }

            while (line != null) {
                lineChars = line.toCharArray();
                for (int x = 0; x < line.length(); x++) {
                    tileMap.get(x).add(0, getTile(lineChars[x]));
                }
                line = fileReader.readLine();
            }

            fileReader.close();

        } catch(IOException e) {
            e.printStackTrace();
        } catch(MapParseException e) {
            System.out.println("ERROR: at line " + e.getLineNum());
            e.printStackTrace();
        }

        return tileMap;
    }

    private Map.Tile getTile(char tileChar) {
        switch(tileChar) {
            case 'x':
                return new Map.BasicTile(Color.darkGray, false);
            case 'o':
                return new Map.BasicTile(Color.lightGray);
            default:
                return tileRegistry.getOrDefault(tileChar, new Map.BasicTile(Color.black)).copy();
        }
    }

    //handles the top part of the map file which contains the mapping of chars to specific tile objects
    private void parseHeader(LineNumberReader fileReader) throws MapParseException, IOException {
        String line = fileReader.readLine();

        while(!line.equalsIgnoreCase("map:")) {
            String[] lineSplit = line.split(" ");

            if(lineSplit.length >= 2) {
                switch (lineSplit[1].toLowerCase()) {
                    case "encounter":
                        tileRegistry.put(lineSplit[0].charAt(0), parseEncounter(fileReader));
                        break;
                    case "conversation":
                        tileRegistry.put(lineSplit[0].charAt(0), parseConversation(fileReader));
                        break;
                    case "exit":
                        tileRegistry.put(lineSplit[0].charAt(0), parseExit(fileReader));
                        break;
                    case "entrance":
                        tileRegistry.put(lineSplit[0].charAt(0), parseEntrance(fileReader));
                        break;
                }
            }

            line = fileReader.readLine();
            if(line == null) throw new MapParseException("no map header found", fileReader.getLineNumber());
        }
    }

    //TODO: implement color choice
    //encounter optionally starts with intro: [intro string] and then each line is a mob constructor as below
    private Map.EncounterTile parseEncounter(LineNumberReader fileReader) throws MapParseException, IOException {
        String line = fileReader.readLine();
        String introString = null;
        ArrayList<Mob> mobList = new ArrayList<>();

        if(line.split(" ")[0].equalsIgnoreCase("intro:")) {
            introString = line.substring(line.indexOf(':') + 2);
            line = fileReader.readLine();
        }

        try {
            while (!line.equals("")) {
                String[] lineSplit = line.split(" ");
                switch (lineSplit[0].toLowerCase()) {
                    //TODO: either make this more generic or remember to add parsing for every monster
                    //syntax: goblin: [health] [strength] [dex] [name]
                    case "goblin:":
                        if(lineSplit.length < 5)
                            throw new MapParseException("goblin constructor has too few arguments",
                                                        fileReader.getLineNumber());

                        mobList.add(new Monster.Goblin(Integer.parseInt(lineSplit[1]),
                                Integer.parseInt(lineSplit[2]),
                                Integer.parseInt(lineSplit[3]),
                                lineSplit[4]));
                        break;
                        //syntax: spider: [health] [armor] [name]
                    case "spider:":
                        if(lineSplit.length < 3)
                            throw new MapParseException("spider constructor has too few arguments",
                                    fileReader.getLineNumber());

                        mobList.add(new Monster.Spider(Integer.parseInt(lineSplit[1]),
                                Integer.parseInt(lineSplit[2]),
                                lineSplit[3]));

                }
                line = fileReader.readLine();
            }
        } catch(NumberFormatException e) {
            System.out.println("Exception at line " + fileReader.getLineNumber());
            e.printStackTrace();
        }

        return new Map.EncounterTile(Color.gray, mobList.toArray(new Mob[0]), introString);
    }

    //TODO: implement color choice again
    //conversation is a tree built through nodes as [answer]: [reply] with single spaces used to show tree depth
    //must start with intro: [intro string]
    // '\' is used as a newline character
    private Map.ConversationTile parseConversation(LineNumberReader fileReader) throws MapParseException, IOException {
        String line = fileReader.readLine();
        String[] lineSplit = line.split(" ");
        TileEvent.ConvoTree convoTree;

        if(lineSplit[0].equalsIgnoreCase("intro:")) {
            convoTree = new TileEvent.ConvoTree("",
                    line.substring(line.indexOf(':') + 2).replace('\\', '\n'));
        } else throw new MapParseException("conversation block does not start with 'intro:'",
                                           fileReader.getLineNumber());
        line = fileReader.readLine();

        TileEvent.ConvoTree curNode = convoTree;
        int lastAnsNum = 0;
        while(!line.equals("")) {
            lineSplit = line.split(" ");

            int ansNum;
            //gets first lineSplit index that isn't a space and saves it in ansNum
            for(ansNum = 0; lineSplit[ansNum].equals(""); ansNum++);

            TileEvent.ConvoTree child = new TileEvent.ConvoTree(lineSplit[ansNum].substring(0,
                    lineSplit[ansNum].length() - 1),
                    line.substring(line.indexOf(':') + 2).replace('\\', '\n'));

            if(ansNum == 0) {
                throw new MapParseException("invalid nesting in conversation", fileReader.getLineNumber());
            } else if(ansNum == lastAnsNum) {
                curNode.getParent().setChild(child);
            } else if(ansNum > lastAnsNum) {
                curNode.setChild(child);
            } else if(ansNum < lastAnsNum) {
                for(int i = lastAnsNum; i > ansNum; i--) {
                    curNode = curNode.getParent();
                }
                curNode.getParent().setChild(child);
            }

            lastAnsNum = ansNum;
            curNode = child;
            line = fileReader.readLine();
        }

        return new Map.ConversationTile(Color.yellow, new TileEvent.GenericConversation(convoTree));
    }

    //TODO: implement color choice again again
    //syntax: [name]: [filename]
    private Map.ExitTile parseExit(LineNumberReader fileReader) throws MapParseException, IOException {
        String line = fileReader.readLine();
        String[] lineSplit = line.split(" ");

        if(lineSplit.length != 2) throw new MapParseException("Incorrect syntax for exit", fileReader.getLineNumber());

        return new Map.ExitTile(Color.red, lineSplit[0].substring(0, lineSplit[0].length() - 1), lineSplit[1]);
    }

    //TODO: implement color choice again again again
    //syntax: [name]
    private Map.EntranceTile parseEntrance(LineNumberReader fileReader) throws MapParseException, IOException {
        String line = fileReader.readLine();
        String[] lineSplit = line.split(" ");

        if(lineSplit.length != 1) throw new MapParseException("Incorrect syntax for entrance", fileReader.getLineNumber());

        return new Map.EntranceTile(Color.green, line);
    }

    private class MapParseException extends Exception {
        private int lineNum;

         protected MapParseException(String message, int lineNum) {
             super(message);
             this.lineNum = lineNum;
        }

        private int getLineNum() {
             return lineNum;
        }
    }
}
