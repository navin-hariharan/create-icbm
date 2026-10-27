package com.createicbm.lib.network;

import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Server side limit on how often a player may send an item packet. Item cooldowns that only exist on the client can
 * be skipped by a modified client, and some packets (laser designations) broadcast a radio message each.
 * Server thread only.
 */
public final class PacketRateLimit
{
    private final int minTicks;
    /** Server tick of each player's last accepted packet, weak so players that left are dropped */
    private final Map<Player, Integer> lastAccepted = new WeakHashMap<>();

    /**
     * @param minTicks least ticks between two accepted packets of one player
     */
    public PacketRateLimit(int minTicks)
    {
        this.minTicks = minTicks;
    }

    /**
     * @return true and records the packet if the player may send one now, false to drop it
     */
    public boolean tryAccept(Player player)
    {
        final int now = player.getServer() != null ? player.getServer().getTickCount() : player.tickCount;
        final Integer last = lastAccepted.get(player);
        if (last != null && now - last < minTicks && now >= last)
        {
            return false;
        }
        lastAccepted.put(player, now);
        return true;
    }
}
