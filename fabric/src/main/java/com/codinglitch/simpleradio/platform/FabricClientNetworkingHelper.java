package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.core.networking.Packeter;
import com.codinglitch.simpleradio.platform.services.ClientNetworkingHelper;
import com.codinglitch.simpleradio.platform.services.ClientRegistryHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class FabricClientNetworkingHelper implements ClientNetworkingHelper {
    @Override
    public void sendToServer(Packeter packet) {
        ClientPlayNetworking.send(packet.resource(), packet.toBuffer());
    }
}
