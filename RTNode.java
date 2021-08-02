package rtree;

import java.util.List;
import rtree.Constants;

public abstract class RTNode {
    protected RTree rtree;
    protected int level;
    protected Rectangle[] datas;
    protected RTNode parent;
    protected int usedSpace;
    protected int insertIndex;
    protected int deleteIndex;

    public RTNode(RTree rtree, RTNode parent, int level) {
        this.rtree = rtree;
        this.parent = parent;
        this.level = level;
        datas = new Rectangle[rtree.getNodeCapacity() + 1];
        usedSpace = 0;
    }

    public RTNode getParent() {
        return parent;
    }

    protected void addData(Rectangle rectangle) {
        if (usedSpace == rtree.getNodeCapacity()) {
            throw new IllegalArgumentException("Node is full.");
        }
        datas[usedSpace++] = rectangle;
    }

    protected void deleteData(int i) {
        if (datas[i + 1] != null)
        {
            System.arraycopy(datas, i + 1, datas, i, usedSpace - i - 1);
            datas[usedSpace - 1] = null;
        } else
            datas[i] = null;
        usedSpace--;
    }

    protected void condenseTree(List<RTNode> list) {
        if (isRoot()) {
            if (!isLeaf() && usedSpace == 1) {
                RTDirNode root = (RTDirNode) this;

                RTNode child = root.getChild(0);
                root.children.remove(this);
                child.parent = null;
                rtree.setRoot(child);

            }
        } else {
            RTNode parent = getParent();
            int min = Math.round(rtree.getNodeCapacity() * rtree.getFillFactor());
            if (usedSpace < min) {
                parent.deleteData(parent.deleteIndex);
                ((RTDirNode) parent).children.remove(this);
                this.parent = null;
                list.add(this);
            } else {
                parent.datas[parent.deleteIndex] = getNodeRectangle();
            }
            parent.condenseTree(list);
        }
    }

    protected int[][] quadraticSplit(Rectangle rectangle) {
        if (rectangle == null) {
            throw new IllegalArgumentException("Rectangle cannot be null.");
        }

        datas[usedSpace] = rectangle;

        int total = usedSpace + 1;

        int[] mask = new int[total];
        for (int i = 0; i < total; i++) {
            mask[i] = 1;
        }

        int c = total / 2 + 1;
        int minNodeSize = Math.round(rtree.getNodeCapacity() * rtree.getFillFactor());
        if (minNodeSize < 2)
            minNodeSize = 2;

        int rem = total;

        int[] group1 = new int[c];
        int[] group2 = new int[c];
        int i1 = 0, i2 = 0;

        int[] seed = pickSeeds();
        group1[i1++] = seed[0];
        group2[i2++] = seed[1];
        rem -= 2;
        mask[group1[0]] = -1;
        mask[group2[0]] = -1;

        while (rem > 0) {
            if (minNodeSize - i1 == rem) {
                for (int i = 0; i < total; i++)
                {
                    if (mask[i] != -1)
                    {
                        group1[i1++] = i;
                        mask[i] = -1;
                        rem--;
                    }
                }
            } else if (minNodeSize - i2 == rem) {
                for (int i = 0; i < total; i++)
                {
                    if (mask[i] != -1)
                    {
                        group2[i2++] = i;
                        mask[i] = -1;
                        rem--;
                    }
                }
            } else {
                Rectangle mbr1 = (Rectangle) datas[group1[0]].clone();
                for (int i = 1; i < i1; i++) {
                    mbr1 = mbr1.getUnionRectangle(datas[group1[i]]);
                }
                Rectangle mbr2 = (Rectangle) datas[group2[0]].clone();
                for (int i = 1; i < i2; i++) {
                    mbr2 = mbr2.getUnionRectangle(datas[group2[i]]);
                }

                double dif = Double.NEGATIVE_INFINITY;
                double areaDiff1 = 0, areaDiff2 = 0;
                int sel = -1;
                for (int i = 0; i < total; i++) {
                    if (mask[i] != -1)
                    {
                        Rectangle a = mbr1.getUnionRectangle(datas[i]);
                        areaDiff1 = a.getArea() - mbr1.getArea();

                        Rectangle b = mbr2.getUnionRectangle(datas[i]);
                        areaDiff2 = b.getArea() - mbr2.getArea();

                        if (Math.abs(areaDiff1 - areaDiff2) > dif) {
                            dif = Math.abs(areaDiff1 - areaDiff2);
                            sel = i;
                        }
                    }
                }

                if (areaDiff1 < areaDiff2)
                {
                    group1[i1++] = sel;
                } else if (areaDiff1 > areaDiff2) {
                    group2[i2++] = sel;
                } else if (mbr1.getArea() < mbr2.getArea())
                {
                    group1[i1++] = sel;
                } else if (mbr1.getArea() > mbr2.getArea()) {
                    group2[i2++] = sel;
                } else if (i1 < i2)
                {
                    group1[i1++] = sel;
                } else if (i1 > i2) {
                    group2[i2++] = sel;
                } else {
                    group1[i1++] = sel;
                }
                mask[sel] = -1;
                rem--;

            }
        }

        int[][] ret = new int[2][];
        ret[0] = new int[i1];
        ret[1] = new int[i2];

        for (int i = 0; i < i1; i++) {
            ret[0][i] = group1[i];
        }
        for (int i = 0; i < i2; i++) {
            ret[1][i] = group2[i];
        }
        return ret;
    }

    protected int[] pickSeeds() {
        double inefficiency = Double.NEGATIVE_INFINITY;
        int i1 = 0, i2 = 0;

        for (int i = 0; i < usedSpace; i++) {
            for (int j = i + 1; j <= usedSpace; j++)
            {
                Rectangle rectangle = datas[i].getUnionRectangle(datas[j]);
                double d = rectangle.getArea() - datas[i].getArea() - datas[j].getArea();

                if (d > inefficiency) {
                    inefficiency = d;
                    i1 = i;
                    i2 = j;
                }
            }
        }
        return new int[] { i1, i2 };
    }

    public Rectangle getNodeRectangle() {
        if (usedSpace > 0) {
            Rectangle[] rectangles = new Rectangle[usedSpace];
            System.arraycopy(datas, 0, rectangles, 0, usedSpace);
            return Rectangle.getUnionRectangle(rectangles);
        } else {
            return new Rectangle(new Point(new float[] { 0, 0 }), new Point(new float[] { 0, 0 }));
        }
    }

    public boolean isRoot() {
        return (parent == Constants.NULL);
    }

    public boolean isIndex() {
        return (level != 0);
    }

    public boolean isLeaf() {
        return (level == 0);
    }

    protected abstract RTDataNode chooseLeaf(Rectangle rectangle);

    protected abstract RTDataNode findLeaf(Rectangle rectangle);
}