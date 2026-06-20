package com.piglinmine.fastpipes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class FPipesCapabilities {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // TODO 1.21.11: Capability registration needs IItemHandler->ResourceHandler<ItemResource> adapter; pipe<->machine connections will not work until ported.
        // Capabilities.Item.BLOCK now expects ResourceHandler<ItemResource>, Capabilities.Fluid.BLOCK expects ResourceHandler<FluidResource>,
        // Capabilities.Energy.BLOCK now uses EnergyHandler instead of IEnergyStorage. All getItemHandler/getFluidHandler/getEnergyStorage
        // methods on the BlockEntities still return the old types, so wiring is broken until an adapter layer is implemented.
    }
}
