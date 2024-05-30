package io.github.pingisfun.hitboxplus;

import io.github.pingisfun.hitboxplus.util.ConfEnums;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkSectionPos;
import xaero.common.XaeroMinimapSession;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointSet;
import xaero.common.minimap.waypoints.WaypointsManager;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.abs;
import static java.lang.Math.hypot;


public class HitboxPlusClient implements ClientModInitializer {

    ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

    public String identifier = "test"; //This is a unique identifier for players

    @Override
    public void onInitializeClient() {



        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) ->{
            // When a chat message is received
            assert sender != null;
            assert MinecraftClient.getInstance().player != null;

            //########################################################################
            //                                                                       #
            //                         COORDINATE DETECTION                          #
            //                                                                       #
            //########################################################################

            if (config.friend.acceptCoordsFromFriends && config.friend.list.contains(sender.getName()) && message.toString().contains("my coords (") ){
                // Make sure you accept waypoints from players and the player is in your friends list
                String regex = "my coords \\((-?\\d+),(-?\\d+),(-?\\d+)\\)"; //Make a pattern to detect the coords

                Pattern pattern = Pattern.compile(regex); //Compile it

                // Match the pattern against the message
                Matcher matcher = pattern.matcher(message.toString());

                if (matcher.find()) {

                    // Extract the coordinates
                    int x = Integer.parseInt(matcher.group(1));
                    int y = Integer.parseInt(matcher.group(2));
                    int z = Integer.parseInt(matcher.group(3));

                    List<Waypoint> waypoints = getWaypointList(); //Get the waypoint list to add/remove from it

                    assert waypoints != null;
                    waypoints.add(new Waypoint(x, y, z, //Add the waypoint with the detected coordinates
                            sender.getName() + "'s location", "[T]", 65535, 0, true));
                }
            }
        });


        ClientReceiveMessageEvents.GAME.register((message, overlay) ->{ // When you get a server message

            int yOffset = config.pingTowns.yOffset; // Y offset from the settings
            List<Waypoint> waypoints = getWaypointList(); //Get the waypoint list
            
            //########################################################################
            //                                                                       #
            //                            TOWN ATTACKS                               #
            //                                                                       #
            //########################################################################
            String waypointSymbol = "[F]"; //Define the flag symbol (for in game)

            String clientName = MinecraftClient.getInstance().player.getName().getString(); //Get the players name

            Pattern pattern = Pattern.compile("\\[War\\] (\\w+) is attacking (\\w+) at \\((-?\\d+),\\s*(-?\\d+),\\s*(-?\\d+)\\)");
            Matcher matcher = pattern.matcher(message.getString()); //Compile the pattern


            if (matcher.find()){ //If the pattern is found
                String name = matcher.group(1); // get the player who placed it and his name
                String town = matcher.group(2); // get the town that is being attacked
                int x = Integer.parseInt(matcher.group(3));
                int y = Integer.parseInt(matcher.group(4)); // Get the coords
                int z = Integer.parseInt(matcher.group(5));


                if (name.equals(clientName) && !config.pingTowns.enemyTownList.contains(town)){
                    config.pingTowns.enemyTownList.add(town); // Adds town as enemy when you attack it
                }

                if (config.pingTowns.isPingingEnabled && config.pingTowns.oreoModList.contains(town)){ //If flag waypoints is enabled

                    assert MinecraftClient.getInstance().player != null;

                    double playerX = MinecraftClient.getInstance().player.getX();
                    double playerZ = MinecraftClient.getInstance().player.getZ(); // Get the players coords


                    if (config.pingTowns.limitRange == ConfEnums.FlagLimiter.DISABLED || isInRange((int) playerX, (int) playerZ, x, z)){
                        // make sure the town is within defined range or the setting is disabled
                        makeTimerWaypoint(waypoints,x,y,yOffset,z,town,waypointSymbol); //Calls the function that makes thw waypoint
                    }
                }
            }

        //########################################################################
        //                                                                       #
        //                            TOWN LIBERATION                            #
        //                                                                       #
        //########################################################################

            //set the pattern to the liberating message
            pattern = Pattern.compile("liberating (\\w+) at \\((-?\\d+),\\s*(-?\\d+),\\s*(-?\\d+)\\)");
            matcher = pattern.matcher(message.getString());


            if (matcher.find()){ //if pattern is found
                String town = matcher.group(1); // Set the town name to the first section of the pattern
                int x = Integer.parseInt(matcher.group(2));
                int y = Integer.parseInt(matcher.group(3)); //Detect the coordinates
                int z = Integer.parseInt(matcher.group(4));


                if (config.pingTowns.isPingingEnabled && config.pingTowns.enemyTownList.contains(town)){

                    assert MinecraftClient.getInstance().player != null;

                    double playerX = MinecraftClient.getInstance().player.getX();
                    double playerZ = MinecraftClient.getInstance().player.getZ(); //get the players coordinates

                    if (config.pingTowns.limitRange == ConfEnums.FlagLimiter.DISABLED || isInRange((int) playerX, (int) playerZ, x, z)){
                        //Make sure there is no flag range limit or the flag is within the limit

                        makeTimerWaypoint(waypoints,x,y,yOffset,z,town,waypointSymbol); //Calls the function that makes thw waypoint
                    }
                }

            }


            //########################################################################
            //                                                                       #
            //                     ATTACK FLAG BREAK DETECTION                       #
            //                                                                       #
            //########################################################################

            // We don't have to define a new pattern all the time we can just redefine the old one
            pattern = Pattern.compile("Attack\\s*at\\s*\\(\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)\\s*defeated");
            matcher = pattern.matcher(message.getString());


            if (matcher.find()){ // If the patter is found

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

            if (matcher.find()){ //If its found

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
            pattern = Pattern.compile("captured chunk \\((-?\\d+),\\s*(-?\\d+)\\) from (\\w+)");
            matcher = pattern.matcher(message.getString());

            if (matcher.find()){ //if its found

                int x = Integer.parseInt(matcher.group(1));//Get the chunk coordinates (X and Z)
                int z = Integer.parseInt(matcher.group(2));
                String town = matcher.group(3); //Get the town name

                if (config.pingTowns.oreoModList.contains(town)){ // If the town is within your towns list
                    assert waypoints != null;
                    waypoints.removeIf(waypoint -> ChunkSectionPos.getSectionCoord(waypoint.getX()) == x &&
                            ChunkSectionPos.getSectionCoord(waypoint.getZ()) == z); //Remove any waypoints that are within the chunk from the message

                    if(config.specialTowns.showNotifications && config.specialTowns.soundList.contains(town)){
                        MinecraftClient.getInstance().player.sendMessage(Text.literal("Chunk from " + town + " has been captured"));
                    } //If yoy have the notifications setting enabled and the town is a "special town"
                }
            }


            //########################################################################
            //                                                                       #
            //                        FLAG LIBERATE DETECTION                        #
            //                                                                       #
            //########################################################################

            pattern = Pattern.compile("liberated chunk \\((-?\\d+),\\s*(-?\\d+)\\) from (\\w+)"); // This one detects liberating messages
            matcher = pattern.matcher(message.getString());

            if (matcher.find()){ // If the pattern is found

                int x = Integer.parseInt(matcher.group(1));// Get the coordinates
                int z = Integer.parseInt(matcher.group(2));
                String town = matcher.group(3); // Get the town

                if (config.pingTowns.enemyTownList.contains(town)){ // if the town is in the enemy town list
                    assert waypoints != null;
                    waypoints.removeIf(waypoint -> ChunkSectionPos.getSectionCoord(waypoint.getX()) == x &&
                            ChunkSectionPos.getSectionCoord(waypoint.getZ()) == z); // Remove the waypoint if its within the chunk
                }
            }
        });
    }





    public boolean isInRange (int playerX,int playerZ, int waypointX, int waypointZ){

        int trueX = abs(playerX - waypointX); // Get the difference between the player X and waypoint X
        int trueZ = abs(playerZ - waypointZ); // Get the difference between the player Z and waypoint Z

        int distance = (int) hypot(trueX,trueZ); //Calculate the distance thanks to the pythagorian theorem
            // keeping it simple with just integers

        if (config.pingTowns.limitRange == ConfEnums.FlagLimiter.WITHIN){ // If the setting is set to WITHIN
            return distance <= config.pingTowns.pingDistanceLimit; // Return true if it is within the distance
        }else {//This one is when you have the setting to OUT_OF
            return distance > config.pingTowns.pingDistanceLimit; // Return true if it is out of the distance
        }
    }

    private List<Waypoint> getWaypointList (){ // This simply makes the code more DRY
        XaeroMinimapSession minimapSession = XaeroMinimapSession.getCurrentSession();
        if (minimapSession == null) return null;
        WaypointsManager waypointsManager = minimapSession.getWaypointsManager();
        WaypointSet waypointSet = waypointsManager.getWaypoints();
        if (waypointSet == null) return null;

        return waypointSet.getList(); // All it does is get the waypoint list so that you don't have to do it in every part individually
    }

    private void makeTimerWaypoint(List<Waypoint> waypoints , int x,int y, int yOffset,int z ,String town, String waypointSymbol){

        new Thread(() -> {
            // Make a thread with a timer to auto delete the waypoint
            assert waypoints != null;
            waypoints.add(new Waypoint(x, y + yOffset, z, // Add the waypoint
                    "Flag on " + town, waypointSymbol, 0, 0, true));

            Waypoint lastWaypoint = waypoints.get(waypoints.size() -1); //Get the waypoint in the thread to delete it later

            if (config.specialTowns.playFlagSounds && config.specialTowns.soundList.contains(town)){ //play a sound if the setting is on and the list has the town
                MinecraftClient.getInstance().player.playSound(SoundEvents.BLOCK_BELL_USE, 1,config.specialTowns.pitch);
            }


            try { // Count down and then delete the waypoint
                TimeUnit.SECONDS.sleep(config.pingTowns.removeCooldown); // 4 minutes by default
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            waypoints.remove(lastWaypoint); // Delete the waypoint

        }).start();

    }
}


