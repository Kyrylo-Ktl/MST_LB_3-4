import main.aima.core.environment.wumpusworld.WumpusPercept;

import java.util.Random;

public class PerceptDictionary {
    private static final String[] stench = {"I could smell the stench.",
            "The smell of hatred, the stench of anger permeates me.",
            "Fie! The stench of rotting fish here..",
            "I won't have the stench of it near me a second longer. What should I do?",
            "Again this stench. ",
            "The stench has become unbearable. "};
    private static final String[] breeze = {"I feel breeze here. ",
            "There's a breeze coming up.", "It’s a cool breeze here. ",
            "Did someone just feel a cold breeze? ",
            "I'd say there's a breeze picking up. ",
            "A chilly breeze started blowing. "};
    private static final String[] glitter = {"Gold and glitter in my eyes!",
            "Don't go for the glitter.. But maybe?",
            "My eyes are dazzled by so much glitter.",
            "It seems like it's a lot of glitter.",
            "You don't need glitter to sparkle, my dear GOLD!! ",
            "Glitter in the air.."};
    private static final String[] bump = {"It was just a little bump.",
            "I just got a pretty good bump on the noggin.",
            "I bumped into a wall, I think...",
            "I didn't get over my... bumps in the road.",
            "I am the one who goes bump in the night.. =/",
            "Bump into the wall in three, two..."};
    private static final String[] scream = {"I got a shot, he screamed like a banshee!",
            "I heard monster's scream.", "Wumpus scream like a little girl.",
            "Now no one more would hear you scream, Wumpus.",
            "*sings* I scream, you scream, we all scream for ice cream.."};
    private static final String[] nothing = {"I feel nothing here. Maybe I'm lost?",
            "I just don't feel anything anymore. ",
            "I kind of feel nothing, but it feels so good. ",
            "I can't feel anything, it's fine. "};

    private static String getRandom(String[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }

    public static String getBreeze() {
        return getRandom(breeze);
    }

    public static String getBump() {
        return getRandom(bump);
    }

    public static String getGlitter() {
        return getRandom(glitter);
    }

    public static String getScream() {
        return getRandom(scream);
    }

    public static String getStench() {
        return getRandom(stench);
    }

    public static String getNothing() {
        return getRandom(nothing);
    }

    public static String getSentence(String percepts) {
        if (percepts.equals("{}")) {
            return getNothing();
        }
        String[] feels = percepts.replace("{", "").replace("}", "").replace(" ", "").split(",");

        StringBuilder natlang = new StringBuilder();
        for (String feel : feels) {
            if (feel.equalsIgnoreCase("Stench")) {
                natlang.append(getStench());
            } else if (feel.equalsIgnoreCase("Breeze")) {
                natlang.append(getBreeze());
            } else if (feel.equalsIgnoreCase("Glitter")) {
                natlang.append(getGlitter());
            } else if (feel.equalsIgnoreCase("Bump")) {
                natlang.append(getBump());
            } else if (feel.equalsIgnoreCase("Scream")) {
                natlang.append(getScream());
            }
        }
        return natlang.toString();
    }

    public static WumpusPercept getPercept(String natlang) {
        WumpusPercept percept = new WumpusPercept();
        if (natlang.toLowerCase().contains("nothing") ||
                natlang.toLowerCase().contains("anything")) {
            return percept;
        }
        if (natlang.toLowerCase().contains("stench")) {
            percept.setStench();
        }
        if (natlang.toLowerCase().contains("breeze")) {
            percept.setBreeze();
        }
        if (natlang.toLowerCase().contains("glitter")) {
            percept.setGlitter();
        }
        if (natlang.toLowerCase().contains("bump")) {
            percept.setBump();
        }
        if (natlang.toLowerCase().contains("scream")) {
            percept.setScream();
        }
        return percept;
    }
}
