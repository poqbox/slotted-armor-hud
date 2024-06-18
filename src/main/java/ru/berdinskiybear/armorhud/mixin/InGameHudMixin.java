package ru.berdinskiybear.armorhud.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.render.RenderTickCounter;
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
import java.util.List;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow @Final
    private MinecraftClient client;
    @Unique
    private static final Identifier HOTBAR_TEXTURE = Identifier.of("hud/hotbar");
    @Unique
    private static final Identifier OFFHAND_TEXTURE = Identifier.of("hud/hotbar_offhand_left");
    @Unique
    private static final Identifier EMPTY_HELMET_SLOT_TEXTURE = Identifier.of("item/empty_armor_slot_helmet");
    @Unique
    private static final Identifier EMPTY_CHESTPLATE_SLOT_TEXTURE = Identifier.of("item/empty_armor_slot_chestplate");
    @Unique
    private static final Identifier EMPTY_LEGGINGS_SLOT_TEXTURE = Identifier.of("item/empty_armor_slot_leggings");
    @Unique
    private static final Identifier EMPTY_BOOTS_SLOT_TEXTURE = Identifier.of("item/empty_armor_slot_boots");
    @Unique
    private static final Identifier BLOCK_ATLAS_TEXTURE = Identifier.of("textures/atlas/blocks.png");

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
    private static final int[] slotU = {1, 21, 41, 61, 81, 101, 121, 141, 161};

    @Unique
    private final List<ItemStack> armorHudItems = new ArrayList<>(4);

    @Shadow
    protected abstract void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed);

    @Shadow
    protected abstract PlayerEntity getCameraPlayer();

    @Inject(method = "renderHotbar", at = @At("TAIL"))
    public void renderArmorHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        // add this to profiler
        this.client.getProfiler().push("armorHud");

        // get current config
        ArmorHudConfig config = this.getArmorHudConfig();

        // switch to enable the mod
        if (config.isEnabled()) {
            PlayerEntity playerEntity = this.getCameraPlayer();
            if (playerEntity != null) {

                // count the items and save the ones that need to be drawn
                armorHudItems.addAll(playerEntity.getInventory().armor);
                for (int i = 0; i < armorHudItems.size(); i++) {
                    if (armorHudItems.get(i).isEmpty() && config.getSlotsShown() == ArmorHudConfig.SlotsShown.Show_Equipped)
                        armorHudItems.remove(i--);
                }

                // if true, then prepare and draw
                if (!armorHudItems.isEmpty() || config.getSlotsShown() == ArmorHudConfig.SlotsShown.Always_Show) {
                    final int scaledWidth = context.getScaledWindowWidth();
                    final int scaledHeight = context.getScaledWindowHeight();
                    final int y;
                    final int x;
                    final int armorHudLength = slot_borderedLength + ((armorHudItems.size() - 1) * slot_length);
                    final int sideMultiplier;
                    final int sideOffsetMultiplier;
                    final int verticalMultiplier;
                    final int addedHotbarOffset;

                    context.getMatrices().push();
                    context.getMatrices().translate(0, 0, 200);

                    // calculate the position of the armor HUD and all sorts of multipliers based on the current config
                    if ((config.getAnchor() == ArmorHudConfig.Anchor.Hotbar && config.getSide() == ArmorHudConfig.Side.Left) || (config.getAnchor() != ArmorHudConfig.Anchor.Hotbar && config.getSide() == ArmorHudConfig.Side.Right)) {
                        sideMultiplier = -1;
                        sideOffsetMultiplier = -1;
                    } else {
                        sideMultiplier = 1;
                        sideOffsetMultiplier = 0;
                    }
                    switch (config.getAnchor()) {
                        case Top, Top_Center ->
                            verticalMultiplier = 1;
                        case Hotbar, Bottom ->
                            verticalMultiplier = -1;
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
                    int x_temp;
                    int y_temp;
                    switch (config.getOrientation()) {
                        case Horizontal -> {
                            x_temp = switch (config.getAnchor()) {
                                case Top_Center -> scaledWidth / 2 - (armorHudLength / 2);
                                case Top, Bottom -> (armorHudLength - scaledWidth) * sideOffsetMultiplier;
                                case Hotbar -> scaledWidth / 2 + ((hotbar_offset + addedHotbarOffset) * sideMultiplier) + (armorHudLength * sideOffsetMultiplier);
                            };
                            y_temp = switch (config.getAnchor()) {
                                case Bottom, Hotbar -> scaledHeight - slot_borderedLength;
                                case Top, Top_Center -> 0;
                            };
                        }
                        case Vertical -> {
                            x_temp = switch (config.getAnchor()) {
                                case Top_Center -> scaledWidth / 2 - (slot_borderedLength / 2);
                                case Top, Bottom -> (slot_borderedLength - scaledWidth) * sideOffsetMultiplier;
                                case Hotbar -> scaledWidth / 2 + ((hotbar_offset + addedHotbarOffset) * sideMultiplier) + (slot_borderedLength * sideOffsetMultiplier);
                            };
                            y_temp = switch (config.getAnchor()) {
                                case Bottom, Hotbar -> scaledHeight - armorHudLength;
                                case Top, Top_Center -> 0;
                            };
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + config.getOrientation());
                    }
                    if (config.getAnchor() != ArmorHudConfig.Anchor.Top_Center)
                        x_temp += config.getOffsetX() * sideMultiplier;
                    y_temp += config.getOffsetY() * verticalMultiplier;
                    x = x_temp;
                    y = y_temp;

                    // prepare the texture
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.setShaderTexture(0, HOTBAR_TEXTURE);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();

                    // draw the slots
                    int[] slotTextures = new int[armorHudItems.size()];
                    for (int i = 0; i < armorHudItems.size(); i++)
                        slotTextures[i] = config.getSlotTextures()[i] - 1;
                    context.getMatrices().push();
                    context.getMatrices().translate(0, 0, -91);
                    this.drawSlots(config, context, x, y, slotTextures);
                    context.getMatrices().pop();

                    // blend in the empty slot icons
                    if (config.isEmptyIconsShown() && config.getSlotsShown() != ArmorHudConfig.SlotsShown.Show_Equipped && (!armorHudItems.isEmpty() || config.getSlotsShown() == ArmorHudConfig.SlotsShown.Always_Show)) {
                        context.getMatrices().push();
                        context.getMatrices().translate(0, 0, -90);
                        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_COLOR, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
                        this.drawEmptySlotIcons(config, context, x, y);
                        RenderSystem.defaultBlendFunc();
                        context.getMatrices().pop();
                    }

                    // draw the armour items
                    this.drawArmorItems(config, context, x, y, tickCounter, playerEntity);
                    context.getMatrices().pop();
                }
                armorHudItems.clear();
            }
        }
        this.client.getProfiler().pop();
    }

    @Unique
    private void drawSlots(ArmorHudConfig config, DrawContext context, int x, int y, int[] slotTextures) {
        final ArmorHudConfig.Orientation orientation = config.getOrientation();
        final ArmorHudConfig.Style style = config.getStyle();
        final int borderLength = config.getBorderLength();
        final boolean matchBorderAndSlotTextures = config.isMatchBorderAndSlotTextures();
        final int slotAmount = slotTextures.length;

        // calculate slot textures
        // hotbar width = 182
        // hotbar height = 22
        int slotOffsetUV = 0;
        int slotLength = slot_length;
        int edgeSlotLength = slot_length;
        if (borderLength > 0) {
            slotOffsetUV += borderLength - 1;
            slotLength -= 2 * (borderLength - 1);
            edgeSlotLength -= borderLength - 1;
        }
        // draw slot texture
        if (slotAmount == 1)
            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, slotU[slotTextures[0]] + slotOffsetUV, 1 + slotOffsetUV, x + 1 + slotOffsetUV, y + 1 + slotOffsetUV, slotLength, slotLength);
        else {
            if (orientation == ArmorHudConfig.Orientation.Vertical) {
                for (int i = 0; i < slotAmount; i++)
                    if (i == 0)
                        context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, slotU[slotTextures[0]] + slotOffsetUV, 1 + slotOffsetUV, x + 1 + slotOffsetUV, y + 1 + slotOffsetUV, slotLength, edgeSlotLength);
                    else if (i == slotAmount - 1)
                        context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, slotU[slotTextures[i]] + slotOffsetUV, 1, x + 1 + slotOffsetUV, y + 1 + i * slot_length, slotLength, edgeSlotLength);
                    else
                        context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, slotU[slotTextures[i]] + slotOffsetUV, 1, x + 1 + slotOffsetUV, y + 1 + i * slot_length, slotLength, slot_length);
            } else {
                for (int i = 0; i < slotAmount; i++)
                    if (i == 0)
                        context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, slotU[slotTextures[0]] + slotOffsetUV, 1 + slotOffsetUV, x + 1 + slotOffsetUV, y + 1 + slotOffsetUV, edgeSlotLength, slotLength);
                    else if (i == slotAmount - 1)
                        context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, slotU[slotTextures[i]], 1 + slotOffsetUV, x + 1 + i * slot_length, y + 1 + slotOffsetUV, edgeSlotLength, slotLength);
                    else
                        context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, slotU[slotTextures[i]], 1 + slotOffsetUV, x + 1 + i * slot_length, y + 1 + slotOffsetUV, slot_length, slotLength);
            }
        }

        // calculate border textures
        int borderTextureX1 = slotU[0] + borderLength - 1;
        int borderTextureX2 = slotU[8] + slotLength + borderLength - 1;
        int borderTextureY1 = borderLength;
        int borderTextureY2 = slotLength + borderLength;
        int endPieceOffset = slotLength + borderLength;
        int edgePieceLength = 1 + slot_length - borderLength;
        int endBorderOffset = 2 + slot_length * slotAmount - borderLength;
        // draw border texture
        if (borderLength > 0) {
            if (orientation == ArmorHudConfig.Orientation.Vertical) {
                if (matchBorderAndSlotTextures)
                    borderTextureX1 = slotU[slotTextures[0]] + borderLength - 1;
                if (slotAmount == 1) {
                    // side borders
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 0, borderLength, x, y + borderLength, borderLength, slotLength);
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX2, borderLength, x + endPieceOffset, y + borderLength, borderLength, slotLength);
                } else {
                    for (int i = 0; i < slotAmount; i++) {
                        // side borders
                        if (i == 0) {
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 0, borderLength, x, y + borderLength, borderLength, edgePieceLength);
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX2, borderLength, x + endPieceOffset, y + borderLength, borderLength, edgePieceLength);
                        } else if (i == slotAmount - 1) {
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 0, 1, x, y + 1 + i * slot_length, borderLength, edgePieceLength);
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX2, 1, x + endPieceOffset, y + 1 + i * slot_length, borderLength, edgePieceLength);
                        } else {
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 0, 1, x, y + 1 + i * slot_length, borderLength, slot_length);
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX2, 1, x + endPieceOffset, y + 1 + i * slot_length, borderLength, slot_length);
                        }
                    }
                }
                if (style == ArmorHudConfig.Style.Rounded) {
                    // top-bottom borders
                    context.drawGuiTexture(OFFHAND_TEXTURE, 29, 24, 0, 1, x, y, slot_borderedLength, borderLength);
                    context.drawGuiTexture(OFFHAND_TEXTURE, 29, 24, 0, 1 + endPieceOffset, x, y + endBorderOffset, slot_borderedLength, borderLength);
                } else {
                    // top border
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 0, 0, x, y, borderLength, borderLength);
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX1, 0, x + borderLength, y, slotLength, borderLength);
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX2, 0, x + endPieceOffset, y, borderLength, borderLength);
                    // bottom border
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 0, borderTextureY2, x, y + endBorderOffset, borderLength, borderLength);
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX1, borderTextureY2, x + borderLength, y + endBorderOffset, slotLength, borderLength);
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX2, borderTextureY2, x + endPieceOffset, y + endBorderOffset, borderLength, borderLength);
                }
            } else {
                if (slotAmount == 1) {
                    // top-bottom borders
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX1, 0, x + borderLength, y, slotLength, borderLength);
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX1, borderTextureY2, x + borderLength, y + endPieceOffset, slotLength, borderLength);
                } else {
                    int borderTextureX = slotU[0];
                    for (int i = 0; i < slotAmount; i++) {
                        // top-bottom borders
                        if (i > 0 && matchBorderAndSlotTextures)
                            borderTextureX = slotU[slotTextures[i]];
                        if (i == 0) {
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX1, 0, x + borderLength, y, edgePieceLength, borderLength);
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX1, borderTextureY2, x + borderLength, y + endPieceOffset, edgePieceLength, borderLength);
                        } else if (i == slotAmount - 1) {
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX, 0, x + 1 + i * slot_length, y, edgePieceLength, borderLength);
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX, borderTextureY2, x + 1 + i * slot_length, y + endPieceOffset, edgePieceLength, borderLength);
                        } else {
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX, 0, x + 1 + i * slot_length, y, slot_length, borderLength);
                            context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX, borderTextureY2, x + 1 + i * slot_length, y + endPieceOffset, slot_length, borderLength);
                        }
                    }
                }
                if (style == ArmorHudConfig.Style.Rounded) {
                    // left-right borders
                    context.drawGuiTexture(OFFHAND_TEXTURE, 29, 24, 0, 1, x, y, borderLength, slot_borderedLength);
                    context.drawGuiTexture(OFFHAND_TEXTURE, 29, 24, endPieceOffset, 1, x + endBorderOffset, y, borderLength, slot_borderedLength);
                } else {
                    // left border
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 0, 0, x, y, borderLength, borderLength);
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 0, borderTextureY1, x, y + borderLength, borderLength, slotLength);
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 0, borderTextureY2, x, y + endPieceOffset, borderLength, borderLength);
                    // right border
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX2, 0, x + endBorderOffset, y, borderLength, borderLength);
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX2, borderTextureY1, x + endBorderOffset, y + borderLength, borderLength, slotLength);
                    context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, borderTextureX2, borderTextureY2, x + endBorderOffset, y + endPieceOffset, borderLength, borderLength);
                }
            }
        }
    }

    @Unique
    private void drawEmptySlotIcons(ArmorHudConfig config, DrawContext context, int x, int y) {
        for (int i = 0; i < armorHudItems.size(); i++) {
            if (armorHudItems.get(i).isEmpty()) {
                Identifier spriteId = switch (i) {
                    case 0 -> EMPTY_BOOTS_SLOT_TEXTURE;
                    case 1 -> EMPTY_LEGGINGS_SLOT_TEXTURE;
                    case 2 -> EMPTY_CHESTPLATE_SLOT_TEXTURE;
                    case 3 -> EMPTY_HELMET_SLOT_TEXTURE;
                    default -> throw new IllegalStateException("Unexpected value: " + i);
                };
                Sprite sprite = this.client.getSpriteAtlas(BLOCK_ATLAS_TEXTURE).apply(spriteId);
                RenderSystem.setShaderTexture(0, sprite.getAtlasId());

                int iReversed = config.isReversed() ? i : (armorHudItems.size() - i - 1);
                switch (config.getOrientation()) {
                    case Horizontal -> context.drawSprite(x + (slot_length * iReversed) + 3, y + 3, 0, 16, 16, sprite);
                    case Vertical -> context.drawSprite(x + 3, y + (slot_length * iReversed) + 3, 0, 16, 16, sprite);
                }
            }
        }
    }

    @Unique
    private void drawArmorItems(ArmorHudConfig config, DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity playerEntity) {
        for (int i = 0; i < armorHudItems.size(); i++) {
            int iReversed = config.isReversed() ? i : (armorHudItems.size() - i - 1);
            switch (config.getOrientation()) {
                case Horizontal -> this.renderHotbarItem(context, x + (slot_length * iReversed) + 3, y + 3, tickCounter, playerEntity, armorHudItems.get(i), i + 1);
                case Vertical -> this.renderHotbarItem(context, x + 3, y + (slot_length * iReversed) + 3, tickCounter, playerEntity, armorHudItems.get(i), i + 1);
            }
        }
    }

    /**
     * Determines which config to use.
     * If the config screen is open, the preview config is returned. Otherwise, the loaded config is returned.
     *
     * @return config
     */
    @Unique
    private ArmorHudConfig getArmorHudConfig() {
        return this.client.currentScreen != null && this.client.currentScreen.getTitle() == ArmorHudMod.CONFIG_SCREEN_NAME ? ArmorHudMod.previewConfig : ArmorHudMod.getConfig();
    }
}
