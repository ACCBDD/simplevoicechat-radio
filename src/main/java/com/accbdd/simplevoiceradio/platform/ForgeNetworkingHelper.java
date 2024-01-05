package com.accbdd.simplevoiceradio.platform;

import com.accbdd.simplevoiceradio.ForgeLoader;
import com.accbdd.simplevoiceradio.networking.Packeter;
import com.accbdd.simplevoiceradio.services.NetworkingHelper;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class ForgeNetworkingHelper implements NetworkingHelper {
    @Override
    public void sendToPlayer(ServerPlayer player, Packeter packet) {
        ForgeLoader.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
