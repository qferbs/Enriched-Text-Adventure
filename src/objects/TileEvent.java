package objects;

import engine.Mob;

import java.util.ArrayList;

public class TileEvent {
    public enum Type {
        NONE,
        ENCOUNTER,
        CONVERSATION
    }

    public static class Encounter {
        private String introString;
        private final Mob[] mobList;
        public Encounter(Mob[] mobList, String introString) {
            this.mobList = mobList;
            this.introString = introString;
        }

        public Mob[] getMobList() {
            return mobList;
        }

        public String getIntroString() {
            return introString;
        }
    }

    public interface Conversation {
        //returns introduction string to be first displayed
        String getIntroString();

        //returns string replying to the input of the player
        String getReply(String answer);

        //returns true if conversation completed
        boolean isFinished();

        //returns a copy of conversation
        Conversation copy();
    }

    public static class TestConversation implements Conversation {
        boolean finished = false;
        private int state = 0;
        @Override
        public String getIntroString() {
            return "This is the intro string";
        }

        @Override
        public String getReply(String answer) {
            switch(state) {
                case 0:
                    state = 1;
                    return "reply 1.";
                case 1:
                    if(answer.equals("1")) {
                        state = 2;
                        return "reply 2.";
                    } else {
                        return "reply 1.";
                    }
                case 2:
                    if(answer.equals("2")) {
                        finished = true;
                        return "done.";
                    } else {
                        return "reply 2.";
                    }
                default:
                    return "state done died...";
            }
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        //NOTE: using this will cause a crash
        @Override
        public Conversation copy() {
            return null;
        }
    }

    public static class ConvoTree {
        private ConvoTree parent = null;
        private ArrayList<ConvoTree> children = new ArrayList<>();
        private String reply;
        private String answer;

        public ConvoTree(String answer, String reply) {
            this(answer, reply, new ConvoTree[0]);
        }

        //null reply string means that the convoTree ends
        public ConvoTree(String answer, String reply, ConvoTree[] children) {
            this.reply = reply;
            this.answer = answer;
            for(ConvoTree child : children) {
                setChild(child);
            }
        }

        //returns the ConvoTree with matching answer or null
        public ConvoTree getChild(String answer) {
            for(ConvoTree tree : children) {
                if(tree.getAnswer().equalsIgnoreCase(answer)) {
                    return tree;
                }
            }

            return null;
        }

        public String getReply() {
            return reply;
        }

        public ConvoTree getParent() {
            return parent;
        }

        public boolean isFinished() {
            return children.isEmpty();
        }

        public void setChild(ConvoTree child) {
            children.add(child);
            child.setParent(this);
        }

        private void setParent(ConvoTree parent) {
            this.parent = parent;
        }

        private String getAnswer() {
            return answer;
        }


    }

    public static class GenericConversation implements Conversation {

        private String introString;
        private ConvoTree convoTree;
        private ConvoTree originalConvoTree;

        //convoTree's top level node's reply is the intro string
        public GenericConversation(ConvoTree convoTree) {
            this.introString = convoTree.getReply();
            this.convoTree = convoTree;
            originalConvoTree = convoTree;
        }

        @Override
        public String getIntroString() {
            return introString;
        }

        @Override
        public String getReply(String answer) {
            ConvoTree nextTree = convoTree.getChild(answer);
            if(nextTree != null) {
                convoTree = nextTree;
            }

            return convoTree.getReply();
        }

        @Override
        public boolean isFinished() {
            return convoTree.isFinished();
        }

        public Conversation copy() {
            return new GenericConversation(originalConvoTree);
        }
    }
}
