package com.createicbm.content.missile.logic.source.cause;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.actions.cause.IActionCause;
import com.createicbm.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public abstract class ActionCause implements IActionCause, INBTSerializable<CompoundTag> {

    private IActionCause parent;

    @Setter @Getter
    private String name;

    public IActionCause setPreviousCause(IActionCause parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public IActionCause getPreviousCause() {
        return parent;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return SAVE_LOGIC.save(this, new CompoundTag());
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<ActionCause> SAVE_LOGIC = new NbtSaveHandler<ActionCause>()
        .mainRoot()
        /* */.nodeString("name", ActionCause::getName, ActionCause::setName)
        /* */.nodeBuildableObject("parent", () -> CreateICBMAPI.ACTION_CAUSE_REGISTRY, ActionCause::getPreviousCause, ActionCause::setPreviousCause)
        .base();
}
