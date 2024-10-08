package io.github.pingisfun.hitboxplus;

import io.github.pingisfun.hitboxplus.waypoints.FlagsBrokenDetector;
import io.github.pingisfun.hitboxplus.waypoints.FlagsPlacedDetector;
import io.github.pingisfun.hitboxplus.waypoints.PlayerCoordSharing;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import static java.lang.Math.abs;


public class HitboxPlusClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {

            FlagsPlacedDetector.checkForPlacedFlags(message.toString());

            FlagsBrokenDetector.handleFlags(message.toString());

            PlayerCoordSharing.handleServerWaypoint(message.toString());

        });

        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {

            PlayerCoordSharing.handlePlayerWaypoint(message.toString(),sender);

        });

    }


}