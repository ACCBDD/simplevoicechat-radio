package com.accbdd.simplevoiceradio.registry;

import com.accbdd.simplevoiceradio.SimpleVoiceRadio;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SimpleVoiceRadio.MOD_ID);

    public static final RegistryObject<SoundEvent> RADIO_CLOSE = registerSound("radio_close");
    public static final RegistryObject<SoundEvent> RADIO_OPEN = registerSound("radio_open");

    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUNDS.register(name, () -> new SoundEvent(new ResourceLocation(SimpleVoiceRadio.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}
