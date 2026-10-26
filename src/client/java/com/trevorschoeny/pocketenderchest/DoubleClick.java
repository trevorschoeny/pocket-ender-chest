package com.trevorschoeny.pocketenderchest;

import com.mojang.blaze3d.platform.InputConstants;
import com.trevorschoeny.pocketenderchest.mixin.ScreenAccessor;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A plain left double-click on the ender chest slot opens the ender chest. Registered only
 * without Open Anywhere, whose own double-click opens it then.
 *
 * <p>The first click is vanilla's: it lifts the stack onto the cursor. The second, on the
 * same slot of the same screen within {@link #WINDOW_MS} and with the cursor not moved,
 * would be vanilla's "gather all"; it is taken instead and sent as an open, the stack going
 * back in its slot first. The window and the still cursor are Open Anywhere's, so the
 * double-click feels the same with or without it.
 *
 * <p>ponytail: between the clicks the lifted stack shows on the cursor; Open Anywhere draws
 * it back in its slot. Add that if the flicker bothers.
 */
final class DoubleClick {

    private DoubleClick() {}

    /** The window: one second, as long as the cursor stays put. */
    private static final long WINDOW_MS = 1000;

    // The first click worth pairing: its slot on the player's inventory menu (-1: none),
    // when, the screen and where the mouse was.
    private static int lastSlot = -1;
    private static long lastClick;
    private static Screen firstScreen;
    private static double firstMouseX, firstMouseY;

    static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof AbstractContainerScreen<?>) {
                ScreenMouseEvents.allowMouseClick(screen).register(DoubleClick::allow);
            }
        });
    }

    /** False when the click was taken as the second of a double-click. */
    private static boolean allow(Screen screen, MouseButtonEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        AbstractContainerScreen<?> container = (AbstractContainerScreen<?>) screen;
        Slot hovered = ((ScreenAccessor) container).pocketenderchest$hoveredSlot();
        // Creative's inventory tab shows the player's slots on a menu of its own; the server
        // has the real inventory menu open, so the slot is named by its index there. Its
        // other tabs show no gear panel.
        boolean creative = screen instanceof CreativeModeInventoryScreen c;
        if (player == null || hovered == null || creative && !((CreativeModeInventoryScreen) screen).isInventoryOpen()) {
            lastSlot = -1;
            return true;
        }
        int target = creative ? realIndex(player, hovered) : hovered.index;
        Slot real = target < 0 ? null : (creative ? player.inventoryMenu : container.getMenu()).getSlot(target);
        if (real == null || !Opening.isEnderSlot(real)
                || event.button() != InputConstants.MOUSE_BUTTON_LEFT || event.hasShiftDown()) {
            lastSlot = -1;
            return true;
        }

        ItemStack carried = container.getMenu().getCarried();
        long now = Util.getMillis();
        // The same pixel, not the same fraction of one.
        boolean second = target == lastSlot && screen == firstScreen && now - lastClick < WINDOW_MS
                && Math.floor(event.x()) == Math.floor(firstMouseX) && Math.floor(event.y()) == Math.floor(firstMouseY);
        if (!second) {
            // A first click worth pairing lifts an ender chest out of the slot.
            lastSlot = carried.isEmpty() && EnderSlot.isEnderChest(hovered.getItem()) ? target : -1;
            lastClick = now;
            firstScreen = screen;
            firstMouseX = event.x();
            firstMouseY = event.y();
            return true;
        }
        lastSlot = -1;   // a third click starts over
        if (hovered.hasItem() || !EnderSlot.isEnderChest(carried)) return true;

        // Back in its slot now, on this side, rather than once the server says so; the server
        // puts it back too, and its sync corrects this if it refuses.
        if (creative) {
            // Creative keeps the cursor on the client and tells the server each slot's
            // contents itself, the way creative's own click does.
            player.inventoryMenu.getSlot(target).set(carried);
            container.getMenu().setCarried(ItemStack.EMPTY);
            player.inventoryMenu.broadcastChanges();
        } else {
            hovered.set(carried);
            container.getMenu().setCarried(ItemStack.EMPTY);
        }
        ClientPlayNetworking.send(new Opening.OpenC2S(target));
        return false;
    }

    /** The index on the player's inventory menu of the slot creative shows as {@code shown}, or -1. */
    private static int realIndex(LocalPlayer player, Slot shown) {
        for (Slot real : player.inventoryMenu.slots) {
            if (real.container == shown.container && real.getContainerSlot() == shown.getContainerSlot()) return real.index;
        }
        return -1;
    }
}
