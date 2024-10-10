package io.github.pingisfun.hitboxplus;

import io.github.pingisfun.hitboxplus.waypoints.FlagsBrokenDetector;
import io.github.pingisfun.hitboxplus.waypoints.FlagsPlacedDetector;
import io.github.pingisfun.hitboxplus.waypoints.PlayerCoordSharing;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import xaero.common.minimap.waypoints.Waypoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.abs;


public class HitboxPlusClient implements ClientModInitializer {

    public static HashMap<String, Waypoint> pings = new HashMap<>();

    @Override
    public void onInitializeClient() {

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {

            if  (MinecraftClient.getInstance().player == null){
                return true;
            }


            FlagsPlacedDetector.checkForPlacedFlags(message.toString());

            FlagsBrokenDetector.handleFlags(message.toString());

            PlayerCoordSharing.handleServerWaypoint(message.toString());

            return PlayerCoordSharing.handleServerPing(message.toString());

        });

        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {

            if  (sender == null || MinecraftClient.getInstance().player == null){
                MinecraftClient.getInstance().player.sendMessage(Text.literal("sender or instance is null"));
                return true;
            }


            PlayerCoordSharing.handlePlayerWaypoint(message.toString(), sender);

            return PlayerCoordSharing.handlePlayerPing(message.toString(), sender);

        });
    }
}