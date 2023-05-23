package art.lookingup.linear;

/**
 * Basic representation of a point in 3D space.  Also used for orientation unit vectors
 */
public class Point3D {
    public Point3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3D(Point3D p) {
        this.x = p.x;
        this.y = p.y;
        this.z = p.z;
    }

    public Point3D scaled(float scale) {
        return new Point3D(x * scale, y * scale, z * scale);
    }

    static public Point3D delta(Point3D end, Point3D start) {
        return new Point3D(end.x - start.x, end.y - start.y, end.z - start.z);
    }

    static public Point3D unitVectorTo(Point3D end, Point3D start) {
        Point3D vector = delta(end, start);
        float len = vector.length();
        vector.scale(1f / len);
        return vector;
    }

    public void translate(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }

    public void scale(float s) {
        this.x *= s;
        this.y *= s;
        this.z *= s;
    }

    public void rotateZAxis(float angle) {
        float newX = x * (float) Math.cos(angle) + y * (float) Math.sin(angle);
        float newY = -x * (float) Math.sin(angle) + y * (float) Math.cos(angle);
        x = newX;
        y = newY;
    }

    public void rotateYAxis(float angle) {
        float newX = x * (float) Math.cos(angle) - z * (float) Math.sin(angle);
        float newZ = x * (float) Math.sin(angle) + z * (float) Math.cos(angle);
        x = newX;
        z = newZ;
    }

    public void rotateXAxis(float angle) {
        float newY = y * (float) Math.cos(angle) + z * (float) Math.sin(angle);
        float newZ = -y * (float) Math.sin(angle) + z * (float) Math.cos(angle);
        y = newY;
        z = newZ;
    }

    public float distanceTo(Point3D p) {
        return (float) Math.sqrt((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y) + (z - p.z) * (z - p.z));
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }


    public float x;
    public float y;
    public float z;
}

