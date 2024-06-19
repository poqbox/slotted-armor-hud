## Configuration
Below is a description of all parameters for **Slotted Armor HUD** mod. The configuration file
is a JSON file with a JSON object (curly brackets) with all config parameters as its members (name-value pairs
inside curly brackets). If any of the configuration parameters are missing, their default values will be used.

### Position
1. ##### `"enabled"`
    * Toggles the armor HUD on and off.
    * Default value: `true`
1. ##### `"anchor"`
    * Place that the armor HUD is attached to.
    * Works in conjunction with 'Side'.
    * Possible values:
        * `"Hotbar"`: armor HUD is placed beside the hotbar.
        * `"Bottom"`: armor HUD is placed at the bottom corner of the preferred side.
        * `"Top"`: armor HUD is placed at the top corner of the preferred side.
        * `"Top_Center"`: armor HUD is placed at the top center.
    * Default value: `Hotbar`
1. ##### `"side"`
   * Side that the armor HUD is displayed on.
   * Works in conjunction with 'Anchor'.
   * Possible values:
      * `"Left"`
      * `"Right"`
   * Default value: `"Left"`
1. ##### `"orientation"`
   * Orientation of the armor HUD.
   * Possible values:
      * `"Horizontal"`
      * `"Vertical"`
   * Default value: `"Horizontal"`
1. ##### `"offhandSlotBehavior"`
    * Defines how the armor HUD reacts to the offhand slot and the attack indicator.
    * Effects the armor HUD when anchored to the hotbar.
    * Possible values:
        * `"Leave_Space"`: armor HUD always leaves space for the offhand slot, even if it is not shown.
        * `"Adhere"`: armor HUD moves away when the offhand slot is shown or the attack indicator is at hotbar.
        * `"Ignore"`: armor HUD never moves away to make space for the hotbar or the attack indicator.
    * Default value: `"Leave_Space"`
1. ##### `"offsetX"`
    * Offsets the armor HUD along the horizontal axis.
    * Positive numbers move it away from the anchor point,
      negative numbers move it into the anchor point.
    * Default value: `0`
1. ##### `"offsetY"`
    * Offsets the armor HUD along the vertical axis.
    * Positive numbers move it away from the side,
      negative numbers move it into the side.
    * Default value: `0`

### Behavior
1. ##### `"style"`
    * Defines how the armor HUD is drawn.
    * Possible values:
        * `"Squared"`
        * `"Rounded"`
    * Default value: `"Squared"`
1. ##### `"slotsShown"`
    * Defines when and which armor slots are shown.
    * Possible values:
        * `"Show_Equipped"`: only filled slots are shown.
        * `"Show_All"`: all slots are shown if at least one of the armor slots is filled.
        * `"Always_Show"`: slots are always shown.
    * Default value: `"Show_Equipped"`
1. ##### `"emptyIconsShown"`
    * Displays an armor outline in empty slots.
    * Affects the armor HUD when armor slots are always shown.
    * Default value: `true`
1. ##### `"reversed"`
    * Reverses the order of items shown in the armor HUD.
    * Default value: `false`
1. ##### `"pushBossbars"`
    * Pushes bossbars below the armor HUD.
    * Affects the armor HUD when anchored to the top center.
    * Default value: `true`
1. ##### `"pushStatusEffectIcons"`
    * Pushes status effect icons below the armor HUD.
    * Affects the armor HUD when anchored to the top and to the right.
    * Default value: `true`
1. ##### `"pushSubtitles"`
    * Pushes subtitles above the armor HUD.
    * Affects the armor HUD when anchored to the bottom and to the right,
    * Default value: `true`
