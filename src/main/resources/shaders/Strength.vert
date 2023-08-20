/*{
	"DESCRIPTION": "Strength",
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
            "MIN": -1.1,
            "MAX": 1.1
         },
         {
            "NAME": "y1",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -0.1,
            "MAX": 1.1
         },
         {
            "NAME": "freq",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": 0,
            "MAX": 10.0
         },
         {
            "NAME": "zoom",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": 0.1,
            "MAX": 10.0
         },
          {
            "NAME": "pal",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": 0.0,
            "MAX": 9.5
         }
	]
}*/

#version 330

uniform float fTime;
uniform float x1;
uniform float y1;
uniform float freq;
uniform float zoom;
uniform float pal;

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

// http://dev.thi.ng/gradients/
vec3 palette(in float t, in vec3 a, in vec3 b, in vec3 c, in vec3 d)
{
    return a + b*cos(6.28318* (c*t + d));
}


vec3 palette0(float t) {
    vec3 a = vec3(0.5, 0.5, 0.5);
    vec3 b = vec3(0.5, 0.5, 0.5);
    vec3 c = vec3(1.0, 1.0, 1.0);
    vec3 d = vec3(0.263, 0.416, 0.557);
    return palette(t, a, b, c, d);
}

// orange green blue pink
//[[0.572 0.574 0.518] [0.759 0.171 0.358] [1.022 0.318 0.620] [3.138 5.671 -0.172]]
vec3 palette1(float t) {
    vec3 a = vec3(0.572, 0.574, 0.518);
    vec3 b = vec3(0.759, 0.171, 0.358);
    vec3 c = vec3(1.022, 0.318, 0.620);
    vec3 d = vec3(3.138, 5.5671, -0.172);
    return palette(t, a, b, c, d);
}

// orange green blue pink
//[[0.572 0.574 0.518] [0.759 0.171 0.358] [1.022 0.318 0.620] [3.138 5.671 -0.172]]
vec3 palette2(float t) {
    vec3 a = vec3(0.572, 0.574, 0.518);
    vec3 b = vec3(0.759, 0.171, 0.358);
    vec3 c = vec3(1.022, 0.318, 0.620);
    vec3 d = vec3(3.138, 5.5671, -0.172);
    return palette(t, a, b, c, d);
}

// [[0.806 0.355 0.693] [0.802 0.464 0.260] [1.514 1.131 1.197] [1.015 0.738 3.202]]
vec3 palette3(float t) {
    vec3 a = vec3(0.806, 0.355, 0.693);
    vec3 b = vec3(0.802, 0.464, 0.260);
    vec3 c = vec3(1.514, 1.131, 1.197);
    vec3 d = vec3(1.015, 0.783, 3.202);
    return palette(t, a, b, c, d);
}

// orange green blue pink
//[[0.572 0.574 0.518] [0.759 0.171 0.358] [1.022 0.318 0.620] [3.138 5.671 -0.172]]
vec3 palette4(float t) {
    vec3 a = vec3(0.572, 0.574, 0.518);
    vec3 b = vec3(0.759, 0.171, 0.358);
    vec3 c = vec3(1.022, 0.318, 0.620);
    vec3 d = vec3(3.138, 5.5671, -0.172);
    return palette(t, a, b, c, d);
}

// orange green blue pink
//[[0.572 0.574 0.518] [0.759 0.171 0.358] [1.022 0.318 0.620] [3.138 5.671 -0.172]]
vec3 palette5(float t) {
    vec3 a = vec3(0.572, 0.574, 0.518);
    vec3 b = vec3(0.759, 0.171, 0.358);
    vec3 c = vec3(1.022, 0.318, 0.620);
    vec3 d = vec3(3.138, 5.5671, -0.172);
    return palette(t, a, b, c, d);
}

// orange green blue pink
//[[0.572 0.574 0.518] [0.759 0.171 0.358] [1.022 0.318 0.620] [3.138 5.671 -0.172]]
vec3 palette6(float t) {
    vec3 a = vec3(0.572, 0.574, 0.518);
    vec3 b = vec3(0.759, 0.171, 0.358);
    vec3 c = vec3(1.022, 0.318, 0.620);
    vec3 d = vec3(3.138, 5.5671, -0.172);
    return palette(t, a, b, c, d);
}

// orange green blue pink
//[[0.572 0.574 0.518] [0.759 0.171 0.358] [1.022 0.318 0.620] [3.138 5.671 -0.172]]
vec3 palette7(float t) {
    vec3 a = vec3(0.572, 0.574, 0.518);
    vec3 b = vec3(0.759, 0.171, 0.358);
    vec3 c = vec3(1.022, 0.318, 0.620);
    vec3 d = vec3(3.138, 5.5671, -0.172);
    return palette(t, a, b, c, d);
}

// orange green blue pink
//[[0.572 0.574 0.518] [0.759 0.171 0.358] [1.022 0.318 0.620] [3.138 5.671 -0.172]]
vec3 palette8(float t) {
    vec3 a = vec3(0.572, 0.574, 0.518);
    vec3 b = vec3(0.759, 0.171, 0.358);
    vec3 c = vec3(1.022, 0.318, 0.620);
    vec3 d = vec3(3.138, 5.5671, -0.172);
    return palette(t, a, b, c, d);
}

// orange green blue pink
//[[0.572 0.574 0.518] [0.759 0.171 0.358] [1.022 0.318 0.620] [3.138 5.671 -0.172]]
vec3 palette9(float t) {
    vec3 a = vec3(0.572, 0.574, 0.518);
    vec3 b = vec3(0.759, 0.171, 0.358);
    vec3 c = vec3(1.022, 0.318, 0.620);
    vec3 d = vec3(3.138, 5.5671, -0.172);
    return palette(t, a, b, c, d);
}



// which palette to use.
vec3 paletteN(in float t, in float pal_num) {
    pal_num = floor(pal_num);
    if (pal_num == 0.)
    return palette0(t);
    if (pal_num == 1.)
    return palette1(t);
    if (pal_num == 2.)
    return palette2(t);
    if (pal_num == 3.)
    return palette3(t);
    if (pal_num == 4.)
    return palette4(t);
    if (pal_num == 5.)
    return palette5(t);
    if (pal_num == 6.)
    return palette6(t);
    if (pal_num == 7.)
    return palette7(t);
    if (pal_num == 8.)
    return palette8(t);
    if (pal_num == 9.)
    return palette9(t);
    return palette0(t);
}


float rectSDF(vec2 st, vec2 s) {
    st = st*2.-1.;
    return max(abs(st.x/s.x),
    abs(st.y/s.y));
}

mat2 Rot(float a) {
    float s=sin(a), c=cos(a);
    return mat2(c, -s, s, c);
}

float HexDist(vec2 p) {
    p = abs(p);
    float c = dot(p, normalize(vec2(1,1.73)));
    c = max(c, p.x);
    return c;
}



void main(){
    vec2 uv = position.xz - 0.5;
    vec3 col = vec3(0.);

    float od = HexDist(uv);

    //uv = uv * Rot(fTime * -2.);
    uv *= 4.*zoom;
    uv = fract(uv);
    vec2 gid = floor(uv + 0.5);
    vec2 odd = mod(gid, 2.0);
    float rotateDir = 0.5 - gid.x;
    rotateDir *= -1.;
   // if (odd.x > 0.0)
    //    rotateDir = -1.0;
    //uv -= 0.5;
    vec2 uv2 = uv * Rot(rotateDir * fTime * 10.);
    float bright = smoothstep(0.2, x1 + .5 + cos(uv2.y*PI*freq)*.25, uv2.x);
    float osc = 1.; // 25*sin(fTime*1.)+.5;
    float pal_val = 0.5 * sin(od*5.0*fTime) + .5;
    //col = paletteN(bright*(osc), pal)*bright;
    float intensity = 0.05/(1.01 - bright);
    col = paletteN(pal_val, pal)*bright;

    tPosition = clamp(col, 0.0, 1.0);
}
