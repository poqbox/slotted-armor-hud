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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
    @Shadow @Final
    private MinecraftClient client;

    @Unique
    private static final Identifier HOTBAR_TEXTURE = new Identifier("hud/hotbar");
    @Unique
    private static final Identifier OFFHAND_TEXTURE = new Identifier("hud/hotbar_offhand_left");
    @Unique
    private static final Identifier EMPTY_HELMET_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_helmet");
    @Unique
    private static final Identifier EMPTY_CHESTPLATE_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_chestplate");
    @Unique
    private static final Identifier EMPTY_LEGGINGS_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_leggings");
    @Unique
    private static final Identifier EMPTY_BOOTS_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_boots");
    @Unique
    private static final Identifier BLOCK_ATLAS_TEXTURE = new Identifier("textures/atlas/blocks.png");

    @Unique
    private static final int slot_length = 20;
    @Unique
    private static final int slot_borderedLength = 22;
    @Unique
    private static final int hotbar_offset = 98;
    @Unique
    private static final int offhandSlot_offset = 29;
    @Unique
    private static final int attackIndicator_offset = 23;

    @Unique
    private final List<ItemStack> armorItems = new ArrayList<>(4);

    @Shadow
    protected abstract void renderHotbarItem(DrawContext context, int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed);

    @Shadow
    protected abstract PlayerEntity getCameraPlayer();

    @Inject(method = "render", at = @At("TAIL"))
    public void renderArmorHud(DrawContext context, float tickDelta, CallbackInfo ci) {
        // add this to profiler
        this.client.getProfiler().push("armorHud");

        // get current config
        ArmorHudConfig config = this.getArmorHudConfig();

        // switch to enable the mod
        if (config.isEnabled()) {
            PlayerEntity playerEntity = this.getCameraPlayer();
            if (playerEntity != null) {
                int amount = 0;

                // count the items and save the ones that need to be drawn
                List<ItemStack> armor = playerEntity.getInventory().armor;
                armorItems.clear();
                for (ItemStack itemStack : armor) {
                    if (!itemStack.isEmpty())
                        amount++;
                    if (!itemStack.isEmpty() || config.getSlotsShown() != ArmorHudConfig.SlotsShown.Show_Equipped)
                        armorItems.add(itemStack);
                }

                // if true, then prepare and draw
                if (amount > 0 || config.getSlotsShown() == ArmorHudConfig.SlotsShown.Always_Show) {
                    final int scaledWidth = this.client.getWindow().getScaledWidth();
                    final int scaledHeight = this.client.getWindow().getScaledHeight();
                    final int armorWidgetY;
                    final int armorWidgetX;
                    final int sideMultiplier;
                    final int sideOffsetMultiplier;
                    final int verticalMultiplier;
                    final int verticalOffsetMultiplier;
                    final int addedHotbarOffset;
                    final int slotNum = config.getSlotsShown() == ArmorHudConfig.SlotsShown.Show_Equipped ? amount : 4;
                    final int armorHud_longestLength = slot_borderedLength + ((slotNum - 1) * slot_length);

                    context.getMatrices().push();
                    context.getMatrices().translate(0, 0, 200);

                    // here I calculate the position of the widget and all sorts of multipliers based on the current config
                    {
                        if ((config.getAnchor() == ArmorHudConfig.Anchor.Hotbar && config.getSide() == ArmorHudConfig.Side.Left) || (config.getAnchor() != ArmorHudConfig.Anchor.Hotbar && config.getSide() == ArmorHudConfig.Side.Right)) {
                            sideMultiplier = -1;
                            sideOffsetMultiplier = -1;
                        } else {
                            sideMultiplier = 1;
                            sideOffsetMultiplier = 0;
                        }

                        switch (config.getAnchor()) {
                            case Top, Top_Center -> {
                                verticalMultiplier = 1;
                                verticalOffsetMultiplier = 0;
                            }
                            case Hotbar, Bottom -> {
                                verticalMultiplier = -1;
                                verticalOffsetMultiplier = -1;
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + config.getAnchor());
                        }

                        switch (config.getOffhandSlotBehavior()) {
                            case Ignore -> addedHotbarOffset = 0;
                            case Leave_Space -> addedHotbarOffset = Math.max(offhandSlot_offset, attackIndicator_offset);
                            case Adhere -> {
                                if ((playerEntity.getMainArm().getOpposite() == Arm.LEFT && config.getSide() == ArmorHudConfig.Side.Left || playerEntity.getMainArm().getOpposite() == Arm.RIGHT && config.getSide() == ArmorHudConfig.Side.Right) && !playerEntity.getOffHandStack().isEmpty())
                                    addedHotbarOffset = offhandSlot_offset;
                                else if ((playerEntity.getMainArm() == Arm.LEFT && config.getSide() == ArmorHudConfig.Side.Left || playerEntity.getMainArm() == Arm.RIGHT && config.getSide() == ArmorHudConfig.Side.Right) && this.client.options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR)
                                    addedHotbarOffset = attackIndicator_offset;
                                else
                                    addedHotbarOffset = 0;
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + config.getOffhandSlotBehavior());
                        }

                        int armorWidgetX1 = 0;
                        int armorWidgetY1 = 0;
                        switch (config.getOrientation()) {
                            case Horizontal -> {
                                armorWidgetX1 = switch (config.getAnchor()) {
                                    case Top_Center -> scaledWidth / 2 - (armorHud_longestLength / 2);
                                    case Top, Bottom -> (armorHud_longestLength - scaledWidth) * sideOffsetMultiplier;
                                    case Hotbar -> scaledWidth / 2 + ((hotbar_offset + addedHotbarOffset) * sideMultiplier) + (armorHud_longestLength * sideOffsetMultiplier);
                                };
                                armorWidgetY1 = switch (config.getAnchor()) {
                                    case Bottom, Hotbar -> scaledHeight - slot_borderedLength;
                                    case Top, Top_Center -> 0;
                                };
                            }
                            case Vertical -> {
                                armorWidgetX1 = switch (config.getAnchor()) {
                                    case Top_Center -> scaledWidth / 2 - (slot_borderedLength / 2);
                                    case Top, Bottom -> (slot_borderedLength - scaledWidth) * sideOffsetMultiplier;
                                    case Hotbar -> scaledWidth / 2 + ((hotbar_offset + addedHotbarOffset) * sideMultiplier) + (slot_borderedLength * sideOffsetMultiplier);
                                };
                                armorWidgetY1 = switch (config.getAnchor()) {
                                    case Bottom, Hotbar -> scaledHeight - armorHud_longestLength;
                                    case Top, Top_Center -> 0;
                                };
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + config.getOrientation());
                        }
                        armorWidgetX1 += config.getOffsetX() * sideMultiplier;
                        armorWidgetY1 += config.getOffsetY() * verticalMultiplier;

                        armorWidgetX = armorWidgetX1;
                        armorWidgetY = armorWidgetY1;
                    }

                    // here I prepare the widget texture
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.setShaderTexture(0, HOTBAR_TEXTURE);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();

                    // here I draw the slots
                    int[] slotData = new int[slotNum];
                    for (int i = 0; i < slotNum; i++)
                        slotData[i] = config.getSlotTextures()[i];
                    context.getMatrices().push();
                    context.getMatrices().translate(0, 0, -91);
                    this.drawSlots(context, armorWidgetX, armorWidgetY, config.getOrientation(), config.getStyle(), slotData, config.getBorderLength(), config.isMatchBorderAndSlotTextures());
                    context.getMatrices().pop();

                    // here I blend in the empty slot icons
                    if (config.isEmptyIconsShown() && config.getSlotsShown() != ArmorHudConfig.SlotsShown.Show_Equipped && (amount > 0 || config.getSlotsShown() == ArmorHudConfig.SlotsShown.Always_Show)) {
                        context.getMatrices().push();
                        context.getMatrices().translate(0, 0, -90);
                        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_COLOR, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
                        for (int i = 0; i < armorItems.size(); i++) {
                            if (armorItems.get(i).isEmpty()) {
                                Identifier spriteId = switch (i) {
                                    case 0 -> EMPTY_BOOTS_SLOT_TEXTURE;
                                    case 1 -> EMPTY_LEGGINGS_SLOT_TEXTURE;
                                    case 2 -> EMPTY_CHESTPLATE_SLOT_TEXTURE;
                                    case 3 -> EMPTY_HELMET_SLOT_TEXTURE;
                                    default -> throw new IllegalStateException("Unexpected value: " + i);
                                };
                                Sprite sprite = this.client.getSpriteAtlas(BLOCK_ATLAS_TEXTURE).apply(spriteId);
                                RenderSystem.setShaderTexture(0, sprite.getAtlasId());

                                int iReversed = config.isReversed() ? i : (armorItems.size() - i - 1);
                                switch (config.getOrientation()) {
                                    case Horizontal -> context.drawSprite(armorWidgetX + (slot_length * iReversed) + 3, armorWidgetY + 3, 0, 16, 16, sprite);
                                    case Vertical -> context.drawSprite(armorWidgetX + 3, armorWidgetY + (slot_length * iReversed) + 3, 0, 16, 16, sprite);
                                }
                            }
                        }
                        RenderSystem.defaultBlendFunc();
                        context.getMatrices().pop();
                    }

                    // here I draw the armour items
                    for (int i = 0; i < armorItems.size(); i++) {
                        int iReversed = config.isReversed() ? i : (armorItems.size() - i - 1);
                        switch (config.getOrientation()) {
                            case Horizontal -> this.renderHotbarItem(context, armorWidgetX + (slot_length * iReversed) + 3, armorWidgetY + 3, tickDelta, playerEntity, armorItems.get(i), i + 1);
                            case Vertical -> this.renderHotbarItem(context, armorWidgetX + 3, armorWidgetY + (slot_length * iReversed) + 3, tickDelta, playerEntity, armorItems.get(i), i + 1);
                        }
                    }
                    context.getMatrices().pop();
                }
            }
        }
        this.client.getProfiler().pop();
    }

    @Unique
    private void drawSlots(DrawContext context, int armorWidgetX, int armorWidgetY, ArmorHudConfig.Orientation orientation, ArmorHudConfig.Style style, int[] slots, int borderLength, boolean matchBorderAndSlotTextures) {
        final Map<Integer, Integer> armorHud_slotTextureX = new HashMap<Integer, Integer>();
        for (int i = 0; i < 9; i++)
            armorHud_slotTextureX.put(i + 1, i * slot_length + 1);
        final int slotAmount = slots.length;

        // calculate slot textures
        // hotbar width = 182
        // hotbar height = 22
        int slotOffset = 0;
        int slotLength = slot_length;
        int edgeSlotLength = slot_length;
        if (borderLength > 0) {
            slotOffset += borderLength - 1;
            slotLength -= 2 * (borderLength - 1);
            edgeSlotLength -= borderLength - 1;
        }
        // draw slot texture
        if (slotAmount == 1)
            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, armorHud_slotTextureX.get(slots[0]) + slotOffset, 1 + slotOffset, armorWidgetX + 1 + slotOffset, armorWidgetY + 1 + slotOffset, slotLength, slotLength);
        else {
            if (orientation == ArmorHudConfig.Orientation.Vertical) {
                for (int i = 0; i < slotAmount; i++)
                    if (i == 0)
                        context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, armorHud_slotTextureX.get(slots[0]) + slotOffset, 1 + slotOffset, armorWidgetX + 1 + slotOffset, armorWidgetY + 1 + slotOffset, slotLength, edgeSlotLength);
                    else if (i == slotAmount - 1)
                        context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, armorHud_slotTextureX.get(slots[i]) + slotOffset, 1, armorWidgetX + 1 + slotOffset, armorWidgetY + 1 + i * slot_length, slotLength, edgeSlotLength);
                    else
                        context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, armorHud_slotTextureX.get(slots[i]) + slotOffset, 1, armorWidgetX + 1 + slotOffset, armorWidgetY + 1 + i * slot_length, slotLength, slot_length);
            } else {
                for (int i = 0; i < slotAmount; i++)
                    if (i == 0)
                        context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, armorHud_slotTextureX.get(slots[0]) + slotOffset, 1 + slotOffset, armorWidgetX + 1 + slotOffset, armorWidgetY + 1 + slotOffset, edgeSlotLength, slotLength);
                    else if (i == slotAmount - 1)
                        context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, armorHud_slotTextureX.get(slots[i]), 1 + slotOffset, armorWidgetX + 1 + i * slot_length, armorWidgetY + 1 + slotOffset, edgeSlotLength, slotLength);
                    else
                        context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, armorHud_slotTextureX.get(slots[i]), 1 + slotOffset, armorWidgetX + 1 + i * slot_length, armorWidgetY + 1 + slotOffset, slot_length, slotLength);
            }
        }

        // calculate border textures
        int borderTextureX1 = armorHud_slotTextureX.get(1) + borderLength - 1;
        int borderTextureX2 = armorHud_slotTextureX.get(9) + slotLength + borderLength - 1;
        int borderTextureY1 = borderLength;
        int borderTextureY2 = slotLength + borderLength;
        int endPieceOffset = slotLength + borderLength;
        int edgePieceLength = 1 + slot_length - borderLength;
        int endBorderOffset = 2 + slot_length * slotAmount - borderLength;
        // draw border texture
        if (borderLength > 0) {
            if (orientation == ArmorHudConfig.Orientation.Vertical) {
                if (matchBorderAndSlotTextures)
                    borderTextureX1 = armorHud_slotTextureX.get(slots[0]) + borderLength - 1;
                if (slotAmount == 1) {
                    // side borders
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 0, borderLength, armorWidgetX, armorWidgetY + borderLength, borderLength, slotLength);
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX2, borderLength, armorWidgetX + endPieceOffset, armorWidgetY + borderLength, borderLength, slotLength);
                } else {
                    for (int i = 0; i < slotAmount; i++) {
                        // side borders
                        if (i == 0) {
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 0, borderLength, armorWidgetX, armorWidgetY + borderLength, borderLength, edgePieceLength);
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX2, borderLength, armorWidgetX + endPieceOffset, armorWidgetY + borderLength, borderLength, edgePieceLength);
                        } else if (i == slotAmount - 1) {
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 0, 1, armorWidgetX, armorWidgetY + 1 + i * slot_length, borderLength, edgePieceLength);
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX2, 1, armorWidgetX + endPieceOffset, armorWidgetY + 1 + i * slot_length, borderLength, edgePieceLength);
                        } else {
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 0, 1, armorWidgetX, armorWidgetY + 1 + i * slot_length, borderLength, slot_length);
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX2, 1, armorWidgetX + endPieceOffset, armorWidgetY + 1 + i * slot_length, borderLength, slot_length);
                        }
                    }
                }
                if (style == ArmorHudConfig.Style.Rounded) {
                    // top-bottom borders
                    context.drawGuiTexture(OFFHAND_TEXTURE, 29, 24, 0, 1, armorWidgetX, armorWidgetY, slot_borderedLength, borderLength);
                    context.drawGuiTexture(OFFHAND_TEXTURE, 29, 24, 0, 1 + endPieceOffset, armorWidgetX, armorWidgetY + endBorderOffset, slot_borderedLength, borderLength);
                } else {
                    // top border
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 0, 0, armorWidgetX, armorWidgetY, borderLength, borderLength);
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX1, 0, armorWidgetX + borderLength, armorWidgetY, slotLength, borderLength);
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX2, 0, armorWidgetX + endPieceOffset, armorWidgetY, borderLength, borderLength);
                    // bottom border
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 0, borderTextureY2, armorWidgetX, armorWidgetY + endBorderOffset, borderLength, borderLength);
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX1, borderTextureY2, armorWidgetX + borderLength, armorWidgetY + endBorderOffset, slotLength, borderLength);
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX2, borderTextureY2, armorWidgetX + endPieceOffset, armorWidgetY + endBorderOffset, borderLength, borderLength);
                }
            } else {
                if (slotAmount == 1) {
                    // top-bottom borders
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX1, 0, armorWidgetX + borderLength, armorWidgetY, slotLength, borderLength);
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX1, borderTextureY2, armorWidgetX + borderLength, armorWidgetY + endPieceOffset, slotLength, borderLength);
                } else {
                    int borderTextureXvar = armorHud_slotTextureX.get(1);
                    for (int i = 0; i < slotAmount; i++) {
                        // top-bottom borders
                        if (i > 0 && matchBorderAndSlotTextures)
                            borderTextureXvar = armorHud_slotTextureX.get(slots[i]);
                        if (i == 0) {
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX1, 0, armorWidgetX + borderLength, armorWidgetY, edgePieceLength, borderLength);
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX1, borderTextureY2, armorWidgetX + borderLength, armorWidgetY + endPieceOffset, edgePieceLength, borderLength);
                        } else if (i == slotAmount - 1) {
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureXvar, 0, armorWidgetX + 1 + i * slot_length, armorWidgetY, edgePieceLength, borderLength);
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureXvar, borderTextureY2, armorWidgetX + 1 + i * slot_length, armorWidgetY + endPieceOffset, edgePieceLength, borderLength);
                        } else {
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureXvar, 0, armorWidgetX + 1 + i * slot_length, armorWidgetY, slot_length, borderLength);
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureXvar, borderTextureY2, armorWidgetX + 1 + i * slot_length, armorWidgetY + endPieceOffset, slot_length, borderLength);
                        }
                    }
                }
                if (style == ArmorHudConfig.Style.Rounded) {
                    // left-right borders
                    context.drawGuiTexture(OFFHAND_TEXTURE, 29, 24, 0, 1, armorWidgetX, armorWidgetY, borderLength, slot_borderedLength);
                    context.drawGuiTexture(OFFHAND_TEXTURE, 29, 24, endPieceOffset, 1, armorWidgetX + endBorderOffset, armorWidgetY, borderLength, slot_borderedLength);
                } else {
                    // left border
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 0, 0, armorWidgetX, armorWidgetY, borderLength, borderLength);
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 0, borderTextureY1, armorWidgetX, armorWidgetY + borderLength, borderLength, slotLength);
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 0, borderTextureY2, armorWidgetX, armorWidgetY + endPieceOffset, borderLength, borderLength);
                    // right border
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX2, 0, armorWidgetX + endBorderOffset, armorWidgetY, borderLength, borderLength);
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX2, borderTextureY1, armorWidgetX + endBorderOffset, armorWidgetY + borderLength, borderLength, slotLength);
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX2, borderTextureY2, armorWidgetX + endBorderOffset, armorWidgetY + endPieceOffset, borderLength, borderLength);
                }
            }
        }
    }

    /**
     * This function determines which config is supposed to be current. Usually the loaded config is considered current
     * but if config screen is open then the preview config is used as current.
     *
     * @return Current config
     */
    @Unique
    private ArmorHudConfig getArmorHudConfig() {
        return this.client.currentScreen != null && this.client.currentScreen.getTitle() == ArmorHudMod.CONFIG_SCREEN_NAME ? ArmorHudMod.previewConfig : ArmorHudMod.getConfig();
    }
}
