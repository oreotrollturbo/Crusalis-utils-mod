package io.github.pingisfun.hitboxplus.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

import java.util.Collections;

import static io.github.pingisfun.hitboxplus.HitboxPlusClient.registeredUser;

public class Register {
    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        FriendCommand.register(dispatcher);
    }

    public static void initStuff(){
        Collections.addAll(registeredUser,"oreotrollturbo","Mister_Uni","Nyxiia","creeperIsHot","ADoughnut213","ts6ki",
                "Captain_Grey42","ignBaca","TheAngryMan69","imzFunnyman", "UghErwin","Mockas","maltemin","Death__","Error110","TheAngryMan69",
                "TooAnri4You","Vandaling","not_DaCAR","Starwars_16","Nokss__", "Rylix244_","Fortr3ssE","Orban_Viktor54","carboy22","AKHISARLI",
                "VladAndGyat","HixTV","Funkyjayninja","Knack","Frostcy","Abeksyz", "hartebeests","BridgeBoys2","georgeat4k","KILLZx",
                "Speedballi","PaladinWulfen","8kui","Garama","KaiserRLNGO","Abeksyz","JustBraders","OldSpiceBread","Adryx_Was_Taken","personnx","FactionsCoby",
                "Monkeydonian");
    }


}
