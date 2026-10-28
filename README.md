# Pocket Ender Chest

Pocket Ender Chest gives your ender chest a slot of its own. Put an ender chest in it, and double-click it whenever your inventory is open to see your ender chest right there, without placing one down. It's made for the Inventory Plus ecosystem, and sits on top of the elytra and totem slots if you use those mods.

It has to be installed on the server as well as the client. In singleplayer, your game is both.

### Features

#### The ender chest slot

- One slot for ender chests, with a faint ender chest outline when it's empty.
- It holds a whole stack, and shift-clicking an ender chest in your inventory puts it there first.
- In survival it sits above your offhand. On creative's Survival Inventory tab, it sits to the right of your armor.

#### Open it from your inventory

- Double-click the slot and your inventory turns into your ender chest. An x on the corner takes you back.
- It opens only while there's an ender chest in the slot.

#### With Open Anywhere

- The double-click opens your ender chest as an Open Anywhere tab instead, next to your shulker boxes and bundles.

#### With Inventory Plus

- The settings are on an Ender Chest tab in Inventory Plus's settings menu.

### Requirements

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [MenuKit](https://modrinth.com/mod/menukit) 7.5.0 or newer, below 8.0.0, which draws the slot and the x button.
- [SlotKit](https://modrinth.com/mod/slotkit) 2.2.0 or newer, below 3.0.0, which adds the slot to your inventory, saves it with your player on the server, and checks that a joining player has the mod.

### Pairs well with

- [Open Anywhere](https://modrinth.com/mod/open-anywhere), my mod for opening shulker boxes, bundles, and your ender chest from your inventory, each in its own tab.
- [Simple Elytra Slot](https://modrinth.com/mod/simple-elytra-slot) and [Simple Totem Slot](https://modrinth.com/mod/simple-totem-slot), my mods with the same kind of slot for an elytra and a totem. The ender chest slot sits on top of their column.
- [Inventory Plus](https://modrinth.com/mod/inventory-plus-trevlar), my mod for restocking, sorting, and locking slots. With it, Pocket Ender Chest gets a settings tab.

### Compatibility

It runs on Minecraft 26.3 with Fabric. Install it on both the client and the server. A server with Pocket Ender Chest turns away players who don't have it, with a message naming the mods to install.

### How to use

#### The slot

Open your inventory. The ender chest slot is directly above your offhand, on top of the elytra and totem slots if you have them.

- Drag an ender chest into it, or shift-click one in your inventory. Other items can't go in it, and it holds up to a full stack.
- Shift-click the slot to move its ender chests back into your inventory.
- Double-click collecting elsewhere in your inventory leaves the slot alone.
- In creative, open the Survival Inventory tab. There the slot sits to the right of your armor.
- When you die, the slot's ender chests drop with the rest of your inventory, and they're kept when the keepInventory game rule is on.
- The slot's ender chests are saved with your player on the server.

#### Opening your ender chest

- With an ender chest in the slot, double-click it. Keep the mouse still between the two clicks, and land them within one second.
- Your inventory screen turns into your ender chest, the same 27 slots a placed ender chest shows.
- Click the x on the top-right corner to go back to your inventory.
- If the slot is emptied while your ender chest is open, it closes.
- It opens from your inventory only, not from a chest or other container screen.

#### With Open Anywhere

With Open Anywhere installed, the double-click opens your ender chest as an Open Anywhere tab instead, and the x isn't shown. The slot's ender chest is the one Open Anywhere uses for its ender chest tab.

#### Settings

The settings are on the Ender Chest tab in Inventory Plus's settings menu. Without Inventory Plus there's no settings menu, and the defaults apply. They're saved in `config/pocketenderchest/config.json`.

| Setting on the Ender Chest tab | Default |
| --- | --- |
| Pocket Ender Chest (the switch) | On |
| Show the ender chest slot | On |

- Turning Pocket Ender Chest off hides the slot and closes your ender chest if it's open from the slot. The ender chests stay in the slot.
- Reset to Defaults puts both settings back.

### License

MIT.
