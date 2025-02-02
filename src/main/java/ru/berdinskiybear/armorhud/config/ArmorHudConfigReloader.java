package ru.berdinskiybear.armorhud.config;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import ru.berdinskiybear.armorhud.ArmorHudMod;

public class ArmorHudConfigReloader implements SimpleSynchronousResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return Identifier.of(ArmorHudMod.MOD_ID, "config_reloader");
    }

    @Override
    public void reload(ResourceManager manager) {
        ArmorHudMod.readConfig();
    }

    public static void register() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new ArmorHudConfigReloader());
    }
}
