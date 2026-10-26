package com.trevorschoeny.pocketenderchest.ip;

import com.trevorschoeny.pocketenderchest.EnderSlotSettings;
import com.trevorschoeny.pocketenderchest.PocketEnderChest;

import com.trevorschoeny.inventoryplus.api.InventoryPlusApi;

import com.trevlar.menukit.api.element.Button;
import com.trevlar.menukit.api.element.Checkbox;
import com.trevlar.menukit.api.element.Divider;
import com.trevlar.menukit.api.element.Flow;
import com.trevlar.menukit.api.element.PanelElement;
import com.trevlar.menukit.api.element.Tabs;
import com.trevlar.menukit.api.element.TextLabel;
import com.trevlar.menukit.api.element.TextRole;
import com.trevlar.menukit.api.element.Toggle;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The Ender Chest tab in Inventory Plus's settings menu: the frame IP's tabs have (title,
 * description, Reset to Defaults, On/Off, a line), then "Show the ender chest slot", grayed
 * while the mod is off. Without Open Anywhere installed, a line under it says what Open
 * Anywhere adds.
 *
 * <p>The frame is laid out by hand, as Simple Totem Slot's tab does. Move it into MenuKit or
 * IP's API once more mods repeat it.
 */
public final class EnderSettingsTab {

    private EnderSettingsTab() {}

    /** A divider's color on the inventory look, as on IP's own tabs. */
    private static final int LINE_COLOR = 0xFF8B8B8B;

    /** Call once from client init, before the menu can be opened. */
    public static void register() {
        Tabs.addTo(InventoryPlusApi.SETTINGS_MENU,
                Tabs.tab(PocketEnderChest.MOD_ID).label(Component.literal("Ender Chest")).body(EnderSettingsTab::body));
        // General's Reset everything resets this tab too.
        InventoryPlusApi.registerSettingsReset(EnderSlotSettings::reset);
    }

    private static List<PanelElement> body() {
        List<PanelElement> rows = new ArrayList<>(List.of(
                TextLabel.builder().at(0, 0)
                        .text(Component.literal("Pocket Ender Chest").withStyle(ChatFormatting.BOLD))
                        .role(TextRole.HEADING).build(),
                TextLabel.builder().at(0, 14)
                        .text(Component.literal("A slot for your ender chest. Double-click it to open "
                                + "your ender chest from any screen."))
                        .role(TextRole.BODY).build(),
                Flow.builder().at(0, 30).gap(10, 4)
                        .add(Button.builder().label(Component.literal("Reset to Defaults")).size(0, 16)
                                .onClick(() -> InventoryPlusApi.confirmInSettings("Reset Pocket Ender Chest to defaults?",
                                        "Every setting on this tab goes back to how a fresh install has it.",
                                        EnderSlotSettings::reset))
                                .build())
                        .add(Toggle.builder().size(40, 16)
                                .state(EnderSlotSettings::enabled, EnderSlotSettings::setEnabled)
                                .label(() -> Component.literal(EnderSlotSettings.enabled() ? "On" : "Off")).build())
                        .build(),
                Divider.horizontal().at(0, 50).color(LINE_COLOR).build(),
                Checkbox.builder().at(0, 56).label(Component.literal("Show the ender chest slot"))
                        .state(EnderSlotSettings::showSlot, EnderSlotSettings::setShowSlot)
                        .disabledWhen(() -> !EnderSlotSettings.enabled()).build()));
        if (!FabricLoader.getInstance().isModLoaded("openanywhere")) {
            rows.add(TextLabel.builder().at(0, 80)
                    .text(Component.literal("Open Anywhere opens shulker boxes and bundles the same way, "
                            + "with tabs to flip between them. It takes over opening this slot when installed."))
                    .role(TextRole.BODY).build());
        }
        return rows;
    }
}
