package com.createicbm.content.reg;

import com.mojang.serialization.Codec;
import com.createicbm.ICBMConstants;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class AttachmentReg
{
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ICBMConstants.DOMAIN);

    /**
     * Client copy is synced with a packet.
     */
    public static final Supplier<AttachmentType<Boolean>> SPACE_CHICKEN = ATTACHMENTS.register("space_chicken",
        () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL, space -> space).build());

    private AttachmentReg()
    {
    }

    public static void register(IEventBus modBus)
    {
        ATTACHMENTS.register(modBus);
    }
}
