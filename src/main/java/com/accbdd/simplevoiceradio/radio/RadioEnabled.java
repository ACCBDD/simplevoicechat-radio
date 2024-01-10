package com.accbdd.simplevoiceradio.radio;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface RadioEnabled {
    Random RANDOM = new Random();

    default CompoundTag setFrequency(ItemStack stack, String frequencyName) {
        CompoundTag tag = stack.getOrCreateTag();

        tag.putString("frequency", frequencyName);

        return tag;
    }

    default CompoundTag getFrequency(ItemStack stack, String frequencyName) {
        CompoundTag tag = stack.getOrCreateTag();

        tag.putString("frequency", frequencyName);

        return tag;
    }

    default RadioChannel listen(String frequencyName, UUID owner) {
        Frequency frequency = Frequency.getOrCreateFrequency(frequencyName);
        return frequency.tryAddListener(owner);
    }

    default void stopListening(String frequencyName, UUID owner) {
        Frequency frequency = Frequency.getOrCreateFrequency(frequencyName);
        frequency.removeListener(owner);
    }

    default void tick(ItemStack stack, Level level, Entity entity) {
        if (level.isClientSide) return;
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("frequency") || tag.getString("frequency").isEmpty())
            setFrequency(stack,
                    "001.00"
            );
    }

    default void appendTooltip(ItemStack stack, List<Component> components) {
        CompoundTag tag = stack.getOrCreateTag();

        components.add(Component.literal(
                tag.getString("frequency") + " kHz"
        ).withStyle(ChatFormatting.DARK_GRAY));
    }
}
