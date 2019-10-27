package script.fighter.nodes.loot;

import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Inventory;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.framework.Node;
import script.fighter.wrappers.BankWrapper;

public class DepositLootNode extends Node {

    private Fighter main;

    public DepositLootNode(Fighter main) {
        this.main = main;
    }

    @Override
    public boolean validate() {
        if (Config.getLoot().size() == 0) {
            return false;
        }
        return Inventory.contains(Config.getLoot().toArray(new String[0])) && Inventory.isFull();
    }

    @Override
    public int execute() {
        main.invalidateTask(this);

        if (BankWrapper.openNearest()) {
            Bank.depositAllExcept(Config.getProgressive().getRunes().toArray(new String[0]));
            BankWrapper.updateBankValue();
            BankWrapper.updateInventoryValue();
        }
        return Fighter.getLoopReturn();
    }

    @Override
    public String status() {
        return "Depositing Loot";
    }
}
