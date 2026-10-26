package com.trevorschoeny.pocketenderchest;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pocket Ender Chest: a slot for an ender chest in the shared gear panel; a double-click on
 * it opens the player's ender chest from any screen. Common init loads the settings,
 * declares the slot and registers the opening's packets (both sides).
 */
public final class PocketEnderChest implements ModInitializer {

    public static final String MOD_ID = "pocketenderchest";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        EnderSlotSettings.load();
        EnderSlot.register();
        Opening.register();
    }
}
