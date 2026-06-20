package com.piglinmine.fastpipes.render;

import com.piglinmine.fastpipes.blockentity.PipeBlockEntity;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PipeBlockColor implements BlockColor {
    @Override
    public int getColor(BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) {
        if (tintIndex != 0 || level == null || pos == null) return 0xFFFFFFFF;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PipeBlockEntity pipeBe) {
            DyeColor c = pipeBe.getColor();
            if (c != null) {
                int rgb = c.getTextureDiffuseColor();
                return 0xFF000000 | (rgb & 0x00FFFFFF);
            }
        }
        return 0xFFFFFFFF;
    }
}
