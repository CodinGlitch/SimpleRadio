package codinglit.ch.simpleradio.client;

import codinglit.ch.simpleradio.SimpleRadio;
import codinglit.ch.simpleradio.SimpleRadioNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class SimpleRadioClient implements ClientModInitializer {
    private final Map<UUID, Boolean> isTransmitting = new HashMap<>();

    @Override
    public void onInitializeClient() {

        // Model Predicates
        ModelPredicateProviderRegistry.register(SimpleRadio.RADIO, new Identifier("using"),
                (itemStack, clientWorld, livingEntity, i) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getActiveItem() == itemStack ? 1.0f : 0.0f);

        ModelPredicateProviderRegistry.register(SimpleRadio.RADIO, new Identifier("speaking"),
                (itemStack, clientWorld, livingEntity, i) -> livingEntity != null && isTransmitting.containsValue(true) ? 1.0f : 0.0f);


        // Networking

        ClientPlayNetworking.registerGlobalReceiver(SimpleRadioNetworking.STARTED_USING_RADIO_S2C, (client, handler, buf, responseSender) -> {
            ClientWorld clientWorld = client.world;
            ClientPlayerEntity clientPlayer = client.player;

            if (clientWorld == null || clientPlayer == null) return;

            isTransmitting.put(buf.readUuid(), true);
            clientWorld.playSound(clientPlayer.getX(), clientPlayer.getY(), clientPlayer.getZ(), SimpleRadio.RADIO_OPEN, SoundCategory.PLAYERS,1, 1, false);
        });

        ClientPlayNetworking.registerGlobalReceiver(SimpleRadioNetworking.STOPPED_USING_RADIO_S2C, (client, handler, buf, responseSender) -> {
            ClientWorld clientWorld = client.world;
            ClientPlayerEntity clientPlayer = client.player;

            if (clientWorld == null || clientPlayer == null) return;

            isTransmitting.put(buf.readUuid(), false);
            clientWorld.playSound(clientPlayer.getX(), clientPlayer.getY(), clientPlayer.getZ(), SimpleRadio.RADIO_CLOSE, SoundCategory.PLAYERS,1, 1, false);
        });
    }
}
