package ru.berdinskiybear.armorhud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.Level;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ArmorHudConfig {
    protected boolean enabled;
    protected Anchor anchor;
    protected Side side;
    protected Orientation orientation;
    protected OffhandSlotBehavior offhandSlotBehavior;
    protected int offsetX;
    protected int offsetY;
    protected Style style;
    protected SlotsShown slotsShown;
    protected boolean emptyIconsShown;
    protected boolean reversed;
    protected boolean pushBossbars;
    protected boolean pushChatBox;
    protected boolean pushStatusEffectIcons;
    protected boolean pushSubtitles;
    protected int[] slotTextures;
    protected int borderLength;
    protected boolean matchBorderAndSlotTextures;
    protected int bossbarSpacing;
    protected int statusEffectIconSpacing;
    protected int minOffsetBeforePushingChatBox;
    protected int minOffsetBeforePushingSubtitles;
    public enum Anchor {
        Hotbar,
        Bottom,
        Top,
        Top_Center
    }
    public enum Side {
        Left,
        Right
    }
    public enum Orientation {
        Horizontal,
        Vertical
    }
    public enum OffhandSlotBehavior {
        Leave_Space,
        Adhere,
        Ignore
    }
    public enum Style {
        Squared,
        Rounded
    }
    public enum SlotsShown {
        Show_Equipped,
        Show_All,
        Always_Show
    }

    public ArmorHudConfig() {
        this.enabled = true;
        this.anchor = Anchor.Bottom;
        this.side = Side.Left;
        this.orientation = Orientation.Horizontal;
        this.offhandSlotBehavior = OffhandSlotBehavior.Leave_Space;
        this.offsetX = 0;
        this.offsetY = 0;
        this.style = Style.Squared;
        this.slotsShown = SlotsShown.Show_Equipped;
        this.emptyIconsShown = true;
        this.reversed = false;
        this.pushBossbars = true;
        this.pushChatBox = true;
        this.pushStatusEffectIcons = true;
        this.pushSubtitles = true;
        this.slotTextures = new int[]{1, 2, 3, 4};
        this.borderLength = 3;
        this.matchBorderAndSlotTextures = true;
        this.bossbarSpacing = 0;
        this.statusEffectIconSpacing = 0;
        this.minOffsetBeforePushingChatBox = 0;
        this.minOffsetBeforePushingSubtitles = 0;
    }

    public ArmorHudConfig(ArmorHudConfig original) {
        this.enabled = original.enabled;
        this.anchor = original.anchor;
        this.side = original.side;
        this.orientation = original.orientation;
        this.offhandSlotBehavior = original.offhandSlotBehavior;
        this.offsetX = original.offsetX;
        this.offsetY = original.offsetY;
        this.style = original.style;
        this.slotsShown = original.slotsShown;
        this.emptyIconsShown = original.emptyIconsShown;
        this.reversed = original.reversed;
        this.pushBossbars = original.pushBossbars;
        this.pushChatBox = original.pushChatBox;
        this.pushStatusEffectIcons = original.pushStatusEffectIcons;
        this.pushSubtitles = original.pushSubtitles;
        this.slotTextures = original.slotTextures;
        this.borderLength = original.borderLength;
        this.bossbarSpacing = original.bossbarSpacing;
        this.statusEffectIconSpacing = original.statusEffectIconSpacing;
        this.matchBorderAndSlotTextures = original.matchBorderAndSlotTextures;
        this.minOffsetBeforePushingChatBox = original.minOffsetBeforePushingChatBox;
        this.minOffsetBeforePushingSubtitles = original.minOffsetBeforePushingSubtitles;
    }

    public static ArmorHudConfig readConfigFile() {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), ArmorHudMod.MOD_ID + ".json");
        if (configFile.exists()) {
            try (FileReader fileReader = new FileReader(configFile)) {
                ArmorHudConfig config = gson.fromJson(fileReader, ArmorHudConfig.class);
                return replaceNullAttributes(config);
            } catch (IOException e) {
                ArmorHudMod.log(Level.ERROR, "Config file " + configFile.getAbsolutePath() + " can't be read or has disappeared.");
                ArmorHudMod.log(Level.ERROR, e.getLocalizedMessage());
                return new ArmorHudConfig();
            }
        } else {
            ArmorHudMod.log("Config file is missing, creating new one...");
            return createNewConfigFile();
        }
    }

    public static void writeConfigFile(ArmorHudConfig config) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), ArmorHudMod.MOD_ID + ".json");
        try (FileWriter fileWriter = new FileWriter(configFile)) {
            gson.toJson(config, ArmorHudConfig.class, fileWriter);
        } catch (IOException e) {
            ArmorHudMod.log(Level.ERROR, "Config file " + configFile.getAbsolutePath() + " can't be written so it probably wasn't written.");
            ArmorHudMod.log(Level.ERROR, e.getLocalizedMessage());
        }
    }

    private static ArmorHudConfig createNewConfigFile() {
        ArmorHudConfig config = new ArmorHudConfig();
        writeConfigFile(config);
        return config;
    }

    private static ArmorHudConfig replaceNullAttributes(ArmorHudConfig config) {
        boolean containsNull = false;
        ArmorHudConfig.MutableConfig temporaryConfig = new ArmorHudConfig.MutableConfig(config);
        ArmorHudConfig defaultConfig = new ArmorHudConfig();
        if (temporaryConfig.getAnchor() == null) {
            temporaryConfig.setAnchor(defaultConfig.getAnchor());
            containsNull = true;
        }
        if (temporaryConfig.getSide() == null) {
            temporaryConfig.setSide(defaultConfig.getSide());
            containsNull = true;
        }
        if (temporaryConfig.getOrientation() == null) {
            temporaryConfig.setOrientation(defaultConfig.getOrientation());
            containsNull = true;
        }
        if (temporaryConfig.getOffhandSlotBehavior() == null) {
            temporaryConfig.setOffhandSlotBehavior(defaultConfig.getOffhandSlotBehavior());
            containsNull = true;
        }
        if (temporaryConfig.getStyle() == null) {
            temporaryConfig.setStyle(defaultConfig.getStyle());
            containsNull = true;
        }
        if (temporaryConfig.getSlotsShown() == null) {
            temporaryConfig.setSlotsShown(defaultConfig.getSlotsShown());
            containsNull = true;
        }
        if (containsNull)
            writeConfigFile(temporaryConfig);
        return new ArmorHudConfig(temporaryConfig);
    }

    public boolean isPreview() {
        return false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public Side getSide() {
        return side;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public OffhandSlotBehavior getOffhandSlotBehavior() {
        return offhandSlotBehavior;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public Style getStyle() {
        return style;
    }

    public SlotsShown getSlotsShown() {
        return slotsShown;
    }

    public boolean isEmptyIconsShown() {
        return emptyIconsShown;
    }

    public boolean isReversed() {
        return reversed;
    }

    public boolean isPushBossbars() {
        return this.pushBossbars;
    }

    public boolean isPushChatBox() {
        return this.pushChatBox;
    }

    public boolean isPushStatusEffectIcons() {
        return this.pushStatusEffectIcons;
    }

    public boolean isPushSubtitles() {
        return this.pushSubtitles;
    }

    public int[] getSlotTextures() {
        return slotTextures;
    }

    public int getBorderLength() {
        return borderLength;
    }

    public boolean isMatchBorderAndSlotTextures() {
        return matchBorderAndSlotTextures;
    }

    public int getBossbarSpacing() {
        return bossbarSpacing;
    }

    public int getStatusEffectIconSpacing() {
        return statusEffectIconSpacing;
    }

    public int getMinOffsetBeforePushingChatBox() {
        return minOffsetBeforePushingChatBox;
    }

    public int getMinOffsetBeforePushingSubtitles() {
        return minOffsetBeforePushingSubtitles;
    }

    public static class MutableConfig extends ArmorHudConfig {
        public MutableConfig() {
            super();
        }

        public MutableConfig(ArmorHudConfig currentConfig) {
            super(currentConfig);
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void setAnchor(Anchor anchor) {
            this.anchor = anchor;
        }

        public void setSide(Side side) {
            this.side = side;
        }

        public void setOrientation(Orientation orientation) {
            this.orientation = orientation;
        }

        public void setOffhandSlotBehavior(OffhandSlotBehavior offhandSlotBehavior) {
            this.offhandSlotBehavior = offhandSlotBehavior;
        }

        public void setOffsetX(int offsetX) {
            this.offsetX = offsetX;
        }

        public void setOffsetY(int offsetY) {
            this.offsetY = offsetY;
        }

        public void setStyle(Style style) {
            this.style = style;
        }

        public void setSlotsShown(SlotsShown slotsShown) {
            this.slotsShown = slotsShown;
        }

        public void setEmptyIconsShown(boolean iconsShown) {
            this.emptyIconsShown = iconsShown;
        }

        public void setReversed(boolean reversed) {
            this.reversed = reversed;
        }

        public void setPushBossbars(boolean pushBossbars) {
            this.pushBossbars = pushBossbars;
        }

        public void setPushChatBox(boolean pushChatBox) {
            this.pushChatBox = pushChatBox;
        }

        public void setPushStatusEffectIcons(boolean pushStatusEffectIcons) {
            this.pushStatusEffectIcons = pushStatusEffectIcons;
        }

        public void setPushSubtitles(boolean pushSubtitles) {
            this.pushSubtitles = pushSubtitles;
        }

        public void setSlotTexture1(int slot) {
            this.slotTextures[0] = slot;
        }

        public void setSlotTexture2(int slot) {
            this.slotTextures[1] = slot;
        }

        public void setSlotTexture3(int slot) {
            this.slotTextures[2] = slot;
        }

        public void setSlotTexture4(int slot) {
            this.slotTextures[3] = slot;
        }

        public void setBorderLength(int borderLength) {
            this.borderLength = borderLength;
        }

        public void setMatchBorderAndSlotTextures(boolean matchBorderAndSlotTextures) {
            this.matchBorderAndSlotTextures = matchBorderAndSlotTextures;
        }

        public void setBossbarSpacing(int bossbarSpacing) {
            this.bossbarSpacing = bossbarSpacing;
        }

        public void setStatusEffectIconSpacing(int statusEffectIconSpacing) {
            this.statusEffectIconSpacing = statusEffectIconSpacing;
        }

        public void setMinOffsetBeforePushingChatBox(int minOffsetBeforePushingChatBox) {
            this.minOffsetBeforePushingChatBox = minOffsetBeforePushingChatBox;
        }

        public void setMinOffsetBeforePushingSubtitles(int minOffsetBeforePushingSubtitles) {
            this.minOffsetBeforePushingSubtitles = minOffsetBeforePushingSubtitles;
        }
    }
}
