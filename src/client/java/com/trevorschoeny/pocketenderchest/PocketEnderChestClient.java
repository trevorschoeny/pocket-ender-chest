package com.trevorschoeny.pocketenderchest;

import com.trevorschoeny.pocketenderchest.ip.EnderSettingsTab;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Client init: the slot's place in the gear panel, the ender chest screen's x and, without
 * Open Anywhere, the double-click that opens it. With Open Anywhere installed the
 * double-click is Open Anywhere's (Trev, 2026-10-24), so this mod only adds the slot. The
 * settings tab exists only with Inventory Plus; the {@code ip} package is touched nowhere
 * else, so IP's classes never load without IP.
 */
public final class PocketEnderChestClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EnderPanel.register();
        ShownView.register();
        if (!FabricLoader.getInstance().isModLoaded("openanywhere")) DoubleClick.register();
        if (FabricLoader.getInstance().isModLoaded("inventoryplus")) EnderSettingsTab.register();
    }
}
