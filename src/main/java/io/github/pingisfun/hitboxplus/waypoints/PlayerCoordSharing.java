package io.github.pingisfun.hitboxplus.waypoints;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import xaero.common.minimap.waypoints.Waypoint;


import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static io.github.pingisfun.hitboxplus.waypoints.WaypointUtils.*;

public class PlayerCoordSharing {

    public static void handlePlayerWaypoint(String message, GameProfile sender){


        //########################################################################
        //                                                                       #
        //                  COORDINATE DETECTION NORMAL MESSAGES                 #
        //                                                                       #
        //########################################################################


        if (!config.friend.acceptCoordsFromFriends || !config.friend.list.contains(sender.getName()) ||
                !message.contains("my coords (")) {
            return;
        }


        handleWaypointCreation(message, sender.getName());

    }


    public static void handleServerWaypoint(String message){

        // The reason the function that detects player code doesn't work is because many servers in order to
        // filter/redirect messages they convert them into server messages . Nodes does this too;

        if (message == null || !config.friend.acceptCoordsFromFriends || !message.contains("my coords (")) {
            return;
        }


        for (String nick : config.friend.list) {

            if (!message.contains(nick)){
                continue;
            }

            handleWaypointCreation(message,nick);

            break;
        }

    }


    private static void handleWaypointCreation(String message, String playerName) {
        String regex = "my coords \\s*\\((-?\\d+),\\s*(-?\\d+),\\s*(-?\\d+)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        if (!matcher.find()) {
            System.out.println("No coordinates found in the message.");
            return;
        }

        try {
            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            int z = Integer.parseInt(matcher.group(3));
            makePlayerWaypoint(x, y, z, playerName);

        } catch (NumberFormatException e) {
            MinecraftClient.getInstance().player.sendMessage(Text.of("Cannot parse coords"));
        }
    }


    private static void makePlayerWaypoint(int x, int y, int z , String nick){

        if (getWaypointList() == null) {
            MinecraftClient.getInstance().player.sendMessage(Text.literal("Waypoints are null"));
            return;
        }
        
        getWaypointList().add(new Waypoint(x, y, z, //Add the waypoint with the detected coordinates
                nick + "'s location", "[T]", 65535, 0, true));
        Waypoint waypoint = getWaypointList().get(getWaypointList().size() - 1);
        waypoint.setOneoffDestination(true);

        deleteWaypointInTime(waypoint, config.friend.friendWaypointTimer);

    }

}