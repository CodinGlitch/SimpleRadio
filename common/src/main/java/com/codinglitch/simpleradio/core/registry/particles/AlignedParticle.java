package com.codinglitch.simpleradio.core.registry.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class AlignedParticle extends ActivityParticle {
    AlignedParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        this.lifetime = 5;
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partial) {
        this.renderRotatedParticle(consumer, camera, partial, 1);
    }

    @Override
    protected void renderRotatedParticle(VertexConsumer consumer, Camera camera, float partial, float mult) {
        Vec3 cameraPosition = camera.getPosition();
        float x = (float)(Mth.lerp(partial, this.xo, this.x) - cameraPosition.x());
        float y = (float)(Mth.lerp(partial, this.yo, this.y) - cameraPosition.y());
        float z = (float)(Mth.lerp(partial, this.zo, this.z) - cameraPosition.z());

        //TODO: might be some optimizations here
        Vector3f direction = new Vector3f((float) this.xd, (float) this.yd, (float) this.zd).normalize().mul(mult);
        Vector3f up = new Vector3f(
                x * (1 - direction.x),
                y * (1 - direction.y),
                z * (1 - direction.z)
        ).normalize();
        Vector3f left = direction.cross(up, new Vector3f()).normalize();

        Vector3f[] vectors = new Vector3f[] {
                direction.negate(new Vector3f()).sub(left),
                direction.get(new Vector3f()).sub(left),
                direction.get(new Vector3f()).add(left),
                direction.negate(new Vector3f()).add(left)
        };

        float size = this.getQuadSize(partial);

        for(int i = 0; i < 4; ++i) {
            Vector3f vec = vectors[i];
            vec.mul(size);
            vec.add(x, y, z);
        }

        renderQuad(consumer, vectors, partial);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new AlignedParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        }
    }
}
