package com.accbdd.simplevoiceradio.item;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.accbdd.simplevoiceradio.SimpleVoiceRadio;
import com.accbdd.simplevoiceradio.networking.packets.ClientboundRadioPacket;
import com.accbdd.simplevoiceradio.radio.Frequency;
import com.accbdd.simplevoiceradio.radio.RadioEnabled;
import com.accbdd.simplevoiceradio.registry.SoundRegistry;
import com.accbdd.simplevoiceradio.services.Services;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

    

public class RadioItem extends Item implements RadioEnabled {
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, SimpleVoiceRadio.MOD_ID);

    public static final RegistryObject<RadioItem> RADIO_ITEM = ITEMS.register("radio_item",
        () -> new RadioItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public RadioItem(Properties properties) {
        super(properties);
    }

    private void transmit(ServerPlayer player, boolean started) {
        Services.NETWORKING.sendToPlayer(player, new ClientboundRadioPacket(started, player.getUUID()));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean b) {
        super.inventoryTick(stack, level, entity, slot, b);
        tick(stack, level, entity);

        if (entity instanceof Player player && !level.isClientSide) {
            CompoundTag tag = stack.getOrCreateTag();

            String frequency = tag.getString("frequency");
            String modulation = tag.getString("modulation");

            UUID playerUUID = player.getUUID();
            if (tag.contains("user")) {
                UUID currentUUID = tag.getUUID("user");
                if (currentUUID.equals(playerUUID)) return;

                stopListening(frequency, Frequency.modulationOf(modulation), currentUUID);
            }

            listen(frequency, Frequency.modulationOf(modulation), playerUUID);
            tag.putUUID("user", playerUUID);
        }

    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltip) {
        CompoundTag tag = stack.getOrCreateTag();

        components.add(Component.literal(
                tag.getString("frequency") + tag.getString("modulation")
        ).withStyle(ChatFormatting.DARK_GRAY));

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

        //send started using packet
        if (!level.isClientSide) {
            transmit((ServerPlayer) player, true);
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

            // Send stopped using packet
            if (!level.isClientSide) {
                transmit((ServerPlayer) player, false);
            }

            player.getCooldowns().addCooldown(this, 10);
        }

        super.releaseUsing(stack, level, user, remainingUseTicks);
    }
}