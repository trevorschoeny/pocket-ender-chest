package com.trevorschoeny.pocketenderchest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Pocket Ender Chest's settings, the one place they live: plain fields, loaded from and
 * saved to {@code config/pocketenderchest/config.json}, the same shape as Simple Totem
 * Slot's. Every setter saves.
 *
 * <p>Read on the side whose code runs: the server for what the slot accepts and for the
 * opening, the client for showing the slot. In singleplayer both read this one file.
 *
 * <p>Changed from the Ender Chest tab in Inventory Plus's settings menu when IP is
 * installed. Without IP there's no menu and the defaults (or the file) apply.
 */
public final class EnderSlotSettings {

    private EnderSlotSettings() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // The defaults, named once so load and reset agree.
    private static final boolean DEFAULT_ENABLED = true;
    private static final boolean DEFAULT_SHOW_SLOT = true;

    // Off hides the slot and stops the opening; the ender chests stay in the slot.
    private static volatile boolean enabled = DEFAULT_ENABLED;
    // Off hides the slot; nothing new goes in until it's shown again.
    private static volatile boolean showSlot = DEFAULT_SHOW_SLOT;

    public static boolean enabled()  { return enabled; }
    public static boolean showSlot() { return showSlot; }

    public static void setEnabled(boolean v)  { enabled = v; save(); }
    public static void setShowSlot(boolean v) { showSlot = v; save(); }

    /** Every setting back to its default, and saves: Reset to Defaults and IP's Reset everything. */
    public static void reset() {
        enabled = DEFAULT_ENABLED;
        showSlot = DEFAULT_SHOW_SLOT;
        save();
    }

    private static Path filePath() {
        return FabricLoader.getInstance().getConfigDir().resolve(PocketEnderChest.MOD_ID).resolve("config.json");
    }

    /**
     * Reads the file once, at common init (both sides). A missing file, or a missing or
     * unreadable key, leaves that setting at its default. Nothing is written until a
     * setting changes.
     */
    public static void load() {
        Path path = filePath();
        if (!Files.exists(path)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            if (root.has("enabled")) enabled = root.get("enabled").getAsBoolean();
            if (root.has("showSlot")) showSlot = root.get("showSlot").getAsBoolean();
        } catch (IOException | JsonSyntaxException | IllegalStateException | ClassCastException
                 | UnsupportedOperationException ex) {
            PocketEnderChest.LOGGER.error("[pocketenderchest] failed to read {}, using defaults", path, ex);
        }
    }

    private static void save() {
        Path path = filePath();
        try {
            Files.createDirectories(path.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("enabled", enabled);
            root.addProperty("showSlot", showSlot);
            Files.writeString(path, GSON.toJson(root));
        } catch (IOException ex) {
            PocketEnderChest.LOGGER.error("[pocketenderchest] failed to write {}, settings won't persist", path, ex);
        }
    }
}
