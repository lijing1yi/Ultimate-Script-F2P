package script.tanner.tasks;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import script.tanner.Main;

class CommonConditions {

    private Main main;

    CommonConditions(Main main){
        this.main = main;
    }

    // True if got anything other than coins or cowhide
    boolean gotJunkOrLeather() {
        return Inventory.contains(
            item -> item != null
                && !item.getName().equals("Coins")
                && item.getId() != main.COWHIDE
        );
    }

    boolean gotCowhide() {
        return Inventory.contains(main.COWHIDE) && !Inventory.contains(main.COWHIDE+1);
    }

    boolean gotEnoughCoins() {
        Item coins = Inventory.getFirst("Coins");
        if (main.LEATHER == 1741)
            return coins != null && coins.getStackSize() >= 1;
        return coins != null && coins.getStackSize() >= 3;
    }

    boolean nearTanner() {
        Npc tanner = Npcs.getNearest(main.TANNER_ID);
        return main.TANNER_AREA.getCenter().distance(Players.getLocal()) < 10 && tanner != null && tanner.isPositionInteractable();
    }

    boolean atBank() {
        return BankLocation.AL_KHARID.getPosition().distance(Players.getLocal().getPosition()) <= 7;
    }

    boolean atGE() {
        return BankLocation.GRAND_EXCHANGE.getPosition().distance(Players.getLocal().getPosition()) <= 7;
    }

    boolean tanInterfaceIsOpen() {
        return Interfaces.getComponent(324, 124) != null;
    }
}
