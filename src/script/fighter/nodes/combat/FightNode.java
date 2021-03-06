package script.fighter.nodes.combat;

import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.PathingEntity;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.component.tab.Tab;
import org.rspeer.runetek.api.component.tab.Tabs;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.scene.Players;
import script.fighter.CombatStore;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.debug.Logger;
import script.fighter.framework.BackgroundTaskExecutor;
import script.fighter.framework.Node;
import script.fighter.models.NpcResult;
import script.fighter.services.LootService;
import script.fighter.wrappers.CombatWrapper;
import script.fighter.wrappers.GEWrapper;

public class FightNode extends Node {

    private NpcResult result;
    private String status;
    private boolean running;
    private Spell spell;
    private static final Area RESTRICTED_AREA = Area.rectangular(3187, 3363, 3199, 3351);;

    private Fighter main;

    public FightNode(Fighter main){
        BackgroundTaskExecutor.submit(this::findNextTarget, 1000);
        this.main = main;
    }

    public FightNode() {
        BackgroundTaskExecutor.submit(this::findNextTarget, 1000);
    }

    @Override
    public boolean validate() {
        spell = Config.getProgressive().getSpell();
        if (spell != null && !GEWrapper.hasRunes(spell))
            return false;

        NpcResult target = CombatStore.getCurrentTarget();
        if(target != null && !RESTRICTED_AREA.contains(target.getNpc())) {
            return true;
        }
        if(Config.getProgressive().isPrioritizeLooting()) {
            //Item to loot, return.
            System.out.println("Prioritizing looting");
            if(LootService.getItemsToLoot().length > 0) {
                System.out.println("Found item to loot");
                return false;
            }
        }
        status = "Looking for target.";
        NpcResult res = CombatWrapper.findTarget(false);
        if(res == null || res.getNpc() == null || RESTRICTED_AREA.contains(res.getNpc())) {
            status = "No targets around me, waiting...";
            return false;
        }
        Logger.fine("New Target Index: " + res.getNpc().getIndex());
        result = res;
        doAttack(result.getNpc());
        return true;
    }

    @Override
    public int execute() {
        main.invalidateTask(this);

        running = true;
        if(result != null && !CombatStore.hasTarget()) {
            doAttack(result.getNpc());
            return Fighter.getLoopReturn();
        }
        NpcResult target = CombatStore.getCurrentTarget();
        if(target == null || CombatWrapper.isDead(target.getNpc())) {
            status = "Target has died.";
            CombatStore.setCurrentTarget(null);
            return Fighter.getLoopReturn();
        }
        if(!CombatWrapper.isTargetingMe(target.getNpc())) {
            Logger.debug("Our current target is not targeting me.");
            if(CombatStore.getTargetingMe().size() > 0) {
               status = "Switching to target that is targeting me.";
               Npc first = CombatStore.getTargetingMe().stream().filter(n -> {
                   PathingEntity npcsTarget = n.getTarget();
                   return npcsTarget != null && npcsTarget.equals(Players.getLocal()) && n.getIndex() != target.getNpc().getIndex();
               }).findFirst().orElse(null);
               if(first == null) {
                   Logger.debug("Targeting me first is null, grabbing next.");
                   Npc next = CombatStore.getTargetingMe().iterator().next();
                   CombatStore.setCurrentTarget(new NpcResult(next, true));
                   doAttack(next);
               } else {
                   Logger.debug("Changing target to: " + first.getIndex());
                   CombatStore.setCurrentTarget(new NpcResult(first, true));
                   doAttack(first);
               }
               return Fighter.getLoopReturn();
            }
            doAttack(target.getNpc());
        }
        else if(spell != null) {
            doAttack(target.getNpc());
        }
        return Fighter.getLoopReturn();
    }

    @Override
    public void onInvalid() {
        running = false;
        CombatStore.resetTargetingValues();
        super.onInvalid();
    }

    public void invalidateTask(Node active) {
        if (active != null && !this.equals(active)) {
            Logger.debug("Node has changed.");
            active.onInvalid();
        }
        main.setActive(this);
    }

    @Override
    public void onScriptStop() {
        super.onScriptStop();
    }

    @Override
    public String status() {
        return status;
    }

    private void findNextTarget() {
        if(!running)
            return;
        CombatStore.setNextTarget(CombatWrapper.findTarget(true));
    }

    private void doAttack(Npc npc) {
        Player p = Players.getLocal();
        PathingEntity target = p.getTarget();
        PathingEntity targetsTarget = target == null ? null : target.getTarget();
        if(spell == null && p.getTargetIndex() != -1 && target != null && targetsTarget != null && targetsTarget.equals(p)) {
            status = "In combat";
            return;
        }
        if(Movement.isInteractable(npc, false)) {
            status = "Attacking " + npc.getName() + " (" + npc.getIndex() + ").";
            Logger.debug("Attacking target: " + npc.getIndex());
            if (spell == null) {
                npc.interact("Attack");
            } else {
                castSpell(npc);
            }
            Time.sleepUntil(() -> Players.getLocal().getTargetIndex() > 0, 2000);
            return;
        }
        status = "Walking to target.";
        Player player = Players.getLocal();
        if (spell == null || (player.getTargetIndex() == -1 && !player.isAnimating())) {
            Movement.walkTo(npc);
            if (!Time.sleepUntil(() -> Players.getLocal().isMoving(), 2000)) {
                Movement.walkTo(Players.getLocal().getPosition().randomize(5));
            }
        }
    }

    private void castSpell(Npc npc) {
        if (!Tabs.isOpen(Tab.MAGIC)) {
            Tabs.open(Tab.MAGIC);
            Time.sleepUntil(() -> Tabs.isOpen(Tab.MAGIC), 500, 2000);
        }
        Magic.cast(spell, npc);
    }
}
