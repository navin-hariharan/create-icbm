package com.createicbm.content.reg;

import com.createicbm.ICBMConstants;
import com.createicbm.api.EnumTier;
import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.CreateICBMHelpers;
import com.createicbm.api.actions.data.ActionFields;
import com.createicbm.api.caps.IExplosive;
import com.createicbm.api.caps.IGPSData;
import com.createicbm.api.explosion.IBlastFactory;
import com.createicbm.api.refs.ICBMEntities;
import com.createicbm.api.refs.ICBMExplosives;
import com.createicbm.api.reg.IExplosiveData;
import com.createicbm.config.blast.ConfigBlast;
import com.createicbm.content.actions.emp.ActionDataEmpArea;
import com.createicbm.content.actions.entity.ActionSpawnEntity;
import com.createicbm.content.blast.effect.BlastDarkness;
import com.createicbm.content.blast.effect.BlastFlashbang;
import com.createicbm.content.blast.effect.BlastPhantom;
import com.createicbm.content.blast.effect.BlastRecon;
import com.createicbm.content.blast.*;
import com.createicbm.content.blast.BlastTNT.PushType;
import com.createicbm.content.blast.ender.BlastEnder;
import com.createicbm.content.blast.ender.EnderBlastCustomization;
import com.createicbm.content.blast.gas.BlastChemical;
import com.createicbm.content.blast.gas.BlastColor;
import com.createicbm.content.blast.gas.BlastDebilitation;
import com.createicbm.content.blast.gas.BlastContagious;
import com.createicbm.content.blast.redmatter.ActionSpawnRedmatter;
import com.createicbm.content.blast.threaded.BlastAntimatter;
import com.createicbm.content.blast.threaded.BlastNuclear;
import com.createicbm.content.blast.threaded.BlastThermobaric;
import com.createicbm.content.entity.EntityFragments;
import com.createicbm.lib.explosive.reg.ExplosiveRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class ExplosiveInit
{
    //the fuse suppliers all return 100 (default in ExBlockContentReg#getFuseTime) when an unknown enum value is given as a type
    public static void init()
    {
        //=================== Tier 1
        ICBMExplosives.CONDENSED = newEx(0, "condensed", EnumTier.ONE, (w, x, y, z, s) -> new BlastTNT()
            .setDamageToEntities(ConfigBlast.condensed.damage)
            .setBlastWorld(w).setBlastPosition(x, y, z)
            .setBlastSize(ConfigBlast.condensed.energyScale));
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.CONDENSED.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.CONDENSED);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.CONDENSED.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.CONDENSED);

        ICBMExplosives.SHRAPNEL = newEx(1, "shrapnel", EnumTier.ONE, (w, x, y, z, s) -> new BlastShrapnel()
            .setProjectile((world) -> {
                final EntityFragments fragments = new EntityFragments(world);
                fragments.setArrowCritical(true);
                fragments.igniteForSeconds(100);
                fragments.setDamage(ConfigBlast.shrapnel.damage);
                return fragments;
            })
            .setBlastWorld(w).setBlastPosition(x, y, z)
            .setBlastSize(ConfigBlast.shrapnel.fragments));
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.SHRAPNEL.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.SHRAPNEL);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.SHRAPNEL.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.SHRAPNEL);

        ICBMExplosives.INCENDIARY = newEx(2, "incendiary", EnumTier.ONE, (w, x, y, z, s) -> new BlastFire().setBlastWorld(w).setBlastPosition(x, y, z).setBlastSize(ConfigBlast.incendiary.scale));
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseTickListener(ICBMExplosives.INCENDIARY.getRegistryKey(),
                (world, x, y, z, tick) -> world.addParticle(ParticleTypes.LAVA, x, y + 0.5D, z, 0.0D, 0.0D, 0.0D)
                );
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.INCENDIARY.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.INCENDIARY);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.INCENDIARY.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.INCENDIARY);

        ICBMExplosives.DEBILITATION = newEx(3, "debilitation", EnumTier.ONE,
                (w, x, y, z, s) -> new BlastDebilitation()
                    .setDuration(ConfigBlast.debilitation.duration)
                    .setBlastWorld(w).setBlastPosition(x, y, z)
                    .setBlastSize(ConfigBlast.debilitation.size)
        );

        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.DEBILITATION.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.DEBILITATION);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.DEBILITATION.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.DEBILITATION);

        ICBMExplosives.CHEMICAL = newEx(4, "chemical", EnumTier.ONE,
                (w, x, y, z, s) -> new BlastChemical()
                    .setToxicityBuildup(ConfigBlast.chemical.toxicityBuildup)
                    .setToxicityScale(ConfigBlast.chemical.toxicityScale)
                    .setToxicityMinDamage(ConfigBlast.chemical.toxicityMinDamage)
                    .setDuration(ConfigBlast.chemical.duration)
                    .setBlastWorld(w).setBlastPosition(x, y, z)
                    .setBlastSize(ConfigBlast.chemical.size)
        );

        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.CHEMICAL.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.CHEMICAL);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.CHEMICAL.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.CHEMICAL);

        ICBMExplosives.ANVIL = newEx(5, "anvil", EnumTier.ONE,
                (w, x, y, z, s) -> new BlastShrapnel()
                    .setProjectile((world) -> {
                        final EntityFragments fragments = new EntityFragments(world);
                        fragments.setAnvil(true);
                        fragments.setDamage(ConfigBlast.anvil.damage);
                        return fragments;
                    })
                    .setBlastWorld(w).setBlastPosition(x, y, z)
                    .setBlastSize(ConfigBlast.anvil.fragments));

        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ANVIL.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ANVIL);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ANVIL.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ANVIL);

        ICBMExplosives.REPULSIVE = newEx(6, "repulsive", EnumTier.ONE,
                (w, x, y, z, s) -> new BlastTNT().setDestroyItems().setPushType(PushType.REPEL).setBlastSize(ConfigBlast.repulsive.scale).setBlastWorld(w).setBlastPosition(x, y, z));

        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.REPULSIVE.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.REPULSIVE);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.REPULSIVE.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.REPULSIVE);

        ICBMExplosives.ATTRACTIVE = newEx(7, "attractive", EnumTier.ONE,
                (w, x, y, z, s) -> new BlastTNT().setDestroyItems().setPushType(PushType.ATTRACT).setBlastSize(ConfigBlast.attractive.scale).setBlastWorld(w).setBlastPosition(x, y, z));

        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ATTRACTIVE.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ATTRACTIVE);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ATTRACTIVE.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ATTRACTIVE);

        //=================== Tier 2
        ICBMExplosives.FRAGMENTATION = newEx(8, "fragmentation", EnumTier.TWO,
                (w, x, y, z, s) -> new BlastShrapnel()
                    .setProjectile((world) -> {
                        final EntityFragments fragments =  new EntityFragments(world);
                        fragments.setExplosive(true);
                        fragments.setDamage(ConfigBlast.fragmentation.damage);
                        fragments.explosionSize = ConfigBlast.fragmentation.explosionSize;
                        return fragments;
                    })
                    .setBlastSize(ConfigBlast.fragmentation.fragments)
                    .setBlastWorld(w).setBlastPosition(x, y, z));
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.FRAGMENTATION.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.FRAGMENTATION);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.FRAGMENTATION.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.FRAGMENTATION);
        //TODO add fire version of fragmentation with smaller animated flames

        ICBMExplosives.CONTAGIOUS = newEx(9, "contagious", EnumTier.TWO,
                (w, x, y, z, s) -> new BlastContagious()
                    .setToxicityScale(ConfigBlast.contagious.toxicityScale)
                    .setToxicityBuildup(ConfigBlast.contagious.toxicityBuildup)
                    .setToxicityMinDamage(ConfigBlast.contagious.toxicityMinDamage)
                    .setDuration(ConfigBlast.contagious.duration)
                    .setBlastSize(ConfigBlast.contagious.size)
                    .setBlastWorld(w).setBlastPosition(x, y, z)
        );
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.CONTAGIOUS.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.CONTAGIOUS);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.CONTAGIOUS.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.CONTAGIOUS);

        ICBMExplosives.SONIC = newEx(10, "sonic", EnumTier.TWO,
                (w, x, y, z, s) -> new BlastSonic().setBlastSize(ConfigBlast.sonic.scale).setBlastWorld(w).setBlastPosition(x, y, z));
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.SONIC.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.SONIC);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.SONIC.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.SONIC);

        ICBMExplosives.BREACHING = newEx(11, "breaching", EnumTier.TWO,
                (w, x, y, z, s) -> new BlastBreach()
                    .setDepth(ConfigBlast.breaching.depth)
                    .setWidth(ConfigBlast.breaching.size)
                    .setEnergy(ConfigBlast.breaching.energy)
                    .setEnergyDistanceScale(ConfigBlast.breaching.energyDistanceScale)
                    .setEnergyCostDistance(ConfigBlast.breaching.energyCostDistance)
                    .setDamageToEntities(ConfigBlast.breaching.damage)
                    .setBlastWorld(w).setBlastPosition(x, y, z)
        );
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.BREACHING.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.BREACHING);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.BREACHING.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.BREACHING);

        //12 -> Regen

        ICBMExplosives.THERMOBARIC = newEx(13, "thermobaric", EnumTier.TWO,
                (w, x, y, z, s) -> new BlastThermobaric().setEnergy(ConfigBlast.thermobaric.energy).setBlastSize(ConfigBlast.thermobaric.scale).setBlastWorld(w).setBlastPosition(x, y, z));
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.THERMOBARIC.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.THERMOBARIC);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.THERMOBARIC.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.THERMOBARIC);

        //14 -> S-Mine

        //=================== Tier 3
        ICBMExplosives.NUCLEAR = newEx(15, "nuclear", EnumTier.THREE,
                (w, x, y, z, s) -> new BlastNuclear().setEnergy(ConfigBlast.nuclear.energy).setBlastSize(ConfigBlast.nuclear.scale).setBlastWorld(w).setBlastPosition(x, y, z));
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.NUCLEAR.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.NUCLEAR);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.NUCLEAR.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.NUCLEAR);

        ICBMExplosives.EMP = newEx(16, "emp", EnumTier.THREE, (w, x, y, z, s) -> ActionDataEmpArea.INSTANCE.create(w, x, y, z, s, null));
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.EMP.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.EMP);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.EMP.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.EMP);

        ICBMExplosives.EXOTHERMIC = newEx(17, "exothermic", EnumTier.THREE, (w, x, y, z, s) -> new BlastExothermic().setBlastSize(ConfigBlast.exothermic.scale).setBlastWorld(w).setBlastPosition(x, y, z));
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseTickListener(ICBMExplosives.EXOTHERMIC.getRegistryKey(),
                (world, x, y, z, tick) -> world.addParticle(ParticleTypes.LAVA, x, y + 0.5D, z, 0.0D, 0.0D, 0.0D));
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.EXOTHERMIC.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.EXOTHERMIC);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.EXOTHERMIC.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.EXOTHERMIC);

        ICBMExplosives.ENDOTHERMIC = newEx(18, "endothermic", EnumTier.THREE, (w, x, y, z, s) -> new BlastEndothermic().setBlastSize(ConfigBlast.endothermic.scale).setBlastWorld(w).setBlastPosition(x, y, z));
        //TODO add ice fuse animation
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ENDOTHERMIC.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ENDOTHERMIC);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ENDOTHERMIC.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ENDOTHERMIC);

        ICBMExplosives.ANTI_GRAVITATIONAL = newEx(19, "antigravitational", EnumTier.THREE, (w, x, y, z, s) -> new BlastAntiGravitational().setBlastSize(ConfigBlast.antigravitational.scale).setBlastWorld(w).setBlastPosition(x, y, z));
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ANTI_GRAVITATIONAL.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ANTI_GRAVITATIONAL);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ANTI_GRAVITATIONAL.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ANTI_GRAVITATIONAL);

        ICBMExplosives.ENDER = newEx(20, "ender", EnumTier.THREE, (w, x, y, z, s) -> new BlastEnder().setBlastSize(ConfigBlast.ender.scale).setBlastWorld(w).setBlastPosition(x, y, z));
        CreateICBMAPI.EX_MISSILE_REGISTRY.setInteractionListener(ICBMExplosives.ENDER.getRegistryKey(), ExplosiveInit::enderMissileCoordSet);
        CreateICBMAPI.EX_BLOCK_REGISTRY.setActivationListener(ICBMExplosives.ENDER.getRegistryKey(), ExplosiveInit::enderBlockCoordSet);
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ENDER.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ENDER);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ENDER.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ENDER);

        //=================== Tier 4
        ICBMExplosives.ANTIMATTER = newEx(22, "antimatter", EnumTier.FOUR,
                (w, x, y, z, s) -> new BlastAntimatter()
                    .setBlastSize(ConfigBlast.antimatter.size)
                    .setBlastWorld(w).setBlastPosition(x, y, z)
        );
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ANTIMATTER.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ANTIMATTER);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ANTIMATTER.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ANTIMATTER);

        //TODO add config (disable by default) for alarm audio

        ICBMExplosives.REDMATTER = newEx(23, "redmatter", EnumTier.FOUR, (w, x, y, z, s) -> new ActionSpawnRedmatter(w, new Vec3(x, y, z), s, ICBMExplosives.REDMATTER));
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.REDMATTER.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.REDMATTER);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.REDMATTER.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.REDMATTER);

        //=================== No content, only blast

        ICBMExplosives.MISSILEMODULE = newEx(24, "missile",  EnumTier.NONE, (w, x, y, z, s) -> new BlastTNT().setBlastSize(0).setBlastWorld(w).setBlastPosition(x, y, z)); //TODO remove need

        ICBMExplosives.ROT = newEx(25, "rot", EnumTier.NONE, (w, x, y, z, s) -> new BlastRadioactiveBlockSwaps().setBlastWorld(w).setBlastPosition(x, y, z)); //TODO add item version
        ICBMExplosives.MUTATION = newEx(26, "mutation", EnumTier.NONE, (w, x, y, z, s) -> new BlastMutation().setBlastWorld(w).setBlastPosition(x, y, z)); //TODO add item version

        ICBMExplosives.COLOR = newEx(-1, "colors", EnumTier.ONE, (w, x, y, z, s) -> new BlastColor().setBlastSize(ConfigBlast.colorful.scale).setBlastWorld(w).setBlastPosition(x, y, z));
        ICBMExplosives.SMOKE = newEx(-1, "smoke", EnumTier.ONE, (w, x, y, z, s) -> {
            final ActionSpawnEntity actionSpawnEntity = new ActionSpawnEntity(w, new Vec3(x, y, z), s, ICBMExplosives.SMOKE);
            actionSpawnEntity.setValue(ActionFields.ENTITY_REG_NAME, ICBMEntities.SMOKE);
            return actionSpawnEntity;
        });

        //=================== Effect only, break no blocks

        ICBMExplosives.FLASHBANG = newEx(-1, "flashbang", EnumTier.ONE, (w, x, y, z, s) -> new BlastFlashbang().setBlastSize(ConfigBlast.flashbang.size).setBlastWorld(w).setBlastPosition(x, y, z));
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.FLASHBANG.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.FLASHBANG);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.FLASHBANG.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.FLASHBANG);

        ICBMExplosives.RECON = newEx(-1, "recon", EnumTier.ONE, (w, x, y, z, s) -> new BlastRecon().setBlastSize(ConfigBlast.recon.size).setBlastWorld(w).setBlastPosition(x, y, z));
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.RECON.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.RECON);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.RECON.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.RECON);

        ICBMExplosives.PHANTOM = newEx(-1, "phantom", EnumTier.TWO, (w, x, y, z, s) -> new BlastPhantom().setBlastSize(ConfigBlast.phantom.size).setBlastWorld(w).setBlastPosition(x, y, z));
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.PHANTOM.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.PHANTOM);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.PHANTOM.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.PHANTOM);

        ICBMExplosives.DARKNESS = newEx(-1, "darkness", EnumTier.TWO, (w, x, y, z, s) -> new BlastDarkness().setBlastSize(ConfigBlast.darkness.size).setBlastWorld(w).setBlastPosition(x, y, z));
        CreateICBMAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.DARKNESS.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.DARKNESS);
        CreateICBMAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.DARKNESS.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.DARKNESS);

        ((ExplosiveRegistry) CreateICBMAPI.EXPLOSIVE_REGISTRY).lockForce();
    }

    private static IExplosiveData newEx(int id, String name, EnumTier tier, IBlastFactory factory)
    {
        final ResourceLocation regName = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, name);
        if (id != -1)
        {
            //Setup old IDs so saves work
            ((ExplosiveRegistry) CreateICBMAPI.EXPLOSIVE_REGISTRY).forceID(regName, id);
        }
        final IExplosiveData data = CreateICBMAPI.EXPLOSIVE_REGISTRY.register(regName, tier, factory);

        //Do default content types per explosive
        if(tier != EnumTier.NONE) {
            CreateICBMAPI.EX_BLOCK_REGISTRY.enableContent(regName);
            CreateICBMAPI.EX_MISSILE_REGISTRY.enableContent(regName);
            CreateICBMAPI.EX_MINECART_REGISTRY.enableContent(regName);
        }

        return data;
    }

    private static boolean enderMissileCoordSet(Entity entity, Player player, InteractionHand hand)
    {
        final IExplosive explosive = entity.getCapability(CreateICBMAPI.EXPLOSIVE_ENTITY_CAPABILITY);
        if (explosive != null)
        {
            return encodeEnderCoordSet(explosive, player, hand);
        }
        return false;
    }

    private static boolean enderBlockCoordSet(Level world, BlockPos pos, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitz)
    {
        final BlockEntity tileEntity = world.getBlockEntity(pos);

        if (tileEntity != null && world.getCapability(CreateICBMAPI.EXPLOSIVE_BLOCK_CAPABILITY, pos, facing) != null)
        {
            return encodeEnderCoordSet(world.getCapability(CreateICBMAPI.EXPLOSIVE_BLOCK_CAPABILITY, pos, null), player, hand);
        }

        return false;
    }

    private static boolean encodeEnderCoordSet(IExplosive provider, Player player, InteractionHand hand) {
        if(provider == null) {
            return false;
        }
        final ItemStack stack = player.getItemInHand(hand);
        final IGPSData gpsData = CreateICBMHelpers.getGPSData(stack);
        if (gpsData != null)
        {
            final Vec3 position = gpsData.getPosition();
            if (position != null)
            {
                provider.addCustomization(new EnderBlastCustomization(gpsData.getWorldId(), position));
                player.sendSystemMessage(Component.translatable("chat.launcher.toolTargetSet"));
            }
            else
            {
                player.sendSystemMessage(Component.translatable("chat.launcher.noTargetInTool"));
            }
            return true;
        }
        return false;
    }
}
