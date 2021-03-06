package script.tutorial_island;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.runetek.providers.RSTileDecor;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Script;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public abstract class TutorialSection extends Task {

    private final String INSTRUCTOR_NAME;

    public TutorialSection(final String INSTRUCTOR_NAME) {
        this.INSTRUCTOR_NAME = INSTRUCTOR_NAME;
        //daxWalker = new DaxWalker(new Server("sub_DPjXXzL5DeSiPf", "PUBLIC-KEY"));
    }

    //public abstract void onLoop() throws InterruptedException;

    protected final int getProgress() {
        return Varps.get(281);
    }

    public int getTutorialSection() {
        return Varps.get(406);
    }

    protected final boolean talkToInstructor() {
        Npc i = getInstructor();
        if (i != null && i.isPositionInteractable() && i.interact("Talk-to")) {
            Time.sleepUntil(this::pendingContinue, 2000, 6000);
        } else if (i != null) {
            Log.info("Walking to instructor");
            Movement.walkTo(i.getPosition().randomize(4));
        } else {
            //Movement.walkTo(Players.getLocal().getPosition().randomize(6));
            Log.severe("Cant Find Instructor: Section " + getTutorialSection() + " Progress " + getProgress());
            findInstructorFallback();
            return false;
        }
        return true;
    }

    Npc getInstructor() {
        return Npcs.getNearest(INSTRUCTOR_NAME);
    }

    protected boolean pendingContinue() {
        InterfaceComponent wierdContinue = Interfaces.getComponent(162, 44);
        if (wierdContinue != null && wierdContinue.isVisible() && !wierdContinue.isExplicitlyHidden()) {
            String msg = wierdContinue.getText().toLowerCase();
            if (msg.contains("someone") || msg.contains("reach") || msg.contains("already")) {
                Game.getClient().fireScriptEvent(299, 1, 1);
                randWalker(Players.getLocal().getPosition());
                return false;
            }
        }

        return Dialog.isOpen() && Dialog.canContinue();
    }

    protected boolean selectContinue() {
        return Dialog.processContinue();
    }

    void randWalker(Position posRequired) {
        Log.info("Walking to position");
        while (!Players.getLocal().getPosition().equals(posRequired) && !TutorialIsland.getInstance(null).isStopping() && Game.isLoggedIn()) {
            if (!Time.sleepUntil(() -> Players.getLocal().getPosition().equals(posRequired), Random.low(600, 1800))) {
                Movement.walkTo(posRequired);
            }
        }
        if (posRequired.distance(Players.getLocal()) <= 3) {
            int times = Script.randInt(1, 2);
            Log.info("Random walking " + times + " time(s)");
            for (int i = 0; i < times; i++) {
                //Movement.walkToRandomized(Players.getLocal().getPosition().randomize(8));
                getEmptyPosition(false, Script.randInt(1, 9), false).ifPresent(Movement::walkTo);
                Time.sleepUntil(() -> Players.getLocal().isMoving(), Script.randInt(800, 1500));
                Time.sleepUntil(() -> !Players.getLocal().isMoving(), 600, Script.randInt(2000, 4000));
            }
        }
    }

    Optional<Position> getEmptyPosition(boolean min, int distance, boolean removeNonInteractable) {
        List<Position> allPositions = Area.surrounding(Players.getLocal().getPosition(), distance).getTiles();

        // Remove any position with an object (except ground decorations, as they can be walked on)
        for (SceneObject object : SceneObjects.getLoaded()) {
            if (object.getProvider() instanceof RSTileDecor) {
                continue;
            }
            allPositions.removeIf(position -> object.getPosition().equals(position));
        }

        allPositions.removeIf(position -> !position.isPositionWalkable() || !Movement.isWalkable(position, false));
        allPositions.removeIf(position -> position.distance(Players.getLocal()) <= 0);
        if (removeNonInteractable) {
            allPositions.removeIf(position -> Movement.isInteractable(position, false));
        }

        if (min) {
            return allPositions.stream().min(Comparator.comparingInt(p -> (int) Players.getLocal().getPosition().distance(p)));
        } else {
            return allPositions.stream().max(Comparator.comparingInt(p -> (int) Players.getLocal().getPosition().distance(p)));
        }
    }

    private void findInstructorFallback() {
        switch (Game.getClient().getHintArrowType()) {
            case 0:
                Log.severe("No hint arrow");
                randWalker(Players.getLocal().getPosition());
                break;
            case 1:
                Log.fine("Hint Arrow: Npc");
                Npc npc = Npcs.getAt(Game.getClient().getHintArrowNpcIndex());
                if (npc != null && npc.isPositionInteractable()) {
                    npc.interact("Talk-to");
                } else if (npc != null) {
                    Movement.walkTo(npc.getPosition().randomize(4));
                } else {
                    randWalker(Players.getLocal().getPosition());
                }
                break;
            case 2:
                Position hintPos = new Position(Game.getClient().getHintArrowX(), Game.getClient().getHintArrowY(), Players.getLocal().getFloorLevel());
                for (SceneObject so : SceneObjects.getAt(hintPos)) {
                    if (so.containsAction(a -> true)) {
                        Log.fine("Hint Arrow: SceneObject");
                        so.interact(a -> true);
                        randWalker(so.getPosition());
                        break;
                    }
                }
                break;
            default:
                randWalker(Players.getLocal().getPosition());
                break;
        }
    }

    /*void daxWalker(Position position , Area stopArea) {
        daxWalker.walkTo(position, () -> {
            if (stopArea == null && (pendingContinue() || TutorialIsland.getInstance(null).isIdling || !Game.isLoggedIn())) {
                Time.sleep(1000, 5000);
                return true;
            }
            if (stopArea == null) {
                return false;
            }

            if (stopArea.contains(Players.getLocal()) || pendingContinue() || TutorialIsland.getInstance(null).isIdling || !Game.isLoggedIn()) {
                Time.sleep(1000, 5000);
                return true;
            }
            return false; // false to continue walking after check. true to exit out of walker.
        });
    }

    void daxWalker(Position position) {
        daxWalker(position, null);
    }*/
}
