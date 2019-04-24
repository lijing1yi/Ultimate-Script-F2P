package tasks;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.script.task.Task;

public class TradePlayer extends Task implements ChatMessageListener {

    private Player toTrade;
    private InterfaceComponent switcher;

    @Override
    public void notify(ChatMessageEvent event) {
        if (event.getType().equals(ChatMessageType.TRADE)) {
            String name = event.getMessage().replaceAll(" wishes to trade with you.", "");
            toTrade = Players.getNearest(name);
            if (toTrade != null) {
                toTrade.interact("TradePlayer with");
                Time.sleep(5000);
                switcher = Interfaces.getComponent(335, 3);
            }
        }
    }

    @Override
    public boolean validate() {
        return toTrade != null;
    }

    @Override
    public int execute() {
        return 1000;
    }
}