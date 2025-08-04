#version 440

uniform mat4 clip;
uniform vec3 camera;
uniform float partialTick;
uniform float camRot;
uniform float camRotX;
uniform int runTick;
uniform int blockSize;
uniform float windDirection;

layout(std430, binding = 0) readonly buffer vertexPullBuffer
{
    uint particleData[];
};

layout(std430, binding = 1) readonly buffer lightmapPullBuffer
{
    uint packedLightmapData[]; /*Each value stores the data of 4 separate particles*/
};

const vec3 facePos[4] = vec3[4]
(
    vec3(0.0, 1.0, 0.0),
    vec3(1.0, 0.0, 0.0),
    vec3(1.0, 1.0, 0.0),
    vec3(0.0, 0.0, 0.0)
);

const vec3 inset[4] = vec3[4]
(
    vec3(0.0005, -0.0005, 0.0),
    vec3(-0.0005, 0.0005, 0.0),
    vec3(-0.0005, -0.0005, 0.0),
    vec3(0.0005, 0.0005, 0.0)
);

/*Winding order to access the face positions*/
int indices[6] = {0, 1, 2, 1, 0, 3};

out vec2 uv;
out vec2 lightUV;
out vec2 faceuv;

vec3 lerp_vec(vec3 a, vec3 b) {
    return a + (b - a) * partialTick;
}

vec3 lerp_delta(vec3 a, vec3 delta) {
    return a + delta * partialTick;
}

mat4 rotationY( in float angle ) {
    return mat4(	cos(angle),		0,		sin(angle),	0,
    0,		1.0,			 0,	0,
    -sin(angle),	0,		cos(angle),	0,
    0, 		0,				0,	1);
}

mat4 rotationX( in float angle ) {
    return mat4(1,		0,               0,	            0,
                0,		cos(angle),     -sin(angle),	0,
                0,  	sin(angle),		cos(angle),     0,
                0,      0,		        0,	            1);
        }


mat4 rotationZ( in float angle ) {
    return mat4(
    cos(angle),		sin(angle),    0,  0,
    -sin(angle),		cos(angle),     0,	0,
    0,  	        0,		        0,  0,
    0,              0,              0,  1);
}

vec3 cam_transform(vec3 pos) {
    float rad = radians(180 - camRot);
    pos.x = pos.x * cos(rad) + pos.z * -sin(rad);
    pos.y = pos.y;
    pos.z = pos.x * sin(rad) + pos.z * cos(rad);
    return pos;
}

vec3 unpack3(uint index) {
    return vec3(uintBitsToFloat(particleData[index]), uintBitsToFloat(particleData[index + 1]), uintBitsToFloat(particleData[index + 2]));
}

vec2 unpack2(uint index) {
    return vec2(uintBitsToFloat(particleData[index]), uintBitsToFloat(particleData[index + 1]));
}

void main()
{
    /*texcoord = gl_MultiTexCoord0.xy;*/
    int index = (gl_VertexID / 6);
    int currVertexID = gl_VertexID % 6;
    int subIndex = (index / 4);
    int shift = index - subIndex * 4;

    vec3 bufferPos = unpack3(index * 3);
    vec3 bufferPosOld = unpack3(blockSize * 3 + index * 3);
    vec2 texUV = unpack2(blockSize * 6 + index * 2);

    uint packedLight = particleData[blockSize * 8 + subIndex];
    float blocklight = bitfieldExtract(packedLight, shift * 8 + 4, 4) * 1/16f;
    float skylight = bitfieldExtract(packedLight, shift * 8, 4) * 1/16f;
    vec2 lUV = vec2(blocklight, skylight);

    /*vec3 bufferPos = vec3(particleData[index], particleData[index + 1], particleData[index + 2]);
    vec3 bufferPosOld = vec3(particleData[index + 3], particleData[index + 4], particleData[index + 5]);
    vec2 texUV = vec2(particleData[index + 6], particleData[index + 7]);
    vec2 lUV = vec2(particleData[index + 8], particleData[index + 9]);*/

    vec3 lerpPos = lerp_vec(bufferPosOld, bufferPos);
    vec3 faceVecRaw = facePos[indices[currVertexID]];
    vec2 tempUV = faceVecRaw.xy;

    float scale = 0.5F;
    vec4 faceVec = vec4(faceVecRaw * scale, 1.0);
    vec4 centerOffset = vec4(0.5, 0.5, 0, 0) * scale;

    /*vec4 off = vec4(sin(lerpPos.y + faceVec.y), 0, 0, 0);*/
    vec3 position = lerpPos + ((faceVec /*+ off*/ - centerOffset) * rotationX(-radians(camRotX)) * rotationY(radians(180 - camRot))).xyz;

    /*
    --------------------WEATHER STUFF--------------------
    */
    //float tilt = (length(bufferPos - bufferPosOld) / 2.0) * 90;
    //float camRotFixed = camRot + 360;
    //float pi = 3.14159265359;
    //float cameraBasedTilt = clamp(tan((1.0 / 2*pi) * radians(camRotFixed - windDirection)), -1, 1);
    //vec3 position = lerpPos + ((faceVec /*+ off*/ - centerOffset) * rotationX(radians(cameraBasedTilt * tilt)) * rotationY(radians(180 - camRot))).xyz;
    /*
    --------------------WEATHER STUFF--------------------
    */

    gl_Position = clip * vec4(position + camera, 1.0);

    uv = texUV + (1 - tempUV) * 1/8f;
    lightUV = lUV + tempUV * 1/16f + inset[indices[currVertexID]].xy;
    faceuv = tempUV;
}
