package com.epherical.shoppy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import com.epherical.shoppy.block.entity.Ownable;

/**
 * Registers “/shopowner clear|set <pos>” commands.
 */
public final class ShopOwnerCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("shopowner")
                        .requires(src -> src.hasPermission(4))          // level-4 only
                        .then(Commands.literal("clear")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> setOwner(ctx, false))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> setOwner(ctx, true))))
        );
    }

    /**
     * @param toPlayer if true, owner becomes the command executor; else NIL_UUID
     */
    private static int setOwner(CommandContext<CommandSourceStack> ctx, boolean toPlayer) throws CommandSyntaxException {
        ServerLevel level = ctx.getSource().getLevel();
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        BlockEntity be = level.getBlockEntity(pos);

        if (!(be instanceof Ownable ownable)) {
            ctx.getSource().sendFailure(Component.translatable("command.shoppy.shopowner.no_ownable_at", pos.toShortString()));
            return 0;
        }

        if (toPlayer) {
            ownable.setOwner(ctx.getSource().getPlayerOrException().getUUID());
            ctx.getSource().sendSuccess(() -> Component.translatable("command.shoppy.shopowner.owner_set_self"), true);
        } else {
            ownable.setOwner(Util.NIL_UUID);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.shoppy.shopowner.owner_cleared"), true);
        }
        be.setChanged();
        return 1;
    }

    private ShopOwnerCommand() {} // no instantiation
}
