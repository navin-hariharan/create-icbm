package com.createicbm.lib.network;

import com.createicbm.CreateICBM;
import com.createicbm.ICBMConstants;
import com.createicbm.lib.network.lambda.PacketCodex;
import com.createicbm.lib.network.lambda.PacketEventUtils;
import com.createicbm.lib.tracker.EventTrackerHelpers;
import com.createicbm.lib.tracker.EventTrackerType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class PacketEvents {

    public static final ResourceLocation EXPECTED_WORLD = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "level.id.expected");
    public static final EventTrackerType WRONG_WORLD =
        new EventTrackerType.Builder(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "packet.tile.error.handling"))
            .asWarn()
            .with(PacketEventUtils.fieldCodexId(0))
            .with(PacketEventUtils.fieldCodexKey(1))
            .withString(EventTrackerHelpers.WORLD_SIDE, EventTrackerHelpers.stringAt(2))
            .withString(EventTrackerHelpers.WORLD_ID, EventTrackerHelpers.stringAt(3))
            .withString(EXPECTED_WORLD, EventTrackerHelpers.stringAt(4))
            // Console logging
            .listen(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "logger"),
                () -> CreateICBM.logger().isDebugEnabled(),
                (entry) -> {
                    final String ERROR_MSG = "Packet(%s, %s): Received packet on side %s for world(%s) but got world(%s)... ignoring.";
                    final String message = String.format(ERROR_MSG,
                        entry.getString(PacketEventUtils.CODEX_ID, "?"),
                        entry.getString(PacketEventUtils.CODEX_KEY, "?"),
                        entry.getString(EventTrackerHelpers.WORLD_SIDE, "?"),
                        entry.getString(EventTrackerHelpers.WORLD_ID, "?"),
                        entry.getString(EXPECTED_WORLD, "?")
                    );
                    CreateICBM.logger().debug(message);
                })

            .build();

    public static final EventTrackerType NOT_SERVER_WORLD =
        new EventTrackerType.Builder(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "packet.tile.error.handling"))
            .asWarn()
            .with(PacketEventUtils.fieldCodexId(0))
            .with(PacketEventUtils.fieldCodexKey(1))
            .withString(EventTrackerHelpers.WORLD_ID, EventTrackerHelpers.stringAt(2))
            // Console logging
            .listen(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "logger"),
                () -> CreateICBM.logger().isDebugEnabled(),
                (entry) -> {
                    final String ERROR_MSG = "Packet(%s, %s): Received packet server side but world(%s) is not WorldServer";
                    final String message = String.format(ERROR_MSG,
                        entry.getString(PacketEventUtils.CODEX_ID, "?"),
                        entry.getString(PacketEventUtils.CODEX_KEY, "?"),
                        entry.getString(EventTrackerHelpers.WORLD_ID, "?")
                    );
                    CreateICBM.logger().debug(message);
                })

            .build();

    public static void onWrongWorld(PacketCodex builder, String side, ResourceKey<Level> expected, ResourceKey<Level> received) {
        CreateICBM.MAIN_TRACKER.post(WRONG_WORLD, () -> new Object[]{
            PacketEventUtils.getPacketId(builder),
            PacketEventUtils.getPacketName(builder),
            side,
            String.valueOf(received.location()),
            String.valueOf(expected.location())
        });
    }

    public static void onNotServerWorld(PacketCodex builder, ResourceKey<Level> dim) {
        CreateICBM.MAIN_TRACKER.post(NOT_SERVER_WORLD, () -> new Object[]{
            PacketEventUtils.getPacketId(builder),
            PacketEventUtils.getPacketName(builder),
            String.valueOf(dim.location())
        });
    }
}
