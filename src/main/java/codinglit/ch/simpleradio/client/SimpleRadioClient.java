package codinglit.ch.simpleradio.client;

import codinglit.ch.simpleradio.SimpleRadio;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SimpleRadioClient implements ClientModInitializer {
    public static boolean isAnyoneTalking = false; // i think this is fine?

    @Override
    public void onInitializeClient() {
        ModelPredicateProviderRegistry.register(SimpleRadio.RADIO, new Identifier("using"),
                (itemStack, clientWorld, livingEntity, i) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getActiveItem() == itemStack ? 1.0f : 0.0f);

        ModelPredicateProviderRegistry.register(SimpleRadio.RADIO, new Identifier("speaking"),
                (itemStack, clientWorld, livingEntity, i) -> livingEntity != null && isAnyoneTalking ? 1.0f : 0.0f);
    }
}
