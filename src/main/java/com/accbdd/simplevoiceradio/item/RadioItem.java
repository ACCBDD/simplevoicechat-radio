package com.accbdd.simplevoiceradio.item;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.accbdd.simplevoiceradio.SimpleVoiceRadio;
import com.accbdd.simplevoiceradio.networking.NetworkingManager;
import com.accbdd.simplevoiceradio.networking.packet.RadioTransmitPacket;
import com.accbdd.simplevoiceradio.radio.RadioEnabled;
import com.accbdd.simplevoiceradio.registry.SoundRegistry;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class RadioItem extends Item implements RadioEnabled {
    public RadioItem(Properties properties) {
        super(properties);
    }

    private void transmit(boolean started) {
        NetworkingManager.sendToServer(new RadioTransmitPacket(started, RadioTransmitPacket.PacketContext.ITEM));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean b) {
        super.inventoryTick(stack, level, entity, slot, b);
        tick(stack, level, entity);

        if (!entity.isAlive()) {
            SimpleVoiceRadio.LOGGER.debug("player is dying");
        }

        if (!level.isClientSide) {
            if (entity instanceof Player player) {
                CompoundTag tag = stack.getOrCreateTag();
                String frequency = tag.getString("frequency");
                UUID playerUUID = player.getUUID();

                //check if frequency has been changed
                if (tag.contains("changeFrequency")) {
                    String changeFreq = tag.getString("changeFrequency");
                    if (!frequency.equals(changeFreq)) {
                        stopListening(frequency, playerUUID);
                        tag.remove("changeFrequency");
                        tag.putString("frequency", changeFreq);
                        listen(changeFreq, playerUUID);
                    }
                }
                
                if (tag.contains("user")) {
                    UUID currentUUID = tag.getUUID("user");
                    if (currentUUID.equals(playerUUID)) {
                        return;
                    }

                    stopListening(frequency, currentUUID);
                }

                listen(frequency, playerUUID);
                tag.putUUID("user", playerUUID);
            } else {
                //no longer in a player's inventory, remove last held (current UUID) from listening
                CompoundTag tag = stack.getOrCreateTag();
                String frequency = tag.getString("frequency");
                if (tag.contains("user")) {
                    UUID currentUUID = tag.getUUID("user");
                    stopListening(frequency, currentUUID);
                    tag.remove("user");
                }
            }
        }
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player)
    {
        CompoundTag tag = item.getOrCreateTag();
        String frequency = tag.getString("frequency");

        if (tag.contains("user")) {
            UUID currentUUID = tag.getUUID("user");
            stopListening(frequency, currentUUID);
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltip) {
        CompoundTag tag = stack.getOrCreateTag();

        components.add(Component.literal(
                tag.getString("frequency") + " mHz"
        ).withStyle(ChatFormatting.GRAY));

        super.appendHoverText(stack, level, components, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(
            null, player.blockPosition(),
            SoundRegistry.RADIO_OPEN.get(),
            SoundSource.PLAYERS,
            1f,1f
        );
        player.startUsingItem(hand);

        //send started using packet to server
        if (level.isClientSide) {
            transmit(true);
        }

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.TOOT_HORN;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingUseTicks) {
        if (user instanceof Player player) {
            level.playSound(
                null, user.blockPosition(),
                SoundRegistry.RADIO_CLOSE.get(),
                SoundSource.PLAYERS,
                1f,1f
            );

            //send stopped using packet to server
            if (level.isClientSide) {
                transmit(false);
            }

            player.getCooldowns().addCooldown(this, 10);
        }

        super.releaseUsing(stack, level, user, remainingUseTicks);
    }
}