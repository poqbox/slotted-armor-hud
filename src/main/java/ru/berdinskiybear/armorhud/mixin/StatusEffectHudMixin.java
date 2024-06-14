package ru.berdinskiybear.armorhud.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;
import java.util.List;

@Mixin(InGameHud.class)
public class StatusEffectHudMixin {
    @Shadow @Final
    private MinecraftClient client;
    @Unique
    private int offset = 0;

    @Inject(method = "renderStatusEffectOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffectInstance;shouldShowIcon()Z", shift = At.Shift.AFTER))
    public void calculateOffset(DrawContext context, float tickDelta, CallbackInfo ci) {
        ArmorHudConfig config = this.getArmorHudConfig();
        if (config.isEnabled() && config.isPushStatusEffectIcons() && config.getAnchor() == ArmorHudConfig.Anchor.Top && config.getSide() == ArmorHudConfig.Side.Right) {
            int add = 0;
            int amount = 0;
            PlayerEntity playerEntity = this.getCameraPlayer();
            if (playerEntity != null) {
                if (config.getSlotsShown() == ArmorHudConfig.SlotsShown.Always_Show)
                    amount = 4;
                else {
                    List<ItemStack> armorList = playerEntity.getInventory().armor;
                    for (ItemStack itemStack : armorList) {
                        if (!itemStack.isEmpty()) {
                            amount++;
                            if (config.getSlotsShown() != ArmorHudConfig.SlotsShown.Show_Equipped) {
                                amount = 4;
                                break;
                            }
                        }
                    }
                }
                if (amount != 0)
                    add += 22 + config.getOffsetY();
                if (config.getOrientation() == ArmorHudConfig.Orientation.Vertical)
                    add += 20 * (amount - 1);
            }
            this.offset = Math.max(add, 0);
        } else
            this.offset = 0;
    }

    @ModifyVariable(method = "renderStatusEffectOverlay", at = @At(value = "STORE", ordinal = 0), ordinal = 3)
    public int statusEffectIconsOffset(int y) {
        return y + this.offset;
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

    @Unique
    private PlayerEntity getCameraPlayer() {
        return !(this.client.getCameraEntity() instanceof PlayerEntity) ? null : (PlayerEntity) this.client.getCameraEntity();
    }
}
