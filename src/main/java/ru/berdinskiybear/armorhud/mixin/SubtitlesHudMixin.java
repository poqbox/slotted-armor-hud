package ru.berdinskiybear.armorhud.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.SubtitlesHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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

@Mixin(SubtitlesHud.class)
public class SubtitlesHudMixin {

    @Shadow @Final private MinecraftClient client;

    @Unique
    private int offset = 0;
    @Unique
    private final List<ItemStack> armorHud_armorItems = new ArrayList<>(4);

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Ljava/lang/String;)I", ordinal = 3, shift = At.Shift.BY, by = 4))
    public void calculateOffset(DrawContext context, CallbackInfo ci) {
        ArmorHudConfig currentConfig = this.armorHud_getCurrentArmorHudConfig();
        if (currentConfig.isEnabled() && currentConfig.isPushSubtitles()) {
            int add = 0;
            if (currentConfig.getAnchor() == ArmorHudConfig.Anchor.Bottom && currentConfig.getSide() == ArmorHudConfig.Side.Right) {
                int amount = 0;
                PlayerEntity playerEntity = this.getCameraPlayer();
                if (playerEntity != null) {
                    this.armorHud_armorItems.clear();
                    for (ItemStack itemStack : playerEntity.getInventory().armor) {
                        if (!itemStack.isEmpty())
                            amount++;
                        if (!itemStack.isEmpty() || currentConfig.getSlotsShown() != ArmorHudConfig.SlotsShown.Show_Equipped)
                            this.armorHud_armorItems.add(itemStack);
                    }

                    if (!(amount == 0 && currentConfig.getSlotsShown() != ArmorHudConfig.SlotsShown.Always_Show)) {
                        if (currentConfig.getOrientation() == ArmorHudConfig.Orientation.Vertical) {
                            if (currentConfig.getSlotsShown() == ArmorHudConfig.SlotsShown.Show_Equipped)
                                add += 20 * (amount - 1) + currentConfig.getOffsetY();
                            else
                                add += 60 + currentConfig.getOffsetY();
                        }
                        else
                            add += currentConfig.getOffsetY();
                    }
                }
            }
            this.offset = Math.max(add, 0);
        } else
            this.offset = 0;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", shift = At.Shift.AFTER))
    public void offset(DrawContext context, CallbackInfo ci) {
        context.getMatrices().translate(0.0F, -((float) this.offset), 0.0F);
    }

    /**
     * This function determines which config is supposed to be current. Usually the loaded config is considered current
     * but if config screen is open then the preview config is used as current.
     *
     * @return Current config
     */
    @Unique
    private ArmorHudConfig armorHud_getCurrentArmorHudConfig() {
        return this.client.currentScreen != null && this.client.currentScreen.getTitle() == ArmorHudMod.CONFIG_SCREEN_NAME ? ArmorHudMod.previewConfig : ArmorHudMod.getCurrentConfig();
    }

    @Unique
    private PlayerEntity getCameraPlayer() {
        return !(this.client.getCameraEntity() instanceof PlayerEntity) ? null : (PlayerEntity) this.client.getCameraEntity();
    }
}
