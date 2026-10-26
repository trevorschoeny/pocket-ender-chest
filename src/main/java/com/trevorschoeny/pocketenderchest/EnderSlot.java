package com.trevorschoeny.pocketenderchest;

import com.trevlar.slotkit.api.group.SlotGroupCategory;
import com.trevlar.slotkit.api.group.SlotGroupId;
import com.trevlar.slotkit.api.slot.ParityGroup;
import com.trevlar.slotkit.api.slot.SlotSpec;
import com.trevlar.slotkit.api.storage.StorageAttachment;
import com.trevlar.slotkit.api.storage.StorageAttachment.PlayerStorageAttachment;
import com.trevlar.slotkit.api.window.GatingContext;
import com.trevlar.slotkit.api.window.SlotGate;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The ender chest slot: its storage and what it accepts.
 *
 * <p>One real synced slot backed by a 1-slot player attachment, registered as a SlotKit
 * {@link ParityGroup} at common init, so SlotKit builds it on every container menu on both
 * sides. Where it shows is the client's {@code EnderPanel}. The same shape as Simple Elytra
 * Slot's and Simple Totem Slot's slots, which share the column; it differs in taking a
 * whole stack and in what a double-click on it does ({@link Opening}).
 */
public final class EnderSlot {

    private EnderSlot() {}

    /** The group's owner id, also the client panel's id; SlotKit derives {@link #GROUP_ID} from it. */
    public static final String PANEL_ID = PocketEnderChest.MOD_ID + ":ender_chest";
    public static final String GROUP = "slot";

    /**
     * Its own category, not {@code PLAYER_INVENTORY}: a search for spares (a restock, a
     * move-matching, another mod's inventory scan) must not take the ender chest the player
     * keeps here.
     */
    public static final SlotGroupCategory CATEGORY = new SlotGroupCategory(PocketEnderChest.MOD_ID, "ender_chest");

    /** The slot's group, for SlotKit lookups (operations, Reach, locks). */
    public static final SlotGroupId.Created GROUP_ID = ParityGroup.idOf(PANEL_ID, GROUP);

    /** The slot group, set by {@link #register}. */
    public static ParityGroup ENDER_CHEST;

    /**
     * The ender chests, saved with the player on the server. Drops on death as vanilla's
     * inventory does (SlotKit's attachment default, which follows keepInventory). Only the
     * owner's client needs the contents, through the menu, so it isn't the "visible" kind.
     */
    private static PlayerStorageAttachment<NonNullList<ItemStack>> STORAGE;

    /** Call once from the main entrypoint, on both sides. */
    public static void register() {
        STORAGE = StorageAttachment.playerAttached(PocketEnderChest.MOD_ID, "ender_chest", 1);

        // The slot's data only. The panel it shows in is the client's (EnderPanel).
        ENDER_CHEST = ParityGroup.register(PANEL_ID, SlotSpec.at(GROUP, CATEGORY)
                .storage(p -> STORAGE.bind(p))
                .label("Ender chest slot")
                .gate(GATE)
                // Shift-clicked ender chests land here first.
                .shiftClickIn(true)
                // Ender chests stack, so vanilla's double-click collect elsewhere would
                // sweep this slot's into the cursor; the kept stack stays put.
                .collect(false));
    }

    /**
     * Ender chests only, a whole stack allowed (Trev, 2026-10-24). Placing is refused while
     * the mod is off or the slot is hidden, which also stops shift-click routing into a slot
     * nobody can see; taking out is always allowed.
     */
    private static final SlotGate GATE = new SlotGate() {
        @Override public boolean mayPlace(ItemStack stack, GatingContext ctx) {
            return EnderSlotSettings.enabled() && EnderSlotSettings.showSlot() && isEnderChest(stack);
        }
        @Override public boolean mayPickup(Player player, GatingContext ctx) { return true; }
        @Override public int maxStackSize(ItemStack stack, int vanillaMax) { return vanillaMax; }
    };

    /** True for an ender chest, the only item the slot takes. */
    public static boolean isEnderChest(ItemStack stack) {
        return stack.is(Items.ENDER_CHEST);
    }

    /** What's in the slot, or EMPTY. A copy. */
    public static ItemStack get(Player player) {
        return STORAGE.bind(player).getStack(0);
    }
}
