package engine;

import objects.TileEvent;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

public class Console {

    private enum Direction {
        UP,
        DOWN
    }

    private final static int FONT_HEIGHT = 16;
    private final static int FONT_SPACING = 9;
    private final static int DELETE_WAIT_TIME = 500;
    private final static boolean HISTORY_ENABLED = false;

    private int consoleWidth;
    private final int historyHeight;
    private Pointer pointer;
    private BasicWindow history;
    private String inputString;
    private GameClass gameClass;
    private Color color;
    private boolean canInput = false;
    private boolean commandSuccessful = false;

    private TileEvent.Conversation conversation = null;

    private int x;
    private int y;

    private int historyPos = -1;
    private int deleteTime = 0;

    public Console (int x, int y, int consoleWidth, int historyHeight, Color color, GameClass gameClass) {
        this.x = x;
        this.y = y;
        this.consoleWidth = consoleWidth;
        this.historyHeight = historyHeight;
        this.color = color;
        this.gameClass = gameClass;
        inputString = "";
        history = new BasicWindow(x, y - FONT_HEIGHT - 15, consoleWidth,
                historyHeight, color, "", true);
        pointer = new Pointer(0, x, y, FONT_SPACING, 20,
                              FONT_SPACING, -2, Color.cyan, Color.darkGray);
    }

    public void update(GameContainer gc, int delta) {
        if(gc.getInput().isKeyDown(Input.KEY_BACK) && pointer.getPosition() != 0) {
            if(deleteTime >= DELETE_WAIT_TIME) {
                deleteTime = 0;
                inputString = inputString.substring(pointer.getPosition());
                pointer.setPosition(0);
            } else {
                deleteTime += delta;
            }
        } else {
            deleteTime = 0;
        }
    }

    public void render(Graphics g) {
        //restrict input viewport
        g.setWorldClip(x, y - 5, consoleWidth, FONT_HEIGHT + 10);

        //render input string
        g.setColor(Color.white);
        g.drawString(inputString, x, y);

        //render pointer
        pointer.render(g, inputString);

        g.clearWorldClip();

        //draw bounding box
        g.setColor(color);
        g.drawRoundRect(x - 3, y - 6,
                consoleWidth + 6, FONT_HEIGHT + 12, 3);

        //render history
        if(HISTORY_ENABLED) {
            history.render(g);
        }
    }

    public boolean keyPressed(int key, char c) {
        if(c != 0) {
            switch (c) {
                case '\r':
                    enterLine();
                    break;
                case '\b':
                    delete();
                    break;
                default:
                    inputChar(c);
            }
        } else {
            switch(key) {
                case Input.KEY_LEFT:
                    pointer.left();
                    break;
                case Input.KEY_RIGHT:
                    pointer.right(inputString);
                    break;
                case Input.KEY_UP:
                    scrollHistory(Direction.UP);
                    break;
                case Input.KEY_DOWN:
                    scrollHistory(Direction.DOWN);
                    break;
            }
        }

        return true;
    }

    public boolean MouseWheelMoved(int change, Input input) {
        history.mouseWheelMoved(change, input);
        return false;
    }

    private void inputChar(char c) {
        int pos = pointer.getPosition();
        if(pos < inputString.length()) {
            inputString = inputString.substring(0, pos) + c + inputString.substring(pos);
        } else {
            inputString += c;
        }
        pointer.right(inputString);
    }

    private void delete() {
        int pos = pointer.getPosition();

        if(inputString.length() > 0 && pos != 0) {

            if(pos < inputString.length()) {
                inputString = inputString.substring(0, pos - 1) + inputString.substring(pos);
            } else {
                inputString = inputString.substring(0, inputString.length()- 1);
            }
            pointer.left();
        }
    }

    private void enterLine() {
        if(canInput) {
            history.addLine(inputString);

            if(conversation != null) {
                gameClass.consoleOutput.addLine(conversation.getReply(inputString));
                if(conversation.isFinished()) {
                    conversation = null;
                    gameClass.switchState(GameClass.GameState.ADVENTURE);
                }
            } else {
                commandSuccessful = AdventureCommands.runCommand(inputString, gameClass);
            }

            inputString = "";
            pointer.setPosition(0);
            historyPos = -1;
        }
    }

    private void scrollHistory(Direction dir) {
        if(dir == Direction.UP && historyPos < history.getNumLines() - 1) {
            historyPos += 1;
        } else if(dir == Direction.DOWN && historyPos > 0) {
            historyPos -= 1;
        }

        if(historyPos >= 0) {
            inputLine(history.getLine(historyPos));
        }
    }

    public void inputLine(String inputString) {
        this.inputString = inputString;
        pointer.setPosition(inputString.length());
    }

    public StateHandler.State getInputState() {
        return new InputState();
    }

    public void move(int x, int y) {
        this.x = x;
        this.y = y;
        pointer.move(x, y);
    }

    public void move(int x, int y, int consoleWidth) {
        this.x = x;
        this.y = y;
        pointer.move(x, y);
        this.consoleWidth = consoleWidth;
    }

    public void addConversation(TileEvent.Conversation conversation) {
        this.conversation = conversation;
    }

    //class for console's pointer
    private class Pointer {

        private int position;
        private int x;
        private int y;
        private int height;
        private int width;
        private int fontSpacing;
        private int yOffset;
        private int blinkCounter = 0;
        private Color color;
        private Color charColor;

        public Pointer(int position, int x, int y, int width, int height,
                       int fontSpacing, int yOffset, Color color, Color charColor) {
            this.position = position;
            this.x = x;
            this.y = y;
            this.height = height;
            this.width = width;
            this.fontSpacing = fontSpacing;
            this.yOffset = yOffset;
            this.color = color;
            this.charColor = charColor;
        }

        public int getPosition() {
            return position;
        }

        public void render(Graphics g, String input) {
            if(blinkCounter > 30) {
                g.setColor(color);
                g.fillRect(x, y + yOffset, width, height);

                if (input.length() > 0 && position < input.length()) {
                    g.setColor(charColor);
                    g.drawString(input.substring(position, position + 1), x, y);
                }
            }

            blinkCounter++;

            if(blinkCounter >= 60)
                blinkCounter = 0;
        }

        public void left() {
            if(position > 0) {
                x -= fontSpacing;
                position -= 1;
            }
        }

        public void right(String input) {
            if(input == null || position < input.length()) {
                x += fontSpacing;
                position += 1;
            }
        }

        public void setPosition(int newPosition) {
            if(newPosition > position) {
                for(int i = position; i < newPosition; i++) {
                    right(null);
                }
            } else if(newPosition < position) {
                for(int i = position; i > newPosition; i--) {
                    left();
                }
            }

            position = newPosition;
        }

        public void move(int x, int y) {
            this.x = x;
            this.y = y;
            position = 0;
        }
    }

    public class InputState implements StateHandler.State {

        public InputState() {}

        @Override
        public void start(GameClass game) {
            canInput = true;
            commandSuccessful = false;
        }

        @Override
        public boolean isComplete() {
            return commandSuccessful;
        }

        @Override
        public void end() {
            canInput = false;
            commandSuccessful = false;
        }
    }
}
