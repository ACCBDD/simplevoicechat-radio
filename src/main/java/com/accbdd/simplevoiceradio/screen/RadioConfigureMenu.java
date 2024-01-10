package com.accbdd.simplevoiceradio.screen;

import com.accbdd.simplevoiceradio.registry.MenuRegistry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class RadioConfigureMenu extends AbstractContainerMenu {
    public final ItemStack radio;
    public final int radio_location;
    public final Inventory inventory;

    public RadioConfigureMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, extraData.readInt());
    }

    public RadioConfigureMenu(int id, Inventory inv, int radio_location) {
        super(MenuRegistry.RADIO_CONFIG_MENU.get(), id);
        this.inventory = inv;
        this.radio_location = radio_location;
        this.radio = inv.getItem(radio_location);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int movedIndex) {
        return player.getInventory().getItem(movedIndex);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
    
}
