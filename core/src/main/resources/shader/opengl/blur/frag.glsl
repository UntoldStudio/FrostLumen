#version 150 core

in vec2 vTexCoord;

uniform sampler2D uTexture;
uniform vec2 uTexelSize;
uniform int uDirection;
uniform vec2 uUVScale;
uniform float uRadius;
out vec4 FragColor;

void main() {
    vec2 texelDir = (uDirection == 0 ? vec2(uTexelSize.x, 0.0) : vec2(0.0, uTexelSize.y));

    float sigma = max(uRadius * 0.5, 0.1);
    float twoSigma2 = 2.0 * sigma * sigma;

    vec2 uv = vTexCoord * uUVScale;
    vec2 halfTexel = uTexelSize * 0.5;
    vec2 minUV = halfTexel;
    vec2 maxUV = uUVScale - halfTexel;

    vec4 result = texture(uTexture, clamp(uv, minUV, maxUV));
    float weightSum = 1.0;

    for (int i = 1; i <= int(uRadius); i++) {
        float w = exp(-float(i * i) / twoSigma2);
        vec2 offset = texelDir * float(i);
        result += texture(uTexture, clamp(uv + offset, minUV, maxUV)) * w;
        result += texture(uTexture, clamp(uv - offset, minUV, maxUV)) * w;
        weightSum += 2.0 * w;
    }

    FragColor = vec4(result.rgb / weightSum, 1.0);
}