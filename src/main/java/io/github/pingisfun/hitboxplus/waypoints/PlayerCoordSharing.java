package io.github.pingisfun.hitboxplus.waypoints;

import com.mojang.authlib.GameProfile;
import io.github.pingisfun.hitboxplus.HitboxPlusClient;
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

    public static Boolean handlePlayerPing(String message, GameProfile sender){

        // The reason the function that detects player code doesn't work is because many servers in order to
        // filter/redirect messages they convert them into server messages . Nodes does this too;

        if (message == null || !config.friend.acceptPings || !message.contains("pinged location {")
        || !config.friend.list.contains(sender.getName())) {
            return true;
        }

        handlePingCreation(message,sender.getName());

        return false;
    }


    public static Boolean handleServerPing(String message){

        // The reason the function that detects player code doesn't work is because many servers in order to
        // filter/redirect messages they convert them into server messages . Nodes does this too;

        if (message == null || !config.friend.acceptPings || !message.contains("pinged location {")) {
            return true;
        }


        for (String nick : config.friend.list) {

            if (!message.contains(nick)){
                continue;
            }

            handlePingCreation(message,nick);

            return false;
        }

        return true;

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

    private static void handlePingCreation(String message, String playerName) {
        String regex = "pinged location \\s*\\{(-?\\d+),\\s*(-?\\d+),\\s*(-?\\d+)}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        if (!matcher.find()) {
            System.out.println("No coordinates found in message.");
            return;
        }

        try {
            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            int z = Integer.parseInt(matcher.group(3));
            makePlayerPing(x, y, z, playerName);

        } catch (NumberFormatException e) {
            MinecraftClient.getInstance().player.sendMessage(Text.of("Cannot parse coords"));
        }
    }

    private static void makePlayerPing(int x, int y, int z , String nick){

        if (getWaypointList() == null) {
            MinecraftClient.getInstance().player.sendMessage(Text.literal("Waypoints are null"));
            return;
        }

        getWaypointList().add(new Waypoint(x, y, z, //Add the waypoint with the detected coordinates
                nick + "'s ping", "o", 0, 0, true));
        Waypoint waypoint = getWaypointList().get(getWaypointList().size() - 1);
        waypoint.setOneoffDestination(true);

        if (config.friend.deletePreviousPing) {

            if (HitboxPlusClient.pings.containsKey(nick)){
                deleteWaypoint(HitboxPlusClient.pings.get(nick));
            }

            HitboxPlusClient.pings.put(nick,waypoint);
        }

        deleteWaypointInTime(waypoint, config.friend.pingWaypointTimer);
    }

}