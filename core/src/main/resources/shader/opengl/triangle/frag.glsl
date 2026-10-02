#version 150 core

in vec4 vColor;

uniform float uViewportHeight;
uniform int uScissorPlaneCount;
uniform vec3 uScissorPlanes[32];

out vec4 FragColor;

void main() {
    if (uScissorPlaneCount > 0) {
        vec2 p = vec2(gl_FragCoord.x, uViewportHeight - gl_FragCoord.y);
        for (int i = 0; i < uScissorPlaneCount; i++) {
            vec3 pl = uScissorPlanes[i];
            if (pl.x * p.x + pl.y * p.y + pl.z < 0.0) {
                discard;
            }
        }
    }

    FragColor = vColor;
}