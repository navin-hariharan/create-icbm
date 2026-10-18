package com.createicbm.content.reg;

import com.createicbm.ICBMConstants;
import com.createicbm.api.refs.ICBMEntities;
import com.createicbm.content.blast.redmatter.EntityRedmatter;
import com.createicbm.content.entity.EntityBombCart;
import com.createicbm.content.entity.EntityExplosion;
import com.createicbm.content.entity.EntityExplosive;
import com.createicbm.content.entity.EntityFragments;
import com.createicbm.content.entity.EntityLightBeam;
import com.createicbm.content.entity.EntityPlayerSeat;
import com.createicbm.content.entity.EntitySmoke;
import com.createicbm.content.entity.flyingblock.EntityFlyingBlock;
import com.createicbm.content.entity.phantom.PhantomCreeper;
import com.createicbm.content.entity.phantom.PhantomHusk;
import com.createicbm.content.entity.phantom.PhantomSpider;
import com.createicbm.content.entity.phantom.PhantomZombie;
import com.createicbm.content.missile.entity.anti.EntitySurfaceToAirMissile;
import com.createicbm.content.missile.entity.explosive.EntityExplosiveMissile;
import com.createicbm.content.missile.entity.explosive.EntityMissileActionable;
import com.createicbm.content.missile.entity.delivery.EntityDeliveryMissile;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Entity type registry.
 * <p>
 * Entity constructor convention: {@code EntityX(EntityType<? extends EntityX> type, Level level)} plus a
 * convenience {@code EntityX(Level level)} that passes the registered type.
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public final class EntityReg
{
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, ICBMConstants.DOMAIN);

    public static final DeferredHolder<EntityType<?>, EntityType<EntityFlyingBlock>> BLOCK_GRAVITY = register(ICBMEntities.BLOCK_GRAVITY,
        EntityType.Builder.<EntityFlyingBlock>of(EntityFlyingBlock::new, MobCategory.MISC).sized(0.98F, 0.98F).fireImmune(), 128, 1);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityFragments>> BLOCK_FRAGMENT = register(ICBMEntities.BLOCK_FRAGMENT,
        EntityType.Builder.<EntityFragments>of(EntityFragments::new, MobCategory.MISC).sized(0.5F, 0.5F), 40, 1);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityExplosive>> BLOCK_EXPLOSIVE = register(ICBMEntities.BLOCK_EXPLOSIVE,
        EntityType.Builder.<EntityExplosive>of(EntityExplosive::new, MobCategory.MISC).sized(0.98F, 0.98F).fireImmune(), 50, 5);

    public static final DeferredHolder<EntityType<?>, EntityType<EntityExplosiveMissile>> MISSILE_EXPLOSIVE = register(ICBMEntities.MISSILE_EXPLOSIVE,
        EntityType.Builder.<EntityExplosiveMissile>of(EntityExplosiveMissile::new, MobCategory.MISC).sized(0.5F, 0.5F), 500, 1);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityMissileActionable>> MISSILE_GENERIC = register(ICBMEntities.MISSILE_GENERIC,
        EntityType.Builder.<EntityMissileActionable>of(EntityMissileActionable::new, MobCategory.MISC).sized(0.5F, 0.5F), 500, 1);
    public static final DeferredHolder<EntityType<?>, EntityType<EntitySurfaceToAirMissile>> MISSILE_SAM = register(ICBMEntities.MISSILE_SAM,
        EntityType.Builder.<EntitySurfaceToAirMissile>of(EntitySurfaceToAirMissile::new, MobCategory.MISC).sized(0.5F, 0.5F), 500, 1);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityDeliveryMissile>> MISSILE_DELIVERY = register(ICBMEntities.MISSILE_DELIVERY,
        EntityType.Builder.<EntityDeliveryMissile>of(EntityDeliveryMissile::new, MobCategory.MISC).sized(0.5F, 0.5F), 500, 1);

    public static final DeferredHolder<EntityType<?>, EntityType<EntityExplosion>> EXPLOSION = register(ICBMEntities.EXPLOSION,
        EntityType.Builder.<EntityExplosion>of(EntityExplosion::new, MobCategory.MISC).sized(0.98F, 0.98F).fireImmune(), 100, 5);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityLightBeam>> BEAM = register(ICBMEntities.BEAM,
        EntityType.Builder.<EntityLightBeam>of(EntityLightBeam::new, MobCategory.MISC).sized(1F, 1F), 80, 5);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityBombCart>> BOMB_CART = register(ICBMEntities.BOMB_CART,
        EntityType.Builder.<EntityBombCart>of(EntityBombCart::new, MobCategory.MISC).sized(0.98F, 0.7F), 50, 2);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityPlayerSeat>> MISSILE_SEAT = register(ICBMEntities.MISSILE_SEAT,
        EntityType.Builder.<EntityPlayerSeat>of(EntityPlayerSeat::new, MobCategory.MISC).sized(0.5F, 2.5F), 50, 2);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityRedmatter>> REDMATTER = register(ICBMEntities.REDMATTER,
        EntityType.Builder.<EntityRedmatter>of(EntityRedmatter::new, MobCategory.MISC).sized(0.98F, 0.98F).fireImmune(), 500, 1);
    public static final DeferredHolder<EntityType<?>, EntityType<EntitySmoke>> SMOKE = register(ICBMEntities.SMOKE,
        EntityType.Builder.<EntitySmoke>of(EntitySmoke::new, MobCategory.MISC).sized(0.1F, 0.1F), 100, 15);

    // Fake mobs of the phantom enemy missile, same size as the real ones. Never saved, and MISC so they don't count
    // toward the hostile mob cap
    public static final DeferredHolder<EntityType<?>, EntityType<PhantomZombie>> PHANTOM_ZOMBIE = register(ICBMEntities.PHANTOM_ZOMBIE,
        EntityType.Builder.<PhantomZombie>of(PhantomZombie::new, MobCategory.MISC).sized(0.6F, 1.95F).eyeHeight(1.74F).noSave(), 128, 3);
    public static final DeferredHolder<EntityType<?>, EntityType<PhantomHusk>> PHANTOM_HUSK = register(ICBMEntities.PHANTOM_HUSK,
        EntityType.Builder.<PhantomHusk>of(PhantomHusk::new, MobCategory.MISC).sized(0.6F, 1.95F).eyeHeight(1.74F).noSave(), 128, 3);
    public static final DeferredHolder<EntityType<?>, EntityType<PhantomSpider>> PHANTOM_SPIDER = register(ICBMEntities.PHANTOM_SPIDER,
        EntityType.Builder.<PhantomSpider>of(PhantomSpider::new, MobCategory.MISC).sized(1.4F, 0.9F).eyeHeight(0.65F).noSave(), 128, 3);
    public static final DeferredHolder<EntityType<?>, EntityType<PhantomCreeper>> PHANTOM_CREEPER = register(ICBMEntities.PHANTOM_CREEPER,
        EntityType.Builder.<PhantomCreeper>of(PhantomCreeper::new, MobCategory.MISC).sized(0.6F, 1.7F).noSave(), 128, 3);

    private EntityReg()
    {
    }

    private static <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> register(ResourceLocation name, EntityType.Builder<E> builder, int trackingRange, int updateFrequency)
    {
        final int rangeChunks = Math.max(1, (int) Math.ceil(trackingRange / 16.0));
        return ENTITIES.register(name.getPath(), () -> builder
            .clientTrackingRange(rangeChunks)
            .updateInterval(updateFrequency)
            .build(name.toString()));
    }

    public static void register(IEventBus modBus)
    {
        ENTITIES.register(modBus);
        modBus.addListener(EntityReg::registerAttributes);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event)
    {
        event.put(PHANTOM_ZOMBIE.get(), Zombie.createAttributes().build());
        event.put(PHANTOM_HUSK.get(), Zombie.createAttributes().build());
        event.put(PHANTOM_SPIDER.get(), Spider.createAttributes().build());
        event.put(PHANTOM_CREEPER.get(), Creeper.createAttributes().build());
    }
}
