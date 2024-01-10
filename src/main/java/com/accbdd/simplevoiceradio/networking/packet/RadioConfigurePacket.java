package com.accbdd.simplevoiceradio.networking.packet;

import java.util.function.Supplier;

import com.accbdd.simplevoiceradio.SimpleVoiceRadio;
import com.accbdd.simplevoiceradio.networking.Packeter;
import com.accbdd.simplevoiceradio.radio.Frequency;
import com.accbdd.simplevoiceradio.registry.ItemRegistry;
import com.accbdd.simplevoiceradio.registry.item.RadioItem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public record RadioConfigurePacket(int frequency) implements Packeter {
    public static ResourceLocation ID = new ResourceLocation(SimpleVoiceRadio.MOD_ID, "radio_configure_packet");
    @Override
    public ResourceLocation resource() {
        return ID;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(frequency);
    }
    
    public static RadioConfigurePacket decode(FriendlyByteBuf buffer) {
        return new RadioConfigurePacket(buffer.readInt());
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        String freq = String.format("%0"+Frequency.FREQUENCY_DIGITS+"d", this.frequency);
        String freqFormatted = new StringBuilder(freq).insert(freq.length() - Frequency.FREQUENCY_DECIMAL_PLACES, ".").toString();

        context.enqueueWork(() -> {
            //on the server
            RadioItem radioItem = ItemRegistry.RADIO_ITEM.get();
            ItemStack radio = context.getSender().getMainHandItem();
            if(radio.getItem() != radioItem)
                return;
            radio.getOrCreateTag().putString("changeFrequency", freqFormatted);
            SimpleVoiceRadio.LOGGER.info("config packet recieved!");
        });

        return true;
    }
}
