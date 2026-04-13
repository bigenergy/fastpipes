package com.piglinmine.fastpipes.menu;
import com.piglinmine.fastpipes.util.CapabilityUtil;

import com.piglinmine.fastpipes.FPipesContainerMenus;
import com.piglinmine.fastpipes.block.TerminalBlock;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.Objects;

import com.piglinmine.fastpipes.blockentity.TerminalBlockEntity;
import com.piglinmine.fastpipes.network.FastPipesNetwork;
import com.piglinmine.fastpipes.network.message.TerminalItemsSyncMessage;
import com.piglinmine.fastpipes.network.message.TerminalStatusMessage;
import net.minecraft.server.level.ServerPlayer;

public class TerminalContainerMenu extends BaseContainerMenu {
    private final BlockPos terminalPos;
    private final Player player;

    // Crafting
    private final TransientCraftingContainer craftMatrix = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer craftResult = new ResultContainer();

    // Network items — aggregated view sent to client
    private List<ItemStack> networkItems = new ArrayList<>();
    private List<ItemStack> filteredItems = new ArrayList<>();
    private String searchText = "";
    private int scrollOffset = 0;
    private boolean loading = false;

    // 9 columns x 5 rows visible grid
    public static final int GRID_COLS = 9;
    public static final int GRID_ROWS = 5;
    public static final int GRID_SIZE = GRID_COLS * GRID_ROWS;

    // Sorting
    public enum SortMode { NAME, COUNT, MOD }
    private SortMode sortMode = SortMode.NAME;

    // Server constructor
    public TerminalContainerMenu(int windowId, Player player, BlockPos terminalPos) {
        super(FPipesContainerMenus.TERMINAL.get(), windowId, player);
        this.terminalPos = terminalPos;
        this.player = player;

        // Crafting result slot - below network grid
        addSlot(new ResultSlot(player, craftMatrix, craftResult, 0, 90, 154));

        // Crafting grid 3x3 - below network grid
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(craftMatrix, col + row * 3, 8 + col * 18, 136 + row * 18));
            }
        }

        // Player inventory at bottom
        addPlayerInventory(8, 204);

        // Load saved crafting grid from block entity
        if (!player.level().isClientSide) {
            if (player.level().getBlockEntity(terminalPos) instanceof TerminalBlockEntity be) {
                loading = true;
                for (int i = 0; i < 9; i++) {
                    craftMatrix.setItem(i, be.getCraftGrid()[i].copy());
                }
                loading = false;
                updateCraftResult();
            }
        }
    }

    // Client constructor
    public TerminalContainerMenu(int windowId, Player player) {
        this(windowId, player, BlockPos.ZERO);
    }

    private int tickCounter = 0;

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (!player.level().isClientSide) {
            tickCounter++;
            // Fast sync every 4 ticks during first second (client needs time to create menu)
            // Then periodic sync every second
            boolean shouldRefresh = tickCounter <= 20 ? (tickCounter % 4 == 0) : (tickCounter % 20 == 0);
            if (shouldRefresh) {
                refreshNetworkItems();
            }
        }
    }

    public void refreshNetworkItems() {
        if (player.level().isClientSide) return;

        Map<ItemStackKey, Long> aggregated = new LinkedHashMap<>();

        Pipe connectedPipe = TerminalBlock.findConnectedPipe(player.level(), terminalPos);
        if (connectedPipe == null || connectedPipe.getNetwork() == null) {
            networkItems = new ArrayList<>();
            applyFilter();
            sendStatus("gui.fastpipes.terminal.no_network");
            syncToClient();
            return;
        }

        List<ConnectedInventory> handlers = getAllConnectedHandlers(connectedPipe);
        for (ConnectedInventory inv : handlers) {
            for (int i = 0; i < inv.handler().getSlots(); i++) {
                ItemStack stack = inv.handler().getStackInSlot(i);
                if (stack.isEmpty()) continue;

                ItemStackKey key = new ItemStackKey(stack);
                aggregated.merge(key, (long) stack.getCount(), Long::sum);
            }
        }

        networkItems = new ArrayList<>();
        for (Map.Entry<ItemStackKey, Long> entry : aggregated.entrySet()) {
            ItemStack display = entry.getKey().stack.copy();
            display.setCount((int) Math.min(entry.getValue(), Integer.MAX_VALUE));
            networkItems.add(display);
        }

        applySorting();
        applyFilter();
        syncToClient();
    }

    private void syncToClient() {
        if (player instanceof ServerPlayer serverPlayer) {
            FastPipesNetwork.sendToClient(serverPlayer, new TerminalItemsSyncMessage(networkItems));
        }
    }

    private void sendStatus(String langKey) {
        if (player instanceof ServerPlayer serverPlayer) {
            FastPipesNetwork.sendToClient(serverPlayer, new TerminalStatusMessage(langKey));
        }
    }

    /**
     * Called on client when receiving network items from server.
     */
    public void receiveNetworkItems(List<ItemStack> items) {
        this.networkItems = new ArrayList<>(items);
        applyFilter();
    }

    public void setSearchText(String text) {
        this.searchText = text.toLowerCase(Locale.ROOT);
        this.scrollOffset = 0;
        applyFilter();
    }

    private void applyFilter() {
        if (searchText.isEmpty()) {
            filteredItems = new ArrayList<>(networkItems);
        } else {
            filteredItems = new ArrayList<>();
            if (searchText.startsWith("@")) {
                // Search by mod id
                String modQuery = searchText.substring(1);
                for (ItemStack stack : networkItems) {
                    String namespace = ForgeRegistries.ITEMS.getKey(stack.getItem()).getNamespace();
                    if (namespace.contains(modQuery)) {
                        filteredItems.add(stack);
                    }
                }
            } else {
                for (ItemStack stack : networkItems) {
                    if (stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(searchText)) {
                        filteredItems.add(stack);
                    }
                }
            }
        }
    }

    private void applySorting() {
        switch (sortMode) {
            case NAME -> networkItems.sort(Comparator.comparing(s -> s.getHoverName().getString()));
            case COUNT -> networkItems.sort(Comparator.comparingInt(s -> ((ItemStack) s).getCount()).reversed());
            case MOD -> networkItems.sort(Comparator.comparing((ItemStack s) ->
                    net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(s.getItem()).getNamespace())
                .thenComparing(s -> s.getHoverName().getString()));
        }
    }

    public SortMode getSortMode() {
        return sortMode;
    }

    public void setSortMode(SortMode mode) {
        this.sortMode = mode;
        applySorting();
        applyFilter();
    }

    public void cycleSortMode() {
        SortMode[] modes = SortMode.values();
        sortMode = modes[(sortMode.ordinal() + 1) % modes.length];
        applySorting();
        applyFilter();
    }

    public List<ItemStack> getFilteredItems() {
        return filteredItems;
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public void setScrollOffset(int offset) {
        int maxOffset = Math.max(0, (filteredItems.size() + GRID_COLS - 1) / GRID_COLS - GRID_ROWS);
        this.scrollOffset = Math.max(0, Math.min(offset, maxOffset));
    }

    public int getMaxScrollOffset() {
        return Math.max(0, (filteredItems.size() + GRID_COLS - 1) / GRID_COLS - GRID_ROWS);
    }

    public List<ItemStack> getVisibleItems() {
        int start = scrollOffset * GRID_COLS;
        int end = Math.min(start + GRID_SIZE, filteredItems.size());
        if (start >= filteredItems.size()) return Collections.emptyList();
        return filteredItems.subList(start, end);
    }

    /**
     * Extract item from network into player cursor or inventory.
     * @param stack the item type to extract
     * @param amount how many to extract
     * @param toCursor true = put on cursor, false = put in player inventory
     */
    public void extractItem(ItemStack stack, int amount, boolean toCursor) {
        if (player.level().isClientSide) return;

        Pipe connectedPipe = TerminalBlock.findConnectedPipe(player.level(), terminalPos);
        if (connectedPipe == null || connectedPipe.getNetwork() == null) {
            sendStatus("gui.fastpipes.terminal.no_network");
            return;
        }

        List<ConnectedInventory> handlers = getAllConnectedHandlers(connectedPipe);
        int remaining = amount;

        for (ConnectedInventory inv : handlers) {
            if (remaining <= 0) break;

            for (int i = 0; i < inv.handler().getSlots() && remaining > 0; i++) {
                ItemStack slotStack = inv.handler().getStackInSlot(i);
                if (slotStack.isEmpty() || !ItemStack.isSameItemSameTags(slotStack, stack)) continue;

                ItemStack extracted = inv.handler().extractItem(i, remaining, false);
                if (!extracted.isEmpty()) {
                    if (toCursor) {
                        ItemStack carried = getCarried();
                        if (carried.isEmpty()) {
                            setCarried(extracted);
                        } else if (ItemStack.isSameItemSameTags(carried, extracted)) {
                            int canAdd = Math.min(extracted.getCount(), carried.getMaxStackSize() - carried.getCount());
                            carried.grow(canAdd);
                            if (canAdd < extracted.getCount()) {
                                ItemStack leftover = extracted.copy();
                                leftover.setCount(extracted.getCount() - canAdd);
                                inv.handler().insertItem(i, leftover, false);
                                remaining -= canAdd;
                                continue;
                            }
                        } else {
                            inv.handler().insertItem(i, extracted, false);
                            continue;
                        }
                    } else {
                        ItemStack leftover = ItemHandlerHelper.insertItemStacked(
                            new PlayerInventoryWrapper(player), extracted, false);
                        if (!leftover.isEmpty()) {
                            inv.handler().insertItem(i, leftover, false);
                            remaining -= (extracted.getCount() - leftover.getCount());
                            continue;
                        }
                    }
                    remaining -= extracted.getCount();
                }
            }
        }

        refreshNetworkItems();
    }

    /**
     * Insert item from player cursor into network.
     */
    public void insertItem(ItemStack toInsert) {
        if (player.level().isClientSide || toInsert.isEmpty()) return;

        Pipe connectedPipe = TerminalBlock.findConnectedPipe(player.level(), terminalPos);
        if (connectedPipe == null || connectedPipe.getNetwork() == null) {
            sendStatus("gui.fastpipes.terminal.no_network");
            return;
        }
        ItemStack remaining = insertIntoNetworkDirect(toInsert.copy(), connectedPipe);
        setCarried(remaining);
        if (!remaining.isEmpty()) {
            sendStatus("gui.fastpipes.terminal.network_full");
        }
        // Force immediate sync of carried item back to client
        if (player instanceof ServerPlayer) {
            super.broadcastChanges();
        }
        refreshNetworkItems();
    }

    /**
     * Insert a stack into the network, returns whatever couldn't be inserted.
     */
    public ItemStack insertIntoNetwork(ItemStack stack) {
        if (player.level().isClientSide || stack.isEmpty()) return stack;
        Pipe connectedPipe = TerminalBlock.findConnectedPipe(player.level(), terminalPos);
        return insertIntoNetworkDirect(stack, connectedPipe);
    }

    /**
     * Try to craft using items from the network.
     */
    public void performCraft(boolean craftAll) {
        if (player.level().isClientSide) return;

        Level level = player.level();
        Optional<CraftingRecipe> recipeOpt = level.getServer()
            .getRecipeManager()
            .getRecipeFor(RecipeType.CRAFTING, craftMatrix, level);

        if (recipeOpt.isEmpty()) return;

        CraftingRecipe recipe = recipeOpt.get();
        ItemStack result = recipe.assemble(craftMatrix, level.registryAccess());
        if (result.isEmpty()) return;

        int maxCrafts = craftAll ? 64 : 1;
        int crafted = 0;

        for (int c = 0; c < maxCrafts; c++) {
            // Check we can still craft
            if (!recipe.matches(craftMatrix, level)) break;

            result = recipe.assemble(craftMatrix, level.registryAccess());

            // Check player can hold the result
            ItemStack carried = getCarried();
            if (!carried.isEmpty()) {
                if (!ItemStack.isSameItemSameTags(carried, result)) break;
                if (carried.getCount() + result.getCount() > carried.getMaxStackSize()) break;
            }

            // Consume ingredients from craft grid
            List<ItemStack> remainders = recipe.getRemainingItems(craftMatrix);
            for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
                ItemStack ingredient = craftMatrix.getItem(i);
                if (!ingredient.isEmpty()) {
                    ingredient.shrink(1);
                    if (ingredient.isEmpty()) {
                        craftMatrix.setItem(i, remainders.get(i));
                    }
                }
            }

            // Give result
            if (carried.isEmpty()) {
                setCarried(result.copy());
            } else {
                carried.grow(result.getCount());
            }
            crafted++;
        }

        if (crafted > 0) {
            // Refill craft grid from network
            refillCraftGrid();
            refreshNetworkItems();
        }
    }

    /**
     * Refill the crafting grid from network inventories after crafting.
     * Tries to re-supply each slot with matching items from connected inventories.
     */
    private void refillCraftGrid() {
        // Refill is a no-op for now — we don't track what was in each slot originally.
        // The user can use JEI recipe transfer to refill.
    }

    @Override
    public void removed(Player player) {
        saveCraftGrid();
        super.removed(player);
    }

    private void saveCraftGrid() {
        if (player.level().isClientSide) return;
        if (player.level().getBlockEntity(terminalPos) instanceof TerminalBlockEntity be) {
            for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
                be.setCraftGrid(i, craftMatrix.getItem(i).copy());
            }
        }
    }

    /**
     * Handle JEI recipe transfer — fill craft grid from player inventory + network.
     */
    public void handleRecipeTransfer(List<ItemStack> ingredients) {
        if (player.level().isClientSide) return;

        Pipe connectedPipe = TerminalBlock.findConnectedPipe(player.level(), terminalPos);

        // Return current craft grid items to network/player
        for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
            ItemStack current = craftMatrix.removeItemNoUpdate(i);
            if (!current.isEmpty()) {
                ItemStack remaining = insertIntoNetworkDirect(current, connectedPipe);
                if (!remaining.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(remaining);
                }
            }
        }

        // Fill craft grid from player inventory + network
        for (int i = 0; i < Math.min(ingredients.size(), 9); i++) {
            ItemStack wanted = ingredients.get(i);
            if (wanted.isEmpty()) continue;

            // First try player inventory
            ItemStack found = findAndExtractFromPlayerInventory(wanted, 1);
            if (found.isEmpty() && connectedPipe != null && connectedPipe.getNetwork() != null) {
                found = findAndExtractFromNetwork(wanted, 1, connectedPipe);
            }
            if (!found.isEmpty()) {
                craftMatrix.setItem(i, found);
            }
        }

        updateCraftResult();
        saveCraftGrid();
        refreshNetworkItems();
    }

    private ItemStack insertIntoNetworkDirect(ItemStack stack, Pipe connectedPipe) {
        if (connectedPipe == null || connectedPipe.getNetwork() == null) return stack;
        List<ConnectedInventory> handlers = getAllConnectedHandlers(connectedPipe);
        ItemStack remaining = stack.copy();

        for (ConnectedInventory inv : handlers) {
            if (remaining.isEmpty()) break;
            remaining = ItemHandlerHelper.insertItemStacked(inv.handler(), remaining, false);
        }
        return remaining;
    }

    private ItemStack findAndExtractFromPlayerInventory(ItemStack wanted, int amount) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack invStack = player.getInventory().items.get(i);
            if (!invStack.isEmpty() && ItemStack.isSameItemSameTags(invStack, wanted)) {
                return player.getInventory().removeItem(i, amount);
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack findAndExtractFromNetwork(ItemStack wanted, int amount, Pipe connectedPipe) {
        List<ConnectedInventory> handlers = getAllConnectedHandlers(connectedPipe);

        for (ConnectedInventory inv : handlers) {
            for (int i = 0; i < inv.handler().getSlots(); i++) {
                ItemStack slotStack = inv.handler().getStackInSlot(i);
                if (!slotStack.isEmpty() && ItemStack.isSameItemSameTags(slotStack, wanted)) {
                    ItemStack extracted = inv.handler().extractItem(i, amount, false);
                    if (!extracted.isEmpty()) return extracted;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(terminalPos.getX() + 0.5, terminalPos.getY() + 0.5, terminalPos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack slotStack = slot.getItem();
        ItemStack original = slotStack.copy();

        // Craft result slot (index 0) — move to player inventory
        if (index == 0) {
            if (!moveItemStackTo(slotStack, 10, 46, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(slotStack, original);
        }
        // Craft grid slots (1-9) — move to player inventory
        else if (index >= 1 && index <= 9) {
            if (!moveItemStackTo(slotStack, 10, 46, false)) {
                return ItemStack.EMPTY;
            }
        }
        // Player inventory (10-45) — try network first, then craft grid
        else if (index >= 10 && index <= 45) {
            // Try inserting into network
            ItemStack remaining = insertIntoNetwork(slotStack.copy());
            int inserted = slotStack.getCount() - remaining.getCount();
            if (inserted > 0) {
                slotStack.shrink(inserted);
                if (!remaining.isEmpty()) {
                    sendStatus("gui.fastpipes.terminal.network_full");
                }
                refreshNetworkItems();
            } else {
                // If network is full, try craft grid
                if (!moveItemStackTo(slotStack, 1, 10, false)) {
                    sendStatus("gui.fastpipes.terminal.network_full");
                    return ItemStack.EMPTY;
                }
            }
        }

        if (slotStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (slotStack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, slotStack);
        return original;
    }

    @Override
    public void slotsChanged(net.minecraft.world.Container container) {
        super.slotsChanged(container);
        if (container == craftMatrix && !loading) {
            updateCraftResult();
            saveCraftGrid();
        }
    }

    private void updateCraftResult() {
        if (player.level().isClientSide) return;
        Level level = player.level();

        Optional<CraftingRecipe> recipe = level.getServer()
            .getRecipeManager()
            .getRecipeFor(RecipeType.CRAFTING, craftMatrix, level);

        if (recipe.isPresent()) {
            craftResult.setItem(0, recipe.get().assemble(craftMatrix, level.registryAccess()));
        } else {
            craftResult.setItem(0, ItemStack.EMPTY);
        }
    }

    public BlockPos getTerminalPos() {
        return terminalPos;
    }

    /**
     * A connected inventory found adjacent to a pipe in the network.
     */
    private record ConnectedInventory(BlockPos pos, Direction accessSide, IItemHandler handler) {}

    /**
     * Scans ALL pipes in the network and finds every adjacent IItemHandler,
     * regardless of attachment type. This allows the terminal to see inventories
     * connected via extractors, inserters, or bare pipe faces.
     */
    private List<ConnectedInventory> getAllConnectedHandlers(Pipe connectedPipe) {
        if (connectedPipe == null || connectedPipe.getNetwork() == null) return Collections.emptyList();

        Set<Pipe> pipes = connectedPipe.getNetwork().getPipes();
        Set<BlockPos> scanned = new HashSet<>();
        List<ConnectedInventory> result = new ArrayList<>();
        Level level = player.level();

        for (Pipe pipe : pipes) {
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pipe.getPos().relative(dir);
                if (!scanned.add(neighborPos)) continue;
                // Skip positions that are pipes themselves
                if (NetworkManager.get(level).getPipe(neighborPos) != null) continue;

                IItemHandler handler = CapabilityUtil.getItemHandler(level, neighborPos, dir.getOpposite());
                if (handler != null) {
                    result.add(new ConnectedInventory(neighborPos, dir.getOpposite(), handler));
                }
            }
        }
        return result;
    }

    // Helper key for aggregating same items
    private record ItemStackKey(ItemStack stack) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemStackKey other)) return false;
            return ItemStack.isSameItemSameTags(stack, other.stack);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ForgeRegistries.ITEMS.getKey(stack.getItem()), stack.getTag());
        }
    }

    // Wrapper to insert into player inventory via IItemHandler
    private static class PlayerInventoryWrapper implements IItemHandler {
        private final Player player;

        PlayerInventoryWrapper(Player player) {
            this.player = player;
        }

        @Override
        public int getSlots() {
            return player.getInventory().items.size();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return player.getInventory().items.get(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            ItemStack existing = player.getInventory().items.get(slot);
            if (stack.isEmpty()) return ItemStack.EMPTY;

            if (!existing.isEmpty()) {
                if (!ItemStack.isSameItemSameTags(existing, stack)) return stack;
                int canAdd = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
                if (canAdd <= 0) return stack;
                if (!simulate) existing.grow(canAdd);
                if (canAdd >= stack.getCount()) return ItemStack.EMPTY;
                ItemStack leftover = stack.copy();
                leftover.shrink(canAdd);
                return leftover;
            } else {
                int canAdd = Math.min(stack.getCount(), stack.getMaxStackSize());
                if (!simulate) {
                    ItemStack placed = stack.copy();
                    placed.setCount(canAdd);
                    player.getInventory().items.set(slot, placed);
                }
                if (canAdd >= stack.getCount()) return ItemStack.EMPTY;
                ItemStack leftover = stack.copy();
                leftover.shrink(canAdd);
                return leftover;
            }
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }
    }
}
