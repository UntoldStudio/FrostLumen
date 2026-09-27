#version 150 core

in ivec2 aPos;
in vec4 aColor;

uniform mat4 uProjection;

out vec4 vColor;

void main() {
    gl_Position = uProjection * vec4(aPos, 0.0, 1.0);
    vColor = aColor;
}