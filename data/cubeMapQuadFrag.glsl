//grid code originally appears: https://github.com/diroru/domemod
#define PI 3.1415926535897932384626433832795
#define AA 0.1

uniform samplerCube cubemap;
uniform float aperture;
uniform float renderGrid;

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

float rad2Deg(float r) {
  return r * 180.0 / PI;
}

vec3 getLatitudeGrid(vec2 longLat, float gratOffset, float gratWidth, vec3 gratColour) {
	float gr = mod(rad2Deg(longLat.y) + gratWidth * 0.5, gratOffset) - gratWidth * 0.5;
	//return mix(gratColour, vec3(0.0), smoothstep(gratWidth*0.5 - AA, gratWidth*0.5 + AA, abs(gr)));
	return mix(gratColour, vec3(0.0), step(gratWidth, abs(gr)));
}

vec3 getLongtitudeGrid(vec2 longLat, float gratOffset, float gratWidth, vec3 gratColour) {
	float longDeg = rad2Deg(longLat.x);
	float latDeg = rad2Deg(longLat.y);
	float go = gratWidth / sin(longLat.y);
	float gr = mod(longDeg + go , gratOffset) - go;
	//return mix(gratColour, vec3(0.0), smoothstep(go*0.5 - AA, go*0.5 + AA, abs(gr)));
	return mix(gratColour, vec3(0.0), step(go, abs(gr)));
}

vec3 getGrid(vec2 longLat, vec3 colour, float gratOffset0, float gratWidth0, float gratOffset1, float gratWidth1) {
	vec3 longGrid_0 = getLongtitudeGrid(longLat, gratOffset0, gratWidth0, colour);
  	vec3 longGrid_1 = getLongtitudeGrid(longLat, gratOffset1, gratWidth1, colour);
	vec3 latGrid_0 = getLatitudeGrid(longLat, gratOffset0, gratWidth0, colour);
  	vec3 latGrid_1 = getLatitudeGrid(longLat, gratOffset1, gratWidth1, colour);
	vec3 grid_rgb = longGrid_0 + longGrid_1 + latGrid_0 + latGrid_1;
	//grid_rgb = longGrid_0;
	//TODO eg. vec3(0.0) as const
	return clamp(grid_rgb, vec3(0.0), vec3(1.0));
	//return grid_rgb;
}



void main() {
  //vec3 color = vec3(textureCube(cubemap, vec3(vertTexCoord,1.0)));
  vec2 latLon = domeXYToLatLon(vertTexCoord, aperture*PI);
  vec3 ray = latLonToXYZ(latLon);
  //vec3 color = ray * 0.5 + vec3(0.5);
  vec3 color = vec3(textureCube(cubemap, ray));

  vec3 gridColor = getGrid(latLon.yx, vec3(1.0, 1.0, 0.0), 45.0, 0.6, 15.0, 0.2);
//  float gridX = 1.0 - smoothstep(gridWeight*0.5-AA, gridWeight*0.5+AA, mod(latLon.x - gridWeight*0.5, gridSize.x));
//  float gridY = 1.0 - smoothstep(gridWeight*0.5-AA, gridWeight*0.5+AA, mod(latLon.y - gridWeight*0.5, gridSize.y));

  gl_FragColor = vec4(mix(color, gridColor, min(length(gridColor)*0.5, renderGrid)), 1.0);
  //gl_FragColor = vec4(rgb + gridColor, 1.0);
}
