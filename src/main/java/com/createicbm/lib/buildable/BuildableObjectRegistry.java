package com.createicbm.lib.buildable;

import com.createicbm.api.reg.obj.IBuildableObject;
import com.createicbm.api.reg.obj.IBuilderRegistry;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoadingContext;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class BuildableObjectRegistry<Part extends IBuildableObject> implements IBuilderRegistry<Part>
{
    private final Map<ResourceLocation, Supplier<Part>> builders = new HashMap<>();
    @Getter
    private boolean isLocked = false;

    private final String loggerPrefix;
    private final String key;

    @Deprecated
    public BuildableObjectRegistry(String loggerPrefix) {
        this.loggerPrefix = loggerPrefix;
        this.key = loggerPrefix.toLowerCase(Locale.ROOT).replace("_", ".");
    }

    public BuildableObjectRegistry(String loggerPrefix, String key) {
        this.loggerPrefix = loggerPrefix;
        this.key = key;
    }

    @Override
    public void register(@Nonnull ResourceLocation key, @Nonnull Supplier<Part> builder) {
        if (isLocked) {
            throw new RuntimeException(this.loggerPrefix + ": mod '" + activeModId() + "' attempted to do a late registry");
        }
        if (builders.containsKey(key)) {
            throw new RuntimeException(this.loggerPrefix + ": mod '" + activeModId() + "' attempted to override '" + key + "'. " +
                    "This method does not allow replacing existing registries. See implementing class for override call.");
        }
        builders.put(key, builder);
    }

    @Override
    public Part getOrBuild(@Nonnull ResourceLocation name) {
        return Optional.ofNullable(builders.get(name)).map(Supplier::get).orElse(null);
    }

    @Nonnull
    @Override
    public String getUniqueName() {
        return key;
    }

    private static String activeModId() {
        return ModLoadingContext.get().getActiveNamespace();
    }

    public void lock() {
        this.isLocked = true;
    }
}
