package com.codinglitch.simpleradio.core.events;

import com.codinglitch.intermediator.Phase;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.RadioEvent;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * Events relating to when a {@link Router} should be garbage collected.
 */
public class CollectionEvent extends RadioEvent {

    public enum Result {
        PASS,
        IGNORE,
        COLLECT
    }

    /**
     * Called when a router needs to be location-verified.
     */
    public static class VerifyLocation extends RadioEvent implements Phase {
        public Result result;
        public final WorldlyPosition position;
        public final Class<?> clazz;

        public VerifyLocation(WorldlyPosition position, Class<?> clazz) {
            this.result = Result.PASS;
            this.position = position;
            this.clazz = clazz;
        }
    }

    /**
     * Called when a router needs to be entity-verified.
     */
    public static class VerifyEntity extends RadioEvent implements Phase {
        public Result result;
        public final Entity entity;
        public final Predicate<ItemStack> criteria;

        public VerifyEntity(Entity entity, Predicate<ItemStack> criteria) {
            this.result = Result.PASS;
            this.entity = entity;
            this.criteria = criteria;
        }
    }
}
