#version 150 core

in vec2 vTexCoord;
in vec4 vColor;

uniform sampler2D uTexture;

out vec4 FragColor;

void main() {
    float coverage = texture(uTexture, vTexCoord).r;
    FragColor = vec4(vColor.rgb, vColor.a * coverage);
}