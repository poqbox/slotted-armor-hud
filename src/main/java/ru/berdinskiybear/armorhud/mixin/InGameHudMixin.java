package ru.berdinskiybear.armorhud.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private Random random;

    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;

    private static final Identifier armorHud_WIDGETS_TEXTURE = new Identifier("textures/gui/widgets.png");
    private static final Identifier armorHud_EMPTY_HELMET_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_helmet");
    private static final Identifier armorHud_EMPTY_CHESTPLATE_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_chestplate");
    private static final Identifier armorHud_EMPTY_LEGGINGS_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_leggings");
    private static final Identifier armorHud_EMPTY_BOOTS_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_boots");
    private static final Identifier armorHud_BLOCK_ATLAS_TEXTURE = new Identifier("textures/atlas/blocks.png");

    private static final int armorHud_step = 20;
    private static final int armorHud_width = 22;
    private static final int armorHud_height = 22;
    private static final int armorHud_defaultHotbarOffset = 98;
    private static final int armorHud_defaultOffhandSlotOffset = 29;
    private static final int armorHud_defaultHotbarAttackIndicatorOffset = 23;
    private static final int armorHud_minWarningHeight = 2;
    private static final int armorHud_maxWarningHeight = 7;
    private static final int armorHud_warningHorizontalOffset = 7;
    private static final int armorSlot_length = 20;

    private long armorHud_lastMeasuredTime;
    private long armorHud_measuredTime;
    private float[] armorHud_cycleProgress = null;
    private final List<ItemStack> armorHud_armorItems = new ArrayList<>(4);
    private final List<Integer> armorHud_armorItemIndexes = new ArrayList<>(4);
    private int armorHud_shift = 0;

    @Shadow
    protected abstract void renderHotbarItem(DrawContext context, int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed);

    @Shadow
    protected abstract PlayerEntity getCameraPlayer();

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbar(FLnet/minecraft/client/gui/DrawContext;)V", shift = At.Shift.AFTER))
    public void armorHud_renderArmorHud(DrawContext context, float tickDelta, CallbackInfo ci) {
        // add this to profiler
        this.client.getProfiler().push("armorHud");

        // getting current config
        ArmorHudConfig currentArmorHudConfig = this.armorHud_getCurrentArmorHudConfig();

        // updating measuring time fields
        {
            armorHud_lastMeasuredTime = armorHud_measuredTime;
            armorHud_measuredTime = Util.getMeasuringTimeMs();
        }

        // if current config tells that this mod should be enabled then the action starts
        if (currentArmorHudConfig.isEnabled()) {
            PlayerEntity playerEntity = this.getCameraPlayer();
            if (playerEntity != null) {
                int amount = 0;

                // here we count the items, save ones that we need to draw and their indexes
                {
                    List<ItemStack> armor = playerEntity.getInventory().armor;
                    armorHud_armorItems.clear();
                    armorHud_armorItemIndexes.clear();
                    for (int i = 0; i < armor.size(); i++) {
                        ItemStack itemStack = armor.get(i);
                        if (!itemStack.isEmpty())
                            amount++;
                        if (!itemStack.isEmpty() || currentArmorHudConfig.getSlotsShown() != ArmorHudConfig.SlotsShown.Equipped) {
                            armorHud_armorItems.add(itemStack);
                            armorHud_armorItemIndexes.add(i);
                        }
                    }
                }

                // if amount of armor items is not 0 or if we allow showing empty widget then we prepare and draw
                if (amount > 0 || currentArmorHudConfig.getSlotsShown() == ArmorHudConfig.SlotsShown.Always) {
                    final int armorWidgetY;
                    final int armorWidgetX;
                    final int sideMultiplier;
                    final int sideOffsetMultiplier;
                    final int verticalMultiplier;
                    final int verticalOffsetMultiplier;
                    final int widgetWidth;
                    final int widgetHeight;
                    final int slotNum;

                    //
                    context.getMatrices().push();
                    context.getMatrices().translate(0, 0, 200);

                    // here I calculate position of the widget, its width and all sorts of multipliers based of current config
                    {
                        if ((currentArmorHudConfig.getAnchor() == ArmorHudConfig.Anchor.Hotbar && currentArmorHudConfig.getSide() == ArmorHudConfig.Side.Left) || (currentArmorHudConfig.getAnchor() != ArmorHudConfig.Anchor.Hotbar && currentArmorHudConfig.getSide() == ArmorHudConfig.Side.Right)) {
                            sideMultiplier = -1;
                            sideOffsetMultiplier = -1;
                        } else {
                            sideMultiplier = 1;
                            sideOffsetMultiplier = 0;
                        }

                        switch (currentArmorHudConfig.getAnchor()) {
                            case Top, Top_Center -> {
                                verticalMultiplier = 1;
                                verticalOffsetMultiplier = 0;
                            }
                            case Hotbar, Bottom -> {
                                verticalMultiplier = -1;
                                verticalOffsetMultiplier = -1;
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + currentArmorHudConfig.getAnchor());
                        }

                        int addedHotbarOffset;
                        switch (currentArmorHudConfig.getOffhandSlotBehavior()) {
                            case Ignore -> {
                                addedHotbarOffset = 0;
                            }
                            case Leave_Space -> {
                                addedHotbarOffset = Math.max(armorHud_defaultOffhandSlotOffset, armorHud_defaultHotbarAttackIndicatorOffset);
                            }
                            case Adhere -> {
                                if ((playerEntity.getMainArm().getOpposite() == Arm.LEFT && currentArmorHudConfig.getSide() == ArmorHudConfig.Side.Left
                                        || playerEntity.getMainArm().getOpposite() == Arm.RIGHT && currentArmorHudConfig.getSide() == ArmorHudConfig.Side.Right
                                    ) && !playerEntity.getOffHandStack().isEmpty()
                                )
                                    addedHotbarOffset = armorHud_defaultOffhandSlotOffset;
                                else if ((playerEntity.getMainArm() == Arm.LEFT && currentArmorHudConfig.getSide() == ArmorHudConfig.Side.Left
                                        || playerEntity.getMainArm() == Arm.RIGHT && currentArmorHudConfig.getSide() == ArmorHudConfig.Side.Right
                                    ) && this.client.options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR
                                )
                                    addedHotbarOffset = armorHud_defaultHotbarAttackIndicatorOffset;
                                else
                                    addedHotbarOffset = 0;
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + currentArmorHudConfig.getOffhandSlotBehavior());
                        }

                        slotNum = currentArmorHudConfig.getSlotsShown() == ArmorHudConfig.SlotsShown.Equipped ? amount : 4;

                        int armorWidgetX1 = 0;
                        int armorWidgetY1 = 0;
                        widgetWidth = armorHud_width + ((slotNum - 1) * armorHud_step);
                        widgetHeight = armorHud_height + ((slotNum - 1) * armorHud_step);
                        switch (currentArmorHudConfig.getOrientation()) {
                            case Horizontal -> {
                                armorWidgetX1 = switch (currentArmorHudConfig.getAnchor()) {
                                    case Top_Center -> this.scaledWidth / 2 - (widgetWidth / 2);
                                    case Top, Bottom -> (widgetWidth - this.scaledWidth) * sideOffsetMultiplier;
                                    case Hotbar -> this.scaledWidth / 2 + ((armorHud_defaultHotbarOffset + addedHotbarOffset) * sideMultiplier) + (widgetWidth * sideOffsetMultiplier);
                                    default -> throw new IllegalStateException("Unexpected value: " + currentArmorHudConfig.getAnchor());
                                };
                                armorWidgetY1 = switch (currentArmorHudConfig.getAnchor()) {
                                    case Bottom, Hotbar -> this.scaledHeight - armorHud_height;
                                    case Top, Top_Center -> 0;
                                    default -> throw new IllegalStateException("Unexpected value: " + currentArmorHudConfig.getAnchor());
                                };
                            }
                            case Vertical -> {
                                armorWidgetX1 = switch (currentArmorHudConfig.getAnchor()) {
                                    case Top_Center -> this.scaledWidth / 2 - (armorHud_width / 2);
                                    case Top, Bottom -> (armorHud_width - this.scaledWidth) * sideOffsetMultiplier;
                                    case Hotbar -> this.scaledWidth / 2 + ((armorHud_defaultHotbarOffset + addedHotbarOffset) * sideMultiplier) + (armorHud_width * sideOffsetMultiplier);
                                    default -> throw new IllegalStateException("Unexpected value: " + currentArmorHudConfig.getAnchor());
                                };
                                armorWidgetY1 = switch (currentArmorHudConfig.getAnchor()) {
                                    case Bottom, Hotbar -> this.scaledHeight - widgetHeight;
                                    case Top, Top_Center -> 0;
                                    default -> throw new IllegalStateException("Unexpected value: " + currentArmorHudConfig.getAnchor());
                                };
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + currentArmorHudConfig.getOrientation());
                        }

                        armorWidgetX1 += currentArmorHudConfig.getOffsetX() * sideMultiplier;
                        armorWidgetY1 += currentArmorHudConfig.getOffsetY() * verticalMultiplier;

                        armorWidgetX = armorWidgetX1;
                        armorWidgetY = armorWidgetY1;
                    }

                    //RenderSystem.setShader(GameRenderer::getPositionTexProgram); // maybe that is redundant
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.setShaderTexture(0, armorHud_WIDGETS_TEXTURE);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();

                    // here I draw the slots
                    {
                        int[] slotData = new int[slotNum];
                        for (int i = 0; i < slotNum; i++)
                            slotData[i] = currentArmorHudConfig.getSlotTextures()[i];

                        context.getMatrices().push();
                        context.getMatrices().translate(0, 0, -91);
//                        switch (currentArmorHudConfig.getStyle()) {
//                            case Squared_Corners -> this.drawSlots1(context, armorWidgetX, armorWidgetY, widgetWidth, 3, currentArmorHudConfig.getOrientation(), slotNum);
//                            case Rounded_Corners -> this.drawSlots2(context, armorWidgetX, armorWidgetY, widgetWidth, 3, currentArmorHudConfig.getOrientation(), slotNum);
//                            case Rounded_Slots -> this.drawSlots3(context, armorWidgetX, armorWidgetY, widgetWidth, 3, currentArmorHudConfig.getOrientation(), slotNum);
//                        }

                        this.drawCustomSlots(context, armorWidgetX, armorWidgetY, widgetWidth, currentArmorHudConfig.getBorderLength(), currentArmorHudConfig.getOrientation(), slotData, currentArmorHudConfig.isMatchBorderAndSlotTextures(), currentArmorHudConfig.getCornerStyle());
                        context.getMatrices().pop();
                    }

                    // here I draw warning icons if necessary
                    if (currentArmorHudConfig.isWarningShown()) {
                        context.getMatrices().push();
                        context.getMatrices().translate(0, 0, 90);
                        for (int i = 0; i < armorHud_armorItems.size(); i++) {
                            int iReversed = currentArmorHudConfig.isReversed() ? i : (armorHud_armorItems.size() - i - 1);
                            if (!armorHud_armorItems.get(i).isEmpty() && armorHud_armorItems.get(i).isDamageable()) {
                                int damage = armorHud_armorItems.get(i).getDamage();
                                int maxDamage = armorHud_armorItems.get(i).getMaxDamage();
                                if ((1.0F - ((float) damage) / ((float) maxDamage) <= currentArmorHudConfig.getMinDurabilityPercentage()) || (maxDamage - damage <= currentArmorHudConfig.getMinDurabilityValue())) {
                                    switch (currentArmorHudConfig.getOrientation()) {
                                        case Horizontal -> {
                                            context.drawTexture(armorHud_WIDGETS_TEXTURE,
                                                    armorWidgetX
                                                            + (armorHud_step * iReversed)
                                                            + armorHud_warningHorizontalOffset,
                                                    armorWidgetY
                                                            + (armorHud_height * (verticalOffsetMultiplier + 1))
                                                            + (8 * verticalOffsetMultiplier)
                                                            + ((armorHud_minWarningHeight + Math.round(Math.abs(this.armorHud_getCycleProgress(armorHud_armorItemIndexes.get(i), currentArmorHudConfig) * 2.0F - 1.0F) * armorHud_maxWarningHeight)) * verticalMultiplier),
                                                    238, 22, 8, 8);
                                        }
                                        case Vertical -> {
                                            context.drawTexture(armorHud_WIDGETS_TEXTURE,
                                                    armorWidgetX
                                                            + (armorHud_width * (sideOffsetMultiplier + 1))
                                                            + (8 * sideOffsetMultiplier)
                                                            + (4 * sideMultiplier),
                                                    armorWidgetY
                                                            + (armorHud_step * iReversed)
                                                            + armorHud_warningHorizontalOffset
                                                            + ((armorHud_minWarningHeight + Math.round(Math.abs(this.armorHud_getCycleProgress(armorHud_armorItemIndexes.get(i), currentArmorHudConfig) * 2.0F - 1.0F) * armorHud_maxWarningHeight) - 8) * verticalMultiplier),
                                                    238, 22, 8, 8);
                                        }
                                    }
                                }
                            }
                        }
                        context.getMatrices().pop();
                    }

                    // here I blend in slot icons if so tells the current config
                    if (currentArmorHudConfig.getEmptyIconsShown()) {
                        if (currentArmorHudConfig.getSlotsShown() != ArmorHudConfig.SlotsShown.Equipped && (amount > 0 || currentArmorHudConfig.getSlotsShown() == ArmorHudConfig.SlotsShown.Always)) {
                            context.getMatrices().push();
                            context.getMatrices().translate(0, 0, -90);
                            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_COLOR, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
                            for (int i = 0; i < armorHud_armorItems.size(); i++) {
                                if (armorHud_armorItems.get(i).isEmpty()) {
                                    Identifier spriteId = switch (i) {
                                        case 0 -> armorHud_EMPTY_BOOTS_SLOT_TEXTURE;
                                        case 1 -> armorHud_EMPTY_LEGGINGS_SLOT_TEXTURE;
                                        case 2 -> armorHud_EMPTY_CHESTPLATE_SLOT_TEXTURE;
                                        case 3 -> armorHud_EMPTY_HELMET_SLOT_TEXTURE;
                                        default -> throw new IllegalStateException("Unexpected value: " + i);
                                    };
                                    Sprite sprite = this.client.getSpriteAtlas(armorHud_BLOCK_ATLAS_TEXTURE).apply(spriteId);
                                    RenderSystem.setShaderTexture(0, sprite.getAtlasId());

                                    int iReversed = currentArmorHudConfig.isReversed() ? i : (armorHud_armorItems.size() - i - 1);
                                    switch (currentArmorHudConfig.getOrientation()) {
                                        case Horizontal -> context.drawSprite(armorWidgetX + (armorHud_step * iReversed) + 3, armorWidgetY + 3, 0, 16, 16, sprite);
                                        case Vertical -> context.drawSprite(armorWidgetX + 3, armorWidgetY + (armorHud_step * iReversed) + 3, 0, 16, 16, sprite);
                                    }
                                }
                            }
                            RenderSystem.defaultBlendFunc();
                            context.getMatrices().pop();
                        }
                    }

                    // and at last I draw the armour items
                    for (int i = 0; i < armorHud_armorItems.size(); i++) {
                        int iReversed = currentArmorHudConfig.isReversed() ? i : (armorHud_armorItems.size() - i - 1);
                        switch (currentArmorHudConfig.getOrientation()) {
                            case Horizontal -> this.renderHotbarItem(context, armorWidgetX + (armorHud_step * iReversed) + 3, armorWidgetY + 3, tickDelta, playerEntity, armorHud_armorItems.get(i), i + 1);
                            case Vertical -> this.renderHotbarItem(context, armorWidgetX + 3, armorWidgetY + (armorHud_step * iReversed) + 3, tickDelta, playerEntity, armorHud_armorItems.get(i), i + 1);
                        }
                    }

                    // remove my translations
                    context.getMatrices().pop();
                }
            }
        }

        // pop this out of profiler
        this.client.getProfiler().pop();
    }

    private void drawSlots1(DrawContext context, int armorWidgetX, int armorWidgetY, int widgetWidth, int endPieceLength, ArmorHudConfig.Orientation orientation, int slots) {
        if (orientation == ArmorHudConfig.Orientation.Vertical) {
            context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX, armorWidgetY, 0, 0, armorHud_width, 1);
            for (int i = 0; i < slots; i++) {
                context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX, armorWidgetY + 1 + (armorHud_height - 2) * i, 0, 1, armorHud_width - 1, armorHud_height - 2);
                context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + armorHud_width - 1, armorWidgetY + 1 + (armorHud_height - 2) * i, 181, 0, 1, armorHud_height - 2);
            }
            context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX, armorWidgetY + 1 + (armorHud_height - 2) * slots, 0, armorHud_height - 1, armorHud_width, 1);
        }
        else {
            context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX, armorWidgetY, 0, 0, widgetWidth - endPieceLength, armorHud_height);
            context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + widgetWidth - endPieceLength, armorWidgetY, 182 - endPieceLength, 0, endPieceLength, armorHud_height);
        }
    }

    private void drawSlots2(DrawContext context, int armorWidgetX, int armorWidgetY, int widgetWidth, int endPieceLength, ArmorHudConfig.Orientation orientation, int slots) {
        if (orientation == ArmorHudConfig.Orientation.Vertical) {
            for (int i = 0; i < slots; i++) {
                context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + 1, armorWidgetY + 1 + (armorHud_height - 2) * i, 1, 1, armorHud_width - 2, armorHud_height - 2);
            }
            if (slots > 0) {
                if (slots == 1) {
                    context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX, armorWidgetY + 2, 0, 0, 1, armorHud_height - 4);
                    context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + armorHud_width - 1, armorWidgetY + 2, 181, 0, 1, armorHud_height - 4);
                }
                else {
                    for (int i = 1; i < slots - 1; i++) {
                        context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX, armorWidgetY + (armorHud_height - 2) * i, 0, 0, 1, armorHud_height - 1);
                        context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + armorHud_width - 1, armorWidgetY + (armorHud_height - 2) * i, 181, 0, 1, armorHud_height - 1);

                    }
                    context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX, armorWidgetY + 2, 0, 0, 1, armorHud_height - 3);
                    context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + armorHud_width - 1, armorWidgetY + 2, 181, 0, 1, armorHud_height - 3);
                    context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX, armorWidgetY + 1 + (armorHud_height - 2) * (slots - 1), 0, 0, 1, armorHud_height - 3);
                    context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + armorHud_width - 1, armorWidgetY + 1 + (armorHud_height - 2) * (slots - 1), 181, 0, 1, armorHud_height - 3);
                }
            }
            context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX, armorWidgetY, 24, 23, armorHud_width, 2);
            context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX, armorWidgetY + widgetWidth - 2, 24, 23 + armorHud_height - 2, armorHud_width, 2);
        }
        else {
            context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX, armorWidgetY, 24, 23, endPieceLength, armorHud_height);
            if (widgetWidth > endPieceLength * 2)
                context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + endPieceLength, armorWidgetY, endPieceLength, 0, widgetWidth - 2 * endPieceLength, armorHud_height);
            if (widgetWidth - endPieceLength < endPieceLength)
                endPieceLength = widgetWidth - endPieceLength;
            context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + widgetWidth - endPieceLength, armorWidgetY, 24 + armorHud_width - endPieceLength, 23, endPieceLength, armorHud_height);
        }
    }

    private void drawSlots3(DrawContext context, int armorWidgetX, int armorWidgetY, int widgetWidth, int endPieceLength, ArmorHudConfig.Orientation orientation, int slots) {
        if (orientation == ArmorHudConfig.Orientation.Vertical) {
            for (int i = 0; i < slots; i++) {
                context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + 1, armorWidgetY + 1 + (armorHud_height - 2) * i, 1, 1, armorHud_width - 2, armorHud_height - 2);
                context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX, armorWidgetY + (armorHud_height - 2) * i, 24, 23, armorHud_width - armorHud_step, armorHud_height);
                context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + armorHud_width - 1, armorWidgetY + (armorHud_height - 2) * i + 2, 181, 0, 1, armorHud_height - 4);
                context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + armorHud_width - 2, armorWidgetY + (armorHud_height - 2) * i + 1, 181, 0, 1, 1);
                context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + armorHud_width - 2, armorWidgetY + (armorHud_height - 2) * i + 20, 181, 0, 1, 1);
            }
            context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX, armorWidgetY, 24, 23, armorHud_width, 2);
            context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX, armorWidgetY + (armorHud_height - 2) * slots, 24, 23 + armorHud_height - 2, armorHud_width, 2);
        }
        else {
            context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX, armorWidgetY, 24, 23, (armorHud_width - armorHud_step) / 2, armorHud_height);
            for (int i = 0; i < slots; i++)
                context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + (armorHud_width - armorHud_step) / 2 + i * armorHud_step, armorWidgetY, 24 + (armorHud_width - armorHud_step) / 2, 23, armorHud_step, armorHud_height);
            context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + widgetWidth - (armorHud_width - armorHud_step) / 2, armorWidgetY, 24, 23, (armorHud_width - armorHud_step) / 2, armorHud_height);
        }
    }

    private void drawCustomSlots(DrawContext context, int armorWidgetX, int armorWidgetY, int widgetWidth, int borderLength, ArmorHudConfig.Orientation orientation, int[] slots, boolean matchBorderAndSlotTextures, ArmorHudConfig.CornerStyle cornerStyle) {
        Map<Integer, Integer> armorHud_slotTextureX = new HashMap<Integer, Integer>();
        for (int i = 0; i < 9; i++)
            armorHud_slotTextureX.put(i + 1, 1 + i * armorSlot_length);
        // round corners texture location (uses offhand slot texture)
        // (24, 23)
        int offhandTextureX = 24;
        int offhandTextureY = 23;
        if (orientation == ArmorHudConfig.Orientation.Vertical) {
            // calculate slot textures
            int slotWidth = armorSlot_length;
            int slotOffset = 0;
            if (borderLength > 0) {
                slotWidth -= 2 * (borderLength - 1);
                slotOffset += borderLength - 1;
            }
            // draw slot texture
            for (int i = 0; i < slots.length; i++)
                context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + 1 + slotOffset, armorWidgetY + 1 + i * armorSlot_length, armorHud_slotTextureX.get(slots[i]) + slotOffset, 1, slotWidth, armorSlot_length);
            // draw border texture
            if (borderLength > 0) {
                // calculate border textures
                int endPieceOffset = slotWidth + borderLength;
                int borderTextureX1 = armorHud_slotTextureX.get(1) + borderLength - 1;
                int borderTextureX2 = armorHud_slotTextureX.get(9) + slotWidth + borderLength - 1;
                int borderTextureY2 = slotWidth + borderLength;
                int borderYPos = 2 + armorSlot_length * slots.length - borderLength;
                if (matchBorderAndSlotTextures)
                    borderTextureX1 = armorHud_slotTextureX.get(slots[0]) + borderLength - 1;
                // draw border
                for (int i = 0; i < slots.length; i++) {
                    // sides
                    context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX, armorWidgetY + 1 + i * armorSlot_length, 0, 1, borderLength, armorSlot_length);
                    context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + endPieceOffset, armorWidgetY + 1 + i * armorSlot_length, borderTextureX2, 1, borderLength, armorSlot_length);
                }
                if (cornerStyle == ArmorHudConfig.CornerStyle.Rounded) {
                    // top-bottom
                    context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX, armorWidgetY, offhandTextureX, offhandTextureY, armorHud_width, borderLength);
                    context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX, armorWidgetY + borderYPos, offhandTextureX, offhandTextureY + endPieceOffset, armorHud_width, borderLength);
                }
                else {
                    // top
                    context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX, armorWidgetY, 0, 0, borderLength, borderLength);
                    context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + borderLength, armorWidgetY, borderTextureX1, 0, slotWidth, borderLength);
                    context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + endPieceOffset, armorWidgetY, borderTextureX2, 0, borderLength, borderLength);
                    // bottom
                    context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX, armorWidgetY + borderYPos, 0, borderTextureY2, borderLength, borderLength);
                    context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + borderLength, armorWidgetY + borderYPos, borderTextureX1, borderTextureY2, slotWidth, borderLength);
                    context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + endPieceOffset, armorWidgetY + borderYPos, borderTextureX2, borderTextureY2, borderLength, borderLength);
                }
            }
        }
        else {
            int slotHeight = armorSlot_length;
            if (borderLength > 0)
                slotHeight -= borderLength + 1;
            for (int i = 0; i < slots.length; i++)
                context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + 1 + i * armorSlot_length, armorWidgetY + 1, armorHud_slotTextureX.get(slots[i]), 1, armorSlot_length, slotHeight);
        }
    }

    private float armorHud_getCycleProgress(int index, ArmorHudConfig currentArmorHudConfig) {
        // if warning icon bobbing cycle progress array wasn't initialised we do that now
        if (armorHud_cycleProgress == null) {
            armorHud_cycleProgress = new float[]{this.random.nextFloat(), this.random.nextFloat(), this.random.nextFloat(), this.random.nextFloat()};
        }

        // if interval was set to 0 then it means that icons should not bob, so we always return 0.5 and don't update progress
        if (currentArmorHudConfig.getWarningIconBobbingIntervalMs() == 0.0F) {
            return 0.5F;
        }

        // we want progress updated only when we want icons to move, that is if game is not paused or config screen is open
        if (!this.client.isPaused() || currentArmorHudConfig.isPreview()) {
            armorHud_cycleProgress[index] += (armorHud_measuredTime - armorHud_lastMeasuredTime) / currentArmorHudConfig.getWarningIconBobbingIntervalMs();
            armorHud_cycleProgress[index] %= 1.0F;

            // just in case progress became less than 0 or NaN we set it to some random value
            if (armorHud_cycleProgress[index] < 0 || Float.isNaN(armorHud_cycleProgress[index]))
                armorHud_cycleProgress[index] = this.random.nextFloat();
        }

        return armorHud_cycleProgress[index];
    }

    /**
     * This function determines which config is supposed to be current. Usually the loaded config is considered current
     * but if config screen is open then the preview config is used as current.
     *
     * @return Current config
     */
    private ArmorHudConfig armorHud_getCurrentArmorHudConfig() {
        return this.client.currentScreen != null && this.client.currentScreen.getTitle() == ArmorHudMod.CONFIG_SCREEN_NAME ? ArmorHudMod.previewConfig : ArmorHudMod.getCurrentConfig();
    }
}
