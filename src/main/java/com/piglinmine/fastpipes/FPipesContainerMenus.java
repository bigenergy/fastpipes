package com.piglinmine.fastpipes;

import com.piglinmine.fastpipes.inventory.fluid.FluidInventory;
import com.piglinmine.fastpipes.menu.ExtractorAttachmentContainerMenu;
import com.piglinmine.fastpipes.menu.InserterAttachmentContainerMenu;
import com.piglinmine.fastpipes.menu.SensorAttachmentContainerMenu;
import com.piglinmine.fastpipes.menu.VoidAttachmentContainerMenu;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.*;
import com.piglinmine.fastpipes.network.pipe.attachment.inserter.InserterAttachment;
import com.piglinmine.fastpipes.network.pipe.attachment.inserter.InserterAttachmentType;
import com.piglinmine.fastpipes.network.pipe.attachment.sensor.SensorAttachment;
import com.piglinmine.fastpipes.network.pipe.attachment.void_attachment.VoidAttachment;
import com.piglinmine.fastpipes.util.DirectionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FPipesContainerMenus {
    public static final DeferredRegister<MenuType<?>> CONTAINER_MENUS = DeferredRegister.create(Registries.MENU, FastPipes.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ExtractorAttachmentContainerMenu>> EXTRACTOR_ATTACHMENT = CONTAINER_MENUS.register(
        "extractor_attachment",
        () -> IMenuTypeExtension.create((windowId, inv, data) -> {
            if (data != null) {
                BlockPos pos = data.readBlockPos();
                Direction dir = DirectionUtil.safeGet(data.readByte());
                RedstoneMode rm = RedstoneMode.get(data.readByte());
                BlacklistWhitelist bw = BlacklistWhitelist.get(data.readByte());
                RoutingMode routingMode = RoutingMode.get(data.readByte());
                int stackSize = data.readInt();
                boolean exactMode = data.readBoolean();
                ExtractorAttachmentType type = ExtractorAttachmentType.get(data.readByte());
                boolean fluidMode = data.readBoolean();

                // Read fluid filter contents from buffer
                FluidInventory fluidFilter = ExtractorAttachment.createFluidFilterInventory(null);
                for (int i = 0; i < ExtractorAttachment.MAX_FILTER_SLOTS; i++) {
                    FluidStack fluid = FluidStack.OPTIONAL_STREAM_CODEC.decode(data);
                    if (!fluid.isEmpty()) {
                        fluidFilter.setFluid(i, fluid);
                    }
                }

                return new ExtractorAttachmentContainerMenu(
                    windowId, inv.player, pos, dir, rm, bw, routingMode,
                    stackSize, exactMode, type,
                    ExtractorAttachment.createItemFilterInventory(null),
                    fluidFilter, fluidMode
                );
            } else {
                return new ExtractorAttachmentContainerMenu(windowId, inv.player);
            }
        })
    );

    public static final DeferredHolder<MenuType<?>, MenuType<InserterAttachmentContainerMenu>> INSERTER_ATTACHMENT = CONTAINER_MENUS.register(
        "inserter_attachment",
        () -> IMenuTypeExtension.create((windowId, inv, data) -> {
            if (data != null) {
                BlockPos pos = data.readBlockPos();
                Direction dir = DirectionUtil.safeGet(data.readByte());
                RedstoneMode rm = RedstoneMode.get(data.readByte());
                BlacklistWhitelist bw = BlacklistWhitelist.get(data.readByte());
                boolean exactMode = data.readBoolean();
                InserterAttachmentType type = InserterAttachmentType.get(data.readByte());
                boolean fluidMode = data.readBoolean();

                // Read fluid filter contents from buffer
                FluidInventory fluidFilter = InserterAttachment.createFluidFilterInventory(null);
                for (int i = 0; i < InserterAttachment.MAX_FILTER_SLOTS; i++) {
                    FluidStack fluid = FluidStack.OPTIONAL_STREAM_CODEC.decode(data);
                    if (!fluid.isEmpty()) {
                        fluidFilter.setFluid(i, fluid);
                    }
                }

                return new InserterAttachmentContainerMenu(
                    windowId, inv.player, pos, dir, rm, bw, exactMode, type,
                    InserterAttachment.createItemFilterInventory(null),
                    fluidFilter, fluidMode
                );
            } else {
                return new InserterAttachmentContainerMenu(windowId, inv.player);
            }
        })
    );

    public static final DeferredHolder<MenuType<?>, MenuType<VoidAttachmentContainerMenu>> VOID_ATTACHMENT = CONTAINER_MENUS.register(
        "void_attachment",
        () -> IMenuTypeExtension.create((windowId, inv, data) -> {
            if (data != null) {
                BlockPos pos = data.readBlockPos();
                Direction dir = DirectionUtil.safeGet(data.readByte());
                BlacklistWhitelist bw = BlacklistWhitelist.get(data.readByte());
                boolean exactMode = data.readBoolean();
                boolean fluidMode = data.readBoolean();

                // Read fluid filter contents from buffer
                FluidInventory fluidFilter = VoidAttachment.createFluidFilterInventory(null);
                for (int i = 0; i < VoidAttachment.MAX_FILTER_SLOTS; i++) {
                    FluidStack fluid = FluidStack.OPTIONAL_STREAM_CODEC.decode(data);
                    if (!fluid.isEmpty()) {
                        fluidFilter.setFluid(i, fluid);
                    }
                }

                return new VoidAttachmentContainerMenu(
                    windowId, inv.player, pos, dir, bw, exactMode,
                    VoidAttachment.createItemFilterInventory(null),
                    fluidFilter, fluidMode
                );
            } else {
                return new VoidAttachmentContainerMenu(windowId, inv.player);
            }
        })
    );

    public static final DeferredHolder<MenuType<?>, MenuType<SensorAttachmentContainerMenu>> SENSOR_ATTACHMENT = CONTAINER_MENUS.register(
        "sensor_attachment",
        () -> IMenuTypeExtension.create((windowId, inv, data) -> {
            if (data != null) {
                BlockPos pos = data.readBlockPos();
                Direction dir = DirectionUtil.safeGet(data.readByte());
                BlacklistWhitelist bw = BlacklistWhitelist.get(data.readByte());
                boolean exactMode = data.readBoolean();
                boolean fluidMode = data.readBoolean();

                // Read fluid filter contents from buffer
                FluidInventory fluidFilter = SensorAttachment.createFluidFilterInventory(null);
                for (int i = 0; i < SensorAttachment.MAX_FILTER_SLOTS; i++) {
                    FluidStack fluid = FluidStack.OPTIONAL_STREAM_CODEC.decode(data);
                    if (!fluid.isEmpty()) {
                        fluidFilter.setFluid(i, fluid);
                    }
                }

                return new SensorAttachmentContainerMenu(
                    windowId, inv.player, pos, dir, bw, exactMode,
                    SensorAttachment.createItemFilterInventory(null),
                    fluidFilter, fluidMode
                );
            } else {
                return new SensorAttachmentContainerMenu(windowId, inv.player);
            }
        })
    );
}
