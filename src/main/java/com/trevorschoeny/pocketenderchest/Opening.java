package com.trevorschoeny.pocketenderchest;

import com.trevlar.slotkit.api.menu.MenuSwitch;
import com.trevlar.slotkit.api.slot.CreatedSlot;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Opening the ender chest from the slot, on the server, without Open Anywhere.
 *
 * <p>A double-click on the slot ({@link OpenC2S}) turns the inventory into vanilla's ender
 * chest screen on the player's own 27 ender chest slots, through SlotKit's
 * {@link MenuSwitch}. The slot shows only on the inventory screens (Trev, 2026-10-25: no
 * opening from a chest), but the x ({@link CloseC2S}) still asks MenuSwitch to go back first,
 * so a menu set aside would come back rather than being dropped. Escape closes it.
 *
 * <p>The ender chest screen stays open only while an ender chest is in the slot and the mod
 * is on ({@link EnderView#stillValid}); vanilla closes it the tick that stops being true.
 *
 * <p>With Open Anywhere on the client, the client never sends {@code OpenC2S}: the double-click
 * is Open Anywhere's. The handlers stay registered either way; they only ever act on this
 * mod's slot and this mod's screen.
 */
public final class Opening {

    private Opening() {}

    private static final Component TITLE = Component.translatable("container.enderchest");

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(OpenC2S.TYPE, OpenC2S.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(CloseC2S.TYPE, CloseC2S.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ShownS2C.TYPE, ShownS2C.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(OpenC2S.TYPE, (payload, context) -> open(context.player(), payload.menuSlot()));
        ServerPlayNetworking.registerGlobalReceiver(CloseC2S.TYPE, (payload, context) -> close(context.player()));
    }

    /**
     * A double-click on menu slot {@code menuSlot} of the player's open menu. Acts only on
     * this mod's slot. The double-click's first click lifted the stack onto the cursor, so it
     * goes back into the slot first; then, with an ender chest there, the ender chest opens.
     */
    static void open(ServerPlayer player, int menuSlot) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menuSlot < 0 || menuSlot >= menu.slots.size()) return;
        Slot slot = menu.getSlot(menuSlot);
        if (!isEnderSlot(slot)) return;

        ItemStack carried = menu.getCarried();
        if (!slot.hasItem() && EnderSlot.isEnderChest(carried)) menu.setCarried(slot.safeInsert(carried));

        // Nothing to open (mod off, slot empty, a refused put-back): only resync what the client guessed.
        if (!EnderSlotSettings.enabled() || !EnderSlot.isEnderChest(slot.getItem())) {
            menu.broadcastFullState();
            return;
        }
        // Already looking at the ender chest, ours or a placed one's.
        if (menu instanceof ChestMenu chest && chest.getContainer() == player.getEnderChestInventory()) return;

        if (MenuSwitch.open(player, new SimpleMenuProvider(EnderView::new, TITLE)).isPresent()) {
            ServerPlayNetworking.send(player, ShownS2C.INSTANCE);
        }
    }

    /** The x on this mod's ender chest screen: back where the double-click came from. */
    static void close(ServerPlayer player) {
        if (!(player.containerMenu instanceof EnderView)) return;
        if (MenuSwitch.back(player)) return;
        if (!MenuSwitch.openInventory(player)) player.closeContainer();
    }

    /** Whether {@code slot} is this mod's ender chest slot, on any menu. */
    public static boolean isEnderSlot(Slot slot) {
        CreatedSlot created = CreatedSlot.of(slot);
        return created != null && EnderSlot.GROUP_ID.panelId().equals(created.panelId());
    }

    /** The ender chest as opened from the slot: vanilla's 3-row chest menu on the player's own ender chest. */
    static final class EnderView extends ChestMenu {
        EnderView(int containerId, Inventory inventory, Player player) {
            super(MenuType.GENERIC_9x3, containerId, inventory, player.getEnderChestInventory(), 3);
        }

        /** Open only while an ender chest is in the slot and the mod is on. */
        @Override
        public boolean stillValid(Player player) {
            return super.stillValid(player) && EnderSlotSettings.enabled() && EnderSlot.isEnderChest(EnderSlot.get(player));
        }
    }

    // The packets

    /** A double-click on menu slot {@code menuSlot}, this mod's slot. */
    public record OpenC2S(int menuSlot) implements CustomPacketPayload {
        public static final Type<OpenC2S> TYPE = new Type<>(Identifier.fromNamespaceAndPath(PocketEnderChest.MOD_ID, "open"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenC2S> CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, OpenC2S::menuSlot, OpenC2S::new);
        @Override public Type<OpenC2S> type() { return TYPE; }
    }

    /** The x on this mod's ender chest screen. */
    public record CloseC2S() implements CustomPacketPayload {
        public static final CloseC2S INSTANCE = new CloseC2S();
        public static final Type<CloseC2S> TYPE = new Type<>(Identifier.fromNamespaceAndPath(PocketEnderChest.MOD_ID, "close"));
        public static final StreamCodec<RegistryFriendlyByteBuf, CloseC2S> CODEC = StreamCodec.unit(INSTANCE);
        @Override public Type<CloseC2S> type() { return TYPE; }
    }

    /** The menu just opened is this mod's ender chest screen: the client shows the x on it. */
    public record ShownS2C() implements CustomPacketPayload {
        public static final ShownS2C INSTANCE = new ShownS2C();
        public static final Type<ShownS2C> TYPE = new Type<>(Identifier.fromNamespaceAndPath(PocketEnderChest.MOD_ID, "shown"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ShownS2C> CODEC = StreamCodec.unit(INSTANCE);
        @Override public Type<ShownS2C> type() { return TYPE; }
    }
}
