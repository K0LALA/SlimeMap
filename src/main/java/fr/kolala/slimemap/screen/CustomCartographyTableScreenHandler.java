package fr.kolala.slimemap.screen;

import fr.kolala.slimemap.item.FilledSlimeMapItem;
import fr.kolala.slimemap.item.ModItems;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class CustomCartographyTableScreenHandler extends ScreenHandler {
    public static final int MAP_SLOT_INDEX = 0;
    public static final int MATERIAL_SLOT_INDEX = 1;
    public static final int RESULT_SLOT_INDEX = 2;
    private static final int INVENTORY_START = 3;
    private static final int INVENTORY_END = 30;
    private static final int HOTBAR_START = 30;
    private static final int HOTBAR_END = 39;
    private final ScreenHandlerContext context;
    long lastTakeResultTime;
    public final Inventory inventory = new SimpleInventory(2){

        @Override
        public void markDirty() {
            CustomCartographyTableScreenHandler.this.onContentChanged(this);
            super.markDirty();
        }
    };
    private final CraftingResultInventory resultInventory = new CraftingResultInventory(){

        @Override
        public void markDirty() {
            CustomCartographyTableScreenHandler.this.onContentChanged(this);
            super.markDirty();
        }
    };

    public CustomCartographyTableScreenHandler(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
        super(ScreenHandlerType.CARTOGRAPHY_TABLE, syncId);
        int i;
        this.context = context;
        this.addSlot(new Slot(this.inventory, MAP_SLOT_INDEX, 15, 15){

            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.FILLED_MAP) || stack.isOf(ModItems.FILLED_SLIME_MAP);
            }
        });
        this.addSlot(new Slot(this.inventory, MATERIAL_SLOT_INDEX, 15, 52){

            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.PAPER) || stack.isOf(Items.MAP) || stack.isOf(Items.GLASS_PANE) || stack.isOf(Items.SLIME_BALL) || stack.isOf(ModItems.SLIME_MAP);
            }
        });
        this.addSlot(new Slot(this.resultInventory, RESULT_SLOT_INDEX, 145, 39){

            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                CustomCartographyTableScreenHandler.this.slots.get(MAP_SLOT_INDEX).takeStack(1);
                CustomCartographyTableScreenHandler.this.slots.get(MATERIAL_SLOT_INDEX).takeStack(1);
                stack.getItem().onCraftByPlayer(stack, player.getWorld(), player);
                context.run((world, pos) -> {
                    long l = world.getTime();
                    if (CustomCartographyTableScreenHandler.this.lastTakeResultTime != l) {
                        world.playSound(null, pos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        CustomCartographyTableScreenHandler.this.lastTakeResultTime = l;
                    }
                });
                super.onTakeItem(player, stack);
            }
        });
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return CustomCartographyTableScreenHandler.canUse(this.context, player, Blocks.CARTOGRAPHY_TABLE);
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        ItemStack itemStack = this.inventory.getStack(MAP_SLOT_INDEX);
        ItemStack itemStack2 = this.inventory.getStack(MATERIAL_SLOT_INDEX);
        ItemStack itemStack3 = this.resultInventory.getStack(RESULT_SLOT_INDEX);
        if (!itemStack3.isEmpty() && (itemStack.isEmpty() || itemStack2.isEmpty())) {
            this.resultInventory.removeStack(2);
        } else if (!itemStack.isEmpty() && !itemStack2.isEmpty()) {
            this.updateResult(itemStack, itemStack2, itemStack3);
        }
    }

    private void updateResult(ItemStack map, ItemStack item, ItemStack oldResult) {
        this.context.run((world, pos) -> {
            ItemStack itemStack4;
            boolean isSlimeMap = map.isOf(ModItems.FILLED_SLIME_MAP);
            MapState mapState = isSlimeMap ?
                    FilledSlimeMapItem.getMapState(map, world) :
                    FilledMapItem.getMapState(map, world);
            if (mapState == null) {
                return;
            }

            if ((isSlimeMap && item.isOf(Items.SLIME_BALL) || !isSlimeMap && item.isOf(Items.PAPER))
                    && !mapState.locked && mapState.scale < 4) {
                itemStack4 = map.copyWithCount(1);
                itemStack4.getOrCreateNbt().putInt("map_scale_direction", 1);
                this.sendContentUpdates();
            } else if (item.isOf(Items.GLASS_PANE) && !mapState.locked) {
                itemStack4 = map.copyWithCount(1);
                itemStack4.getOrCreateNbt().putBoolean("map_to_lock", true);
                this.sendContentUpdates();
            } else if ((isSlimeMap && item.isOf(ModItems.SLIME_MAP)) || (!isSlimeMap && item.isOf(Items.MAP))) {
                itemStack4 = map.copyWithCount(2);
                this.sendContentUpdates();
            } else {
                this.resultInventory.removeStack(RESULT_SLOT_INDEX);
                this.sendContentUpdates();
                return;
            }

            if (!ItemStack.areEqual(itemStack4, oldResult)) {
                this.resultInventory.setStack(RESULT_SLOT_INDEX, itemStack4);
                this.sendContentUpdates();
            }
        });
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.inventory != this.resultInventory && super.canInsertIntoSlot(stack, slot);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = this.slots.get(slot);
        if (slot2.hasStack()) {
            ItemStack itemStack2 = slot2.getStack();
            itemStack = itemStack2.copy();
            if (slot == RESULT_SLOT_INDEX) {
                itemStack2.getItem().onCraftByPlayer(itemStack2, player.getWorld(), player);
                if (!this.insertItem(itemStack2, INVENTORY_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot2.onQuickTransfer(itemStack2, itemStack);
            } else if (slot == MATERIAL_SLOT_INDEX || slot == MAP_SLOT_INDEX ? !this.insertItem(itemStack2, INVENTORY_START, HOTBAR_END, false) : (itemStack2.isOf(Items.FILLED_MAP) || itemStack2.isOf(ModItems.FILLED_SLIME_MAP) ? !this.insertItem(itemStack2, MAP_SLOT_INDEX, MATERIAL_SLOT_INDEX, false) : (itemStack2.isOf(Items.PAPER) || itemStack2.isOf(Items.MAP) || itemStack2.isOf(Items.GLASS_PANE) || itemStack2.isOf(ModItems.SLIME_MAP) || itemStack2.isOf(Items.SLIME_BALL) ? !this.insertItem(itemStack2, MATERIAL_SLOT_INDEX, RESULT_SLOT_INDEX, false) : (slot >= INVENTORY_START && slot < INVENTORY_END ? !this.insertItem(itemStack2, HOTBAR_START, HOTBAR_END, false) : slot >= HOTBAR_START && slot < HOTBAR_END && !this.insertItem(itemStack2, INVENTORY_START, INVENTORY_END, false))))) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot2.setStack(ItemStack.EMPTY);
            }
            slot2.markDirty();
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot2.onTakeItem(player, itemStack2);
            this.sendContentUpdates();
        }
        return itemStack;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.resultInventory.removeStack(2);
        this.context.run((world, pos) -> this.dropInventory(player, this.inventory));
    }
}