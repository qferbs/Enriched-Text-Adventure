package engine;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Rectangle;

import java.util.ArrayList;

public class BasicWindow {
    private static final int TEXT_SPACING = 20;
    private static final int FONT_WIDTH = 9;

    private final String name;

    private int x;
    private int y;
    private int width;
    private int height;
    private Color color;
    private ArrayList<String> history;
    private boolean canScroll;

    private int scrollAmount = 0;

    public BasicWindow(int x, int y, int width, int height, Color color) {
        this(x, y, width, height, color, "", false);
    }

    public BasicWindow(int x, int y, int width, int height, Color color, String name) {
        this(x, y, width, height, color, name, false);
    }

    public BasicWindow(int x, int y, int width, int height, Color color, String name, boolean canScroll) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        history = new ArrayList<>();
        this.name = name;
        this.canScroll = canScroll;
    }

    public void render(Graphics g) {

        //restrict history viewport
        g.setWorldClip(x, y - height + TEXT_SPACING, width, height);

        //render history lines
        g.setColor(Color.white);
        for(int i = 0; i < history.size(); i++) {
            if(history.get(i) != null) {
                g.drawString(history.get(i), x, y - (i)*TEXT_SPACING + scrollAmount);
            } else {
                g.drawString("NULL STRING AT LINE " + i + " IN " + this.toString(),
                        x, y - (i)*TEXT_SPACING + scrollAmount);
            }
        }

        g.clearWorldClip();

        //draw bounding box
        g.setColor(color);
        g.drawRoundRect(x - 3, y - height + TEXT_SPACING - 3,
                width + 6, height + 6, 3);

        //render title
        if(!name.equals("")) {
            int nameWidth = name.length()*FONT_WIDTH;
            int x1 = x + width/2 - nameWidth/2;

            g.setColor(Color.black);
            g.fillRect(x1 - 4, y - height + TEXT_SPACING/2 - 3, nameWidth + 8, 22);

            g.setColor(color);
            g.drawRect(x1 - 5, y - height + TEXT_SPACING/2 - 4, nameWidth + 10, 24);

            g.setColor(Color.white);
            g.drawString(name, x1, y - height + TEXT_SPACING/2);
        }
    }

    public void mouseWheelMoved(int change, Input input) {
        int mouseX = input.getMouseX();
        int mouseY = input.getMouseY();

        if(Rectangle.contains(mouseX, mouseY, x, y - height + TEXT_SPACING, width, height) && canScroll) {
            int linesHeight = history.size()*TEXT_SPACING;
            if(scrollAmount + change >= 0 && scrollAmount + change <= linesHeight - height) {
                scrollAmount += change;
            }
        }
    }

    //this is technically art
    public void addLine(String line) {
        String[] lineSplit = line.split("\n");
        if(lineSplit.length > 1) {
            addMultipleLines(lineSplit);
        } else {
            history.add(0, line);
        }

        scrollAmount = 0;
    }

    public void addMultipleLines(String[] lines) {
        for(String line : lines) {
            addLine(line);
        }
    }

    //returns line at index or last line in history
    public String getLine(int index) {
        if(history.size() > index && history.size() > 0 && index >= 0) {
            return history.get(index);
        } else if(history.size() > 0 && index >= 0) {
            return history.get(history.size() - 1);
        } else {
            return "";
        }
    }

    public int getNumLines() {
        return history.size();
    }

    public void clear() {
        history.clear();
    }

    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
    }
}