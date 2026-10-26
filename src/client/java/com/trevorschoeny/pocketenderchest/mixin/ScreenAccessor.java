package com.trevorschoeny.pocketenderchest.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

/** A container screen's slot under the mouse: what a double-click acts on. */
@Mixin(AbstractContainerScreen.class)
public interface ScreenAccessor {

    @Accessor("hoveredSlot")
    Slot pocketenderchest$hoveredSlot();
}
