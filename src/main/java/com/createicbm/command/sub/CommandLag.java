package com.createicbm.command.sub;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.createicbm.command.CommandUtils;
import com.createicbm.command.system.SubCommand;
import com.createicbm.lib.actions.WorkTickingActionHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by Dark(DarkGuardsman, Robin) on 4/13/2018.
 */
public class CommandLag extends SubCommand
{
    public static final String TRANSLATION_LAG_REMOVE = "command.create_icbm:icbm.lag.remove";
    private final Predicate<Entity> icbmEntitySelector = (entity) -> entity.isAlive() && CommandUtils.isICBMEntity(entity);

    public CommandLag()
    {
        super("lag");
    }

    @Override
    protected void collectHelpForAll(Consumer<String> consumer)
    {

    }

    @Override
    protected void collectHelpWorldOnly(Consumer<String> consumer)
    {
        consumer.accept("[radius]");
    }

    @Override
    public void handleCommand(@Nonnull MinecraftServer server, @Nonnull CommandSourceStack sender, @Nonnull String[] args) throws CommandSyntaxException
    {
        double range = args.length > 0 ? CommandUtils.parseDouble(args[0]) : 1000;

        final Vec3 pos = sender.getPosition();

        //Remove ICBM entities
        final List<Entity> entities = CommandUtils.getEntities(sender.getLevel(),
                pos.x, pos.y, pos.z,
                range,
                icbmEntitySelector);
        entities.forEach(Entity::discard);

        //Remove blasts queue to run or currently running
        final int blastRemoveCount = WorkTickingActionHandler.removeNear(sender.getLevel(),
                pos.x, pos.y, pos.z,
                range);

        //Update user with data
        sender.sendSystemMessage(Component.translatable(TRANSLATION_LAG_REMOVE, blastRemoveCount, entities.size(), range));
    }
}
