package com.createicbm.mods.jei;

import com.createicbm.ICBMConstants;
import com.createicbm.api.refs.ICBMExplosives;
import com.createicbm.config.ConfigJEI;
import com.createicbm.content.missile.entity.delivery.ItemDeliveryMissile;
import com.createicbm.content.reg.ComponentReg;
import com.createicbm.content.reg.ItemReg;
import com.createicbm.lib.explosive.ExplosiveStacks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

@JeiPlugin
public class JEIProxy implements IModPlugin {
    // https://github.com/mezz/JustEnoughItems/wiki/Getting-Started

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    // ------------------------------------------------------------------
    // Subtypes
    // ------------------------------------------------------------------

    private static String nestedKey(ItemStack stack) {
        String key = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        final ResourceLocation explosive = stack.get(ComponentReg.EXPLOSIVE.get());
        if (explosive != null) {
            key += ":" + explosive;
        }
        final CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && !customData.isEmpty()) {
            key += ":" + customData.copyTag().hashCode();
        }
        return key;
    }

    @Nullable
    private static String explosiveKey(ItemStack itemStack) {
        final ResourceLocation explosive = itemStack.get(ComponentReg.EXPLOSIVE.get());
        return explosive != null ? explosive.toString() : null;
    }

    @Nullable
    private static String deliveryKey(ItemStack itemStack) {
        final ItemStack cargo = ItemDeliveryMissile.getCargo(itemStack);
        return cargo.isEmpty() ? null : nestedKey(cargo) + "*" + cargo.getCount();
    }

    private static ISubtypeInterpreter<ItemStack> interpreter(Function<ItemStack, String> keyFunction) {
        return new ISubtypeInterpreter<>() {
            @Override
            @Nullable
            public Object getSubtypeData(ItemStack ingredient, UidContext context) {
                return keyFunction.apply(ingredient);
            }

            @Override
            @SuppressWarnings("removal")
            public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
                final String key = keyFunction.apply(ingredient);
                return key != null ? key : "";
            }
        };
    }

    private static void register(ISubtypeRegistration registration, Item item, Function<ItemStack, String> keyFunction) {
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item, interpreter(keyFunction));
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        if(!ConfigJEI.DISABLED) {
            register(registration, ItemReg.itemBombCart.get(), JEIProxy::explosiveKey);
            register(registration, ItemReg.itemExplosiveMissile.get(), JEIProxy::explosiveKey);
            register(registration, ItemReg.itemBlockExplosive.get(), JEIProxy::explosiveKey);

            register(registration, ItemReg.itemDeliveryMissile.get(), JEIProxy::deliveryKey);
        }
    }

    // ------------------------------------------------------------------
    // Info pages
    // ------------------------------------------------------------------

    @Override
    public void registerRecipes(IRecipeRegistration registration) {

        if(!ConfigJEI.DISABLED) {
            // Info pages for what no recipe shows: fallout only comes from nuclear blasts, and warheads go on the module
            registration.addIngredientInfo(List.of(new ItemStack(ItemReg.itemRadioactiveDirt.get()), new ItemStack(ItemReg.itemRadioactiveStone.get())),
                VanillaTypes.ITEM_STACK, Component.translatable("jei.create_icbm.info.radioactive"));
            registration.addIngredientInfo(ExplosiveStacks.create(ItemReg.itemExplosiveMissile.get(), ICBMExplosives.MISSILEMODULE),
                VanillaTypes.ITEM_STACK, Component.translatable("jei.create_icbm.info.missile_module"));
            registration.addIngredientInfo(new ItemStack(ItemReg.itemDeliveryMissile.get()),
                VanillaTypes.ITEM_STACK, Component.translatable("jei.create_icbm.info.delivery_missile"));
        }
    }
}
