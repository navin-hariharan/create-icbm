package com.createicbm.content.blocks.mail;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Every delivered {@link TileMail} nobody has opened yet, per level and saved with it, so web maps can show the parcels
 * in chunks that aren't loaded. Kept up to date by the mail itself, see {@link TileMail#fill}, {@link TileMail#collect}
 * and {@link BlockMail}.
 */
public class UnopenedMail extends SavedData
{
    private static final String DATA_ID = "create_icbm_unopened_mail";

    /** Mail position to the name of its sender, empty if unknown */
    private final Map<BlockPos, String> mail = new LinkedHashMap<>();

    public static UnopenedMail get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(UnopenedMail::new, UnopenedMail::load), DATA_ID);
    }

    static void add(Level level, BlockPos pos, String sender)
    {
        if (level instanceof ServerLevel server)
        {
            final UnopenedMail data = get(server);
            if (!sender.equals(data.mail.put(pos.immutable(), sender)))
            {
                data.setDirty();
            }
        }
    }

    static void remove(Level level, BlockPos pos)
    {
        if (level instanceof ServerLevel server)
        {
            final UnopenedMail data = get(server);
            if (data.mail.remove(pos) != null)
            {
                data.setDirty();
            }
        }
    }

    /**
     * @param action - called with the position and sender name (empty if unknown) of each unopened mail
     */
    public void forEach(BiConsumer<BlockPos, String> action)
    {
        mail.forEach(action);
    }

    public static UnopenedMail load(CompoundTag nbt, HolderLookup.Provider registries)
    {
        final UnopenedMail data = new UnopenedMail();
        final ListTag list = nbt.getList("mail", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            final CompoundTag entry = list.getCompound(i);
            data.mail.put(BlockPos.of(entry.getLong("pos")), entry.getString("sender"));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries)
    {
        final ListTag list = new ListTag();
        mail.forEach((pos, sender) -> {
            final CompoundTag entry = new CompoundTag();
            entry.putLong("pos", pos.asLong());
            entry.putString("sender", sender);
            list.add(entry);
        });
        nbt.put("mail", list);
        return nbt;
    }
}
