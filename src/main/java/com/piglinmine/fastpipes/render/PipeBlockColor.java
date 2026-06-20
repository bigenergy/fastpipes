package com.piglinmine.fastpipes.render;

import com.piglinmine.fastpipes.blockentity.PipeBlockEntity;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// 26.1.2: BlockColor (single getColor(...) interface) replaced by BlockTintSource (a layer-style
// API with color(state)+colorInWorld(state, level, pos)). Pipes only have a single tint layer
// (the dye color), so we register a single-element List<BlockTintSource> per pipe block.
public class PipeBlockColor implements BlockTintSource {
    @Override
    public int color(BlockState state) {
        return 0xFFFFFFFF;
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        if (level == null || pos == null) return 0xFFFFFFFF;
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
