package com.accbdd.simplevoiceradio.services;

import com.accbdd.simplevoiceradio.networking.Packeter;

import net.minecraft.server.level.ServerPlayer;

public interface NetworkingHelper {
    public void sendToPlayer(ServerPlayer player, Packeter packet);
}
