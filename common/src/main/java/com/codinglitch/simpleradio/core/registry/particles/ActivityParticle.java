package com.codinglitch.simpleradio.core.registry.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class ActivityParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    protected float growth = 0;

    ActivityParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z);
        this.setAlpha(1);
        this.quadSize = 0.4f;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.lifetime = 7;
        this.sprites = sprites;
        this.age = 0;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.xd *= 0.9f;
            this.yd *= 0.9f;
            this.zd *= 0.9f;

            this.move(this.xd, this.yd, this.zd);
            this.setSpriteFromAge(this.sprites);
            this.quadSize += growth;
        }
    }

    public void render(VertexConsumer consumer, Camera camera, float partial) {
        this.renderRotatedParticle(consumer, camera, partial, 1);
        this.renderRotatedParticle(consumer, camera, partial, -1);
    }

    protected void renderRotatedParticle(VertexConsumer consumer, Camera camera, float partial, float mult) {
        Vec3 cameraPosition = camera.getPosition();
        float x = (float)(Mth.lerp(partial, this.xo, this.x) - cameraPosition.x());
        float y = (float)(Mth.lerp(partial, this.yo, this.y) - cameraPosition.y());
        float z = (float)(Mth.lerp(partial, this.zo, this.z) - cameraPosition.z());

        Vector3f direction = new Vector3f((float) this.xd, (float) this.yd, (float) this.zd).mul(mult);
        Vector3f up = direction.cross(this.xd > 0 ? new Vector3f(0, 0, 1) : new Vector3f(1, 0, 0), new Vector3f()).normalize();
        Vector3f left = direction.cross(up, new Vector3f()).normalize();

        Vector3f[] vectors = new Vector3f[] {
                up.negate(new Vector3f()).sub(left),
                up.get(new Vector3f()).sub(left),
                up.get(new Vector3f()).add(left),
                up.negate(new Vector3f()).add(left)
        };
        float size = this.getQuadSize(partial);

        for(int i = 0; i < 4; ++i) {
            Vector3f vec = vectors[i];
            vec.mul(size);
            vec.add(x, y, z);
        }

        renderQuad(consumer, vectors, partial);
    }

    protected void renderQuad(VertexConsumer consumer, Vector3f[] vectors, float partial) {
        int lightColor = this.getLightColor(partial);

        this.makeCornerVertex(consumer, vectors[0], this.getU1(), this.getV1(), lightColor);
        this.makeCornerVertex(consumer, vectors[1], this.getU1(), this.getV0(), lightColor);
        this.makeCornerVertex(consumer, vectors[2], this.getU0(), this.getV0(), lightColor);
        this.makeCornerVertex(consumer, vectors[3], this.getU0(), this.getV1(), lightColor);
    }

    protected void makeCornerVertex(VertexConsumer consumer, Vector3f pos, float u, float v, int light) {
        consumer.addVertex(pos.x(), pos.y(), pos.z()).setUv(u, v).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
    }

    public int getLightColor(float t) {
        return 15728880;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new ActivityParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        }
    }
}
