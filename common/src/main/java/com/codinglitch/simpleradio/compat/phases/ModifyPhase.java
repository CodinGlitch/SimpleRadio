package com.codinglitch.simpleradio.compat.phases;

import com.codinglitch.intermediator.Phase;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import org.joml.Quaternionf;

public class ModifyPhase {
    public static class Position implements Phase {
        public WorldlyPosition position;

        public Position(WorldlyPosition position) {
            this.position = position;
        }
    }

    public static class Rotation implements Phase {
        public WorldlyPosition position;
        public Quaternionf rotation;

        public Rotation(WorldlyPosition position, Quaternionf rotation) {
            this.position = position;
            this.rotation = rotation;
        }
    }
}
