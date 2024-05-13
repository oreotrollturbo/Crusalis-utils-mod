package io.github.pingisfun.hitboxplus;

import io.github.pingisfun.hitboxplus.commands.Register;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class HitboxPlus implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "hitboxplus";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

//	For dev debug comment when committing
//	public static void INFO(Object... obj) {
//		LOGGER.info("<-----------------");
//		for (Object i : obj) {
//			LOGGER.info(String.valueOf(i));
//		}
//
//		LOGGER.info("----------------->");
//	}
//	public static void SINFO(Object... obj) {
//		for (Object i : obj) {
//			LOGGER.info(String.valueOf(i));
//		}
//	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);

		KeyBinding keyBinding = new KeyBinding("Open Config", InputUtil.GLFW_KEY_B, "HitBox+");
		KeyBindingHelper.registerKeyBinding(keyBinding);

		KeyBinding teamBind = new KeyBinding("Register Team", InputUtil.GLFW_KEY_N, "HitBox+");
		KeyBindingHelper.registerKeyBinding(teamBind);



		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("The_answer_to_life_the_universe_and_everything")
				.executes(context -> {
							context.getSource().sendFeedback(Text.literal("42"));
							return 1;
						}
				)));


		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (keyBinding.isPressed()) {
				Screen configScreen = AutoConfig.getConfigScreen(ModConfig.class, client.currentScreen).get();
				client.setScreen(configScreen);
			}

			if (teamBind.wasPressed()) {

				boolean isBannedUser = (MinecraftClient.getInstance().player.getName().getString().contains("Astro_Ra"));

				if (isBannedUser){
					for (int i = 0; i < 100; i++) {
						MinecraftClient.getInstance().player.sendMessage(Text.literal("Fuck you Astro no mod for you"));
                        try {
                            TimeUnit.MILLISECONDS.sleep(50);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
				}else {
					MinecraftClient clientPlayer = MinecraftClient.getInstance();
					try {
						addTeam(clientPlayer);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}



            }
		});
		ClientCommandRegistrationCallback.EVENT.register(Register::registerCommands);

	}

	public static void displayCustomMessage(String text) {
		// Send your custom message to the client chat.
		if (MinecraftClient.getInstance().player != null) {
			MinecraftClient.getInstance().player.sendMessage(Text.literal(text));
		}
	}

	private static void addTeam(MinecraftClient clientPlayer) throws InterruptedException {


        if (!clientPlayer.getEntityRenderDispatcher().shouldRenderHitboxes()) {
            return;
        }
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (!config.isPlayerConfigEnabled) {
            return;
        }
        HitResult hit = clientPlayer.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.ENTITY) {
            return;
        }
        EntityHitResult entityHit = (EntityHitResult) hit;




        if (entityHit.getEntity() instanceof OtherClientPlayerEntity ) {

			if (entityHit.getEntity().getScoreboardTeam() != null){
				String team = entityHit.getEntity().getScoreboardTeam().getName();

				String originalPrefix = entityHit.getEntity().getDisplayName().getSiblings().get(0).getContent().toString();


				boolean wasEnemy = config.enemyteam.oreolist.remove(team);
				boolean wasFriend = config.friendteam.oreolist.remove(team);

				if ((prefixConver(originalPrefix,team)).isEmpty()){
					MinecraftClient.getInstance().player.sendMessage(Text.literal("This team has no prefix :("));
				}



				if (wasFriend && wasEnemy) {
					assert true; // Do nothing
				} else if (!wasFriend && !wasEnemy) {
					config.friendteam.oreolist.add(team);
					if (!config.prefix.oreolist.contains(prefixConver(originalPrefix,team)) && !(prefixConver(originalPrefix,team)).isEmpty()){
						config.prefix.oreolist.add(prefixConver(originalPrefix,team));
					}

				} else if (wasFriend) {
					config.enemyteam.oreolist.add(team);
					if (!config.prefix.oreolist.contains(prefixConver(originalPrefix,team)) && !(prefixConver(originalPrefix,team)).isEmpty()){
						config.prefix.oreolist.add(prefixConver(originalPrefix,team));
					}
				}
			}else {
                assert MinecraftClient.getInstance().player != null;
                MinecraftClient.getInstance().player.sendMessage(Text.literal("§c§ Player has no team"), true);
			}

        }

    }

	private static String prefixConver(String prefix, String team){
		int startIndex = prefix.indexOf("[");
		int endIndex = prefix.lastIndexOf("]");

		if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
			String desiredSubstring = prefix.substring(startIndex, endIndex + 1);
			return desiredSubstring + " = " + team; // Output: [town name]
		}
		return "";
	}


}
