# Timewarp

[![Made for ModFest 1.21](https://cdn.modrinth.com/data/cached_images/3b57a6c281e514bc6d3bb36f3dd5621b015a0185.png)](https://modfest.net/1.21)

**Requires Fabric API. Also compatible with NeoForge, using Sintrya Connector, requires Forgified Fabric API.**

**Timewarp** is a Minecraft mod that brings back classic game mechanics with a fun twist! It adds random challenges with retro gameplay and lets you create areas where old-school mechanics can be enabled. Customize everything to bring back the feel of older Minecraft versions.

The mod’s data is saved in the world’s root folder as `timewarp_data.json`. This includes all configuration settings, timewarp areas, and shift settings, making your Timewarp setup world-specific.
## Usage

To set up a Timewarp area:

1. Use the *Timewarp Axe* to select corners:
   - **Left-click** on a block to set the first corner.
   - **Right-click** on a block to set the second corner.
2. Run `/timewarp create [name]` to create the area using the selected corners. Replace `[name]` with a custom name for your area, e.g., `/timewarp create "Retro Zone"`.

## Old Mechanics

<details>
<summary>Overview</summary>

- **`allowStacking`**: Prevents stacking of food items.
- **`oldMinecart`**: The player’s head moves according to the minecart's direction.
- **`oldAnimalBehavior`**: Sheep, chickens, and cows don’t drop food. Punching sheep drops wool.
- **`allowSprinting`**: Toggles whether sprinting is allowed, as in early Minecraft versions.
- **`versionText`**: Displays classic version text on the screen.
- **`oldGUI`**: Mimics the classic player HUD. Enables the old eating system where food replenishes health directly (no hunger bar) and removes the eating animation.
- **`noFrontView`**: Disables the front-facing player view, as in early versions.
- **`noSneaking`**: Disables sneaking.
- **`noSwimming`**: Disables swimming, as in pre-1.13 versions.
- **`oldCombat`**: Reverts combat mechanics to earlier versions, removing the attack cooldown.
- **`noTrading`**: Disables trading with villagers.
- **`oldLook`**: Uses old textures.
- **`noSmoothLighting`**: Disables smooth lighting.

</details>

## Commands

<details>
<summary><code>/timewarp</code> - General command to access Timewarp functions</summary>

- **Grants** the player a *Timewarp Axe*, allowing manipulation of Timewarp areas.
</details>

<details>
<summary><code>/timewarp create [name]</code> - Create a Timewarp Area</summary>

- **Creates** a Timewarp area using two selected corners.
- **Parameters**: `name` - the name of the area.
- **Example**: `/timewarp create "Retro Zone"`
</details>

<details>
<summary><code>/timewarp edit [id] [feature] [enabled]</code> - Edit a feature in a Timewarp Area</summary>

- **Edits** specific features of an area by ID.
- **Parameters**:
  - `id`: ID of the area.
  - `feature`: Name of the feature to edit.
  - `enabled`: true/false to enable or disable the feature.
- **Features**:
  - `allowStacking`, `oldMinecart`, `oldAnimalBehavior`, `allowSprinting`, `versionText`, `oldGUI`, `noFrontView`, `noSneaking`, `noSwimming`, `oldCombat`, `noTrading`, `oldLook`, `noSmoothLighting`
- **Example**: `/timewarp edit 1 oldCombat true`
</details>

<details>
<summary><code>/timewarp delete [id]</code> - Delete a Timewarp Area</summary>

- **Deletes** a Timewarp area by its ID.
- **Example**: `/timewarp delete 2`
</details>

<details>
<summary><code>/timewarp trigger [player]</code> - Trigger a Time Shift</summary>

- **Forces** a time shift on a specified player, applying random retro mechanics.
- **Example**: `/timewarp trigger Player1`
</details>

<details>
<summary><code>/timewarp tp [id]</code> - Teleport to a Timewarp Area</summary>

- **Teleports** the player to the specified area by ID.
- **Example**: `/timewarp tp 2`
</details>

<details>
<summary><code>/timewarp config [reload|edit]</code> - Manage Timewarp Configuration</summary>

- **reload**: Reloads all configuration settings from the data file.
- **edit**: Modify specific config variables.
- **Parameters** for `edit`:
  - `variable`: Setting to edit (`shiftDurationMin`, `shiftDurationMax`, `timeUntilShiftMin`, `timeUntilShiftMax`, `saveInterval`, `opCommandLevel`, `enableTriggering`)
  - `value`: New value for the variable.
- **Example**: `/timewarp config edit shiftDurationMax 300`
</details>

## Configuration

**Customization options** are available to change how the time shifts work. Use these configurations in the `config` command or edit directly in the `timewarp_data.json` file in the world folder.

<details>
<summary>Shift Duration</summary>

- **`shiftDurationMin`** - Minimum duration of a time shift.
- **`shiftDurationMax`** - Maximum duration of a time shift.
</details>

<details>
<summary>Time Until Shift</summary>

- **`timeUntilShiftMin`** - Minimum time before a new shift starts.
- **`timeUntilShiftMax`** - Maximum time before a new shift starts.
</details>

<details>
<summary>Save Interval</summary>

- **`saveInterval`** - Frequency to save data automatically.
</details>

<details>
<summary>Permissions and Triggers</summary>

- **`opCommandLevel`** - Minimum permission level for commands.
- **`enableTriggering`** - Enable or disable automatic time shift triggers for players.
</details>
