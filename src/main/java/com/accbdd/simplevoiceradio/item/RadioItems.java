package com.accbdd.simplevoiceradio.item;

import com.accbdd.simplevoiceradio.SimpleVoiceRadio;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

    

public class RadioItems {
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, SimpleVoiceRadio.MOD_ID);

    public static final RegistryObject<Item> RADIO_ITEM = ITEMS.register("radio_item",
        () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}