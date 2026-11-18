package com.createicbm.mods.mixin;

import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Applies a compatibility mixin only when the mods it is for are installed. A mixin in package
 * {@code com.createicbm.mixin.compat.<modid>} needs that mod, and every one of them also needs Sable (Create
 * Aeronautics), they fix blocks of other mods that are built into ships.
 */
public class CompatMixinPlugin implements IMixinConfigPlugin
{
    private static final String COMPAT_PACKAGE = "com.createicbm.mixin.compat.";

    @Override
    public void onLoad(String mixinPackage)
    {
    }

    @Override
    public String getRefMapperConfig()
    {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
    {
        if (!mixinClassName.startsWith(COMPAT_PACKAGE))
        {
            return true;
        }
        final String rest = mixinClassName.substring(COMPAT_PACKAGE.length());
        final int dot = rest.indexOf('.');
        return dot > 0 && isLoaded(rest.substring(0, dot)) && isLoaded("sable");
    }

    private static boolean isLoaded(String modId)
    {
        final LoadingModList mods = LoadingModList.get();
        return mods != null && mods.getModFileById(modId) != null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets)
    {
    }

    @Override
    public List<String> getMixins()
    {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
    {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
    {
    }
}
