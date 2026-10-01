#version 150 core

in vec2 vTexCoord;

uniform sampler2D uTexture;
uniform vec2 uTexelSize;
uniform int uDirection;
uniform vec2 uUVScale;
uniform float uRadius;

out vec4 FragColor;

void main() {
    vec2 offset = (uDirection == 0 ? vec2(uTexelSize.x, 0.0) : vec2(0.0, uTexelSize.y)) * uRadius;

    vec2 uv = vTexCoord * uUVScale;
    vec4 result = texture(uTexture, uv) * 0.2270270270;
    result += texture(uTexture, uv + offset * 1.0) * 0.1945945946;
    result += texture(uTexture, uv - offset * 1.0) * 0.1945945946;
    result += texture(uTexture, uv + offset * 2.0) * 0.1216216216;
    result += texture(uTexture, uv - offset * 2.0) * 0.1216216216;
    result += texture(uTexture, uv + offset * 3.0) * 0.0540540541;
    result += texture(uTexture, uv - offset * 3.0) * 0.0540540541;
    result += texture(uTexture, uv + offset * 4.0) * 0.0162162162;
    result += texture(uTexture, uv - offset * 4.0) * 0.0162162162;

    FragColor = result;
}