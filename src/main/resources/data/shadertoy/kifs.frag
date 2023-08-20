#version 150

#define SAMPLER0 sampler2D // sampler2D, sampler3D, samplerCube
#define SAMPLER1 sampler2D // sampler2D, sampler3D, samplerCube
#define SAMPLER2 sampler2D // sampler2D, sampler3D, samplerCube
#define SAMPLER3 sampler2D // sampler2D, sampler3D, samplerCube

uniform SAMPLER0 iChannel0; // image/buffer/sound    Sampler for input textures 0
uniform SAMPLER1 iChannel1; // image/buffer/sound    Sampler for input textures 1
uniform SAMPLER2 iChannel2; // image/buffer/sound    Sampler for input textures 2
uniform SAMPLER3 iChannel3; // image/buffer/sound    Sampler for input textures 3

uniform vec3  iResolution;           // image/buffer          The viewport resolution (z is pixel aspect ratio, usually 1.0)
uniform float iTime;                 // image/sound/buffer    Current time in seconds
uniform float iTimeDelta;            // image/buffer          Time it takes to render a frame, in seconds
uniform int   iFrame;                // image/buffer          Current frame
uniform float iFrameRate;            // image/buffer          Number of frames rendered per second
uniform vec4  iMouse;                // image/buffer          xy = current pixel coords (if LMB is down). zw = click pixel
uniform vec4  iDate;                 // image/buffer/sound    Year, month, day, time in seconds in .xyzw
uniform float iSampleRate;           // image/buffer/sound    The sound sample rate (typically 44100)
uniform float iChannelTime[4];       // image/buffer          Time for channel (if video or sound), in seconds
uniform vec3  iChannelResolution[4]; // image/buffer/sound    Input texture resolution for each channel


#ifdef GL_ES
precision mediump float;
#endif

#define M_PI 3.1415926

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

vec2 N(float angle) {
    return vec2(sin(angle), cos(angle));
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    vec2 uv = (fragCoord-.5*iResolution.xy)/iResolution.y;
    vec3 col = vec3(0);

    uv *= 2.;
    uv.x = abs(uv.x);
    uv.y += tan((5./6.)*3.1415)*.5;

    float angle = (2./3.)*3.1415; // mouse.x*3.1415;

    vec2 n = N((5./6.)*3.1415); //vec2(sin(angle), cos(angle));
    float d = dot(uv-vec2(.5, .0), n);
    uv -= n*max(0., d)*2.;

    //col += smoothstep(.01, .0, abs(d));

    n = N(10.*iMouse.y*(2./3.)*3.1415);
    float scale = 1.;
    uv.x += .5;
    for (int i=0; i<floor(iMouse.z * 10.); i++) {
      uv *= 3.;
      scale *= 3.;
      uv.x -= 1.5;

      uv.x = abs(uv.x);
      uv.x -= .5;
      uv -= n*min(0., dot(uv, n))*2.;

    }

    d = length(uv - vec2(clamp(uv.x, -1., 1.), 0.));
    //col += smoothstep(1./iResolution.y, .0, d/scale);
    //col.rg += uv;
    uv /= scale;
    col += texture(iChannel1, uv*2.-iTime*.1 * iMouse.x*10.).rgb;
    //col.rg += uv/(scale*.06);
    // Output to screen11
    fragColor = vec4(col,1.0);
}
