package bigsir.particlesmod.mixin;

import bigsir.particlesmod.particle.ParticleHandler;
import net.minecraft.core.world.World;
import org.lwjgl.opengl.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = World.class, remap = false)
public abstract class WorldMixin {
	@Inject(method = "tick", at = @At("HEAD"))
	public void testParticles(CallbackInfo ci) {
		ParticleHandler.simulate((World) (Object) this);
	}
}
