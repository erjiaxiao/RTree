package rtree;

public class Rectangle implements Cloneable
{
    private Point low;
    private Point high;

    public Rectangle(Point p1, Point p2)
    {
        if (p1 == null || p2 == null)
        {
            throw new IllegalArgumentException("Points cannot be null.");
        }
        if (p1.getDimension() != p2.getDimension())
        {
            throw new IllegalArgumentException("Points must be of same dimension.");
        }

        for (int i = 0; i < p1.getDimension(); i++) {
            if (p1.getFloatCoordinate(i) > p2.getFloatCoordinate(i)) {
                throw new IllegalArgumentException("坐标点为先左下角后右上角");
            }
        }
        low = (Point) p1.clone();
        high = (Point) p2.clone();
    }

    public Point getLow() {
        return (Point) low.clone();
    }

    public Point getHigh() {
        return high;
    }

    public Rectangle getUnionRectangle(Rectangle rectangle) {
        if (rectangle == null)
            throw new IllegalArgumentException("Rectangle cannot be null.");

        if (rectangle.getDimension() != getDimension())
        {
            throw new IllegalArgumentException("Rectangle must be of same dimension.");
        }

        float[] min = new float[getDimension()];
        float[] max = new float[getDimension()];

        for (int i = 0; i < getDimension(); i++) {
            min[i] = Math.min(low.getFloatCoordinate(i), rectangle.low.getFloatCoordinate(i));
            max[i] = Math.max(high.getFloatCoordinate(i), rectangle.high.getFloatCoordinate(i));
        }

        return new Rectangle(new Point(min), new Point(max));
    }

    public float getArea() {
        float area = 1;
        for (int i = 0; i < getDimension(); i++) {
            area *= high.getFloatCoordinate(i) - low.getFloatCoordinate(i);
        }

        return area;
    }

    public static Rectangle getUnionRectangle(Rectangle[] rectangles) {
        if (rectangles == null || rectangles.length == 0)
            throw new IllegalArgumentException("Rectangle array is empty.");

        Rectangle r0 = (Rectangle) rectangles[0].clone();
        for (int i = 1; i < rectangles.length; i++) {
            r0 = r0.getUnionRectangle(rectangles[i]);
        }

        return r0;
    }

    @Override
    protected Object clone() {
        Point p1 = (Point) low.clone();
        Point p2 = (Point) high.clone();
        return new Rectangle(p1, p2);
    }

    @Override
    public String toString() {
        return "Rectangle Low:" + low + " High:" + high;
    }

    public static void main(String[] args) {
        float[] f1 = { 1.3f, 2.4f };
        float[] f2 = { 3.4f, 4.5f };
        Point p1 = new Point(f1);
        Point p2 = new Point(f2);
        Rectangle rectangle = new Rectangle(p1, p2);
        System.out.println(rectangle);
        // Point point = rectangle.getHigh();
        // point = p1;
        // System.out.println(rectangle);

        float[] f_1 = { -2f, 0f };
        float[] f_2 = { 0f, 2f };
        float[] f_3 = { -2f, 1f };
        float[] f_4 = { 3f, 3f };
        float[] f_5 = { 1f, 0f };
        float[] f_6 = { 2f, 4f };
        p1 = new Point(f_1);
        p2 = new Point(f_2);
        Point p3 = new Point(f_3);
        Point p4 = new Point(f_4);
        Point p5 = new Point(f_5);
        Point p6 = new Point(f_6);
        Rectangle re1 = new Rectangle(p1, p2);
        Rectangle re2 = new Rectangle(p3, p4);
        Rectangle re3 = new Rectangle(p5, p6);
        // Rectangle re4 = new Rectangle(p3, p4);

        System.out.println(re1.isIntersection(re2));
        System.out.println(re1.isIntersection(re3));
        System.out.println(re1.intersectingArea(re2));
        System.out.println(re1.intersectingArea(re3));
    }

    public float intersectingArea(Rectangle rectangle) {
        if (!isIntersection(rectangle))
        {
            return 0;
        }

        float ret = 1;
        for (int i = 0; i < rectangle.getDimension(); i++) {
            float l1 = this.low.getFloatCoordinate(i);
            float h1 = this.high.getFloatCoordinate(i);
            float l2 = rectangle.low.getFloatCoordinate(i);
            float h2 = rectangle.high.getFloatCoordinate(i);

            if (l1 <= l2 && h1 <= h2) {
                ret *= (h1 - l1) - (l2 - l1);
            }
            else if (l1 >= l2 && h1 >= h2) {
                ret *= (h2 - l2) - (l1 - l2);
            }
            else if (l1 >= l2 && h1 <= h2) {
                ret *= h1 - l1;
            }
            else if (l1 <= l2 && h1 >= h2) {
                ret *= h2 - l2;
            }
        }
        return ret;
    }

    public boolean isIntersection(Rectangle rectangle) {
        if (rectangle == null)
            throw new IllegalArgumentException("Rectangle cannot be null.");

        if (rectangle.getDimension() != getDimension())
        {
            throw new IllegalArgumentException("Rectangle cannot be null.");
        }

        for (int i = 0; i < getDimension(); i++) {
            if (low.getFloatCoordinate(i) > rectangle.high.getFloatCoordinate(i)
                    || high.getFloatCoordinate(i) < rectangle.low.getFloatCoordinate(i)) {
                return false;
            }
        }
        return true;
    }

    private int getDimension() {
        return low.getDimension();
    }

    public boolean enclosure(Rectangle rectangle) {
        if (rectangle == null)
            throw new IllegalArgumentException("Rectangle cannot be null.");

        if (rectangle.getDimension() != getDimension())
            throw new IllegalArgumentException("Rectangle dimension is different from current dimension.");
        for (int i = 0; i < getDimension(); i++) {
            if (rectangle.low.getFloatCoordinate(i) < low.getFloatCoordinate(i)
                    || rectangle.high.getFloatCoordinate(i) > high.getFloatCoordinate(i))
                return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Rectangle) {
            Rectangle rectangle = (Rectangle) obj;
            if (low.equals(rectangle.getLow()) && high.equals(rectangle.getHigh()))
                return true;
        }
        return false;
    }
}