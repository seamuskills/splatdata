package com.seamus.splatdata.menus;

import com.seamus.splatdata.menus.buttons.MenuButton;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class MenuContainer implements Container, MenuProvider
{
    public final MenuSize size;
    public final Component displayTitle;
    protected final NonNullList<MenuButton> buttons;
    public final ServerPlayer player;

    public static final MenuButton EMPTY_SLOT = new MenuButton(ItemStack.EMPTY, "") {
        @Override
        public void onClick(ServerPlayer player) {}
    };

    public MenuContainer(MenuSize size, Component displayTitle, ServerPlayer player)
    {
        this.displayTitle = displayTitle;
        this.size = size;
        this.player = player;
        buttons = NonNullList.withSize(size.slots, EMPTY_SLOT);

        init(player);
    }

    public abstract void init(ServerPlayer player);

    public void addButton(int row, int column, MenuButton button)
    {
        addButton(size.rowWidth * row + column, button);
    }

    public void removeButton(int row, int column)
    {
        removeButton(size.rowWidth * column + row);
    }

    public void addButton(int slot, MenuButton button)
    {
        buttons.set(slot, button);
    }

    public void removeButton(int slot)
    {
        buttons.set(slot, EMPTY_SLOT);
    }

    @Override
    public int getContainerSize() {
        return size.slots;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getItem(int slot)
    {
        ItemStack stack = buttons.get(slot).getDisplayItem().copy();
        stack.setHoverName(buttons.get(slot).getDisplayText());

        return stack;
    }

    @Override
    public ItemStack removeItem(int slot, int amount)
    {
        buttons.get(slot).onClick(player);
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {

    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {

    }

    @Override
    public Component getDisplayName() {
        return displayTitle;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player)
    {
        switch (size)
        {
            case SIZE_1X5 -> {return new HopperMenu(id, playerInventory, this);}
            case SIZE_3X3 -> {return new DispenserMenu(id, playerInventory, this);}
            case SIZE_1X9 -> {return new ChestMenu(MenuType.GENERIC_9x1, id, playerInventory, this, 1);}
            case SIZE_2X9 -> {return new ChestMenu(MenuType.GENERIC_9x2, id, playerInventory, this, 2);}
            case SIZE_3X9 -> {return ChestMenu.threeRows(id, playerInventory, this);}
            case SIZE_4X9 -> {return new ChestMenu(MenuType.GENERIC_9x4, id, playerInventory, this, 3);}
            case SIZE_5X9 -> {return new ChestMenu(MenuType.GENERIC_9x5, id, playerInventory, this, 4);}
            default -> {return ChestMenu.sixRows(id, playerInventory, this);}

        }
    }

    public enum MenuSize
    {
        SIZE_1X5(5, 5),
        SIZE_3X3(9, 3),
        SIZE_1X9(9, 9),
        SIZE_2X9(18, 9),
        SIZE_3X9(27, 9),
        SIZE_4X9(36, 9),
        SIZE_5X9(45, 9),
        SIZE_6X9(54, 9)
        ;

        public final int slots;
        public final int rowWidth;
        MenuSize(int slots, int rowWidth)
        {
            this.slots = slots;
            this.rowWidth = rowWidth;
        }
    }
}