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
    protected Side side;
    protected Anchor anchor;
    protected Orientation orientation;
    protected OffhandSlotBehavior offhandSlotBehavior;
    protected int offsetX;
    protected int offsetY;
    protected Style style;
    protected SlotsShown slotsShown;
    protected boolean emptyIconsShown;
    protected boolean reversed;
    protected boolean pushBossbars;
    protected boolean pushStatusEffectIcons;
    protected boolean pushSubtitles;
    protected boolean warningShown;
    protected int minDurabilityValue;
    protected double minDurabilityPercentage;
    protected float warningIconBobbingIntervalMs;
    protected int[] slotTextures;
    protected int borderLength;
    protected boolean matchBorderAndSlotTextures;

    public enum Orientation {
        Horizontal,
        Vertical
    }

    public enum Anchor {
        Bottom,
        Hotbar,
        Top,
        Top_Center
    }

    public enum Side {
        Left,
        Right
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
        Always,
        All,
        Equipped
    }

    public ArmorHudConfig() {
        this.enabled = true;
        this.orientation = Orientation.Horizontal;
        this.anchor = Anchor.Hotbar;
        this.side = Side.Left;
        this.offhandSlotBehavior = OffhandSlotBehavior.Leave_Space;
        this.offsetX = 0;
        this.offsetY = 0;
        this.style = Style.Squared;
        this.slotsShown = SlotsShown.Equipped;
        this.emptyIconsShown = true;
        this.reversed = false;
        this.pushBossbars = true;
        this.pushStatusEffectIcons = true;
        this.pushSubtitles = true;
        this.warningShown = false;
        this.minDurabilityValue = 10;
        this.minDurabilityPercentage = 0.115D;
        this.warningIconBobbingIntervalMs = 2000.0F;
        this.slotTextures = new int[]{1, 1, 1, 1};
        this.borderLength = 3;
        this.matchBorderAndSlotTextures = false;
    }

    public ArmorHudConfig(ArmorHudConfig original) {
        this.enabled = original.enabled;
        this.orientation = original.orientation;
        this.anchor = original.anchor;
        this.side = original.side;
        this.offhandSlotBehavior = original.offhandSlotBehavior;
        this.offsetX = original.offsetX;
        this.offsetY = original.offsetY;
        this.style = original.style;
        this.slotsShown = original.slotsShown;
        this.emptyIconsShown = original.emptyIconsShown;
        this.reversed = original.reversed;
        this.pushBossbars = original.pushBossbars;
        this.pushStatusEffectIcons = original.pushStatusEffectIcons;
        this.pushSubtitles = original.pushSubtitles;
        this.warningShown = original.warningShown;
        this.minDurabilityValue = original.minDurabilityValue;
        this.minDurabilityPercentage = original.minDurabilityPercentage;
        this.warningIconBobbingIntervalMs = original.warningIconBobbingIntervalMs;
        this.slotTextures = original.slotTextures;
        this.borderLength = original.borderLength;
        this.matchBorderAndSlotTextures = original.matchBorderAndSlotTextures;
    }

    public static ArmorHudConfig readConfigFile() {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), ArmorHudMod.MOD_ID + ".json");
        if (configFile.exists()) {
            try (FileReader fileReader = new FileReader(configFile)) {
                return gson.fromJson(fileReader, ArmorHudConfig.class);
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

    public boolean isPreview() {
        return false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public Side getSide() {
        return side;
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

    public boolean getEmptyIconsShown() {
        return emptyIconsShown;
    }

    public boolean isReversed() {
        return reversed;
    }

    public boolean getPushBossbars() {
        return this.pushBossbars;
    }

    public boolean getPushStatusEffectIcons() {
        return this.pushStatusEffectIcons;
    }

    public boolean getPushSubtitles() {
        return this.pushSubtitles;
    }

    public boolean isWarningShown() {
        return warningShown;
    }

    public int getMinDurabilityValue() {
        return minDurabilityValue;
    }

    public double getMinDurabilityPercentage() {
        return minDurabilityPercentage;
    }

    public float getWarningIconBobbingIntervalMs() {
        return warningIconBobbingIntervalMs;
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

        public void setOrientation(Orientation orientation) {
            this.orientation = orientation;
        }

        public void setAnchor(Anchor anchor) {
            this.anchor = anchor;
        }

        public void setSide(Side side) {
            this.side = side;
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

        public void setPushStatusEffectIcons(boolean pushStatusEffectIcons) {
            this.pushStatusEffectIcons = pushStatusEffectIcons;
        }

        public void setPushSubtitles(boolean pushSubtitles) {
            this.pushSubtitles = pushSubtitles;
        }

        public void setWarningShown(boolean warningShown) {
            this.warningShown = warningShown;
        }

        public void setMinDurabilityValue(int minDurabilityValue) {
            this.minDurabilityValue = minDurabilityValue;
        }

        public void setMinDurabilityPercentage(double minDurabilityPercentage) {
            this.minDurabilityPercentage = minDurabilityPercentage;
        }

        public void setWarningIconBobbingIntervalMs(float warningIconBobbingIntervalMs) {
            this.warningIconBobbingIntervalMs = warningIconBobbingIntervalMs;
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
    }
}
