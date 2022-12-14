import main.aima.core.environment.wumpusworld.WumpusAction;

import java.util.Random;

public class ActionDictionary {
    private static final String[] forward = {"You just need to focus on moving forward. ",
            "Small steps forward are still steps forward. Go. ",
            "Сome forward and stay connected. ",
            "I think you should move forward. ",
            "Continue with your forward movement.",
            "Nevertheless, we will move forward. "
    };
    private static final String[] turn_right = {"I think you should turn right.",
            "Spin around you.. Ok, I'm joke, just turn right. ",
            "Now, reaching the wall, turn right.",
            "Please, just turn right here.",
            "In two centimetres, gently turn right.",
            "And now turn right.",
            "Close your eyes and turn right. So, how was it?"};
    private static final String[] turn_left = {"You can turn left. ",
            "It means \"turn left,\" probably.",
            "If I remember correctly, I think you have to turn left. ",
            "And now turn left. ",
            "To defeat Wumpus, you must act cunningly. Switch the right blinker and turn left.",
            "Close your eyes and turn left. So, how was it?"};
    private static final String[] grab = {"Let's grab what we can and book. ",
            "Grab what you can and grab it fast. ",
            "All right, grab all you can. ",
            "All you have to do is reach out and grab gold.",
            "Which is why I need you to grab it. ",
            "We should grab this gold and get out."
    };
    private static final String[] shoot = {"Literally, all you need to do is point and shoot. ",
            "And do you have just one arrow?! Have you ever heard of the game store?! Shoot like Legolas!",
            "You must shoot Wumpus before he kill you. ",
            "If you don't shoot Wumpus now, I will shoot you both. Joke. "};
    private static final String[] climb = {"We're here to climb this stairway to Heaven. ",
            "So, if you climb higher...",
            "You should climb the rope.",
            "You now need to climb back up the slope."};

    private static String getRandom(String[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }

    public static String getClimb() {
        return getRandom(climb);
    }

    public static String getForward() {
        return getRandom(forward);
    }

    public static String getGrab() {
        return getRandom(grab);
    }

    public static String getShoot() {
        return getRandom(shoot);
    }

    public static String getTurnLeft() {
        return getRandom(turn_left);
    }

    public static String getTurnRight() {
        return getRandom(turn_right);
    }

    public static String getSentence(WumpusAction action) {
        switch (action) {
            case FORWARD:
                return getForward();
            case TURN_LEFT:
                return getTurnLeft();
            case TURN_RIGHT:
                return getTurnRight();
            case GRAB:
                return getGrab();
            case SHOOT:
                return getShoot();
            case CLIMB:
                return getClimb();
            default:
                throw new IllegalArgumentException();
        }
    }

    public static WumpusAction getAction(String act) {
        if (act.toLowerCase().contains("forward")) {
            return WumpusAction.FORWARD;
        } else if (act.toLowerCase().contains("climb")) {
            return WumpusAction.CLIMB;
        } else if (act.toLowerCase().contains("grab")) {
            return WumpusAction.GRAB;
        } else if (act.toLowerCase().contains("shoot")) {
            return WumpusAction.SHOOT;
        } else if (act.toLowerCase().contains("turn left")) {
            return WumpusAction.TURN_LEFT;
        } else if (act.toLowerCase().contains("turn right")) {
            return WumpusAction.TURN_RIGHT;
        } else {
            throw new IllegalArgumentException("Statement: \n" + act + "\n doesn't contain any action.");
        }
    }
}
