package bigsir.particlesmod.mixin;

import bigsir.particlesmod.ParticlesMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Minecraft.class, remap = false)
public abstract class MinecraftMixin {
	@Inject(method = "checkBoundInputs", at = @At(value = "HEAD"), cancellable = true)
	public void keyBindingCheck(InputDevice currentInputDevice, CallbackInfoReturnable<Boolean> cir){

		if(ParticlesMod.recompile.isPressEvent(currentInputDevice)) { //TODO halplibe pr because this is awful
			ParticlesMod.devBuildResources(); //FIXME should not be in release
			ParticlesMod.compileShaders();
			cir.setReturnValue(true);
		}

	}
}
