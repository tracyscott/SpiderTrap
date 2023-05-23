/*{
	"DESCRIPTION": "Donut Time",
	"CREDIT": "by tracyscott",
	"ISFVSN": "2.0",
	"CATEGORIES": [
		"VERTEX SDF"
	],
	"INPUTS": [
		{
         	"NAME": "fScale",
         	"TYPE": "float",
         	"DEFAULT": 1.0,
         	"MIN": 0.0,
         	"MAX": 10.0
         },
         {
            "NAME": "xOffset",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -30.0,
            "MAX": 30.0
         },
         {
            "NAME": "yOffset",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -30.0,
            "MAX": 30.0
         }
	]
}*/

#version 330

uniform float fTime;
uniform float fScale;
uniform float xOffset;
uniform float yOffset;

layout(location = 0) in vec3 position;
out vec3 tPosition;

float sdTorus( vec3 p, vec2 t )
{
  vec2 q = vec2(length(p.xz)-t.x,p.y);
  return length(q)-t.y;
}

// Rotation matrix around the X axis.
mat3 rotateX(float theta) {
    float c = cos(theta);
    float s = sin(theta);
    return mat3(
        vec3(1, 0, 0),
        vec3(0, c, -s),
        vec3(0, s, c)
    );
}

// Rotation matrix around the Y axis.
mat3 rotateY(float theta) {
    float c = cos(theta);
    float s = sin(theta);
    return mat3(
        vec3(c, 0, s),
        vec3(0, 1, 0),
        vec3(-s, 0, c)
    );
}

// Rotation matrix around the Z axis.
mat3 rotateZ(float theta) {
    float c = cos(theta);
    float s = sin(theta);
    return mat3(
        vec3(c, -s, 0),
        vec3(s, c, 0),
        vec3(0, 0, 1)
    );
}

void main(){
    vec3 pos = position;
    pos.y = pos.y - 19.0 + yOffset; // * cos(fTime);
    pos.x = pos.x + xOffset;
    pos = pos / fScale;
    pos = pos * rotateZ(fTime);
    vec2 t = vec2(1.5, 0.5);
    float distance = sdTorus(pos, t);
	tPosition = vec3(distance, distance, distance);
}
