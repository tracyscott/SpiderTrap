/*{
	"DESCRIPTION": "Squigglies",
	"CREDIT": "by tracyscott",
	"ISFVSN": "2.0",
	"CATEGORIES": [
		"VERTEX SDF"
	],
	"INPUTS": [
         {
            "NAME": "x1",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -2.0,
            "MAX": 8.0
         },
         {
            "NAME": "y1",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -2.0,
            "MAX": 8.0
         },
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
            "NAME": "radius",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -4.0,
            "MAX": 4.0
         },
         {
            "NAME": "thick",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": 0.0,
            "MAX": 1.0
         },
          {
            "NAME": "brt",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": .1,
            "MAX": 20.
         },
         {
            "NAME": "palval",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": 0.0,
            "MAX": 9.9
         },
          {
            "NAME": "pw",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.0,
            "MAX": 5.0
         }
	]
}*/

#version 330

uniform float fTime;
uniform float x1;
uniform float y1;
uniform float s1;
uniform float s2;
uniform float radius;
uniform float thick;
uniform float zoom;
uniform float brt;
uniform float palval;
uniform float pald;
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

float ring(vec2 ruv3) {
    float d = HexDist(ruv3); //length(ruv3);
    d -= radius;

    d = abs(d)-thick;
    d = smoothstep(s1, s2, d);
    d = pow(brt*0.01/d, 1.3);
    return clamp(d, 0., 1.);
}

mat2 Rot(float a) {
    float s=sin(a), c=cos(a);
    return mat2(c, -s, s, c);
}

void main(){
    vec2 uv = position.xz - 0.5;
    vec3 color = vec3(1., 1., 1.);

    float x = x1;
    float y = y1;

    //uv = uv * 2.0 - 1.0;
    //uv *= 4.0;

    //uv.x -= x;
    //uv.y -= y;

    float center_dist = length(vec2(x, y));
    float radius_scale = 1.0 - center_dist;
    // Time varying pixel color
    vec3 col = vec3(0.);
    // Output to screen

    float r = 0.2;
    float speed =20.0;
    float speedy = 20.0;
    float ampx = 0.06;
    float ampy = 0.09;
    float smoothval = 0.01;
    float xfreq = 45.28;
    float yfreq = 30.28;
    float dynradius = mix(-0.5, 0.36, 1.- length(vec2(x,y)));
    float d = 1.0 - (length(uv - vec2(x, y)) - radius);
    d = length(uv - vec2(x, y)) - dynradius;
    d += ampx * (0.5 * 0.5*sin(xfreq * uv.x + fTime * speed));
    d += (ampx/2.) * (0.5 * 0.5*sin(2. * xfreq * uv.x + fTime * speed));
    d += ampy * (0.5 * 0.5*sin(yfreq * uv.y + fTime * speedy));
    d = abs(d) - thick;

    d = smoothstep(s1, s2, d);
    d = 1.0-d;

    d = clamp(d, 0.0, 1.0);
    float bright = (0.1 * brt)/(1. - d);
    bright = clamp(bright, 0.0, 1.0);
    bright = pow(bright, pw);
    color *= vec3(clamp(paletteN(bright, palval)*bright, 0., 1.));

    //col = vec3(d);

    tPosition = color;
}
