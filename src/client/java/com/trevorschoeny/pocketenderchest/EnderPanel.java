package com.trevorschoeny.pocketenderchest;

import com.trevlar.menukit.api.panel.Contribution;
import com.trevlar.menukit.api.panel.ModeScope;
import com.trevlar.menukit.api.panel.OutsideRegion;
import com.trevlar.menukit.api.panel.PanelPosition;
import com.trevlar.menukit.api.panel.PanelStyle;
import com.trevlar.menukit.api.panel.SharedPanels;
import com.trevlar.slotkit.api.group.SlotGroupCategory;
import com.trevlar.slotkit.api.slot.ClientSlots;
import com.trevlar.slotkit.api.slot.SlotFlowElement;
import com.trevlar.slotkit.api.slot.SlotPlacement;

/**
 * The ender chest slot's part of {@code trevlar:gear}, the panel it shares with Simple
 * Elytra Slot (order 10) and Simple Totem Slot (order 20): one column above the offhand in
 * survival, right of the armor in creative. Order 0 puts the ender chest on top (Trev,
 * 2026-10-24). A screen without the offhand or the armor (a chest) has no group to place it
 * on, so it doesn't show there.
 *
 * <p>Everything after {@code visibleWhen} is the panel's own declaration and matches the
 * other two mods' exactly; MenuKit warns if they differ.
 */
final class EnderPanel {

    private EnderPanel() {}

    static final String SHARED_ID = "trevlar:gear";
    private static final int ORDER = 0;

    static void register() {
        SharedPanels.join(SHARED_ID, Contribution.builder(ORDER, ModeScope.BOTH)
                .add(SlotFlowElement.of(EnderSlot.ENDER_CHEST))
                // Hidden while the mod is off or the slot is set hidden, and wherever the
                // server has no slot to show.
                .visibleWhen(() -> EnderSlotSettings.enabled() && EnderSlotSettings.showSlot()
                        && ClientSlots.isShown(EnderSlot.GROUP_ID))
                // The shared declaration. gap(0) on the placements and the flow keeps the
                // slots on vanilla's 18 px pitch, against the box and between each other.
                .in(ModeScope.SURVIVAL, SlotPlacement.group(SlotGroupCategory.PLAYER_OFFHAND,
                        PanelPosition.region(OutsideRegion.TOP_CENTER).gap(0)))
                .in(ModeScope.CREATIVE, SlotPlacement.group(SlotGroupCategory.PLAYER_ARMOR,
                        PanelPosition.region(OutsideRegion.RIGHT_CENTER).gap(0)))
                .style(PanelStyle.NONE)
                .vertical()
                .gap(0)
                .build());
    }
}
