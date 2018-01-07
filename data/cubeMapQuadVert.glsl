precision highp float;
precision highp int;

uniform mat4 transform;
uniform mat4 modelview;
uniform mat3 normalMatrix;
attribute vec4 vertex;
attribute vec3 normal;
attribute vec2 texCoord;

varying vec2 vertTexCoord;

void main() {
  gl_Position = transform * vertex;
  vertTexCoord = texCoord;
}
