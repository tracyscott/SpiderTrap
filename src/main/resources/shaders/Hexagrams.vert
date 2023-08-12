/*{
	"DESCRIPTION": "Hexagrams",
	"CREDIT": "by tracyscott",
	"ISFVSN": "2.0",
	"CATEGORIES": [
		"VERTEX SDF"
	],
	"INPUTS": [
         {
            "NAME": "rspeed",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": -5.0,
            "MAX": 5.0
         },
         {
            "NAME": "layers",
            "TYPE": "float",
            "DEFAULT": 4.0,
            "MIN": 1.0,
            "MAX": 10.0
         },
         {
            "NAME": "brt",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.1,
            "MAX": 5.0
         },
         {
            "NAME": "pal",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.0,
            "MAX": 9.5
         },
          {
            "NAME": "pald",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.0,
            "MAX": 30.0
         },
         {
            "NAME": "s1",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": 0.0,
            "MAX": 1.0
         },
         {
            "NAME": "s2",
            "TYPE": "float",
            "DEFAULT": 0.2,
            "MIN": 0.0,
            "MAX": 1.0
         },
           {
            "NAME": "cspeed",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.0,
            "MAX": 10.0
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
uniform float rspeed;
uniform float layers;
uniform float brt;
uniform float pal;
uniform float pald;
uniform float s1;
uniform float s2;
uniform float cspeed;
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

mat2 Rot(float a) {
    float s=sin(a), c=cos(a);
    return mat2(c, -s, s, c);
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

// neon green red purple
// [[0.846 0.430 0.206] [0.349 0.678 0.651] [0.690 1.319 0.654] [6.205 2.511 3.523]]
vec3 palette2(float t) {
    vec3 a = vec3(0.846, 0.430, 0.206);
    vec3 b = vec3(0.349, 0.678, 0.651);
    vec3 c = vec3(0.690, 1.319, 0.654);
    vec3 d = vec3(6.205, 2.511, 3.523);
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

// yellow red yellow
//[[0.990 0.520 0.071] [0.063 0.800 0.918] [1.548 0.740 0.062] [1.261 5.091 5.773]]
vec3 palette4(float t) {
    vec3 a = vec3(0.990, 0.520, 0.071);
    vec3 b = vec3(0.063, 0.800, 0.918);
    vec3 c = vec3(1.548, 0.740, 0.062);
    vec3 d = vec3(1.261, 5.091, 5.773);
    return palette(t, a, b, c, d);
}

// many color
// [[0.481 0.619 0.755] [0.424 0.158 0.810] [3.136 1.650 2.155] [4.963 4.889 4.418]]
vec3 palette5(float t) {
    vec3 a = vec3(0.481, 0.619, 0.755);
    vec3 b = vec3(0.424, 0.158, 0.810);
    vec3 c = vec3(3.136, 1.650, 2.155);
    vec3 d = vec3(4.963, 4.889, 4.418);
    return palette(t, a, b, c, d);
}

// red cyan blue purple
//[[0.354 -0.322 0.578] [0.321 0.861 0.394] [1.197 1.258 0.758] [0.788 0.368 0.434]]
vec3 palette6(float t) {
    vec3 a = vec3(0.354, -0.322, 0.578);
    vec3 b = vec3(0.321, 0.861, 0.394);
    vec3 c = vec3(1.197, 1.258, 0.758);
    vec3 d = vec3(0.788, 0.368, 0.434);
    return palette(t, a, b, c, d);
}

// multi pastel
//[[0.768 0.748 0.828] [0.798 0.108 1.048] [3.108 0.798 2.008] [2.808 1.998 4.544]]
vec3 palette7(float t) {
    vec3 a = vec3(0.768, 0.748, 0.828);
    vec3 b = vec3(0.798, 0.108, 1.048);
    vec3 c = vec3(3.1, 0.798, 2.008);
    vec3 d = vec3(2.808, 1.998, 4.544);
    return palette(t, a, b, c, d);
}


// purple white blue green
//[[0.472 0.658 0.577] [0.837 0.606 0.653] [1.025 1.508 0.407] [2.753 4.488 2.828]]
vec3 palette8(float t) {
    vec3 a = vec3(0.472, 0.658, 0.577);
    vec3 b = vec3(0.837, 0.606, 0.653);
    vec3 c = vec3(1.025, 1.508, 0.407);
    vec3 d = vec3(2.753, 4.488, 2.828);
    return palette(t, a, b, c, d);
}

// orange green blue pink
//[[0.572 0.574 0.518] [0.759 0.171 0.358] [1.022 0.318 0.620] [3.138 5.671 -0.172]]
vec3 palette9(float t) {
    return clamp(vec3(t, t, t), 0.0, 1.0);
    //vec3 a = vec3(0.572, 0.574, 0.518);
    //vec3 b = vec3(0.759, 0.171, 0.358);
    //vec3 c = vec3(1.022, 0.318, 0.620);
    //vec3 d = vec3(3.138, 5.5671, -0.172);
    //return palette(t, a, b, c, d);
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

float sdHexagram( in vec2 p, in float r )
{
    const vec4 k = vec4(-0.5,0.8660254038,0.5773502692,1.7320508076);
    p = abs(p);
    p -= 2.0*min(dot(k.xy,p),0.0)*k.xy;
    p -= 2.0*min(dot(k.yx,p),0.0)*k.yx;
    p -= vec2(clamp(p.x,r*k.z,r*k.w),r);
    return length(p)*sign(p.y);
}


float Hash21(vec2 p) {
    p = fract(p*vec2(123.34, 456.12));
    p += dot(p, p+45.32);
    return fract(p.x*p.y);
}


float HexLayer(vec2 uv) {
    float hex = sdHexagram(uv, 0.1);
    hex = abs(hex);
    hex = smoothstep(s1, s2, hex);
    return clamp(pow(0.02 * brt / hex, pw), 0.0, 1.0);
}

void main(){
    vec2 uv = position.xz - 0.5;
    vec3 col = vec3(0.);

    float t = fTime * .1;
    for (float i=0.; i<1.; i+= 1./layers) {
        float depth = fract(i + t);
        float scale = mix(6., .1, depth);
        float fade = depth*smoothstep(1, .9, depth);
        float bright = HexLayer((uv*Rot(fTime*depth*.1*rspeed))*scale)*fade;
        col += paletteN(bright*pald + i*fTime*cspeed, pal)*bright;
        //col += paletteN(bright*i*fTime*cspeed, pal)*bright;
        //col += bright;
    }

    tPosition = clamp(col, 0.0, 1.0);
}
