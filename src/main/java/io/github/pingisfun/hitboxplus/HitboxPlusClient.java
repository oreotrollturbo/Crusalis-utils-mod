package io.github.pingisfun.hitboxplus;

import io.github.pingisfun.hitboxplus.waypoints.FlagsBrokenDetector;
import io.github.pingisfun.hitboxplus.waypoints.FlagsPlacedDetector;
import io.github.pingisfun.hitboxplus.waypoints.PlayerCoordSharing;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import static java.lang.Math.abs;


public class HitboxPlusClient implements ClientModInitializer {

    ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

    @Override
    public void onInitializeClient() {

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {

            FlagsPlacedDetector.handleTownAttack(message.toString());
            FlagsPlacedDetector.handleTownLiberation(message.toString());

            FlagsBrokenDetector.handleFlags(message.toString());

            PlayerCoordSharing.handleServerWaypoint(message.toString());

        });

        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {

            PlayerCoordSharing.handlePlayerWaypoint(message.toString(),sender);

        });

    }


}