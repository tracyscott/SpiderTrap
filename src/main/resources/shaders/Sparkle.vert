/*{
	"DESCRIPTION": "LRings",
	"CREDIT": "by tracyscott",
	"ISFVSN": "2.0",
	"CATEGORIES": [
		"VERTEX SDF"
	],
	"INPUTS": [
         {
            "NAME": "s1",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -1.1,
            "MAX": 1.1
         },
         {
            "NAME": "s2",
            "TYPE": "float",
            "DEFAULT": 0.1,
            "MIN": -1.1,
            "MAX": 1.1
         },
         {
            "NAME": "x",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -4.0,
            "MAX": 4.0
         },
         {
            "NAME": "y",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -4.0,
            "MAX": 4.0
         },
         {
            "NAME": "zoom",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.1,
            "MAX": 4.0
         },
          {
            "NAME": "brt",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": .1,
            "MAX": 20.
         },
          {
            "NAME": "pw",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.0,
            "MAX": 20.0
         }
	]
}*/

#version 330

uniform float fTime;
uniform float s1;
uniform float s2;
uniform float x;
uniform float y;
uniform float zoom;
uniform float brt;
uniform float pw;

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

float opOnion( in float sdf, in float thickness )
{
    return abs(sdf)-thickness;
}

float rectSDF(vec2 st, vec2 s) {
    st = st*2.-1.;
    return max(abs(st.x/s.x),
    abs(st.y/s.y));
}

// INSERT-PALETTES


float HexDist(vec2 p) {
    p = abs(p);
    float c = dot(p, normalize(vec2(1,1.73)));
    c = max(c, p.x);
    return c;
}

mat2 Rot(float a) {
    float s=sin(a), c=cos(a);
    return mat2(c, -s, s, c);
}

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

float getDegree(vec2 position, float offset, float om, float time) {
    float degree = atan(position.y, position.x) + offset;
    degree = (degree + M_PI) / M_PI;
    degree = fract(time / 5.0 + degree / 2.0 + om);
    //degree = degree > 0.5 ? 1.0 - (degree - 0.5) * 2.0: degree * 2.0;
    //degree = degree > 0.5 ? 1.0 - (degree - 0.5) * 2.0: degree * 2.0;
    degree = sin(degree) * cos(degree);
    return degree;
}


void main(){
    vec2 uv = position.xz - 0.5;
    uv *= 2.0;

    float power = s1 * 3.0;
    float powermul = pow(10.0, power);
    float mul = 3.0 * powermul;

    vec2 pos = position.xz;  // ported from shader toy, where position was 0 to 1. and uv is -1 to 1.

    pos = fract(pos * mul); // + fract(-position * mul)) / 2.0;
    pos.x -= x;
    pos.y -= y;
    pos *= zoom;
    pos -= 0.5;
    pos *= 30.0;  // 30.0

    // With these commented out, there are interesting negative spaces movement
    pos.x = pos.x > 0.5 ? 1.0 - pos.x : pos.x;
    pos.y = pos.y > 0.5 ? 1.0 - pos.y : pos.y;

    float inverseDensity = 0.1;
    float om = getDegree(uv, inverseDensity * pow(length(uv), 1.0) * 1.0, 0.0, 0.0);
    float v = 0.0;
    vec3 color = vec3(0.0);
    int xx = 3;
    int yy = 3;
    float c = float(xx * yy);
    for (int x = 0; x < xx; x++) {
        for (int y = 0; y < yy; y++) {
            float th = 1.5 * M_PI * float(x * yy + y) / c;
            vec2 p = pos * mat2(cos(th), -sin(th), -sin(th), cos(th));
            p += vec2(float(x), float(y)) / vec2(float(xx), float(yy));
            v = getDegree(p, 0.0, om, fTime * s2 * 5.0);

            vec2 co = floor(uv * mul) * float(x * yy + y) / c; // floor(uv * mul) * float(x * yy + y) / c;
            float r = rand(co * 13.342354);
            float g = rand(co * 4324.23423432);
            float b = rand(co * 14.314);

            color += vec3(r, g, b) / (v * c * 20.0);
        }
    }

    tPosition = color;
}
