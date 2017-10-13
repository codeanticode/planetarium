#define PI 3.1415926535897932384626433832795

precision highp float;
precision highp int;

uniform samplerCube cubemap;
uniform float aperture;

varying vec2 vertTexCoord;

vec2 domeXYToLatLon(vec2 xy, float aperture) {
  float x = xy.x - 0.5;
  float y = xy.y - 0.5;
  float lat = sqrt(x*x + y*y) * aperture;
  float lon = atan(y,x);
  return vec2(lat, lon);
}

vec3 latLonToXYZ(vec2 latLon) {
  float lat = latLon.x;
  float lon = latLon.y;
  float x = cos(lon) * sin(lat);
  float y = sin(lon) * sin(lat);
  float z = cos(lat);
  return vec3(x,y,z);
}

vec3 domeXYToXYZ(vec2 xy, float aperture) {
  return latLonToXYZ(domeXYToLatLon(xy, aperture));
}

void main() {
  vec3 ray = domeXYToXYZ(vertTexCoord, aperture*PI);
  //vec3 rgb = ray * 0.5 + vec3(0.5); //DEBUG
  vec3 color = vec3(textureCube(cubemap, ray));
  gl_FragColor = vec4(color, 1.0);
}
