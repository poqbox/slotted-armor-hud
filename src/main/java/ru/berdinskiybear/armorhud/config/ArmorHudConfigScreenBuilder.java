package ru.berdinskiybear.armorhud.config;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig.*;
import java.util.Arrays;
import java.util.Optional;

public class ArmorHudConfigScreenBuilder {

    public static Screen create(Screen parentScreen) {
        ArmorHudConfig defaultConfig = new ArmorHudConfig();

        ConfigBuilder configBuilder = ConfigBuilder.create()
                .setParentScreen(parentScreen)
                .setShouldListSmoothScroll(false)
                .setShouldTabsSmoothScroll(false)
                .transparentBackground()
                .setTitle(ArmorHudMod.CONFIG_SCREEN_NAME)
                .setAfterInitConsumer((screen) -> {
                    ArmorHudMod.temporaryConfig = new ArmorHudConfig.MutableConfig(ArmorHudMod.getCurrentConfig());
                    ArmorHudMod.previewConfig = new ArmorHudConfig.MutableConfig(ArmorHudMod.getCurrentConfig()) {
                        @Override
                        public boolean isPreview() {
                            return true;
                        }
                    };
                })
                .setSavingRunnable(() -> {
                    ArmorHudMod.setCurrentConfig(new ArmorHudConfig(ArmorHudMod.temporaryConfig));
                    ArmorHudMod.writeCurrentConfig();
                });

        ConfigCategory positionCategory = configBuilder.getOrCreateCategory(Text.translatable("armorHud.configScreen.category.position"));
        ConfigCategory appearanceCategory = configBuilder.getOrCreateCategory(Text.translatable("armorHud.configScreen.category.appearance"));
        ConfigCategory utilityCategory = configBuilder.getOrCreateCategory(Text.translatable("armorHud.configScreen.category.utility"));
        ConfigCategory advancedCategory = configBuilder.getOrCreateCategory(Text.translatable("armorHud.configScreen.category.advanced"));
        ConfigEntryBuilder configEntryBuilder = configBuilder.entryBuilder();

        AbstractConfigListEntry<Boolean> enabledEntry;
        AbstractConfigListEntry<Side> sideEntry;
        AbstractConfigListEntry<Anchor> anchorEntry;
        AbstractConfigListEntry<Orientation> orientationEntry;
        AbstractConfigListEntry<Integer> offsetXEntry;
        AbstractConfigListEntry<Integer> offsetYEntry;
        AbstractConfigListEntry<OffhandSlotBehavior> offhandSlotBehaviorEntry;
        AbstractConfigListEntry<Style> styleEntry;
        AbstractConfigListEntry<SlotsShown> slotsShownEntry;
        AbstractConfigListEntry<Boolean> emptyIconsEntry;
        AbstractConfigListEntry<Boolean> reversedEntry;
        AbstractConfigListEntry<Boolean> pushBossbarsEntry;
        AbstractConfigListEntry<Boolean> pushStatusEffectIconsEntry;
        AbstractConfigListEntry<Boolean> pushSubtitlesEntry;
        AbstractConfigListEntry<Boolean> warningEntry;
        AbstractConfigListEntry<Integer> minDurabilityValueEntry;
        AbstractConfigListEntry<Double> minDurabilityPercentageEntry;
        AbstractConfigListEntry<Float> warningIconBobbingIntervalEntry;
        AbstractConfigListEntry<Integer> slotTexture1Entry;
        AbstractConfigListEntry<Integer> slotTexture2Entry;
        AbstractConfigListEntry<Integer> slotTexture3Entry;
        AbstractConfigListEntry<Integer> slotTexture4Entry;
        AbstractConfigListEntry<Integer> borderLengthEntry;
        AbstractConfigListEntry<Boolean> matchBorderAndSlotTexturesEntry;

        enabledEntry = configEntryBuilder
                .startBooleanToggle(Text.translatable("armorHud.configScreen.setting.enable.name"), ArmorHudMod.getCurrentConfig().isEnabled())
                .setDefaultValue(defaultConfig.isEnabled())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.enable.description"))
                .setSaveConsumer((Boolean value) -> ArmorHudMod.temporaryConfig.setEnabled(value))
                .setErrorSupplier((Boolean value) -> {
                    ArmorHudMod.previewConfig.setEnabled(value);
                    return Optional.empty();
                })
                .build();
        positionCategory.addEntry(enabledEntry);

        orientationEntry = configEntryBuilder
                .startEnumSelector(Text.translatable("armorHud.configScreen.setting.orientation.name"), Orientation.class, ArmorHudMod.getCurrentConfig().getOrientation())
                .setDefaultValue(defaultConfig.getOrientation())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.orientation.description"))
                .setSaveConsumer((Orientation value) -> ArmorHudMod.temporaryConfig.setOrientation(value))
                .setErrorSupplier((Orientation value) -> {
                    ArmorHudMod.previewConfig.setOrientation(value);
                    return Optional.empty();
                })
                .build();
        positionCategory.addEntry(orientationEntry);

        anchorEntry = configEntryBuilder
                .startEnumSelector(Text.translatable("armorHud.configScreen.setting.anchor.name"), Anchor.class, ArmorHudMod.getCurrentConfig().getAnchor())
                .setDefaultValue(defaultConfig.getAnchor())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.anchor.description"))
                .setSaveConsumer((Anchor value) -> ArmorHudMod.temporaryConfig.setAnchor(value))
                .setErrorSupplier((Anchor value) -> {
                    ArmorHudMod.previewConfig.setAnchor(value);
                    return Optional.empty();
                })
                .build();
        positionCategory.addEntry(anchorEntry);

        sideEntry = configEntryBuilder
                .startEnumSelector(Text.translatable("armorHud.configScreen.setting.side.name"), Side.class, ArmorHudMod.getCurrentConfig().getSide())
                .setDefaultValue(defaultConfig.getSide())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.side.description"))
                .setSaveConsumer((Side value) -> ArmorHudMod.temporaryConfig.setSide(value))
                .setErrorSupplier((Side value) -> {
                    ArmorHudMod.previewConfig.setSide(value);
                    return Optional.empty();
                })
                .build();
        positionCategory.addEntry(sideEntry);

        offhandSlotBehaviorEntry = configEntryBuilder
                .startEnumSelector(Text.translatable("armorHud.configScreen.setting.offhandSlot.name"), OffhandSlotBehavior.class, ArmorHudMod.getCurrentConfig().getOffhandSlotBehavior())
                .setDefaultValue(defaultConfig.getOffhandSlotBehavior())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.offhandSlot.description"))
                .setSaveConsumer((OffhandSlotBehavior value) -> ArmorHudMod.temporaryConfig.setOffhandSlotBehavior(value))
                .setErrorSupplier((OffhandSlotBehavior value) -> {
                    ArmorHudMod.previewConfig.setOffhandSlotBehavior(value);
                    return Optional.empty();
                })
                .build();
        positionCategory.addEntry(offhandSlotBehaviorEntry);

        offsetXEntry = configEntryBuilder
                .startIntField(Text.translatable("armorHud.configScreen.setting.offsetX.name"), ArmorHudMod.getCurrentConfig().getOffsetX())
                .setDefaultValue(defaultConfig.getOffsetX())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.offsetX.description"))
                .setSaveConsumer((Integer value) -> ArmorHudMod.temporaryConfig.setOffsetX(value))
                .setErrorSupplier((Integer value) -> {
                    ArmorHudMod.previewConfig.setOffsetX(value);
                    return Optional.empty();
                })
                .build();
        positionCategory.addEntry(offsetXEntry);

        offsetYEntry = configEntryBuilder
                .startIntField(Text.translatable("armorHud.configScreen.setting.offsetY.name"), ArmorHudMod.getCurrentConfig().getOffsetY())
                .setDefaultValue(defaultConfig.getOffsetY())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.offsetY.description"))
                .setSaveConsumer((Integer value) -> ArmorHudMod.temporaryConfig.setOffsetY(value))
                .setErrorSupplier((Integer value) -> {
                    ArmorHudMod.previewConfig.setOffsetY(value);
                    return Optional.empty();
                })
                .build();
        positionCategory.addEntry(offsetYEntry);

        styleEntry = configEntryBuilder
                .startEnumSelector(Text.translatable("armorHud.configScreen.setting.style.name"), Style.class, ArmorHudMod.getCurrentConfig().getStyle())
                .setDefaultValue(defaultConfig.getStyle())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.style.description"))
                .setSaveConsumer((Style value) -> ArmorHudMod.temporaryConfig.setStyle(value))
                .setErrorSupplier((Style value) -> {
                    ArmorHudMod.previewConfig.setStyle(value);
                    return Optional.empty();
                })
                .build();
        appearanceCategory.addEntry(styleEntry);

        slotsShownEntry = configEntryBuilder
                .startEnumSelector(Text.translatable("armorHud.configScreen.setting.slotsShown.name"), SlotsShown.class, ArmorHudMod.getCurrentConfig().getSlotsShown())
                .setDefaultValue(defaultConfig.getSlotsShown())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.slotsShown.description"))
                .setSaveConsumer((SlotsShown value) -> ArmorHudMod.temporaryConfig.setSlotsShown(value))
                .setErrorSupplier((SlotsShown value) -> {
                    ArmorHudMod.previewConfig.setSlotsShown(value);
                    return Optional.empty();
                })
                .build();
        appearanceCategory.addEntry(slotsShownEntry);

        emptyIconsEntry = configEntryBuilder
                .startBooleanToggle(Text.translatable("armorHud.configScreen.setting.emptyIconsShown.name"), ArmorHudMod.getCurrentConfig().getEmptyIconsShown())
                .setDefaultValue(defaultConfig.getEmptyIconsShown())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.emptyIconsShown.description"))
                .setSaveConsumer((Boolean value) -> ArmorHudMod.temporaryConfig.setEmptyIconsShown(value))
                .setErrorSupplier((Boolean value) -> {
                    ArmorHudMod.previewConfig.setEmptyIconsShown(value);
                    return Optional.empty();
                })
                .build();
        appearanceCategory.addEntry(emptyIconsEntry);

        reversedEntry = configEntryBuilder
                .startBooleanToggle(Text.translatable("armorHud.configScreen.setting.reversed.name"), ArmorHudMod.getCurrentConfig().isReversed())
                .setDefaultValue(defaultConfig.isReversed())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.reversed.description"))
                .setSaveConsumer((Boolean value) -> ArmorHudMod.temporaryConfig.setReversed(value))
                .setErrorSupplier((Boolean value) -> {
                    ArmorHudMod.previewConfig.setReversed(value);
                    return Optional.empty();
                })
                .build();
        appearanceCategory.addEntry(reversedEntry);

        pushBossbarsEntry = configEntryBuilder
                .startBooleanToggle(Text.translatable("armorHud.configScreen.setting.pushBossbars.name"), ArmorHudMod.getCurrentConfig().getPushBossbars())
                .setDefaultValue(defaultConfig.getPushBossbars())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.pushBossbars.description"))
                .setSaveConsumer((Boolean value) -> ArmorHudMod.temporaryConfig.setPushBossbars(value))
                .setErrorSupplier((Boolean value) -> {
                    ArmorHudMod.previewConfig.setPushBossbars(value);
                    return Optional.empty();
                })
                .build();
        appearanceCategory.addEntry(pushBossbarsEntry);

        pushStatusEffectIconsEntry = configEntryBuilder
                .startBooleanToggle(Text.translatable("armorHud.configScreen.setting.pushStatusEffectIcons.name"), ArmorHudMod.getCurrentConfig().getPushStatusEffectIcons())
                .setDefaultValue(defaultConfig.getPushStatusEffectIcons())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.pushStatusEffectIcons.description"))
                .setSaveConsumer((Boolean value) -> ArmorHudMod.temporaryConfig.setPushStatusEffectIcons(value))
                .setErrorSupplier((Boolean value) -> {
                    ArmorHudMod.previewConfig.setPushStatusEffectIcons(value);
                    return Optional.empty();
                })
                .build();
        appearanceCategory.addEntry(pushStatusEffectIconsEntry);

        pushSubtitlesEntry = configEntryBuilder
                .startBooleanToggle(Text.translatable("armorHud.configScreen.setting.pushSubtitles.name"), ArmorHudMod.getCurrentConfig().getPushSubtitles())
                .setDefaultValue(defaultConfig.getPushSubtitles())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.pushSubtitles.description"))
                .setSaveConsumer((Boolean value) -> ArmorHudMod.temporaryConfig.setPushSubtitles(value))
                .setErrorSupplier((Boolean value) -> {
                    ArmorHudMod.previewConfig.setPushSubtitles(value);
                    return Optional.empty();
                })
                .build();
        appearanceCategory.addEntry(pushSubtitlesEntry);

        warningEntry = configEntryBuilder
                .startBooleanToggle(Text.translatable("armorHud.configScreen.setting.warningShown.name"), ArmorHudMod.getCurrentConfig().isWarningShown())
                .setDefaultValue(defaultConfig.isWarningShown())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.warningShown.description"))
                .setSaveConsumer((Boolean value) -> ArmorHudMod.temporaryConfig.setWarningShown(value))
                .setErrorSupplier((Boolean value) -> {
                    ArmorHudMod.previewConfig.setWarningShown(value);
                    return Optional.empty();
                })
                .build();
        utilityCategory.addEntry(warningEntry);

        minDurabilityValueEntry = configEntryBuilder
                .startIntField(Text.translatable("armorHud.configScreen.setting.minDurabilityValue.name"), ArmorHudMod.getCurrentConfig().getMinDurabilityValue())
                .setDefaultValue(defaultConfig.getMinDurabilityValue())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.minDurabilityValue.description"))
                .setSaveConsumer((Integer value) -> ArmorHudMod.temporaryConfig.setMinDurabilityValue(value))
                .setErrorSupplier((Integer value) -> {
                    ArmorHudMod.previewConfig.setMinDurabilityValue(value);
                    return Optional.empty();
                })
                .setMin(0)
                .build();
        utilityCategory.addEntry(minDurabilityValueEntry);

        minDurabilityPercentageEntry = configEntryBuilder
                .startDoubleField(Text.translatable("armorHud.configScreen.setting.minDurabilityPercentage.name"), ArmorHudMod.getCurrentConfig().getMinDurabilityPercentage() * 100.0D)
                .setDefaultValue(defaultConfig.getMinDurabilityPercentage() * 100.0D)
                .setTooltip(Text.translatable("armorHud.configScreen.setting.minDurabilityPercentage.description"))
                .setSaveConsumer((Double value) -> ArmorHudMod.temporaryConfig.setMinDurabilityPercentage(value / 100.0D))
                .setErrorSupplier((Double value) -> {
                    ArmorHudMod.previewConfig.setMinDurabilityPercentage(value / 100.0D);
                    return Optional.empty();
                })
                .setMin(0.0D)
                .setMax(100.0D)
                .build();
        utilityCategory.addEntry(minDurabilityPercentageEntry);

        final float minWarningIconBobbingInterval = 0.2F;
        warningIconBobbingIntervalEntry = configEntryBuilder
                .startFloatField(Text.translatable("armorHud.configScreen.setting.warningIconBobbingIntervalEntry.name"), ArmorHudMod.getCurrentConfig().getWarningIconBobbingIntervalMs() / 1000.0F)
                .setDefaultValue(defaultConfig.getWarningIconBobbingIntervalMs() / 1000.0F)
                .setTooltip(Text.translatable("armorHud.configScreen.setting.warningIconBobbingIntervalEntry.description"))
                .setSaveConsumer((Float value) -> ArmorHudMod.temporaryConfig.setWarningIconBobbingIntervalMs(value * 1000.0F))
                .setErrorSupplier((Float value) -> {
                    if (value != 0.0F && value < minWarningIconBobbingInterval)//
                        return Optional.of(Text.translatable("text.cloth-config.error.too_small", minWarningIconBobbingInterval));
                    ArmorHudMod.previewConfig.setWarningIconBobbingIntervalMs(value * 1000.0F);
                    return Optional.empty();
                })
                //.setMin(minWarningIconBobbingInterval)
                .setMax(5.0F)
                .build();
        utilityCategory.addEntry(warningIconBobbingIntervalEntry);

        slotTexture1Entry = configEntryBuilder
                .startIntSlider(Text.translatable("armorHud.configScreen.setting.slotTexture1.name"), ArmorHudMod.getCurrentConfig().getSlotTextures()[0], 1, 9)
                .setDefaultValue(defaultConfig.getSlotTextures()[0])
                .setTooltip(Text.translatable("armorHud.configScreen.setting.slotTexture1.description"))
                .setSaveConsumer((Integer value) -> ArmorHudMod.temporaryConfig.setSlotTexture1(value))
                .setErrorSupplier((Integer value) -> {
                    ArmorHudMod.previewConfig.setSlotTexture1(value);
                    return Optional.empty();
                })
                .build();
        advancedCategory.addEntry(slotTexture1Entry);

        slotTexture2Entry = configEntryBuilder
                .startIntSlider(Text.translatable("armorHud.configScreen.setting.slotTexture2.name"), ArmorHudMod.getCurrentConfig().getSlotTextures()[1], 1, 9)
                .setDefaultValue(defaultConfig.getSlotTextures()[1])
                .setTooltip(Text.translatable("armorHud.configScreen.setting.slotTexture2.description"))
                .setSaveConsumer((Integer value) -> ArmorHudMod.temporaryConfig.setSlotTexture2(value))
                .setErrorSupplier((Integer value) -> {
                    ArmorHudMod.previewConfig.setSlotTexture2(value);
                    return Optional.empty();
                })
                .build();
        advancedCategory.addEntry(slotTexture2Entry);

        slotTexture3Entry = configEntryBuilder
                .startIntSlider(Text.translatable("armorHud.configScreen.setting.slotTexture3.name"), ArmorHudMod.getCurrentConfig().getSlotTextures()[2], 1, 9)
                .setDefaultValue(defaultConfig.getSlotTextures()[2])
                .setTooltip(Text.translatable("armorHud.configScreen.setting.slotTexture3.description"))
                .setSaveConsumer((Integer value) -> ArmorHudMod.temporaryConfig.setSlotTexture3(value))
                .setErrorSupplier((Integer value) -> {
                    ArmorHudMod.previewConfig.setSlotTexture3(value);
                    return Optional.empty();
                })
                .build();
        advancedCategory.addEntry(slotTexture3Entry);

        slotTexture4Entry = configEntryBuilder
                .startIntSlider(Text.translatable("armorHud.configScreen.setting.slotTexture4.name"), ArmorHudMod.getCurrentConfig().getSlotTextures()[3], 1, 9)
                .setDefaultValue(defaultConfig.getSlotTextures()[3])
                .setTooltip(Text.translatable("armorHud.configScreen.setting.slotTexture4.description"))
                .setSaveConsumer((Integer value) -> ArmorHudMod.temporaryConfig.setSlotTexture4(value))
                .setErrorSupplier((Integer value) -> {
                    ArmorHudMod.previewConfig.setSlotTexture4(value);
                    return Optional.empty();
                })
                .build();
        advancedCategory.addEntry(slotTexture4Entry);

        borderLengthEntry = configEntryBuilder
                .startIntSlider(Text.translatable("armorHud.configScreen.setting.borderLength.name"), ArmorHudMod.getCurrentConfig().getBorderLength(), 0, 10)
                .setDefaultValue(defaultConfig.getBorderLength())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.borderLength.description"))
                .setSaveConsumer((Integer value) -> ArmorHudMod.temporaryConfig.setBorderLength(value))
                .setErrorSupplier((Integer value) -> {
                    ArmorHudMod.previewConfig.setBorderLength(value);
                    return Optional.empty();
                })
                .build();
        advancedCategory.addEntry(borderLengthEntry);

        matchBorderAndSlotTexturesEntry = configEntryBuilder
                .startBooleanToggle(Text.translatable("armorHud.configScreen.setting.matchBorderAndSlotTextures.name"), ArmorHudMod.getCurrentConfig().isMatchBorderAndSlotTextures())
                .setDefaultValue(defaultConfig.isMatchBorderAndSlotTextures())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.matchBorderAndSlotTextures.description"))
                .setSaveConsumer((Boolean value) -> ArmorHudMod.temporaryConfig.setMatchBorderAndSlotTextures(value))
                .setErrorSupplier((Boolean value) -> {
                    ArmorHudMod.previewConfig.setMatchBorderAndSlotTextures(value);
                    return Optional.empty();
                })
                .build();
        advancedCategory.addEntry(matchBorderAndSlotTexturesEntry);

        return configBuilder.build();
    }
}
