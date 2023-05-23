/*{
	"DESCRIPTION": "Squishy Sphere",
	"CREDIT": "by tracyscott",
	"ISFVSN": "2.0",
	"CATEGORIES": [
		"VERTEX SDF"
	],
	"INPUTS": [
	    {
             "NAME": "xScale",
             "TYPE": "float",
             "DEFAULT": 1.0,
             "MIN": 0.0,
             "MAX": 10.0
        },
        {
             "NAME": "yScale",
             "TYPE": "float",
             "DEFAULT": 1.0,
             "MIN": 0.0,
             "MAX": 10.0
        },
		{
         	"NAME": "zScale",
         	"TYPE": "float",
         	"DEFAULT": 1.0,
         	"MIN": 0.0,
         	"MAX": 10.0
         },
         {
            "NAME": "xOffset",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -50.0,
            "MAX": 50.0
         },
         {
            "NAME": "yOffset",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -50.0,
            "MAX": 50.0
         },
         {
            "NAME": "zOffset",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -50.0,
            "MAX": 50.0
         },
         {
            "NAME": "disFreq",
            "TYPE": "float",
            "DEFAULT": 20.0,
            "MIN": 0.0,
            "MAX": 40.0
         },
         {
            "NAME": "dScale",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.0,
            "MAX": 2.0
         }
	]
}*/

#version 330

uniform float fTime;
uniform float zScale;
uniform float xScale;
uniform float yScale;
uniform float xOffset;
uniform float yOffset;
uniform float zOffset;
uniform float disFreq;
uniform float dScale;



layout(location = 0) in vec3 position;


out vec3 tPosition;


float sdSphere( vec3 p, float s )
{
  return length(p)-s;
}

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

    pos.y = pos.y - 19.0 - yOffset; // * cos(fTime);
    pos.x = pos.x - xOffset;
    pos.z = pos.z - zOffset;
    pos = pos * rotateX(fTime);
    pos = pos / 10.0;
    pos.z = pos.z / (zScale/5.0);
    pos.x = pos.x / (xScale/5.0);
    pos.y = pos.y / (yScale/5.0);

    float distance = sdSphere(pos, 1.0);
    float displacement = sin(disFreq * position.x) * sin(disFreq * position.y) * sin(disFreq * position.z);
    distance = distance + displacement * dScale;
	tPosition = vec3(distance, distance, distance);
}
