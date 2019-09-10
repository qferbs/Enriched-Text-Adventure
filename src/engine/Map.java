package engine;

import objects.Adventurer;
import objects.Monster;
import objects.TileEvent;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Rectangle;

import java.util.ArrayList;

public class Map {
    //for displaying map
    private final static int HORIZONTAL_SPACING = 40;
    private final static int VERTICAL_SPACING = 40;
    private int x;
    private int y;
    private int height;
    private int width;
    private Adventurer adventurer;

    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private boolean mouseStillDown = false;

    private int mapX = 0;
    private int mapY = 0;
    private int mapWidth;
    private int mapHeight;

    private ArrayList<ArrayList<Tile>> tileMap;
    private MapLoader mapLoader = new MapLoader();

    public Map(int x1, int y1, int width, int height, Adventurer adventurer) {
        this(x1, y1, width, height, adventurer, new ArrayList<>());

        //temp test map
        for(int x = 0; x < 50; x++) {
            ArrayList<Tile> tileListX = new ArrayList<>();
            for(int y = 0; y < 50; y++) {
                tileListX.add(y, new BasicTile(Color.white.darker((float) Math.random())));
            }

            tileMap.add(x, tileListX);
        }
    }

    public Map(int x, int y, int width, int height, Adventurer adventurer, String fileName) {
        this(x, y, width, height, adventurer, new ArrayList<>());
        tileMap = mapLoader.load(fileName);
    }

    public Map(int x, int y, int width, int height, Adventurer adventurer, ArrayList<ArrayList<Tile>> tileMap) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.tileMap = tileMap;
        this.adventurer = adventurer;

        mapWidth = this.tileMap.size() * HORIZONTAL_SPACING;
        if(tileMap.size() != 0) {
            mapHeight = this.tileMap.get(0).size() * VERTICAL_SPACING;
        } else {
            mapHeight = 0;
        }
    }

    public void update(GameContainer gc, int delta, Input input) {
        if(input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)
                && (Rectangle.contains(input.getMouseX(), input.getMouseY(), x, y - height, width, height)
                    || mouseStillDown)) {
            mapX += input.getMouseX() - lastMouseX;
            mapY += input.getMouseY() - lastMouseY;
            mouseStillDown = true;
        }

        if(!input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
            mouseStillDown = false;
        }

        if(mapX > 0) {
            mapX = 0;
        } else if(mapX < width - mapWidth && width - mapWidth < 0) {
            mapX = width - mapWidth;
        } else if(width - mapWidth >= 0) {
            mapX = 0;
        }

        if(mapY < 0) {
            mapY = 0;
        } else if(mapY > mapHeight - height && mapHeight - height > 0) {
            mapY = mapHeight - height;
        } else if (mapHeight - height <= 0) {
            mapY = 0;
        }

        lastMouseX = input.getMouseX();
        lastMouseY = input.getMouseY();
    }

    public void render(Graphics g) {
        g.setWorldClip(x, y - height, width, height);
        for(int x1 = 0; x1 < tileMap.size(); x1++) {
            for(int y1 = 0; y1 < tileMap.get(0).size(); y1++) {
                Tile tile = tileMap.get(x1).get(y1);

                if(adventurer.isTileSeen(x1, y1)) {
                    g.setColor(tile.getColor());
                    g.fillRect(x + mapX + x1 * HORIZONTAL_SPACING,
                            y + mapY - (y1 + 1) * VERTICAL_SPACING,
                            HORIZONTAL_SPACING - 2, VERTICAL_SPACING - 2);
                    tile.revealTile();
                } else if(tile.isRevealed()) {
                    g.setColor(tile.getColor().darker((float) 0.7));
                    g.fillRect(x + mapX + x1 * HORIZONTAL_SPACING,
                            y + mapY - (y1 + 1) * VERTICAL_SPACING,
                            HORIZONTAL_SPACING - 2, VERTICAL_SPACING - 2);
                }
            }
        }

        g.setColor(Color.cyan);

        g.fillOval(x + mapX + adventurer.getMapX() * HORIZONTAL_SPACING + 2,
                y + mapY - (adventurer.getMapY() + 1) * VERTICAL_SPACING + 2,
                HORIZONTAL_SPACING - 6, VERTICAL_SPACING - 6, 24);

        g.clearWorldClip();
    }

    //returns true if tile passable
    public boolean canMove(int x, int y) {

        //check if space in range
        if(x >= tileMap.size() || x < 0 || y >= tileMap.get(0).size() || y < 0) {
            return false;
        } else if(!tileMap.get(x).get(y).isPassable()){
            return false;
        } else {
            return true;
        }
    }

    public void change(String entranceName, String filename) {
        mapX = 0;
        mapY = 0;
        tileMap = mapLoader.load(filename);
        mapWidth = this.tileMap.size() * HORIZONTAL_SPACING;
        mapHeight = this.tileMap.get(0).size() * VERTICAL_SPACING;

        //TODO: implement default behaviour in case the entrance cannot be found
        for(int x = 0; x < tileMap.size(); x++) {
            for(int y = 0; y < tileMap.get(0).size(); y++) {
                Tile tile = tileMap.get(x).get(y);
                if(tile.getClass() == EntranceTile.class) {
                    EntranceTile entrance = (EntranceTile) tile;
                    if(entrance.getName().equals(entranceName)) {
                        adventurer.setMapX(x);
                        adventurer.setMapY(y);
                        break;
                    }
                }
            }
        }
    }

    public TileEventState getTileEventState() {
        return new TileEventState();
    }

    public interface Tile {
        //TODO: replace with sprites
        Color getColor();

        //gets what is on the tile
        TileEvent.Type getEventType();

        //returns encounter attached to this tile or null if not of encounter type
        TileEvent.Encounter getEncounter();

        //returns conversation attached to this tile or null if not of conversation type
        TileEvent.Conversation getConversation();

        //returns true if tile is passable
        boolean isPassable();

        //sets tile's status to revealed (ie has already been seen)
        void revealTile();

        //returns true if tile has been revealed
        boolean isRevealed();

        //returns deep copy of tile
        Tile copy();
    }

    public static class BasicTile implements Tile {
        //temp display
        private Color color;
        private boolean passable;
        private boolean isRevealed = false;

        public BasicTile(Color color) {
            this(color, true);
        }

        public BasicTile(Color color, boolean passable) {
            this.color = color;
            this.passable = passable;
        }

        @Override
        public Color getColor() {
            return color;
        }

        @Override
        public TileEvent.Type getEventType() {
            return TileEvent.Type.NONE;
        }

        @Override
        public TileEvent.Encounter getEncounter() {
            return null;
        }

        @Override
        public TileEvent.Conversation getConversation() {
            return null;
        }

        @Override
        public boolean isPassable() {
            return passable;
        }

        @Override
        public void revealTile() {
            isRevealed = true;
        }

        @Override
        public boolean isRevealed() {
            return isRevealed;
        }

        @Override
        public Tile copy() {
            return new BasicTile(color, passable);
        }
    }

    public static class EncounterTile implements Tile {
        private  Color color;
        private TileEvent.Encounter encounter;
        private boolean isRevealed = false;
        private boolean isCompleted = false;

        public EncounterTile(Color color, Mob[] mobList, String introString) {
            this.color = color;
            this.encounter = new TileEvent.Encounter(mobList, introString);
        }

        @Override
        public Color getColor() {
            return color;
        }

        @Override
        public TileEvent.Type getEventType() {
            if(!isCompleted) {
                isCompleted = true;
                return TileEvent.Type.ENCOUNTER;
            } else return TileEvent.Type.NONE;
        }

        @Override
        public TileEvent.Encounter getEncounter() {
            return encounter;
        }

        @Override
        public TileEvent.Conversation getConversation() {
            return null;
        }

        @Override
        public boolean isPassable() {
            return true;
        }

        @Override
        public void revealTile() {
            isRevealed = true;
        }

        @Override
        public boolean isRevealed() {
            return isRevealed;
        }

        @Override
        public Tile copy() {
            Mob[] mobList = new Mob[encounter.getMobList().length];
            Mob[] oldMobList = encounter.getMobList();

            for(int i = 0; i < mobList.length; i++) {
                mobList[i] = oldMobList[i].copy();
            }

            return new EncounterTile(color, mobList, encounter.getIntroString());
        }
    }

    public static class ConversationTile implements Tile {
        private Color color;
        private TileEvent.Conversation conversation;
        private boolean isRevealed = false;
        private boolean isFinished =false;

        public ConversationTile(Color color, TileEvent.Conversation conversation) {
            this.color = color;
            this.conversation = conversation;
        }

        @Override
        public Color getColor() {
            return color;
        }

        @Override
        public TileEvent.Type getEventType() {
            if(!isFinished) {
                isFinished = true;
                return TileEvent.Type.CONVERSATION;
            } else return TileEvent.Type.NONE;
        }

        @Override
        public TileEvent.Encounter getEncounter() {
            return null;
        }

        @Override
        public TileEvent.Conversation getConversation() {
            return conversation;
        }

        @Override
        public boolean isPassable() {
            return true;
        }

        @Override
        public void revealTile() {
            isRevealed = true;
        }

        @Override
        public boolean isRevealed() {
            return isRevealed;
        }

        @Override
        public Tile copy() {
            return new ConversationTile(color, conversation.copy());
        }
    }

    public static class ExitTile implements Tile {

        private Color color;
        private String name;
        private String filename;
        private boolean isRevealed = false;

        public ExitTile(Color color, String name, String filename) {
            this.color = color;
            this.filename = filename;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getMapFile() {
            return filename;
        }

        @Override
        public Color getColor() {
            return color;
        }

        @Override
        public TileEvent.Type getEventType() {
            return TileEvent.Type.NONE;
        }

        @Override
        public TileEvent.Encounter getEncounter() {
            return null;
        }

        @Override
        public TileEvent.Conversation getConversation() {
            return null;
        }

        @Override
        public boolean isPassable() {
            return true;
        }

        @Override
        public void revealTile() {
            isRevealed = true;
        }

        @Override
        public boolean isRevealed() {
            return isRevealed;
        }

        @Override
        public Tile copy() {
            return new ExitTile(color, name, filename);
        }
    }

    public static class EntranceTile implements Tile {

        private Color color;
        private String name;
        private boolean isRevealed = false;

        public EntranceTile(Color color, String name) {
            this.color = color;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public Color getColor() {
            return color;
        }

        @Override
        public TileEvent.Type getEventType() {
            return TileEvent.Type.NONE;
        }

        @Override
        public TileEvent.Encounter getEncounter() {
            return null;
        }

        @Override
        public TileEvent.Conversation getConversation() {
            return null;
        }

        @Override
        public boolean isPassable() {
            return true;
        }

        @Override
        public void revealTile() {
            isRevealed = true;
        }

        @Override
        public boolean isRevealed() {
            return isRevealed;
        }

        @Override
        public Tile copy() {
            return new EntranceTile(color, name);
        }
    }

    public class TileEventState implements StateHandler.State {
        @Override
        public void start(GameClass game) {
            Tile curTile = tileMap.get(adventurer.getMapX()).get(adventurer.getMapY());
            switch (curTile.getEventType()) {
                case ENCOUNTER:
                    TileEvent.Encounter encounter = curTile.getEncounter();
                    game.consoleOutput.addLine(encounter.getIntroString());
                    for(Mob mob : encounter.getMobList()) {
                        game.consoleOutput.addLine(mob.name + " appeared!");
                        Monster.monsterList.add(mob);
                    }
                    //NOTE: switchState MUST be called last to guarantee that this state finishes
                    game.switchState(GameClass.GameState.COMBAT);
                    break;
                case CONVERSATION:
                    TileEvent.Conversation conversation = curTile.getConversation();
                    game.consoleOutput.addLine(conversation.getIntroString());
                    game.console.addConversation(conversation);
                    game.switchState(GameClass.GameState.CONVERSATION);
                    break;
                case NONE:
                    if(curTile.getClass() == ExitTile.class) {
                        ExitTile exitTile = (ExitTile) curTile;
                        game.adventureLayout.getMap().change(exitTile.getName(), exitTile.getMapFile());
                    } else {
                        game.consoleOutput.addLine("nothin here chief.");
                    }
                    break;
            }
        }

        @Override
        public boolean isComplete() {
            return true;
        }

        @Override
        public void end() {

        }
    }
}
