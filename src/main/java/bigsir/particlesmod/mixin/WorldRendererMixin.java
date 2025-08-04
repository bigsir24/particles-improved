package bigsir.particlesmod.mixin;

import bigsir.particlesmod.ParticlesMod;
import bigsir.particlesmod.particle.ParticleHandler;
import bigsir.particlesmod.particle.ParticleVec;
import bigsir.particlesmod.utils.AccessorClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.LightmapHelper;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.camera.CameraUtil;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.client.render.shader.Shader;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL44;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WorldRenderer.class, remap = false)
public abstract class WorldRendererMixin {
	@Shadow
	public Minecraft mc;
	@Shadow
	@Final
	public LightmapHelper lightmapHelper;
	@Unique
	public boolean isSetup = false;

	@Unique
	public int buffer = 0;

	@Unique
	public float[] fogColor = new float[4];
	@Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/util/debug/Debug;change(Ljava/lang/String;)V", ordinal = 7))
	public void renderTest(float partialTicks, long updateRenderersUntil, CallbackInfo ci) {
		if(!isSetup) {
			buffer = GL20.glGenBuffers();

			isSetup = true;
		}

		Shader shader = ParticlesMod.particleShader;
		int program = ((ShaderAccessor)shader).getProgram();

		//GL11.glEnable(GL11.GL_BLEND);
		//GL11.glDepthMask(false);
		GL11.glDisable(GL11.GL_CULL_FACE);
		shader.bind();
		TextureRegistry.particleAtlas.bind();
		if (LightmapHelper.isLightmapEnabled()) {
			GL20.glUniform1i(shader.getUniform("lightmap"), 1);
			GL20.glActiveTexture(GL13.GL_TEXTURE0 + 1);
			int lmt = ((LightmapHelperAccessor)this.lightmapHelper).getLightmapTexture();
			GL20.glBindTexture(GL11.GL_TEXTURE_2D, lmt);
			GL13.glTexParameteri(3553, 10241, GL20.GL_NEAREST);
			GL13.glTexParameteri(3553, 10240, GL20.GL_NEAREST);
			GL13.glTexParameteri(3553, 10242, GL20.GL_CLAMP_TO_EDGE);
			GL13.glTexParameteri(3553, 10243, GL20.GL_CLAMP_TO_EDGE);
		}

		GL11.glGetFloatv(GL11.GL_FOG_COLOR, fogColor);
		GL20.glUniform3f(shader.getUniform("fogColor"), fogColor[0], fogColor[1], fogColor[2]);
		GL20.glUniform1f(shader.getUniform("farPlane"), mc.gameSettings.renderDistance.value * 16F);
		GL20.glUniform1i(shader.getUniform("runTick"), mc.thePlayer.tickCount);
		GL20.glUniform1i(shader.getUniform("blockSize"), ParticleHandler.blockSize);

		World world = Minecraft.getMinecraft().currentWorld;
		Player player = Minecraft.getMinecraft().thePlayer;
		float windDir = world.worldType.getWindManager().getWindDirection(world, 0, 500, 0);
		GL20.glUniform1f(shader.getUniform("windDirection"), (float) windDir * 360.0F);

		// Awful
		int fogType = 0;
		if (CameraUtil.isUnderLiquid(this.mc.activeCamera, this.mc.currentWorld, Material.lava, partialTicks)) {
			fogType = 1;
		} else if( CameraUtil.isUnderLiquid(this.mc.activeCamera, this.mc.currentWorld, Material.water, partialTicks)) {
			fogType = 2;
		}
		GL20.glUniform1i(shader.getUniform("fogType"), fogType);

		float[] clip = AccessorClient.frustum.clip;

		ICamera camera = this.mc.activeCamera;
		float x = (float) camera.getX(partialTicks);
		float y = (float) camera.getY(partialTicks);
		float z = (float) camera.getZ(partialTicks);

		GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(program, "clip"), false, clip);
		GL20.glUniform3f(shader.getUniform("camera"), -x, -y, -z);
		GL20.glUniform1f(shader.getUniform("partialTick"), partialTicks);
		GL20.glUniform1f(shader.getUniform("camRot"), (float) (camera.getYRot(partialTicks) % 360));
		GL20.glUniform1f(shader.getUniform("camRotX"), (float) (camera.getXRot(partialTicks)));

		ParticleHandler.floatBuffer.bufferData(GL44.GL_SHADER_STORAGE_BUFFER, buffer, GL20.GL_STATIC_DRAW);

		GL20.glDrawArrays(GL20.GL_TRIANGLES, 0, ParticleHandler.particles.size() * 6);

		//Needs to be reset because some calls do not explicitly select the first texture unit to modify
		GL20.glActiveTexture(GL13.GL_TEXTURE0);

		shader.unbind();
		//GL11.glDepthMask(true);
		//GL11.glDisable(GL11.GL_BLEND);
	}
}
