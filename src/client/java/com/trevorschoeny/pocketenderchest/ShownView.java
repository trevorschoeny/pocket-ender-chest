package com.trevorschoeny.pocketenderchest;

import com.trevlar.menukit.api.element.Button;
import com.trevlar.menukit.api.panel.ModeScope;
import com.trevlar.menukit.api.panel.OutsideRegion;
import com.trevlar.menukit.api.panel.Panel;
import com.trevlar.menukit.api.panel.PanelPosition;
import com.trevlar.menukit.api.panel.PanelStyle;
import com.trevlar.menukit.api.panel.Placement;
import com.trevlar.menukit.api.panel.ScreenPanelAdapter;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.lang.ref.WeakReference;

/**
 * The ender chest screen opened from the slot, on the client: which menu it is (the server
 * says so right after opening it, {@link Opening.ShownS2C}), and the x on its frame's
 * top-right corner that goes back where the double-click came from.
 */
final class ShownView {

    private ShownView() {}

    /**
     * An 11 px raised button around a 7 px "x", straddling the frame's top-right corner, the
     * same size and place as Open Anywhere's close button.
     */
    private static final int CLOSE_SIZE = 11;
    private static final Identifier CLOSE_ICON = Identifier.fromNamespaceAndPath(PocketEnderChest.MOD_ID, "close");

    // The menu the server last opened from the slot. Weak: a closed screen's menu goes.
    private static WeakReference<AbstractContainerMenu> shown = new WeakReference<>(null);

    static void register() {
        ClientPlayNetworking.registerGlobalReceiver(Opening.ShownS2C.TYPE, (payload, context) -> {
            // The open-screen packet came first, so the menu in front is the new one.
            shown = new WeakReference<>(context.player().containerMenu);
        });

        Panel close = Panel.builder(PocketEnderChest.MOD_ID + ":close")
                .add(Button.builder()
                        .icon(CLOSE_ICON)
                        .size(CLOSE_SIZE, CLOSE_SIZE)
                        .onClick(() -> ClientPlayNetworking.send(Opening.CloseC2S.INSTANCE))
                        .build())
                .style(PanelStyle.NONE)
                // Just above the frame at its right end, then nudged half its size down and
                // right, so its center sits on the corner.
                .in(ModeScope.BOTH, Placement.screen(PanelPosition.region(OutsideRegion.TOP_ALIGN_RIGHT).gap(0)
                        .offset(CLOSE_SIZE - CLOSE_SIZE / 2 - 1, CLOSE_SIZE - CLOSE_SIZE / 2)))
                .build()
                .visibleWhen(ShownView::showing);
        new ScreenPanelAdapter(close).on(ContainerScreen.class);
    }

    /** Whether the screen in front is the ender chest opened from the slot. */
    static boolean showing() {
        var player = Minecraft.getInstance().player;
        return player != null && player.containerMenu == shown.get();
    }
}
