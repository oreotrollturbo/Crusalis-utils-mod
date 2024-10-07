package io.github.pingisfun.hitboxplus.waypoints;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import xaero.common.minimap.waypoints.Waypoint;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.pingisfun.hitboxplus.waypoints.FlagsPlacedDetector.waypoints;
import static io.github.pingisfun.hitboxplus.waypoints.WaypointUtils.*;

public class PlayerCoordSharing {

    public static void handlePlayerWaypoint(String message, GameProfile sender){

        // When a chat message is received
        assert sender != null;
        assert MinecraftClient.getInstance().player != null;

        //########################################################################
        //                                                                       #
        //                  COORDINATE DETECTION NORMAL MESSAGES                 #
        //                                                                       #
        //########################################################################


        if (!config.friend.acceptCoordsFromFriends || !config.friend.list.contains(sender.getName()) || !message.toString().contains("my coords (")){
            return;
        }

        handleWaypointCreation(message.toString(),sender.getName());

    }


    public static void handleServerWaypoint(String message){

        // The reason the function that detects player code doesn't work is because many servers in order to
        // filter/redirect messages they convert them into server messages . Nodes does this too;

        if (message == null || !config.friend.acceptCoordsFromFriends) {
            return;
        }


        for (String nick : config.friend.list) {

            if (!message.contains(nick) || !message.contains("my coords (")){
                return;
            }

            handleWaypointCreation(message,nick);

            break;
        }

    }


    private static void handleWaypointCreation(String message, String playerName){

        String regex = "my coords \\((-?\\d+),(-?\\d+),(-?\\d+)\\)"; //Make a pattern to detect the coords

        Pattern pattern = Pattern.compile(regex); //Compile it

        // Match the pattern against the message
        Matcher matcher = pattern.matcher(message);


        if (!matcher.find()) {
            return;
        }


        // Extract the coordinates
        int x = Integer.parseInt(matcher.group(1));
        int y = Integer.parseInt(matcher.group(2));
        int z = Integer.parseInt(matcher.group(3));


        makePlayerWaypoint(x,y,z,playerName);

    }


    private static void makePlayerWaypoint(int x, int y, int z , String nick){
        assert waypoints != null;
        waypoints.add(new Waypoint(x, y, z, //Add the waypoint with the detected coordinates
                nick + "'s location", "[T]", 65535, 0, true));
        Waypoint waypoint = waypoints.get(waypoints.size() - 1);
        waypoint.setOneoffDestination(true);

        deleteWaypointInTime(waypoint, config.friend.friendWaypointTimer);
    }

}