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

    private static final Identifier armorHud_HOTBAR_TEXTURE = new Identifier("hud/hotbar");
    private static final Identifier armorHud_OFFHAND_TEXTURE = new Identifier("hud/hotbar_offhand_left");
    private static final Identifier armorHud_EMPTY_HELMET_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_helmet");
    private static final Identifier armorHud_EMPTY_CHESTPLATE_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_chestplate");
    private static final Identifier armorHud_EMPTY_LEGGINGS_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_leggings");
    private static final Identifier armorHud_EMPTY_BOOTS_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_boots");
    private static final Identifier armorHud_BLOCK_ATLAS_TEXTURE = new Identifier("textures/atlas/blocks.png");

    private static final int armorSlot_length = 20;
    private static final int armorSlot_borderedLength = 22;
    private static final int hotbar_width = 182;
    private static final int hotbar_offset = 98;
    private static final int offhandSlot_offset = 29;
    private static final int attackIndicator_offset = 23;

    private final List<ItemStack> armorItems = new ArrayList<>(4);

    @Shadow
    protected abstract void renderHotbarItem(DrawContext context, int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed);

    @Shadow
    protected abstract PlayerEntity getCameraPlayer();

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbar(FLnet/minecraft/client/gui/DrawContext;)V", shift = At.Shift.AFTER))
    public void armorHud_renderArmorHud(DrawContext context, float tickDelta, CallbackInfo ci) {
        // add this to profiler
        this.client.getProfiler().push("armorHud");

        // get current config
        ArmorHudConfig currentArmorHudConfig = this.armorHud_getCurrentArmorHudConfig();

        // switch to enable the mod
        if (currentArmorHudConfig.isEnabled()) {
            PlayerEntity playerEntity = this.getCameraPlayer();
            if (playerEntity != null) {
                int amount = 0;

                // count the items and save the ones that need to be drawn
                List<ItemStack> armor = playerEntity.getInventory().armor;
                armorItems.clear();
                for (int i = 0; i < armor.size(); i++) {
                    ItemStack itemStack = armor.get(i);
                    if (!itemStack.isEmpty())
                        amount++;
                    if (!itemStack.isEmpty() || currentArmorHudConfig.getSlotsShown() != ArmorHudConfig.SlotsShown.Show_Equipped)
                        armorItems.add(itemStack);
                }

                // if true, then prepare and draw
                if (amount > 0 || currentArmorHudConfig.getSlotsShown() == ArmorHudConfig.SlotsShown.Always_Show) {
                    final int armorWidgetY;
                    final int armorWidgetX;
                    final int sideMultiplier;
                    final int sideOffsetMultiplier;
                    final int verticalMultiplier;
                    final int verticalOffsetMultiplier;
                    final int addedHotbarOffset;
                    final int slotNum = currentArmorHudConfig.getSlotsShown() == ArmorHudConfig.SlotsShown.Show_Equipped ? amount : 4;
                    final int armorHud_longestLength = armorSlot_borderedLength + ((slotNum - 1) * armorSlot_length);

                    context.getMatrices().push();
                    context.getMatrices().translate(0, 0, 200);

                    // here I calculate the position of the widget and all sorts of multipliers based on the current config
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

                        switch (currentArmorHudConfig.getOffhandSlotBehavior()) {
                            case Ignore -> addedHotbarOffset = 0;
                            case Leave_Space -> addedHotbarOffset = Math.max(offhandSlot_offset, attackIndicator_offset);
                            case Adhere -> {
                                if ((playerEntity.getMainArm().getOpposite() == Arm.LEFT && currentArmorHudConfig.getSide() == ArmorHudConfig.Side.Left || playerEntity.getMainArm().getOpposite() == Arm.RIGHT && currentArmorHudConfig.getSide() == ArmorHudConfig.Side.Right) && !playerEntity.getOffHandStack().isEmpty())
                                    addedHotbarOffset = offhandSlot_offset;
                                else if ((playerEntity.getMainArm() == Arm.LEFT && currentArmorHudConfig.getSide() == ArmorHudConfig.Side.Left || playerEntity.getMainArm() == Arm.RIGHT && currentArmorHudConfig.getSide() == ArmorHudConfig.Side.Right) && this.client.options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR)
                                    addedHotbarOffset = attackIndicator_offset;
                                else
                                    addedHotbarOffset = 0;
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + currentArmorHudConfig.getOffhandSlotBehavior());
                        }

                        int armorWidgetX1 = 0;
                        int armorWidgetY1 = 0;
                        switch (currentArmorHudConfig.getOrientation()) {
                            case Horizontal -> {
                                armorWidgetX1 = switch (currentArmorHudConfig.getAnchor()) {
                                    case Top_Center -> this.scaledWidth / 2 - (armorHud_longestLength / 2);
                                    case Top, Bottom -> (armorHud_longestLength - this.scaledWidth) * sideOffsetMultiplier;
                                    case Hotbar -> this.scaledWidth / 2 + ((hotbar_offset + addedHotbarOffset) * sideMultiplier) + (armorHud_longestLength * sideOffsetMultiplier);
                                    default -> throw new IllegalStateException("Unexpected value: " + currentArmorHudConfig.getAnchor());
                                };
                                armorWidgetY1 = switch (currentArmorHudConfig.getAnchor()) {
                                    case Bottom, Hotbar -> this.scaledHeight - armorSlot_borderedLength;
                                    case Top, Top_Center -> 0;
                                    default -> throw new IllegalStateException("Unexpected value: " + currentArmorHudConfig.getAnchor());
                                };
                            }
                            case Vertical -> {
                                armorWidgetX1 = switch (currentArmorHudConfig.getAnchor()) {
                                    case Top_Center -> this.scaledWidth / 2 - (armorSlot_borderedLength / 2);
                                    case Top, Bottom -> (armorSlot_borderedLength - this.scaledWidth) * sideOffsetMultiplier;
                                    case Hotbar -> this.scaledWidth / 2 + ((hotbar_offset + addedHotbarOffset) * sideMultiplier) + (armorSlot_borderedLength * sideOffsetMultiplier);
                                    default -> throw new IllegalStateException("Unexpected value: " + currentArmorHudConfig.getAnchor());
                                };
                                armorWidgetY1 = switch (currentArmorHudConfig.getAnchor()) {
                                    case Bottom, Hotbar -> this.scaledHeight - armorHud_longestLength;
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

                    // here I prepare the widget texture
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.setShaderTexture(0, armorHud_HOTBAR_TEXTURE);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();

                    // here I draw the slots
                    int[] slotData = new int[slotNum];
                    for (int i = 0; i < slotNum; i++)
                        slotData[i] = currentArmorHudConfig.getSlotTextures()[i];
                    context.getMatrices().push();
                    context.getMatrices().translate(0, 0, -91);
                    this.drawSlots(context, armorWidgetX, armorWidgetY, currentArmorHudConfig.getOrientation(), currentArmorHudConfig.getStyle(), slotData, currentArmorHudConfig.getBorderLength(), currentArmorHudConfig.isMatchBorderAndSlotTextures());
                    context.getMatrices().pop();

                    // here I blend in slot icons
                    if (currentArmorHudConfig.isEmptyIconsShown()) {
                        if (currentArmorHudConfig.getSlotsShown() != ArmorHudConfig.SlotsShown.Show_Equipped && (amount > 0 || currentArmorHudConfig.getSlotsShown() == ArmorHudConfig.SlotsShown.Always_Show)) {
                            context.getMatrices().push();
                            context.getMatrices().translate(0, 0, -90);
                            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_COLOR, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
                            for (int i = 0; i < armorItems.size(); i++) {
                                if (armorItems.get(i).isEmpty()) {
                                    Identifier spriteId = switch (i) {
                                        case 0 -> armorHud_EMPTY_BOOTS_SLOT_TEXTURE;
                                        case 1 -> armorHud_EMPTY_LEGGINGS_SLOT_TEXTURE;
                                        case 2 -> armorHud_EMPTY_CHESTPLATE_SLOT_TEXTURE;
                                        case 3 -> armorHud_EMPTY_HELMET_SLOT_TEXTURE;
                                        default -> throw new IllegalStateException("Unexpected value: " + i);
                                    };
                                    Sprite sprite = this.client.getSpriteAtlas(armorHud_BLOCK_ATLAS_TEXTURE).apply(spriteId);
                                    RenderSystem.setShaderTexture(0, sprite.getAtlasId());

                                    int iReversed = currentArmorHudConfig.isReversed() ? i : (armorItems.size() - i - 1);
                                    switch (currentArmorHudConfig.getOrientation()) {
                                        case Horizontal -> context.drawSprite(armorWidgetX + (armorSlot_length * iReversed) + 3, armorWidgetY + 3, 0, 16, 16, sprite);
                                        case Vertical -> context.drawSprite(armorWidgetX + 3, armorWidgetY + (armorSlot_length * iReversed) + 3, 0, 16, 16, sprite);
                                    }
                                }
                            }
                            RenderSystem.defaultBlendFunc();
                            context.getMatrices().pop();
                        }
                    }
                    // here I draw the armour items
                    for (int i = 0; i < armorItems.size(); i++) {
                        int iReversed = currentArmorHudConfig.isReversed() ? i : (armorItems.size() - i - 1);
                        switch (currentArmorHudConfig.getOrientation()) {
                            case Horizontal -> this.renderHotbarItem(context, armorWidgetX + (armorSlot_length * iReversed) + 3, armorWidgetY + 3, tickDelta, playerEntity, armorItems.get(i), i + 1);
                            case Vertical -> this.renderHotbarItem(context, armorWidgetX + 3, armorWidgetY + (armorSlot_length * iReversed) + 3, tickDelta, playerEntity, armorItems.get(i), i + 1);
                        }
                    }
                    context.getMatrices().pop();
                }
            }
        }
        this.client.getProfiler().pop();
    }

    private void drawSlots(DrawContext context, int armorWidgetX, int armorWidgetY, ArmorHudConfig.Orientation orientation, ArmorHudConfig.Style style, int[] slots, int borderLength, boolean matchBorderAndSlotTextures) {
        final Map<Integer, Integer> armorHud_slotTextureX = new HashMap<Integer, Integer>();
        for (int i = 0; i < 9; i++)
            armorHud_slotTextureX.put(i + 1, 1 + i * armorSlot_length);
        final int slotAmount = slots.length;

        // calculate slot textures
        int slotLength = armorSlot_length;
        int edgeSlotLength = armorSlot_length;
        int slotOffset = 0;
        if (borderLength > 0) {
            slotLength -= 2 * (borderLength - 1);
            edgeSlotLength -= borderLength - 1;
            slotOffset += borderLength - 1;
        }
        // draw slot texture
        {
            if (slotAmount == 1)
//                context.drawGuiTexture(armorHud_WIDGETS_TEXTURE, 182, 22, armorHud_slotTextureX.get(slots[0]), 1, 0, 0, 182, 22);
//                context.drawGuiTexture(armorHud_WIDGETS_TEXTURE, 182, 22, armorHud_slotTextureX.get(slots[0]), 1, 0, 22, 20, 22);
//                context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + 1 + slotOffset, armorWidgetY + 1 + slotOffset, armorHud_slotTextureX.get(slots[0]) + slotOffset, 1 + slotOffset, slotLength, slotLength);
                context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, armorHud_slotTextureX.get(slots[0]) + slotOffset, 1 + slotOffset, armorWidgetX + 1 + slotOffset, armorWidgetY + 1 + slotOffset, slotLength, slotLength);
            else {
                if (orientation == ArmorHudConfig.Orientation.Vertical) {
                    for (int i = 0; i < slotAmount; i++)
                        if (i == 0)
                            //context.drawTexture(armorHud_WIDGETS_TEXTURE, armorWidgetX + 1 + slotOffset, armorWidgetY + 1 + slotOffset, armorHud_slotTextureX.get(slots[0]) + slotOffset, 1 + slotOffset, slotLength, edgeSlotLength);
                            context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, armorHud_slotTextureX.get(slots[0]) + slotOffset, 1 + slotOffset, armorWidgetX + 1 + slotOffset, armorWidgetY + 1 + slotOffset, slotLength, edgeSlotLength);
                        else if (i == slotAmount - 1)
                            context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, armorHud_slotTextureX.get(slots[i]) + slotOffset, 1, armorWidgetX + 1 + slotOffset, armorWidgetY + 1 + i * armorSlot_length, slotLength, edgeSlotLength);
                        else
                            context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, armorHud_slotTextureX.get(slots[i]) + slotOffset, 1, armorWidgetX + 1 + slotOffset, armorWidgetY + 1 + i * armorSlot_length, slotLength, armorSlot_length);
                } else {
                    for (int i = 0; i < slotAmount; i++)
                        if (i == 0)
                            context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, armorHud_slotTextureX.get(slots[0]) + slotOffset, 1 + slotOffset, armorWidgetX + 1 + slotOffset, armorWidgetY + 1 + slotOffset, edgeSlotLength, slotLength);
                        else if (i == slotAmount - 1)
                            context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, armorHud_slotTextureX.get(slots[i]), 1 + slotOffset, armorWidgetX + 1 + i * armorSlot_length, armorWidgetY + 1 + slotOffset, edgeSlotLength, slotLength);
                        else
                            context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, armorHud_slotTextureX.get(slots[i]), 1 + slotOffset, armorWidgetX + 1 + i * armorSlot_length, armorWidgetY + 1 + slotOffset, armorSlot_length, slotLength);
                }
            }
        }

        // calculate border textures
        int borderTextureX1 = armorHud_slotTextureX.get(1) + borderLength - 1;
        int borderTextureX2 = armorHud_slotTextureX.get(9) + slotLength + borderLength - 1;
        int borderTextureY1 = borderLength;
        int borderTextureY2 = slotLength + borderLength;
        int endPieceOffset = slotLength + borderLength;
        int edgePieceLength = 1 + armorSlot_length - borderLength;
        int endBorderOffset = 2 + armorSlot_length * slotAmount - borderLength;
        // draw border texture
        if (borderLength > 0) {
            if (orientation == ArmorHudConfig.Orientation.Vertical) {
                if (matchBorderAndSlotTextures)
                    borderTextureX1 = armorHud_slotTextureX.get(slots[0]) + borderLength - 1;
                if (slotAmount == 1) {
                    // side borders
                    context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, 0, borderLength, armorWidgetX, armorWidgetY + borderLength, borderLength, slotLength);
                    context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, borderTextureX2, borderLength, armorWidgetX + endPieceOffset, armorWidgetY + borderLength, borderLength, slotLength);
                } else {
                    for (int i = 0; i < slotAmount; i++) {
                        // side borders
                        if (i == 0) {
                            context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, 0, borderLength, armorWidgetX, armorWidgetY + borderLength, borderLength, edgePieceLength);
                            context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, borderTextureX2, borderLength, armorWidgetX + endPieceOffset, armorWidgetY + borderLength, borderLength, edgePieceLength);
                        } else if (i == slotAmount - 1) {
                            context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, 0, 1, armorWidgetX, armorWidgetY + 1 + i * armorSlot_length, borderLength, edgePieceLength);
                            context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, borderTextureX2, 1, armorWidgetX + endPieceOffset, armorWidgetY + 1 + i * armorSlot_length, borderLength, edgePieceLength);
                        } else {
                            context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, 0, 1, armorWidgetX, armorWidgetY + 1 + i * armorSlot_length, borderLength, armorSlot_length);
                            context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, borderTextureX2, 1, armorWidgetX + endPieceOffset, armorWidgetY + 1 + i * armorSlot_length, borderLength, armorSlot_length);
                        }
                    }
                }
                if (style == ArmorHudConfig.Style.Rounded) {
                    // top-bottom borders
                    context.drawGuiTexture(armorHud_OFFHAND_TEXTURE, 29, 24, 0, 1, armorWidgetX, armorWidgetY, armorSlot_borderedLength, borderLength);
                    context.drawGuiTexture(armorHud_OFFHAND_TEXTURE, 29, 24, 0, 1 + endPieceOffset, armorWidgetX, armorWidgetY + endBorderOffset, armorSlot_borderedLength, borderLength);
                } else {
                    // top border
                    context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, 0, 0, armorWidgetX, armorWidgetY, borderLength, borderLength);
                    context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, borderTextureX1, 0, armorWidgetX + borderLength, armorWidgetY, slotLength, borderLength);
                    context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, borderTextureX2, 0, armorWidgetX + endPieceOffset, armorWidgetY, borderLength, borderLength);
                    // bottom border
                    context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, 0, borderTextureY2, armorWidgetX, armorWidgetY + endBorderOffset, borderLength, borderLength);
                    context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, borderTextureX1, borderTextureY2, armorWidgetX + borderLength, armorWidgetY + endBorderOffset, slotLength, borderLength);
                    context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, borderTextureX2, borderTextureY2, armorWidgetX + endPieceOffset, armorWidgetY + endBorderOffset, borderLength, borderLength);
                }
            } else {
                if (slotAmount == 1) {
                    // top-bottom borders
                    context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, borderTextureX1, 0, armorWidgetX + borderLength, armorWidgetY, slotLength, borderLength);
                    context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, borderTextureX1, borderTextureY2, armorWidgetX + borderLength, armorWidgetY + endPieceOffset, slotLength, borderLength);
                } else {
                    int borderTextureXvar = armorHud_slotTextureX.get(1);
                    for (int i = 0; i < slotAmount; i++) {
                        // top-bottom borders
                        if (i > 0 && matchBorderAndSlotTextures)
                            borderTextureXvar = armorHud_slotTextureX.get(slots[i]);
                        if (i == 0) {
                            context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, borderTextureX1, 0, armorWidgetX + borderLength, armorWidgetY, edgePieceLength, borderLength);
                            context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, borderTextureX1, borderTextureY2, armorWidgetX + borderLength, armorWidgetY + endPieceOffset, edgePieceLength, borderLength);
                        } else if (i == slotAmount - 1) {
                            context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, borderTextureXvar, 0, armorWidgetX + 1 + i * armorSlot_length, armorWidgetY, edgePieceLength, borderLength);
                            context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, borderTextureXvar, borderTextureY2, armorWidgetX + 1 + i * armorSlot_length, armorWidgetY + endPieceOffset, edgePieceLength, borderLength);
                        } else {
                            context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, borderTextureXvar, 0, armorWidgetX + 1 + i * armorSlot_length, armorWidgetY, armorSlot_length, borderLength);
                            context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, borderTextureXvar, borderTextureY2, armorWidgetX + 1 + i * armorSlot_length, armorWidgetY + endPieceOffset, armorSlot_length, borderLength);
                        }
                    }
                }
                if (style == ArmorHudConfig.Style.Rounded) {
                    // left-right borders
                    context.drawGuiTexture(armorHud_OFFHAND_TEXTURE, 29, 24, 0, 1, armorWidgetX, armorWidgetY, borderLength, armorSlot_borderedLength);
                    context.drawGuiTexture(armorHud_OFFHAND_TEXTURE, 29, 24, endPieceOffset, 1, armorWidgetX + endBorderOffset, armorWidgetY, borderLength, armorSlot_borderedLength);
                } else {
                    // left border
                    context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, 0, 0, armorWidgetX, armorWidgetY, borderLength, borderLength);
                    context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, 0, borderTextureY1, armorWidgetX, armorWidgetY + borderLength, borderLength, slotLength);
                    context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, 0, borderTextureY2, armorWidgetX, armorWidgetY + endPieceOffset, borderLength, borderLength);
                    // right border
                    context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, borderTextureX2, 0, armorWidgetX + endBorderOffset, armorWidgetY, borderLength, borderLength);
                    context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, borderTextureX2, borderTextureY1, armorWidgetX + endBorderOffset, armorWidgetY + borderLength, borderLength, slotLength);
                    context.drawGuiTexture(armorHud_HOTBAR_TEXTURE, 182, 22, borderTextureX2, borderTextureY2, armorWidgetX + endBorderOffset, armorWidgetY + endPieceOffset, borderLength, borderLength);
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
    private ArmorHudConfig armorHud_getCurrentArmorHudConfig() {
        return this.client.currentScreen != null && this.client.currentScreen.getTitle() == ArmorHudMod.CONFIG_SCREEN_NAME ? ArmorHudMod.previewConfig : ArmorHudMod.getCurrentConfig();
    }
}
