package com.bigboibeef.treechopper;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockTypes;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LogBreakEvent {
    public static void register () {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            breakBlocks(world, player, pos, state, blockEntity, 0);
        });
    }

    public static void breakBlocks (World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, int recursions) {
        if (player.isCreative()) {
            return;
        }
        if (recursions >= 300) {
            return;
        }

        //LOGS
        //Y
        BlockPos pos1 = new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
        if (world.getBlockState(pos1).isIn(BlockTags.LOGS)) {
            breakBlocks(world, player, pos1, state, blockEntity, recursions + 1);
        }

        BlockPos pos2 = new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
        if (world.getBlockState(pos2).isIn(BlockTags.LOGS)) {
            breakBlocks(world, player, pos2, state, blockEntity, recursions + 1);
        }
        //Y

        //X
        BlockPos pos3 = new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ());
        if (world.getBlockState(pos3).isIn(BlockTags.LOGS)) {
            breakBlocks(world, player, pos3, state, blockEntity, recursions + 1);
        }

        BlockPos pos4 = new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ());
        if (world.getBlockState(pos4).isIn(BlockTags.LOGS)) {
            breakBlocks(world, player, pos4, state, blockEntity, recursions + 1);
        }
        //X

        //Z
        BlockPos pos5 = new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1);
        if (world.getBlockState(pos5).isIn(BlockTags.LOGS)) {
            breakBlocks(world, player, pos5, state, blockEntity, recursions + 1);
        }

        BlockPos pos6 = new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1);
        if (world.getBlockState(pos6).isIn(BlockTags.LOGS)) {
            breakBlocks(world, player, pos6, state, blockEntity, recursions + 1);
        }
        //Z
        //LOGS

        world.breakBlock(pos, true, player);

    }
}
