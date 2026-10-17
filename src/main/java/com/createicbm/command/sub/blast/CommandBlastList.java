package com.createicbm.command.sub.blast;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.reg.IExplosiveData;
import com.createicbm.command.system.SubCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;

/**
 * Created by Robin Seifert on 1/6/20.
 */
public class CommandBlastList extends SubCommand
{
    public CommandBlastList()
    {
        super("list");
    }

    @Override
    public void handleCommand(@Nonnull MinecraftServer server, @Nonnull CommandSourceStack sender, @Nonnull String[] args)
    {
        //Convert list of explosives to string registry names
        String names = CreateICBMAPI.EXPLOSIVE_REGISTRY.getExplosives().stream()
                .map(IExplosiveData::getRegistryKey)
                .map(ResourceLocation::toString)
                .sorted()
                .collect(Collectors.joining(", "));

        //Output message TODO translate if possible?
        sender.sendSystemMessage(Component.literal("Explosive Types: " + names));
    }
}
