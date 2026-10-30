package com.createicbm.lib.radio.imp;

import com.createicbm.api.data.IBoundBox;
import com.createicbm.api.radio.IRadioMessage;
import com.createicbm.api.radio.IRadioSender;
import com.createicbm.lib.radio.RadioRegistry;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class RadioTile<T extends BlockEntity> extends Radio {

    protected final T host;

    public RadioTile(T host) {
        this.host = host;
    }

    @Override
    public BlockPos getBlockPos() {
        return host.getBlockPos();
    }

    @Override
    public Level getWorld() {
        return host.getLevel();
    }

    @Override
    public IBoundBox<BlockPos> getRange() {
        return RadioRegistry.INFINITE;
    }

    public boolean canReceive(IRadioSender sender, IRadioMessage packet) {
        return !isDisabled()
            // Usually sender isn't receive, but could happen in rare cases
            && sender != this
            // Only accept server side, and never on a block entity that was unloaded or broken
            && host.hasLevel() && !host.getLevel().isClientSide() && !host.isRemoved()
            // Validate channel, this might create the channel string if null
            && Objects.equals(getChannel(), packet.getChannel());
    }
}
