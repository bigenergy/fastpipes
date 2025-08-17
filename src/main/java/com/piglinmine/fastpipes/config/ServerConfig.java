package com.piglinmine.fastpipes.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    private final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    private final ModConfigSpec spec;

    private final ItemPipe basicItemPipe;
    private final ItemPipe improvedItemPipe;
    private final ItemPipe advancedItemPipe;

    private final FluidPipe basicFluidPipe;
    private final FluidPipe improvedFluidPipe;
    private final FluidPipe advancedFluidPipe;
    private final FluidPipe eliteFluidPipe;
    private final FluidPipe ultimateFluidPipe;

    private final EnergyPipe basicEnergyPipe;
    private final EnergyPipe improvedEnergyPipe;
    private final EnergyPipe advancedEnergyPipe;
    private final EnergyPipe eliteEnergyPipe;
    private final EnergyPipe ultimateEnergyPipe;

    private final ExtractorAttachment basicExtractorAttachment;
    private final ExtractorAttachment improvedExtractorAttachment;
    private final ExtractorAttachment advancedExtractorAttachment;
    private final ExtractorAttachment eliteExtractorAttachment;
    private final ExtractorAttachment ultimateExtractorAttachment;

    public ServerConfig() {
        builder.push("pipe");
        {
            builder.push("item");
            {
                basicItemPipe = new ItemPipe("basic", 30);
                improvedItemPipe = new ItemPipe("improved", 20);
                advancedItemPipe = new ItemPipe("advanced", 10);
            }
            builder.pop();

            builder.push("fluid");
            {
                basicFluidPipe = new FluidPipe("basic", 1000, 100);
                improvedFluidPipe = new FluidPipe("improved", 4000, 400);
                advancedFluidPipe = new FluidPipe("advanced", 8000, 800);
                eliteFluidPipe = new FluidPipe("elite", 16000, 1600);
                ultimateFluidPipe = new FluidPipe("ultimate", 32000, 3200);
            }
            builder.pop();

            builder.push("energy");
            {
                basicEnergyPipe = new EnergyPipe("basic", 1000, 1000);
                improvedEnergyPipe = new EnergyPipe("improved", 4000, 4000);
                advancedEnergyPipe = new EnergyPipe("advanced", 8000, 8000);
                eliteEnergyPipe = new EnergyPipe("elite", 16000, 16000);
                ultimateEnergyPipe = new EnergyPipe("ultimate", 32000, 32000);
            }
            builder.pop();

            builder.push("attachment");
            {
                builder.push("extractor");
                {
                    basicExtractorAttachment = new ExtractorAttachment(
                        "basic",
                        20 * 3, 8, // item tick interval, items to extract
                        0, 100, // fluid tick interval, fluids to extract
                        0, // filter slots
                        false, // redstone mode
                        false, // whitelist blacklist
                        false, // routing mode
                        false // exact mode
                    );
                    improvedExtractorAttachment = new ExtractorAttachment(
                        "improved",
                        20 * 2, 16, // item tick interval, items to extract
                        0, 400, // fluid tick interval, fluids to extract
                        4, // filter slots
                        true, // redstone mode
                        false, // whitelist blacklist
                        false, // routing mode
                        true // exact mode
                    );
                    advancedExtractorAttachment = new ExtractorAttachment(
                        "advanced",
                        20, 32, // item tick interval, items to extract
                        0, 800, // fluid tick interval, fluids to extract
                        8, // filter slots
                        true, // redstone mode
                        true, // whitelist blacklist
                        true, // routing mode
                        true // exact mode
                    );
                    eliteExtractorAttachment = new ExtractorAttachment(
                        "elite",
                        10, 64, // item tick interval, items to extract
                        0, 1600, // fluid tick interval, fluids to extract
                        12, // filter slots
                        true, // redstone mode
                        true, // whitelist blacklist
                        true, // routing mode
                        true // exact mode
                    );
                    ultimateExtractorAttachment = new ExtractorAttachment(
                        "ultimate",
                        10, 64, // item tick interval, items to extract
                        0, 3200, // fluid tick interval, fluids to extract
                        15, // filter slots
                        true, // redstone mode
                        true, // whitelist blacklist
                        true, // routing mode
                        true // exact mode
                    );
                }
                builder.pop();
            }
            builder.pop();
        }
        builder.pop();

        spec = builder.build();
    }

    public ModConfigSpec getSpec() {
        return spec;
    }

    public ItemPipe getBasicItemPipe() {
        return basicItemPipe;
    }

    public ItemPipe getImprovedItemPipe() {
        return improvedItemPipe;
    }

    public ItemPipe getAdvancedItemPipe() {
        return advancedItemPipe;
    }

    public FluidPipe getBasicFluidPipe() {
        return basicFluidPipe;
    }

    public FluidPipe getImprovedFluidPipe() {
        return improvedFluidPipe;
    }

    public FluidPipe getAdvancedFluidPipe() {
        return advancedFluidPipe;
    }

    public FluidPipe getEliteFluidPipe() {
        return eliteFluidPipe;
    }

    public FluidPipe getUltimateFluidPipe() {
        return ultimateFluidPipe;
    }

    public EnergyPipe getBasicEnergyPipe() {
        return basicEnergyPipe;
    }

    public EnergyPipe getImprovedEnergyPipe() {
        return improvedEnergyPipe;
    }

    public EnergyPipe getAdvancedEnergyPipe() {
        return advancedEnergyPipe;
    }

    public EnergyPipe getEliteEnergyPipe() {
        return eliteEnergyPipe;
    }

    public EnergyPipe getUltimateEnergyPipe() {
        return ultimateEnergyPipe;
    }

    public ExtractorAttachment getBasicExtractorAttachment() {
        return basicExtractorAttachment;
    }

    public ExtractorAttachment getImprovedExtractorAttachment() {
        return improvedExtractorAttachment;
    }

    public ExtractorAttachment getAdvancedExtractorAttachment() {
        return advancedExtractorAttachment;
    }

    public ExtractorAttachment getEliteExtractorAttachment() {
        return eliteExtractorAttachment;
    }

    public ExtractorAttachment getUltimateExtractorAttachment() {
        return ultimateExtractorAttachment;
    }

    public class ItemPipe {
        private final ModConfigSpec.IntValue maxTicks;

        public ItemPipe(String type, int defaultMaxTicks) {
            builder.push(type);

            maxTicks = builder.comment("The maximum amount of ticks that items can be in the pipe. Lower is faster.").defineInRange("maxTicks", defaultMaxTicks, 0, Integer.MAX_VALUE);

            builder.pop();
        }

        public int getMaxTicks() {
            return maxTicks.get();
        }
    }

    public class FluidPipe {
        private final ModConfigSpec.IntValue capacity;
        private final ModConfigSpec.IntValue transferRate;

        public FluidPipe(String type, int defaultCapacity, int defaultTransferRate) {
            builder.push(type);

            capacity = builder.comment("The capacity in mB of the pipe.").defineInRange("capacity", defaultCapacity, 0, Integer.MAX_VALUE);
            transferRate = builder.comment("The transfer rate in mB/t of the pipe.").defineInRange("transferRate", defaultTransferRate, 0, Integer.MAX_VALUE);

            builder.pop();
        }

        public int getCapacity() {
            return capacity.get();
        }

        public int getTransferRate() {
            return transferRate.get();
        }
    }

    public class EnergyPipe {
        private final ModConfigSpec.IntValue capacity;
        private final ModConfigSpec.IntValue transferRate;

        public EnergyPipe(String type, int defaultCapacity, int defaultTransferRate) {
            builder.push(type);

            capacity = builder.comment("The capacity in FE of the pipe.").defineInRange("capacity", defaultCapacity, 0, Integer.MAX_VALUE);
            transferRate = builder.comment("The transfer rate in FE/t of the pipe.").defineInRange("transferRate", defaultTransferRate, 0, Integer.MAX_VALUE);

            builder.pop();
        }

        public int getCapacity() {
            return capacity.get();
        }

        public int getTransferRate() {
            return transferRate.get();
        }
    }

    public class ExtractorAttachment {
        private final ModConfigSpec.IntValue itemTickInterval;
        private final ModConfigSpec.IntValue itemsToExtract;
        private final ModConfigSpec.IntValue fluidTickInterval;
        private final ModConfigSpec.IntValue fluidsToExtract;
        private final ModConfigSpec.IntValue filterSlots;
        private final ModConfigSpec.BooleanValue canSetRedstoneMode;
        private final ModConfigSpec.BooleanValue canSetWhitelistBlacklist;
        private final ModConfigSpec.BooleanValue canSetRoutingMode;
        private final ModConfigSpec.BooleanValue canSetExactMode;

        public ExtractorAttachment(
            String type,
            int defaultItemTickInterval,
            int defaultItemsToExtract,
            int defaultFluidTickInterval,
            int defaultFluidsToExtract,
            int defaultFilterSlots,
            boolean defaultCanSetRedstoneMode,
            boolean defaultCanSetWhitelistBlacklist,
            boolean defaultCanSetRoutingMode,
            boolean defaultCanSetExactMode) {
            builder.push(type);

            itemTickInterval = builder.comment("The interval between item extractions in ticks. Lower is faster.").defineInRange("itemTickInterval", defaultItemTickInterval, 0, Integer.MAX_VALUE);
            itemsToExtract = builder.comment("The amount of items to extract per extraction.").defineInRange("itemsToExtract", defaultItemsToExtract, 0, 64);
            fluidTickInterval = builder.comment("The interval between fluid extractions in ticks. Lower is faster.").defineInRange("fluidTickInterval", defaultFluidTickInterval, 0, Integer.MAX_VALUE);
            fluidsToExtract = builder.comment("The amount of fluids in mB to extract per extraction.").defineInRange("fluidsToExtract", defaultFluidsToExtract, 0, Integer.MAX_VALUE);
            filterSlots = builder.comment("The amount of filter slots allowed.").defineInRange("filterSlots", defaultFilterSlots, 0, 15);
            canSetRedstoneMode = builder.comment("Whether the redstone mode can be configured for this extractor.").define("canSetRedstoneMode", defaultCanSetRedstoneMode);
            canSetWhitelistBlacklist = builder.comment("Whether the extractor can be toggled between whitelist and blacklist.").define("canSetWhitelistBlacklist", defaultCanSetWhitelistBlacklist);
            canSetRoutingMode = builder.comment("Whether the extractor can be toggled between routing modes.").define("canSetRoutingMode", defaultCanSetRoutingMode);
            canSetExactMode = builder.comment("Whether the extractor can toggle exact mode.").define("canSetExactMode", defaultCanSetExactMode);

            builder.pop();
        }

        public int getItemTickInterval() {
            return itemTickInterval.get();
        }

        public int getItemsToExtract() {
            return itemsToExtract.get();
        }

        public int getFluidTickInterval() {
            return fluidTickInterval.get();
        }

        public int getFluidsToExtract() {
            return fluidsToExtract.get();
        }

        public int getFilterSlots() {
            return filterSlots.get();
        }

        public boolean getCanSetRedstoneMode() {
            return canSetRedstoneMode.get();
        }

        public boolean getCanSetWhitelistBlacklist() {
            return canSetWhitelistBlacklist.get();
        }

        public boolean getCanSetRoutingMode() {
            return canSetRoutingMode.get();
        }

        public boolean getCanSetExactMode() {
            return canSetExactMode.get();
        }
    }
} 