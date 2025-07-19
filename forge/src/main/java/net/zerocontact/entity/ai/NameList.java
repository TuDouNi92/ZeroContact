package net.zerocontact.entity.ai;

import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.zerocontact.entity.ArmedRaider;

import java.util.*;

public class NameList {
    private static final List<String> nameSet = List.of(
            "Jack Hunter",
            "Ryan Cross",
            "Logan Steele",
            "Chase Walker",
            "Cole Barrett",
            "Luke Mason",
            "Jake Riker",
            "Dylan Graves",
            "Troy Vance",
            "Blake Mercer",
            "Grant Bishop",
            "Owen Drake",
            "Kyle Tanner",
            "Zane Brooks",
            "Dean Foster",
            "Nate Morgan",
            "Eli Ward",
            "Reed Lawson",
            "Scott Hale",
            "Connor Briggs",
            "Shane Dalton",
            "Vince Carter",
            "Sean Maddox",
            "Eric Nolan",
            "Mark Stryker",
            "Derek Kane",
            "Bryce Archer",
            "Miles Granger",
            "Chad Logan",
            "Sam Hawke",
            "Trent Walker",
            "Jace Turner",
            "Colt Mason",
            "Gabe Hunter",
            "Wade Rivers",
            "Ty Ross",
            "Axel Monroe",
            "Clint Bishop",
            "Brett Wolfe",
            "Tanner Hayes",
            "Mitch Rayner",
            "Cal Knox",
            "Joel Booker",
            "Hank Carter",
            "Rex Chandler",
            "Clay Matthews",
            "Ryder Cole",
            "Bo Sullivan",
            "Seth Dawson",
            "Duke Riley"
    );

    public static void setName(ArmedRaider raider, RandomSource randomSource) {
        raider.setCustomName(Component.literal(nameSet.get(randomSource.nextInt(nameSet.size()))));
    }
}
