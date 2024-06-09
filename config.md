## Configuration
Below is a description of all parameters for **Slotted Armor HUD** mod. The configuration file
is a JSON file with a JSON object (curly brackets) with all config parameters as its members (name-value pairs
inside curly brackets). If any of the configuration parameters are missing, their default values will be used.

### Position
1. ##### `"enabled"`
    * Toggles the armor HUD on and off.
    * Default value: `true`
1. ##### `"orientation"`
   * Orientation of the widget.
   * Possible values:
      * `"Horizontal"`
      * `"Vertical"`
   * Default value: `"Horizontal"`
1. ##### `"side"`
    * Side that the widget is displayed on.
    * Works in conjunction with 'Anchor'.
    * Possible values:
        * `"Left"`
        * `"Right"`
    * Default value: `"Left"`
1. ##### `"anchor"`
    * Place that the widget is attached to.
    * Works in conjunction with 'Side'.
    * Possible values:
        * `"Hotbar"`: widget is placed beside the hotbar.
        * `"Bottom"`: widget is placed at the bottom corner of the preferred side.
        * `"Top"`: widget is placed at the top corner of the preferred side.
        * `"Top_Center"`: widget is placed at the top center.
    * Default value: `Hotbar`
1. ##### `"offhandSlotBehavior"`
    * Defines how the widget reacts to the offhand slot and the attack indicator.
    * Effects the widget when anchored to the hotbar.
    * Possible values:
        * `"Leave_Space"`: widget always leaves space for the offhand slot, even if it is not shown.
        * `"Adhere"`: widget moves away when the offhand slot is shown or the attack indicator is at hotbar.
        * `"Ignore"`: widget never moves away to make space for the hotbar or the attack indicator.
    * Default value: `"Leave_Space"`
1. ##### `"offsetX"`
    * Offsets the widget along the horizontal axis.
    * Positive numbers move it away from the anchor point,
      negative numbers move it into the anchor point.
    * Default value: `0`
1. ##### `"offsetY"`
    * Offsets the widget along the vertical axis.
    * Positive numbers move it away from the side,
      negative numbers move it into the side.
    * Default value: `0`

### Appearance
1. ##### `"style"`
    * Defines how the widget is drawn.
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
    * Effects the widget when armor slots are always shown.
    * Default value: `true`
1. ##### `"reversed"`
    * Reverses the order of items shown in the widget.
    * Default value: `false`
1. ##### `"pushBossbars"`
    * Pushes bossbars below the widget.
    * Effects the widget when anchored to the top center.
    * Default value: `true`
1. ##### `"pushStatusEffectIcons"`
    * Pushes status effect icons below the widget.
    * Effects the widget when anchored to the top and to the right.
    * Default value: `true`
1. ##### `"pushSubtitles"`
    * Pushes subtitles above the widget.
    * Effects the widget when anchored to the bottom and to the right,
    * Default value: `true`
1. ##### `"warningShown"`
    * Displays a warning icon beside low-durability items.
    * Default value: `false`
