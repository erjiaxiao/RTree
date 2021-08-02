package rtree;

import java.util.ArrayList;
import java.util.List;

import rtree.Constants;

public class RTree {
    private RTNode root;
    private int tree_type;
    private int nodeCapacity = -1;
    private float fillFactor = -1;
    private int dimension;

    public RTree(int capacity, float fillFactor, int type, int dimension) {
        this.fillFactor = fillFactor;
        tree_type = type;
        nodeCapacity = capacity;
        this.dimension = dimension;
        root = new RTDataNode(this, Constants.NULL);
    }

    public int getDimension() {
        return dimension;
    }

    public void setRoot(RTNode root) {
        this.root = root;
    }

    public float getFillFactor() {
        return fillFactor;
    }

    public int getNodeCapacity() {
        return nodeCapacity;
    }

    public int getTreeType() {
        return tree_type;
    }

    public boolean insert(Rectangle rectangle) {
        if (rectangle == null)
            throw new IllegalArgumentException("Rectangle cannot be null.");

        if (rectangle.getHigh().getDimension() != getDimension())
        {
            throw new IllegalArgumentException("Rectangle dimension different than RTree dimension.");
        }

        RTDataNode leaf = root.chooseLeaf(rectangle);

        return leaf.insert(rectangle);
    }

    public int delete(Rectangle rectangle) {
        if (rectangle == null) {
            throw new IllegalArgumentException("Rectangle cannot be null.");
        }

        if (rectangle.getHigh().getDimension() != getDimension()) {
            throw new IllegalArgumentException("Rectangle dimension different than RTree dimension.");
        }

        RTDataNode leaf = root.findLeaf(rectangle);

        if (leaf != null) {
            return leaf.delete(rectangle);
        }

        return -1;
    }

    public List<RTNode> traversePostOrder(RTNode root) {
        if (root == null)
            throw new IllegalArgumentException("Node cannot be null.");

        List<RTNode> list = new ArrayList<RTNode>();
        list.add(root);

        if (!root.isLeaf()) {
            for (int i = 0; i < root.usedSpace; i++) {
                List<RTNode> a = traversePostOrder(((RTDirNode) root).getChild(i));
                for (int j = 0; j < a.size(); j++) {
                    list.add(a.get(j));
                }
            }
        }

        return list;
    }

    public static void main(String[] args) throws Exception {
        RTree tree = new RTree(4, 0.4f, Constants.RTREE_QUADRATIC, 2);
        float[] f = { 5, 30, 25, 35, 15, 38, 23, 50, 10, 23, 30, 28, 13, 10, 18, 15, 23, 10, 28, 20, 28, 30, 33, 40, 38,
                13, 43, 30, 35, 37, 40, 43, 45, 8, 50, 50, 23, 55, 28, 70, 10, 65, 15, 70, 10, 58, 20, 63, };

        for (int i = 0; i < f.length;) {
            Point p1 = new Point(new float[] { f[i++], f[i++] });
            Point p2 = new Point(new float[] { f[i++], f[i++] });
            final Rectangle rectangle = new Rectangle(p1, p2);
            tree.insert(rectangle);

            Rectangle[] rectangles = tree.root.datas;
            System.out.println("level:" + tree.root.level);
            for (int j = 0; j < rectangles.length; j++)
                System.out.println(rectangles[j]);
        }
        System.out.println("---------------------------------");
        System.out.println("Insert finished.");

        System.out.println("---------------------------------");
        System.out.println("Begin delete.");

        for (int i = 0; i < f.length;) {
            Point p1 = new Point(new float[] { f[i++], f[i++] });
            Point p2 = new Point(new float[] { f[i++], f[i++] });
            final Rectangle rectangle = new Rectangle(p1, p2);
            tree.delete(rectangle);

            Rectangle[] rectangles = tree.root.datas;
            System.out.println(tree.root.level);
            for (int j = 0; j < rectangles.length; j++)
                System.out.println(rectangles[j]);
        }

        System.out.println("---------------------------------");
        System.out.println("Delete finished.");

        Rectangle[] rectangles = tree.root.datas;
        for (int i = 0; i < rectangles.length; i++)
            System.out.println(rectangles[i]);

    }

}