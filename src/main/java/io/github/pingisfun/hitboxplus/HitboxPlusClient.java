package io.github.pingisfun.hitboxplus;

import io.github.pingisfun.hitboxplus.waypoints.FlagsBrokenDetector;
import io.github.pingisfun.hitboxplus.waypoints.FlagsPlacedDetector;
import io.github.pingisfun.hitboxplus.waypoints.PlayerCoordSharing;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.regex.Pattern;

import static java.lang.Math.abs;


public class HitboxPlusClient implements ClientModInitializer {

    ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

    @Override
    public void onInitializeClient() {

        PlayerCoordSharing.initialize();

        FlagsPlacedDetector.initialize();

        FlagsBrokenDetector.initialize();

    }



}