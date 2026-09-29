#version 150 core

in ivec2 aScreenPos;
in vec2 aTexCoord;
in vec4 aColor;

uniform mat4 uProjection;

out vec2 vTexCoord;
out vec4 vColor;

void main() {
    gl_Position = uProjection * vec4(vec2(aScreenPos), 0.0, 1.0);

    vTexCoord = aTexCoord;
    vColor = aColor;
}