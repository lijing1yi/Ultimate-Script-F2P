package script.beg;

import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.WorldHopper;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.providers.RSWorld;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;
import script.chocolate.Main;

import java.time.Duration;
import java.util.HashSet;

public class StartOther extends Task {

    private final int HIGH_OTHER_PPH_AMNT = 55_000;
    private final int SUM_TOP_3_WORLDS_POP_LIMIT = 3000;
    private final int RUNTIME_HOURS_LIMIT = 7;
    private final int LAST_TRADE_MINUTES = 30;
    private final int LAST_TRADE_MINUTES_MUTED = 35;
    public static final int START_GP = 85_000;
    private final int MIN_START_GP = 55_000;

    public static final int TANS_PER_HR = 1053;
    public static final int CHOC_PER_HR = 3000;

    private Beggar main;

    public StartOther(Beggar beggar){
        main = beggar;
    }

    @Override
    public boolean validate() {
        if(main.isMuling)
            return false;

        checkMuted();

            if ((hasEnoughGP(START_GP) && hasLongRuntime(RUNTIME_HOURS_LIMIT, 0)) ||
                    (hasEnoughGP(START_GP) && hasTopBegWorldsCovered()) ||
                    (hasEnoughGP(MIN_START_GP) && hasLongLastTradeTime(LAST_TRADE_MINUTES)) ||
                    (hasEnoughGP(MIN_START_GP) && (hasLowPPH() || hasHighOtherPPH() ||
                            (hasLowTopWorldsPopulation() && hasTopBegWorldsCovered())))
            ) {
                startComparePPH(true);
                return true;
        }
        return false;
    }

    private void startComparePPH(boolean refresh) {
        if (main.getTannerPPH(TANS_PER_HR, refresh) > main.getChocolatePPH(CHOC_PER_HR, false)) {
            main.isTanning = true;
        } else {
            main.isChoc = true;
        }
    }

    private boolean hasHighOtherPPH() {
        return main.getTannerPPH(TANS_PER_HR, false) >= HIGH_OTHER_PPH_AMNT || main.getChocolatePPH(CHOC_PER_HR, false) >= HIGH_OTHER_PPH_AMNT;
    }

    private boolean hasLowTopWorldsPopulation() {
        return main.sumTopPops < SUM_TOP_3_WORLDS_POP_LIMIT;
    }

    private boolean hasLongRuntime(int hours, int minutes) {
        return (hours > 0) ? main.runtime.exceeds(Duration.ofHours(hours)) : main.runtime.exceeds(Duration.ofMinutes(minutes));
    }

    private boolean hasEnoughGP(int amount) {
        return Inventory.getCount(true, 995) >= amount;
    }

    private boolean hasLowPPH() {
        boolean refresh = false;
        if (main.refreshPrices) {
            refresh = true;
            main.refreshPrices = false;
        }
            return main.runtime.exceeds(Duration.ofMinutes(LAST_TRADE_MINUTES)) && main.runtime.getHourlyRate(main.gainedC) < main.getTannerPPH(TANS_PER_HR, refresh) ||
                    main.runtime.exceeds(Duration.ofMinutes(LAST_TRADE_MINUTES)) && main.runtime.getHourlyRate(main.gainedC) < main.getChocolatePPH(CHOC_PER_HR, refresh);
    }

    private boolean hasTopBegWorldsCovered() {
        return main.OTHER_BEG_WORLDS != null && main.OTHER_BEG_WORLDS.contains(main.popWorldsArr[0]) && main.OTHER_BEG_WORLDS.contains(main.popWorldsArr[1]) && main.OTHER_BEG_WORLDS.contains(main.popWorldsArr[2]) &&
                main.currWorld != main.popWorldsArr[0] && main.currWorld != main.popWorldsArr[1] && main.currWorld != main.popWorldsArr[2];
    }

    private boolean hasLongLastTradeTime(int minutes) {
        return main.lastTradeTime != null && main.lastTradeTime.exceeds(Duration.ofMinutes(minutes));
    }

    private void checkMuted() {
        if (hasLongLastTradeTime(LAST_TRADE_MINUTES_MUTED) || (main.lastTradeTime == null && hasLongRuntime(0,LAST_TRADE_MINUTES_MUTED))) {
            Log.severe("Muted");
            main.muted = true;
        }
    }

    @Override
    public int execute() {
        if (main.isTanning && !main.isChoc) {
            script.tanner.Main tanner = new script.tanner.Main(main);

            main.tanner = tanner;
            main.timesTanned ++;
            main.tanner.amntMuled += main.amntMuled;
            main.removeCurrBegWorld(main.currWorld);
            if (main.isMuling) {
                Mule.logoutMule();
            }
            unequipAll(true);
            tanner.start();
            return 5000;
        }

        if (main.isChoc && !main.isTanning) {
            if (Worlds.get(main.currWorld).getPopulation() > 400) {
                hopToLowPopWorld(400, main.currWorld);
            }
            if (main.isMuling) {
                Mule.logoutMule();
            }
            main.removeCurrBegWorld(main.currWorld);
            main.timesChocolate ++;
            main.chocolate = new Main(main);
            main.chocolate.amntMuled += main.amntMuled;
            main.chocolate.start();
            return 5000;
        }
        return 1000;
    }

    private void unequipAll(boolean dropAll) {
        Tabs.open(Tab.EQUIPMENT);
        Time.sleep(2000, 2500);
        EquipmentSlot[] equipped = Equipment.getOccupiedSlots();
        HashSet<String> itemNames = new HashSet<>();

        for (EquipmentSlot slot : equipped) {
            itemNames.add(slot.getItemName());
            slot.unequip();
            Time.sleep(1500, 2000);
        }

        if (dropAll) {
            dropAllEquipment(itemNames);
        }
    }

    private void dropAllEquipment(HashSet<String> itemNames) {
        Tabs.open(Tab.INVENTORY);
        Time.sleep(2000, 2500);
        for (String name : itemNames) {
            Inventory.getFirst(name).interact("Drop");
            Time.sleep(1500, 2000);
        }
    }

    public static void hopToLowPopWorld(int pop, int currWorld) {
        RSWorld newWorld = Worlds.get(x -> x != null && x.getPopulation() <= pop &&
                !x.isMembers() && !x.isBounty() && !x.isSkillTotal());

        if (newWorld != null) {
            WorldHopper.hopTo(newWorld);
        } else if (pop < 1000) {
            hopToLowPopWorld(pop + 100, currWorld);
        }

        Time.sleepUntil(() -> Worlds.getCurrent() != currWorld, 12000);
    }
}
