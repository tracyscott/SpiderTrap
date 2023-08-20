/*{
	"DESCRIPTION": "LightPosRGB",
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
            "MAX": 2.0
         },
         {
            "NAME": "y1",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -2.0,
            "MAX": 2.0
         },
         {
            "NAME": "brt",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": 0.0,
            "MAX": 2.0
         },
         {
            "NAME": "pw",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": 0.1,
            "MAX": 20.0
         },
         {
            "NAME": "r",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.0,
            "MAX": 1.0
         },
         {
            "NAME": "g",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.0,
            "MAX": 1.0
         },
         {
            "NAME": "b",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.0,
            "MAX": 1.0
         },
         {
            "NAME": "sspeed",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.0,
            "MAX": 10.0
         }
	]
}*/

#version 330

uniform float fTime;
uniform float x1;
uniform float y1;
uniform float brt;
uniform float pw;
uniform float r;
uniform float g;
uniform float b;
uniform float sspeed;

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

vec2 Hash12(float t) {
    float x = fract(sin(t*674.3)*453.2);
    float y = fract(sin((t+x)*711.3)*261.2);
    return vec2(x, y);
}

vec2 Hash12_Polar(float t) {
    float a = fract(sin(t*674.3)*453.2)*6.2832;
    float d = fract(sin((t+a)*711.3)*261.2);
    return vec2(sin(a), cos(a))*d;
}

float Light(vec2 uv) {
    float d = length(uv);
    float m = (0.05*brt)/d;
    m = pow(m, pw);
    //m *= smoothstep(1., .2, d);
    return m;
}


void main(){
    vec2 uv = position.xz - 0.5;

    vec3 col = vec3(0.);

    vec3 color = (.75 + .25 * sin(4.*vec3(.34, .54, .43)));
    vec2 offs = vec2(x1, y1);
    float bright = clamp(Light(uv-offs), 0.0, 1.0);
    float osc = .25*sin(fTime*1.)+.5;
    col = vec3(bright, bright, bright);
    col *= vec3(r, g, b);
    //col = paletteN(bright, pal)*bright;
    tPosition = clamp(col, 0.0, 1.0);
}
