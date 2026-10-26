package com.trevorschoeny.pocketenderchest;

import com.trevlar.slotkit.api.slot.SlotDoubleClick;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * A plain left double-click on the ender chest slot opens the ender chest, through SlotKit's
 * {@link SlotDoubleClick}, the same gesture Open Anywhere's double-click uses. Registered only
 * without Open Anywhere, whose own double-click opens it then.
 *
 * <p>SlotKit owns the gesture: the window, the still cursor, creative's real slot, and putting
 * the lifted stack back on the client. This mod only says which slot qualifies and asks the
 * server to open; the server puts the stack back too ({@link Opening#open}).
 */
final class DoubleClick {

    private DoubleClick() {}

    static void register() {
        SlotDoubleClick.register(PocketEnderChest.MOD_ID + ":open",
                (slot, stack) -> Opening.isEnderSlot(slot) && EnderSlot.isEnderChest(stack),
                pair -> ClientPlayNetworking.send(new Opening.OpenC2S(pair.menuIndex())));
    }
}
