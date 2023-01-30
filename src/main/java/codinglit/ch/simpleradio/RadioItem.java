package codinglit.ch.simpleradio;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class RadioItem extends Item {
    public RadioItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        world.playSound(
                user, user.getBlockPos(),
                SimpleRadio.RADIO_OPEN,
                SoundCategory.PLAYERS,
                1f,1f
        );
        user.setCurrentHand(hand);

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

            player.getItemCooldownManager().set(this, 20);
        }

        super.onStoppedUsing(stack, world, user, remainingUseTicks);
    }
}
