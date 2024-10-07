package io.github.pingisfun.hitboxplus.waypoints;

import io.github.pingisfun.hitboxplus.util.ConfEnums;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import xaero.common.minimap.waypoints.Waypoint;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.pingisfun.hitboxplus.waypoints.WaypointUtils.*;

public class FlagsPlacedDetector {

    static List<Waypoint> waypoints = getWaypointList();



    public static void initialize(){

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> { // When you get a server message

            //########################################################################
            //                                                                       #
            //                            TOWN ATTACKS                               #
            //                                                                       #
            //########################################################################
            String waypointSymbol = "[F]"; //Define the flag symbol (for in game)

            assert MinecraftClient.getInstance().player != null;
            String clientName = MinecraftClient.getInstance().player.getName().getString(); //Get the players name

            Pattern pattern = Pattern.compile("\\[\\s*War\\s*\\]\\s*(\\w+)\\s*is\\s*attacking\\s*(\\w+)\\s*at\\s*\\(\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)");
            Matcher matcher = pattern.matcher(message.getString()); //Compile the pattern


            if (matcher.find()) { //If the pattern is found
                String name = matcher.group(1); // get the player who placed it and his name
                String town = matcher.group(2); // get the town that is being attacked
                int x = Integer.parseInt(matcher.group(3));
                int y = Integer.parseInt(matcher.group(4)); // Get the coords
                int z = Integer.parseInt(matcher.group(5));


                if (name.equals(clientName)) {
                    color = 231212;
                    waypointSymbol = "[Y]";

                    if (!config.pingTowns.enemyTownList.contains(town)) {
                        config.pingTowns.enemyTownList.add(town);// Adds town as enemy when you attack it
                    }
                }

                if (config.pingTowns.isPingingEnabled && config.pingTowns.oreoModList.contains(town)) { //If flag waypoints is enabled

                    assert MinecraftClient.getInstance().player != null;

                    double playerX = MinecraftClient.getInstance().player.getX();
                    double playerZ = MinecraftClient.getInstance().player.getZ(); // Get the players coords


                    if (config.pingTowns.limitRange == ConfEnums.FlagLimiter.DISABLED || isInRange((int) playerX, (int) playerZ, x, z)) {
                        // make sure the town is within defined range or the setting is disabled
                        assert waypoints != null;
                        makeTimerWaypoint(waypoints, x, y, yOffset, z, color, town, waypointSymbol); //Calls the function that makes thw waypoint
                        color = 0;
                        waypointSymbol = "[F]";
                    }
                }
            }

            //########################################################################
            //                                                                       #
            //                            TOWN LIBERATION                            #
            //                                                                       #
            //########################################################################

            //set the pattern to the liberating message
            pattern = Pattern.compile("liberating\\s*(\\w+)\\s*at\\s*\\(\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)");
            matcher = pattern.matcher(message.getString());

            if (matcher.find()) { //if pattern is found
                String town = matcher.group(1); // Set the town name to the first section of the pattern
                int x = Integer.parseInt(matcher.group(2));
                int y = Integer.parseInt(matcher.group(3)); //Detect the coordinates
                int z = Integer.parseInt(matcher.group(4));

                if (config.pingTowns.isPingingEnabled && config.pingTowns.enemyTownList.contains(town)) {

                    assert MinecraftClient.getInstance().player != null;

                    double playerX = MinecraftClient.getInstance().player.getX();
                    double playerZ = MinecraftClient.getInstance().player.getZ(); //get the players coordinates

                    if (config.pingTowns.limitRange == ConfEnums.FlagLimiter.DISABLED || isInRange((int) playerX, (int) playerZ, x, z)) {
                        //Make sure there is no flag range limit or the flag is within the limit

                        assert waypoints != null;
                        makeTimerWaypoint(waypoints, x, y, yOffset, z, color, town, waypointSymbol); //Calls the function that makes thw waypoint
                    }
                }

            }
        });

    }

}
