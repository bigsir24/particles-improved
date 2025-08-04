#version 440

uniform int fogType;

uniform float width;
uniform float height;
uniform vec3 fogColor;

const float nearPlane = 0.05F;
uniform float farPlane;

uniform sampler2D sampler;
uniform sampler2D lightmap;

in vec2 faceuv;
in vec2 uv;
in vec2 lightUV;
out vec4 color;

float density() {
    return fogType == 1 ? 2.0F : 0.1F;
}

float lin_depth(){
    float z = gl_FragCoord.z * 2.0F - 1.0F;
    return (2.0F * nearPlane * farPlane) / (farPlane + nearPlane - z * (farPlane - nearPlane));
}

vec4 computeFog(vec4 color) {
    float factor = clamp(exp(-density() * lin_depth()), 0, 1);
    vec3 rgb = factor * color.rgb + (1 - factor) * fogColor;
    return vec4(rgb, color.a);
}

void main()
{
    vec3 tempColor = vec3(1.0, 1.0, 1.0);
    vec4 texColor = texture2D(sampler, uv);
    vec4 lightColor = vec4(texture2D(lightmap, lightUV).rgb, 1);

    color = fogType == 0 ? texColor * lightColor : computeFog(texColor * lightColor);
}
