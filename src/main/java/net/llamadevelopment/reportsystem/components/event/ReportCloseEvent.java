package net.llamadevelopment.reportsystem.components.event;

import cn.nukkit.event.HandlerList;
import cn.nukkit.event.player.PlayerEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ReportCloseEvent extends PlayerEvent {

    private final String player;
    private final String target;
    private final String reason;
    private final String status;
    private final String member;
    private final String id;
    private final String date;
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }
}