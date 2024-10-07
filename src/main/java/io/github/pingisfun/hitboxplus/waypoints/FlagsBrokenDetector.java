package io.github.pingisfun.hitboxplus.waypoints;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.pingisfun.hitboxplus.waypoints.FlagsPlacedDetector.waypoints;
import static io.github.pingisfun.hitboxplus.waypoints.WaypointUtils.config;

public class FlagsBrokenDetector {

    public static void initialize(){

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> { // When you get a server message


            //########################################################################
            //                                                                       #
            //                     ATTACK FLAG BREAK DETECTION                       #
            //                                                                       #
            //########################################################################

            // We don't have to define a new pattern all the time we can just redefine the old one
            Pattern pattern = Pattern.compile("Attack\\s*at\\s*\\(\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)\\s*defeated");
            Matcher matcher = pattern.matcher(message.getString());


            if (matcher.find()) { // If the patter is found

                int waypointX = Integer.parseInt(matcher.group(1));
                int waypointZ = Integer.parseInt(matcher.group(3)); // We take the X and Z ignoring the Y

                assert waypoints != null;
                waypoints.removeIf(waypoint -> waypoint.getX() == waypointX && waypoint.getZ() == waypointZ); // Remove a waypoint in the list if it matches the message coordinates
            }

            //########################################################################
            //                                                                       #
            //                        DEFEND FLAG BREAK DETECTION                    #
            //                                                                       #
            //########################################################################

            //Still using the same pattern
            pattern = Pattern.compile("defended\\s+chunk\\s*\\(\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)\\s*against");
            matcher = pattern.matcher(message.getString());

            if (matcher.find()) { //If its found

                int chunkX = Integer.parseInt(matcher.group(1));// get the chunk coordinates
                int chunkZ = Integer.parseInt(matcher.group(2)); //This one doesn't send precise coordinates but ones of its chunk

                assert waypoints != null;
                waypoints.removeIf(waypoint -> ChunkSectionPos.getSectionCoord(waypoint.getX()) == chunkX &&
                        ChunkSectionPos.getSectionCoord(waypoint.getZ()) == chunkZ); //Checks if any waypoints are in the chunk of the message
            }

            //########################################################################
            //                                                                       #
            //                        FLAG CAPTURE DETECTION                         #
            //                                                                       #
            //########################################################################

            //Still using the same pattern
            pattern = Pattern.compile("captured\\s*chunk\\s*\\(\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)\\s*from\\s*(\\w+)");
            matcher = pattern.matcher(message.getString());

            if (matcher.find()) { //if its found

                int x = Integer.parseInt(matcher.group(1));//Get the chunk coordinates (X and Z)
                int z = Integer.parseInt(matcher.group(2));
                String town = matcher.group(3); //Get the town name

                if (config.pingTowns.oreoModList.contains(town)) { // If the town is within your towns list
                    assert waypoints != null;
                    waypoints.removeIf(waypoint -> ChunkSectionPos.getSectionCoord(waypoint.getX()) == x &&
                            ChunkSectionPos.getSectionCoord(waypoint.getZ()) == z); //Remove any waypoints that are within the chunk from the message

                    if (config.specialTowns.showNotifications && config.specialTowns.soundList.contains(town)) {
                        MinecraftClient.getInstance().player.sendMessage(Text.literal("§4 Chunk from " + town + " has been captured"), true);
                    } //If yoy have the notifications setting enabled and the town is a "special town"
                }
            }


            //########################################################################
            //                                                                       #
            //                        FLAG LIBERATE DETECTION                        #
            //                                                                       #
            //########################################################################

            pattern = Pattern.compile("liberated\\s*chunk\\s*\\(\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)\\s*from\\s*(\\w+)");
            matcher = pattern.matcher(message.getString());

            if (matcher.find()) { // If the pattern is found

                int x = Integer.parseInt(matcher.group(1));// Get the coordinates
                int z = Integer.parseInt(matcher.group(2));
                String town = matcher.group(3); // Get the town

                if (config.pingTowns.enemyTownList.contains(town)) { // if the town is in the enemy town list
                    assert waypoints != null;
                    waypoints.removeIf(waypoint -> ChunkSectionPos.getSectionCoord(waypoint.getX()) == x &&
                            ChunkSectionPos.getSectionCoord(waypoint.getZ()) == z); // Remove the waypoint if it's within the chunk
                }
            }
        });

    }

}
