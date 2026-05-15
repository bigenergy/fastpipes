package com.piglinmine.fastpipes.network.pipe.energy;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.network.Network;
import com.piglinmine.fastpipes.network.energy.EnergyNetwork;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class EnergyPipe extends Pipe {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "energy");

    private final EnergyPipeType type;
    @Nullable
    private ServerEnergyPipeEnergyStorage energyStorage;

    public EnergyPipe(Level level, BlockPos pos, EnergyPipeType type) {
        super(level, pos);
        this.type = type;
    }

    @Override
    public void joinNetwork(Network network) {
        super.joinNetwork(network);
        this.energyStorage = new ServerEnergyPipeEnergyStorage((EnergyNetwork) network);
    }

    @Override
    public void leaveNetwork() {
        super.leaveNetwork();
        this.energyStorage = null;
    }

    @Nullable
    public ServerEnergyPipeEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public EnergyPipeType getType() {
        return type;
    }

    @Override
    public CompoundTag writeToNbt(CompoundTag tag) {
        tag = super.writeToNbt(tag);
        tag.putInt("type", type.ordinal());
        return tag;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        super.readFromNbt(tag);
        // Type is set in constructor, no need to read from NBT
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public ResourceLocation getNetworkType() {
        return type.getNetworkType();
    }
} 