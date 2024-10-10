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

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {

            if  (MinecraftClient.getInstance().player == null){
                return true;
            }

            try {

                FlagsPlacedDetector.checkForPlacedFlags(message.toString());

                FlagsBrokenDetector.handleFlags(message.toString());

                PlayerCoordSharing.handleServerWaypoint(message.toString());

                return true;
            } catch (Exception e) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("Exception: " + e.getMessage()));
                return false;
            }

        });

        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {

            if  (sender == null || MinecraftClient.getInstance().player == null){
                MinecraftClient.getInstance().player.sendMessage(Text.literal("sender or instance is null"));
                return true;
            }

            try {
                PlayerCoordSharing.handlePlayerWaypoint(message.toString(),sender);
                return true;

            } catch (Exception e){
                MinecraftClient.getInstance().player.sendMessage(Text.literal("Exception: " + e.getMessage()));

                return false;
            }
        });
    }
}