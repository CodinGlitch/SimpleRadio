package codinglit.ch.simpleradio.registry;

import codinglit.ch.simpleradio.SimpleRadio;
import codinglit.ch.simpleradio.SimpleRadioNetworking;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.server.Group;
import de.maxhenkel.voicechat.voice.server.GroupManager;
import de.maxhenkel.voicechat.voice.server.Server;
import de.maxhenkel.voicechat.voice.server.ServerWorldUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.function.Consumer;

public class RadioItem extends Item {
    public RadioItem(Settings settings) {
        super(settings);
    }

    private void transmit(ServerPlayerEntity player, Identifier packetID) {
        Server server = Voicechat.SERVER.getServer();
        if (server != null) {
            GroupManager groupManager = server.getGroupManager();

            Collection<ServerPlayerEntity> players = ServerWorldUtils.getPlayersInRange((ServerWorld) player.world, player.getPos(), SimpleRadio.CONFIG.maxRadioDistance, receiver -> {
                Group groupSender = groupManager.getPlayerGroup(player);
                Group groupReceiver = groupManager.getPlayerGroup(receiver);
                return groupSender != null && groupSender.equals(groupReceiver) && receiver.getUuid() != player.getUuid();
            });

            PacketByteBuf buffer = PacketByteBufs.create().writeUuid(player.getUuid());
            for (ServerPlayerEntity receiver : players) {
                ServerPlayNetworking.send(receiver, packetID, buffer);
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        world.playSound(
                player, player.getBlockPos(),
                SimpleRadio.RADIO_OPEN,
                SoundCategory.PLAYERS,
                1f,1f
        );
        player.setCurrentHand(hand);

        // Send started using packet
        if (!world.isClient) {
            transmit((ServerPlayerEntity) player, SimpleRadioNetworking.STARTED_USING_RADIO_S2C);
        }

        return TypedActionResult.consume(stack);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.TOOT_HORN;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof PlayerEntity player) {
            world.playSound(
                    player, user.getBlockPos(),
                    SimpleRadio.RADIO_CLOSE,
                    SoundCategory.PLAYERS,
                    1f,1f
            );

            // Send stopped using packet
            if (!world.isClient) {
                transmit((ServerPlayerEntity) player, SimpleRadioNetworking.STOPPED_USING_RADIO_S2C);
            }

            player.getItemCooldownManager().set(this, 20);
        }

        super.onStoppedUsing(stack, world, user, remainingUseTicks);
    }
}
