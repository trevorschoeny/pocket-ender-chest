package com.trevorschoeny.pocketenderchest.checks;

import com.trevorschoeny.pocketenderchest.EnderSlot;
import com.trevorschoeny.pocketenderchest.Opening;
import com.trevorschoeny.pocketenderchest.ShownView;

import com.trevlar.slotkit.api.slot.CreatedSlot;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import testgallery.SceneWorld;

import java.util.List;

/**
 * Self-checks for the slot scene (test-gallery/scenes/pocket-ender-chest/slot.json), plus
 * survival screenshots. With {@code -PwithGear} Simple Elytra Slot and Simple Totem Slot are
 * loaded too, and the ender chest slot must sit on top of their column. The double-click
 * itself is checked by hand; these send its packet and check what the server does with it.
 */
public class EnderSlotChecks implements FabricClientGameTest {

    private static final boolean GEAR = FabricLoader.getInstance().isModLoaded("simpleelytraslot");
    private static final String SUFFIX = GEAR ? "-with-gear" : "";

    @Override
    public void runTest(ClientGameTestContext ctx) {
        // With Open Anywhere installed, opening is Open Anywhere's: only its checks run then
        // (OpenAnywhereChecks, compiled only with -PwithOpenAnywhere).
        if (FabricLoader.getInstance().isModLoaded("openanywhere")) {
            try {
                Class.forName("com.trevorschoeny.pocketenderchest.checks.OpenAnywhereChecks")
                        .asSubclass(FabricClientGameTest.class).getDeclaredConstructor().newInstance().runTest(ctx);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("Open Anywhere is loaded but its checks aren't built (run with -PwithOpenAnywhere)", e);
            }
            return;
        }
        try (SceneWorld.Opened scene = SceneWorld.open(ctx, "pocket-ender-chest", "slot")) {
            scene.standAt(scene.station("pocketenderchest:slot_on_top"), 0);
            slotOnTop(ctx, scene);
            openAndBack(ctx, scene);
            collectSkipsSlot(ctx, scene);
            closesWhenEmptied(ctx, scene);
        }
    }

    /**
     * Station "pocketenderchest:slot_on_top": the slot sits on the 18 px grid on top of the
     * gear column above the offhand, refuses dirt, and takes a whole stack of ender chests.
     */
    private static void slotOnTop(ClientGameTestContext ctx, SceneWorld.Opened scene) {
        openInventory(ctx);
        ctx.takeScreenshot("ender-survival-empty" + SUFFIX);
        ctx.runOnClient(mc -> {
            List<Slot> slots = screenSlots(mc);
            Slot ender = slot(slots, EnderSlot.PANEL_ID);
            if (ender == null) throw new AssertionError("no ender chest slot on the inventory screen");
            // Without the other two it sits directly above the offhand; with them, above the elytra.
            Slot below = GEAR ? slot(slots, "simpleelytraslot:") : mc.player.inventoryMenu.getSlot(InventoryMenu.SHIELD_SLOT);
            if (below == null) throw new AssertionError("Simple Elytra Slot is loaded but its slot isn't on the screen");
            if (ender.x != below.x || ender.y != below.y - 18) {
                throw new AssertionError("ender chest slot at (" + ender.x + ", " + ender.y + ") should sit directly above the "
                        + (GEAR ? "elytra slot" : "offhand") + " at (" + below.x + ", " + below.y + ")");
            }
        });

        boolean gate = server(scene).computeOnServer(server -> {
            Slot s = slot(player(server).inventoryMenu.slots, EnderSlot.PANEL_ID);
            return !s.mayPlace(new ItemStack(Items.DIRT)) && s.mayPlace(new ItemStack(Items.ENDER_CHEST))
                    && s.getMaxStackSize(new ItemStack(Items.ENDER_CHEST)) == 64;
        });
        if (!gate) throw new AssertionError("the slot should take a whole stack of ender chests and refuse dirt");

        // The scene's 8 ender chests in the first hotbar slot (menu slot 36), shift-clicked in.
        ctx.runOnClient(mc -> mc.gameMode.handleContainerInput(
                mc.player.inventoryMenu.containerId, 36, 0, ContainerInput.QUICK_MOVE, mc.player));
        ctx.waitTicks(5);
        ctx.takeScreenshot("ender-survival-filled" + SUFFIX);
        int count = server(scene).computeOnServer(server -> EnderSlot.get(player(server)).getCount());
        if (count != 8) throw new AssertionError("shift-clicked 8 ender chests, the slot holds " + count);
    }

    /**
     * Station "pocketenderchest:open_and_back": a double-click's open turns the inventory into
     * the player's own ender chest, with the x showing; the x goes back to the inventory.
     */
    private static void openAndBack(ClientGameTestContext ctx, SceneWorld.Opened scene) {
        openInventory(ctx);
        sendOpen(ctx);
        ctx.waitForScreen(ContainerScreen.class);
        ctx.waitTicks(5);
        ctx.takeScreenshot("ender-open" + SUFFIX);
        String open = server(scene).computeOnServer(server -> {
            ServerPlayer p = player(server);
            return isOwnEnderChest(p, p.containerMenu) ? "" : "the menu in front is " + p.containerMenu.getClass().getSimpleName();
        });
        if (!open.isEmpty()) throw new AssertionError("after the open: " + open);
        if (!ctx.computeOnClient(mc -> ShownView.showing())) throw new AssertionError("the x should show on the ender chest screen");

        ctx.runOnClient(mc -> ClientPlayNetworking.send(Opening.CloseC2S.INSTANCE));
        ctx.waitForScreen(InventoryScreen.class);
        boolean back = server(scene).computeOnServer(server -> player(server).containerMenu == player(server).inventoryMenu);
        if (!back) throw new AssertionError("the x should go back to the inventory");
        int count = server(scene).computeOnServer(server -> EnderSlot.get(player(server)).getCount());
        if (count != 8) throw new AssertionError("after the round trip the slot holds " + count + " ender chests, not 8");
    }

    /**
     * Station "pocketenderchest:collect_skips_slot": vanilla's double-click collect on ender
     * chests elsewhere doesn't sweep the slot's.
     */
    private static void collectSkipsSlot(ClientGameTestContext ctx, SceneWorld.Opened scene) {
        boolean skipped = server(scene).computeOnServer(server -> {
            InventoryMenu menu = player(server).inventoryMenu;
            return !menu.canTakeItemForPickAll(new ItemStack(Items.ENDER_CHEST), slot(menu.slots, EnderSlot.PANEL_ID));
        });
        if (!skipped) throw new AssertionError("double-click collect may take from the ender chest slot");
    }

    /**
     * Station "pocketenderchest:closes_when_emptied": with the ender chest open from the slot,
     * emptying the slot closes the screen (it opens only while an ender chest is there).
     */
    private static void closesWhenEmptied(ClientGameTestContext ctx, SceneWorld.Opened scene) {
        openInventory(ctx);
        sendOpen(ctx);
        ctx.waitForScreen(ContainerScreen.class);
        server(scene).runOnServer(server -> {
            ServerPlayer p = player(server);
            slot(p.containerMenu.slots, EnderSlot.PANEL_ID).set(ItemStack.EMPTY);
        });
        ctx.waitTicks(5);
        boolean closed = server(scene).computeOnServer(server -> !isOwnEnderChest(player(server), player(server).containerMenu));
        if (!closed) throw new AssertionError("emptying the slot should close the ender chest opened from it");
        ctx.setScreen(() -> null);
    }

    // Helpers

    private static void openInventory(ClientGameTestContext ctx) {
        ctx.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
        ctx.waitForScreen(InventoryScreen.class);
        ctx.waitTicks(5);
    }

    /** The double-click's packet for the slot on the menu in front, as {@code DoubleClick} sends it. */
    private static void sendOpen(ClientGameTestContext ctx) {
        ctx.runOnClient(mc -> {
            Slot ender = slot(mc.player.containerMenu.slots, EnderSlot.PANEL_ID);
            ClientPlayNetworking.send(new Opening.OpenC2S(ender.index));
        });
    }

    private static boolean isOwnEnderChest(ServerPlayer p, AbstractContainerMenu menu) {
        return menu instanceof ChestMenu chest && chest.getContainer() == p.getEnderChestInventory();
    }

    private static List<Slot> screenSlots(Minecraft mc) {
        return ((AbstractContainerScreen<?>) mc.gui.screen()).getMenu().slots;
    }

    private static Slot slot(List<Slot> slots, String panelIdPrefix) {
        for (Slot s : slots) {
            CreatedSlot c = CreatedSlot.of(s);
            if (c != null && c.panelId().startsWith(panelIdPrefix)) return s;
        }
        return null;
    }

    private static net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server(SceneWorld.Opened scene) {
        return scene.world().getServer();
    }

    private static ServerPlayer player(MinecraftServer server) {
        return server.getPlayerList().getPlayers().getFirst();
    }
}
