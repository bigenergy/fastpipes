package com.piglinmine.fastpipes.barrel;

import net.minecraft.resources.ResourceLocation;
import com.piglinmine.fastpipes.FastPipes;

public enum BarrelTier {
    OAK("oak", 27, 3),
    COPPER("copper", 45, 5),
    IRON("iron", 54, 6),
    GOLD("gold", 72, 8),
    DIAMOND("diamond", 81, 9),
    NETHERITE("netherite", 108, 12);

    private final String name;
    private final int slots;
    private final int rows;

    BarrelTier(String name, int slots, int rows) {
        this.name = name;
        this.slots = slots;
        this.rows = rows;
    }

    public String getName() {
        return name;
    }

    public int getSlots() {
        return slots;
    }

    public int getRows() {
        return rows;
    }

    public String getRegistryName() {
        return name + "_barrel";
    }

    public ResourceLocation getGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "textures/gui/" + name + "_barrel_gui.png");
    }

    public boolean canUpgradeTo(BarrelTier target) {
        return target.ordinal() > this.ordinal();
    }

    public static BarrelTier fromOrdinal(int ordinal) {
        BarrelTier[] values = values();
        if (ordinal >= 0 && ordinal < values.length) return values[ordinal];
        return OAK;
    }

    public static BarrelTier fromName(String name) {
        for (BarrelTier tier : values()) {
            if (tier.name.equals(name)) return tier;
        }
        return OAK;
    }
}
