#version 330

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

void main(){
    vec3 pos = position;
    pos.y = pos.y - 19.0;
    pos = pos / 5.0;
    vec2 t = vec2(1.5, 0.5);
    float distance = sdTorus(pos, t);
	tPosition = vec3(distance, distance, distance);
}
