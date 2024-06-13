package ru.berdinskiybear.armorhud.config;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig.*;

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
                    ArmorHudMod.temporaryConfig = new ArmorHudConfig.MutableConfig(ArmorHudMod.getConfig());
                    ArmorHudMod.previewConfig = new ArmorHudConfig.MutableConfig(ArmorHudMod.getConfig()) {
                        @Override
                        public boolean isPreview() {
                            return true;
                        }
                    };
                })
                .setSavingRunnable(() -> {
                    ArmorHudMod.setConfig(new ArmorHudConfig(ArmorHudMod.temporaryConfig));
                    ArmorHudMod.writeConfig();
                });

        ConfigCategory positionCategory = configBuilder.getOrCreateCategory(Text.translatable("armorHud.configScreen.category.position"));
        ConfigCategory appearanceCategory = configBuilder.getOrCreateCategory(Text.translatable("armorHud.configScreen.category.appearance"));
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
        AbstractConfigListEntry<Integer> slotTexture1Entry;
        AbstractConfigListEntry<Integer> slotTexture2Entry;
        AbstractConfigListEntry<Integer> slotTexture3Entry;
        AbstractConfigListEntry<Integer> slotTexture4Entry;
        AbstractConfigListEntry<Integer> borderLengthEntry;
        AbstractConfigListEntry<Boolean> matchBorderAndSlotTexturesEntry;
        AbstractConfigListEntry<Integer> minOffsetBeforePushingSubtitlesEntry;

        enabledEntry = configEntryBuilder
                .startBooleanToggle(Text.translatable("armorHud.configScreen.setting.enable.name"), ArmorHudMod.getConfig().isEnabled())
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
                .startEnumSelector(Text.translatable("armorHud.configScreen.setting.orientation.name"), Orientation.class, ArmorHudMod.getConfig().getOrientation())
                .setDefaultValue(defaultConfig.getOrientation())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.orientation.description"))
                .setSaveConsumer((Orientation value) -> ArmorHudMod.temporaryConfig.setOrientation(value))
                .setErrorSupplier((Orientation value) -> {
                    ArmorHudMod.previewConfig.setOrientation(value);
                    return Optional.empty();
                })
                .build();
        positionCategory.addEntry(orientationEntry);

        sideEntry = configEntryBuilder
                .startEnumSelector(Text.translatable("armorHud.configScreen.setting.side.name"), Side.class, ArmorHudMod.getConfig().getSide())
                .setDefaultValue(defaultConfig.getSide())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.side.description"))
                .setSaveConsumer((Side value) -> ArmorHudMod.temporaryConfig.setSide(value))
                .setErrorSupplier((Side value) -> {
                    ArmorHudMod.previewConfig.setSide(value);
                    return Optional.empty();
                })
                .build();
        positionCategory.addEntry(sideEntry);

        anchorEntry = configEntryBuilder
                .startEnumSelector(Text.translatable("armorHud.configScreen.setting.anchor.name"), Anchor.class, ArmorHudMod.getConfig().getAnchor())
                .setDefaultValue(defaultConfig.getAnchor())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.anchor.description"))
                .setSaveConsumer((Anchor value) -> ArmorHudMod.temporaryConfig.setAnchor(value))
                .setErrorSupplier((Anchor value) -> {
                    ArmorHudMod.previewConfig.setAnchor(value);
                    return Optional.empty();
                })
                .build();
        positionCategory.addEntry(anchorEntry);

        offhandSlotBehaviorEntry = configEntryBuilder
                .startEnumSelector(Text.translatable("armorHud.configScreen.setting.offhandSlot.name"), OffhandSlotBehavior.class, ArmorHudMod.getConfig().getOffhandSlotBehavior())
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
                .startIntField(Text.translatable("armorHud.configScreen.setting.offsetX.name"), ArmorHudMod.getConfig().getOffsetX())
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
                .startIntField(Text.translatable("armorHud.configScreen.setting.offsetY.name"), ArmorHudMod.getConfig().getOffsetY())
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
                .startEnumSelector(Text.translatable("armorHud.configScreen.setting.style.name"), Style.class, ArmorHudMod.getConfig().getStyle())
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
                .startEnumSelector(Text.translatable("armorHud.configScreen.setting.slotsShown.name"), SlotsShown.class, ArmorHudMod.getConfig().getSlotsShown())
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
                .startBooleanToggle(Text.translatable("armorHud.configScreen.setting.emptyIconsShown.name"), ArmorHudMod.getConfig().isEmptyIconsShown())
                .setDefaultValue(defaultConfig.isEmptyIconsShown())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.emptyIconsShown.description"))
                .setSaveConsumer((Boolean value) -> ArmorHudMod.temporaryConfig.setEmptyIconsShown(value))
                .setErrorSupplier((Boolean value) -> {
                    ArmorHudMod.previewConfig.setEmptyIconsShown(value);
                    return Optional.empty();
                })
                .build();
        appearanceCategory.addEntry(emptyIconsEntry);

        reversedEntry = configEntryBuilder
                .startBooleanToggle(Text.translatable("armorHud.configScreen.setting.reversed.name"), ArmorHudMod.getConfig().isReversed())
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
                .startBooleanToggle(Text.translatable("armorHud.configScreen.setting.pushBossbars.name"), ArmorHudMod.getConfig().isPushBossbars())
                .setDefaultValue(defaultConfig.isPushBossbars())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.pushBossbars.description"))
                .setSaveConsumer((Boolean value) -> ArmorHudMod.temporaryConfig.setPushBossbars(value))
                .setErrorSupplier((Boolean value) -> {
                    ArmorHudMod.previewConfig.setPushBossbars(value);
                    return Optional.empty();
                })
                .build();
        appearanceCategory.addEntry(pushBossbarsEntry);

        pushStatusEffectIconsEntry = configEntryBuilder
                .startBooleanToggle(Text.translatable("armorHud.configScreen.setting.pushStatusEffectIcons.name"), ArmorHudMod.getConfig().isPushStatusEffectIcons())
                .setDefaultValue(defaultConfig.isPushStatusEffectIcons())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.pushStatusEffectIcons.description"))
                .setSaveConsumer((Boolean value) -> ArmorHudMod.temporaryConfig.setPushStatusEffectIcons(value))
                .setErrorSupplier((Boolean value) -> {
                    ArmorHudMod.previewConfig.setPushStatusEffectIcons(value);
                    return Optional.empty();
                })
                .build();
        appearanceCategory.addEntry(pushStatusEffectIconsEntry);

        pushSubtitlesEntry = configEntryBuilder
                .startBooleanToggle(Text.translatable("armorHud.configScreen.setting.pushSubtitles.name"), ArmorHudMod.getConfig().isPushSubtitles())
                .setDefaultValue(defaultConfig.isPushSubtitles())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.pushSubtitles.description"))
                .setSaveConsumer((Boolean value) -> ArmorHudMod.temporaryConfig.setPushSubtitles(value))
                .setErrorSupplier((Boolean value) -> {
                    ArmorHudMod.previewConfig.setPushSubtitles(value);
                    return Optional.empty();
                })
                .build();
        appearanceCategory.addEntry(pushSubtitlesEntry);

        slotTexture1Entry = configEntryBuilder
                .startIntSlider(Text.translatable("armorHud.configScreen.setting.slotTexture1.name"), ArmorHudMod.getConfig().getSlotTextures()[0], 1, 9)
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
                .startIntSlider(Text.translatable("armorHud.configScreen.setting.slotTexture2.name"), ArmorHudMod.getConfig().getSlotTextures()[1], 1, 9)
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
                .startIntSlider(Text.translatable("armorHud.configScreen.setting.slotTexture3.name"), ArmorHudMod.getConfig().getSlotTextures()[2], 1, 9)
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
                .startIntSlider(Text.translatable("armorHud.configScreen.setting.slotTexture4.name"), ArmorHudMod.getConfig().getSlotTextures()[3], 1, 9)
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
                .startIntSlider(Text.translatable("armorHud.configScreen.setting.borderLength.name"), ArmorHudMod.getConfig().getBorderLength(), 0, 10)
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
                .startBooleanToggle(Text.translatable("armorHud.configScreen.setting.matchBorderAndSlotTextures.name"), ArmorHudMod.getConfig().isMatchBorderAndSlotTextures())
                .setDefaultValue(defaultConfig.isMatchBorderAndSlotTextures())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.matchBorderAndSlotTextures.description"))
                .setSaveConsumer((Boolean value) -> ArmorHudMod.temporaryConfig.setMatchBorderAndSlotTextures(value))
                .setErrorSupplier((Boolean value) -> {
                    ArmorHudMod.previewConfig.setMatchBorderAndSlotTextures(value);
                    return Optional.empty();
                })
                .build();
        advancedCategory.addEntry(matchBorderAndSlotTexturesEntry);

        minOffsetBeforePushingSubtitlesEntry = configEntryBuilder
                .startIntField(Text.translatable("armorHud.configScreen.setting.minOffsetBeforePushingSubtitles.name"), ArmorHudMod.getConfig().getMinOffsetBeforePushingSubtitles())
                .setDefaultValue(defaultConfig.getMinOffsetBeforePushingSubtitles())
                .setTooltip(Text.translatable("armorHud.configScreen.setting.minOffsetBeforePushingSubtitles.description"))
                .setSaveConsumer((Integer value) -> ArmorHudMod.temporaryConfig.setMinOffsetBeforePushingSubtitles(value))
                .setErrorSupplier((Integer value) -> {
                    ArmorHudMod.previewConfig.setMinOffsetBeforePushingSubtitles(value);
                    return Optional.empty();
                })
                .build();
        advancedCategory.addEntry(minOffsetBeforePushingSubtitlesEntry);

        return configBuilder.build();
    }
}
