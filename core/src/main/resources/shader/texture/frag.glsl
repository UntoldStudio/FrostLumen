#version 150 core

in vec2 vTexCoord;
in vec4 vColor;

uniform sampler2D uTexture;

out vec4 FragColor;

void main() {
    vec4 texel = texture(uTexture, vTexCoord);
    FragColor = texel * vColor;
}