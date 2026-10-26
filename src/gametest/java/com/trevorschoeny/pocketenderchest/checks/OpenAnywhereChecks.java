package com.trevorschoeny.pocketenderchest.checks;

import com.trevorschoeny.openanywhere.Settings;
import com.trevorschoeny.openanywhere.TabId;
import com.trevorschoeny.openanywhere.Tabs;
import com.trevorschoeny.pocketenderchest.EnderSlot;
import com.trevorschoeny.pocketenderchest.ShownView;

import com.trevlar.slotkit.api.item.ItemRef;
import com.trevlar.slotkit.api.slot.CreatedSlot;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

import testgallery.SceneWorld;

/**
 * With Open Anywhere installed, the ender chest slot opens through Open Anywhere and this
 * mod only adds the slot. Run with {@code -PwithOpenAnywhere}. Drives a
 * real double-click on the real slot, then checks: the player's ender chest in front as Open
 * Anywhere's tab from the carried slot, still there 20 ticks on; no x of this mod's on it;
 * then in Open Anywhere's "opened by hand" tab mode, back on the inventory the slot shows open
 * (screenshots of the lid, and of the grey mark with animations off) and a second
 * double-click closes it.
 */
public class OpenAnywhereChecks implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext ctx) {
        try (SceneWorld.Opened scene = SceneWorld.open(ctx, "pocket-ender-chest", "slot")) {
            scene.standAt(scene.station("pocketenderchest:slot_on_top"), 0);
            ctx.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
            ctx.waitForScreen(InventoryScreen.class);
            ctx.waitTicks(5);
            // The scene's 8 ender chests (hotbar 0, menu slot 36), shift-clicked into the slot.
            ctx.runOnClient(mc -> mc.gameMode.handleContainerInput(
                    mc.player.inventoryMenu.containerId, 36, 0, ContainerInput.QUICK_MOVE, mc.player));
            ctx.waitTicks(5);

            // Open: a real double-click on the slot.
            doubleClickSlot(ctx);
            ctx.waitForScreen(ContainerScreen.class);
            ctx.waitTicks(20);
            String open = scene.world().getServer().computeOnServer(server -> {
                ServerPlayer p = player(server);
                if (!(p.containerMenu instanceof ChestMenu c) || c.getContainer() != p.getEnderChestInventory()) {
                    return "the menu in front is " + p.containerMenu.getClass().getSimpleName();
                }
                return EnderSlot.get(p).getCount() == 8 ? "" : "the slot holds " + EnderSlot.get(p).getCount();
            });
            if (!open.isEmpty()) throw new AssertionError("20 ticks after the double-click: " + open);
            ctx.runOnClient(mc -> {
                if (!(mc.gui.screen() instanceof ContainerScreen)) throw new AssertionError("the ender chest closed on its own: " + mc.gui.screen());
                var path = Tabs.shown().path();
                if (path == null || path.source() != ItemRef.Source.CARRIED) {
                    throw new AssertionError("Open Anywhere's tab in front isn't the carried slot's: " + Tabs.shown());
                }
                if (ShownView.showing()) throw new AssertionError("this mod's x shows on Open Anywhere's screen");
            });
            ctx.takeScreenshot("oa-ender-open");

            // In the default mode (every box tabbed) an item looks open only while it's in front,
            // and the slot isn't on the ender chest's screen. The lid and the closing double-click
            // are the "opened by hand" mode's, where an opened item keeps its tab: open it again
            // there, then go back to the inventory.
            ctx.runOnClient(mc -> Tabs.pick(TabId.INVENTORY));
            ctx.waitForScreen(InventoryScreen.class);
            ctx.runOnClient(mc -> Settings.setTabs(Settings.TabMode.HAND));
            ctx.waitTicks(5);
            doubleClickSlot(ctx);
            ctx.waitForScreen(ContainerScreen.class);
            ctx.runOnClient(mc -> Tabs.pick(TabId.INVENTORY));
            ctx.waitForScreen(InventoryScreen.class);
            ctx.waitTicks(10);
            ctx.runOnClient(mc -> {
                if (!Tabs.looksOpen(enderSlot(mc))) throw new AssertionError("back on the inventory, the slot doesn't look open");
            });
            ctx.takeScreenshot("oa-slot-lid");
            ctx.runOnClient(mc -> Settings.setAnimation(false));
            ctx.waitTicks(3);
            ctx.takeScreenshot("oa-slot-grey");
            ctx.runOnClient(mc -> Settings.setAnimation(true));

            // Close: a second real double-click on the slot.
            doubleClickSlot(ctx);
            ctx.waitTicks(5);
            ctx.runOnClient(mc -> {
                if (!(mc.gui.screen() instanceof InventoryScreen)) throw new AssertionError("after the closing double-click: " + mc.gui.screen());
                if (Tabs.looksOpen(enderSlot(mc))) throw new AssertionError("the closing double-click left the slot open");
            });
            int count = scene.world().getServer().computeOnServer(server -> EnderSlot.get(player(server)).getCount());
            if (count != 8) throw new AssertionError("after open and close the slot holds " + count + " ender chests, not 8");
            ctx.runOnClient(mc -> Settings.setTabs(Settings.TabMode.EVERY));
            ctx.setScreen(() -> null);
        }
    }

    private static void doubleClickSlot(ClientGameTestContext ctx) {
        EnderSlotChecks.doubleClickSlot(ctx);
    }

    private static Slot enderSlot(Minecraft mc) {
        for (Slot s : ((AbstractContainerScreen<?>) mc.gui.screen()).getMenu().slots) {
            CreatedSlot c = CreatedSlot.of(s);
            if (c != null && c.panelId().equals(EnderSlot.GROUP_ID.panelId())) return s;
        }
        throw new AssertionError("no ender chest slot on " + mc.gui.screen());
    }

    private static ServerPlayer player(MinecraftServer server) {
        return server.getPlayerList().getPlayers().getFirst();
    }
}
