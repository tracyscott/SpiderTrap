/*{
	"DESCRIPTION": "FBMLightning",
	"CREDIT": "by tracyscott",
	"ISFVSN": "2.0",
	"CATEGORIES": [
		"VERTEX SDF"
	],
	"INPUTS": [
         {
            "NAME": "x",
            "TYPE": "float",
            "DEFAULT": -0.34,
            "MIN": -10,
            "MAX": 10
         },
         {
            "NAME": "y",
            "TYPE": "float",
            "DEFAULT": 0.1,
            "MIN": -10,
            "MAX": 10
         },
         {
            "NAME": "radius",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -0.25,
            "MAX": 2.0
         },
         {
            "NAME": "thick",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -1.1,
            "MAX": 1.1
         },
         {
            "NAME": "zoom",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": -4.0,
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
            "NAME": "palval",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": 0.0,
            "MAX": 9.9
         },
          {
            "NAME": "pald",
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
uniform float x;
uniform float y;
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

mat2 Rot(float a) {
    float s=sin(a), c=cos(a);
    return mat2(c, -s, s, c);
}

    #define RAINBOW

    #define STATIC 18
    #define PI 3.14159265358979323846264

// HSV2RGB code taken from http://lolengine.net/blog/2013/07/27/rgb-to-hsv-in-glsl
vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

// Rand, noise, and fbm found here on ShaderToy
float rand(vec2 n)
{
    return fract(sin(dot(n, vec2(17.12037, 5.71713))) * 12345.6789);
}

float noise(vec2 n)
{
    vec2 d = vec2(0.0, 1.0);
    vec2 b = floor(n), f = smoothstep(vec2(0.0), vec2(1.0), fract(n));
    return mix(mix(rand(b + d.xx), rand(b + d.yx), f.x), mix(rand(b + d.xy), rand(b + d.yy), f.x), f.y);
}

float fbm(vec2 n)
{
    float sum = 0.0, amp = 1.0;
    for (int i = 0; i < 10; i++)
    {
        sum += noise(n) * amp;
        n += n;
        amp *= 0.5;
    }
    return sum;
}

float lightning_layer(vec2 uv, vec2 cv, float rotateAmt) {
    uv -= 0.5;
    uv = uv * Rot(rotateAmt);
    uv += 0.5;

    cv -= 0.5;
    cv = cv * Rot(rotateAmt);
    cv += 0.5;
    // What is m?  -0.5 of uv.x + 1.0  -1.0 * 0.5
    float m = 1.0 + 0.5; //-fragCoord.x/iResolution.x*.5+1.0;
    //m = -fragCoord.x/iResolution.x*.5+1.0;
    m = uv.x - 1.9; // + 1.;


    float wasFive = 5.0;
    float wasAlsoFive = 5.0;
    //float d = (-2.5*iMouse.x/iResolution.x+2.7)*abs((0.5+m*(fbm(vec2(5.0*m-iTime*5.0-1.0))-.9))-cv.y);
    float yPos = 0.5;
    float fbmScale = .3;
    float d = 5.0 * abs((0.5+ m*( fbmScale*fbm(vec2(wasFive*m-fTime*.1*wasAlsoFive-1.0)) -yPos)));

    float fbmInput = 1.*m-fTime*.0001;
    vec2 fbmRotInput = vec2(fbmInput) * Rot(rotateAmt);
    d = 5.0 * abs((0.5 + m * (fbmScale*fbm(fbmRotInput) - yPos))-cv.y*1.5);
    return d;
}

void main(){
    vec2 uv = position.xz - 0.5;
    vec3 color = vec3(0., 0., 0.);

    float pal_d = length(uv) * 2.0;
    uv.x -= x;
    uv.y -= y;
    vec2 cv = vec2(-0.5,0) + position.xz;  // 1 to 1 + -.5, 0
    cv.x -= x;
    cv.y -= y;

    uv *= 2.0; // -1 to 1 -1.0 + 2.0*fragCoord.xy / iResolution.xy;

    float angle = fTime;
    float d = clamp(lightning_layer(uv, cv, angle), 0.0, 1.0);
    d = clamp(min(d, clamp(lightning_layer(uv, cv, angle + PI/2.0), 0.0, 1.0)), 0.0, 1.0);

    d = pow(brt*0.01/d, pw);

    color = vec3(clamp(paletteN(pal_d + fTime * .5, palval)*d, 0., 1.));
    //d = m;

    tPosition = clamp(color, 0.0, 1.0);
}