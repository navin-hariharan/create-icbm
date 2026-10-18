package com.createicbm.lib.radio.imp;

import com.createicbm.api.radio.IRadio;
import com.createicbm.api.radio.IRadioChannelAccess;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.apache.commons.lang3.RandomStringUtils;

public abstract class Radio implements IRadio, INBTSerializable<CompoundTag>, IRadioChannelAccess {

    private String channel;

    @Getter @Setter
    private boolean isDisabled = false;

    @Override
    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        if(channel == null) {
            this.channel = RandomStringUtils.random(4, true, true);
        }
        return channel;
    }

    /**
     * Checks for a set or saved channel without creating a random one like {@link #getChannel()} does
     *
     * @return true if the channel is set and not empty
     */
    public boolean hasChannel() {
        return channel != null && !channel.isEmpty();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        final CompoundTag tag = new CompoundTag();
        if(channel != null) {
            tag.putString("channel", channel);
        }
        tag.putBoolean("disabled", isDisabled);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        this.channel = nbt.getString("channel");
        this.isDisabled = nbt.getBoolean("disabled");
    }
}
