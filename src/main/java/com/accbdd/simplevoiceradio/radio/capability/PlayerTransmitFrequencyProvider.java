package com.accbdd.simplevoiceradio.radio.capability;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PlayerTransmitFrequencyProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerTransmitFrequency> PLAYER_TRANSMIT_FREQUENCY = CapabilityManager.get(new CapabilityToken<PlayerTransmitFrequency>() { });

    private PlayerTransmitFrequency frequency = null;
    private final LazyOptional<PlayerTransmitFrequency> optional = LazyOptional.of(this::createPlayerTransmitFrequency);

    private PlayerTransmitFrequency createPlayerTransmitFrequency() {
        if(this.frequency == null) {
            this.frequency = new PlayerTransmitFrequency();
        }

        return this.frequency;
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_TRANSMIT_FREQUENCY) {
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerTransmitFrequency().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerTransmitFrequency().loadNBTData(nbt);
    }
}
