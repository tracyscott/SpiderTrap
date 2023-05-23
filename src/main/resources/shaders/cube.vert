/*{
	"DESCRIPTION": "Boxed frame",
	"CREDIT": "by tracyscott",
	"ISFVSN": "2.0",
	"CATEGORIES": [
		"VERTEX SDF"
	],
	"INPUTS": [
	    {
         	"NAME": "b",
         	"TYPE": "float",
         	"DEFAULT": 1.0,
         	"MIN": 0.0,
         	"MAX": 2.0
         },
         {
         	"NAME": "e",
         	"TYPE": "float",
         	"DEFAULT": 1.0,
         	"MIN": 0.0,
         	"MAX": 2.0
         },
		{
         	"NAME": "fScale",
         	"TYPE": "float",
         	"DEFAULT": 1.0,
         	"MIN": 0.0,
         	"MAX": 4.0
         },
         {
            "NAME": "xOffset",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -3.0,
            "MAX": 3.0
         },
         {
            "NAME": "yOffset",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -3.0,
            "MAX": 3.0
         },
         {
            "NAME": "zOffset",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -3.0,
            "MAX": 3.0
         },
         {
            "NAME": "h",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": 0.0,
            "MAX": 1.0
         },
         {
            "NAME": "r",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": 0.0,
            "MAX": 1.0
         }
	]
}*/

#version 330

uniform float fTime;
uniform float fScale;
uniform float xOffset;
uniform float yOffset;
uniform float zOffset;
uniform float h;
uniform float r;
uniform float b;
uniform float e;

layout(location = 0) in vec3 position;
out vec3 tPosition;

// A bunch of mathematical constants

// The base of natural logarithms (e)
const float M_E = 2.71828182845904523536028747135266250;

// The logarithm to base 2 of M_E (log2(e))
const float M_LOG2E = 1.44269504088896340735992468100189214;

// The logarithm to base 10 of M_E (log10(e))
const float M_LOG10E = 0.434294481903251827651128918916605082;

// The natural logarithm of 2 (loge(2))
const float M_LN2 = 0.693147180559945309417232121458176568;

// The natural logarithm of 10 (loge(10))
const float M_LN10 = 2.30258509299404568401799145468436421;

// Pi, the ratio of a circle's circumference to its diameter.
const float M_PI = 3.14159265358979323846264338327950288;
const float PI = M_PI;

// Pi divided by two (pi/2)
const float M_PI_2 = 1.57079632679489661923132169163975144;

// Pi divided by four  (pi/4)
const float M_PI_4 = 0.785398163397448309615660845819875721;

// The reciprocal of pi (1/pi)
const float M_1_PI = 0.318309886183790671537767526745028724;

// Two times the reciprocal of pi (2/pi)
const float M_2_PI = 0.636619772367581343075535053490057448;

// Two times the reciprocal of the square root of pi (2/sqrt(pi))
const float M_2_SQRTPI = 1.12837916709551257389615890312154517;

// The square root of two (sqrt(2))
const float M_SQRT2 = 1.41421356237309504880168872420969808;

// The reciprocal of the square root of two (1/sqrt(2))
const float M_SQRT1_2 = 0.707106781186547524400844362104849039;

// 1 degree in radians
const float degree = 180.0/M_PI;

// 1 radian in degrees
const float radian = M_PI/180.0;

float sdTorus( vec3 p, vec2 t )
{
    vec2 q = vec2(length(p.xz)-t.x,p.y);
    return length(q)-t.y;
}

float stroke(float x, float s, float w) {
    float d = step(s, x+w*.5)
    - step(s,x-w*.5);
    return clamp(d, 0., 1.);
}

float circleSDF(vec2 st) {
    return length(st-.5)*2.;
}

float circleSDF2(vec2 st, float size) {
    return length(st) - size;
}

float fill(float x, float size) {
    return 1.-step(size, x);
}

float sdVerticalCapsule( vec3 p, float h, float r )
{
    p.y -= clamp( p.y, 0.0, h );
    return length( p ) - r;
}

float sdBox( vec3 p, vec3 b )
{
    vec3 q = abs(p) - b;
    return length(max(q,0.0)) + min(max(q.x,max(q.y,q.z)),0.0);
}

float sdBoxFrame( vec3 p, vec3 b, float e )
{
    p = abs(p  )-b;
    vec3 q = abs(p+e)-e;
    return min(min(
    length(max(vec3(p.x,q.y,q.z),0.0))+min(max(p.x,max(q.y,q.z)),0.0),
    length(max(vec3(q.x,p.y,q.z),0.0))+min(max(q.x,max(p.y,q.z)),0.0)),
    length(max(vec3(q.x,q.y,p.z),0.0))+min(max(q.x,max(q.y,p.z)),0.0));
}

float opOnion( in float sdf, in float thickness )
{
    return abs(sdf)-thickness;
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
    float distance = 0.;
    {
        vec3 p1 = pos;
        p1.y = p1.y + yOffset;// * cos(fTime);
        //p1.x = p1.x + xOffset;
        //p1.z = p1.z + zOffset;
        p1 = p1 / fScale;
        p1 = p1 * rotateY(fTime);

        //distance = sdBoxFrame(p1, vec3(0.5,0.3,0.5), e);
        vec3 dim = vec3(b, e, h);
        distance = sdBox(p1, dim);
    }
    {
        vec3 p2 = pos;

    }

    tPosition = vec3(distance, distance, distance);
}
