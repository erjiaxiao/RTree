package rtree;

import java.util.ArrayList;
import java.util.List;
import rtree.Constants;

public class RTDirNode extends RTNode {
    protected List<RTNode> children;

    public RTDirNode(RTree rtree, RTNode parent, int level) {
        super(rtree, parent, level);
        children = new ArrayList<RTNode>();
    }

    public RTNode getChild(int index) {
        return children.get(index);
    }

    @Override
    public RTDataNode chooseLeaf(Rectangle rectangle) {
        int index;

        switch (rtree.getTreeType()) {
            case Constants.RTREE_LINEAR:

            case Constants.RTREE_QUADRATIC:

            case Constants.RTREE_EXPONENTIAL:
                index = findLeastEnlargement(rectangle);
                break;
            case Constants.RSTAR:
                if (level == 1)
                {
                    index = findLeastOverlap(rectangle);
                } else {
                    index = findLeastEnlargement(rectangle);
                }
                break;

            default:
                throw new IllegalStateException("Invalid tree type.");
        }

        insertIndex = index;

        return getChild(index).chooseLeaf(rectangle);
    }

    private int findLeastOverlap(Rectangle rectangle) {
        float overlap = Float.POSITIVE_INFINITY;
        int sel = -1;

        for (int i = 0; i < usedSpace; i++) {
            RTNode node = getChild(i);
            float ol = 0;

            for (int j = 0; j < node.datas.length; j++) {
                ol += rectangle.intersectingArea(node.datas[j]);
            }
            if (ol < overlap) {
                overlap = ol;
                sel = i;
            }
            else if (ol == overlap) {
                double area1 = datas[i].getUnionRectangle(rectangle).getArea() - datas[i].getArea();
                double area2 = datas[sel].getUnionRectangle(rectangle).getArea() - datas[sel].getArea();

                if (area1 == area2) {
                    sel = (datas[sel].getArea() <= datas[i].getArea()) ? sel : i;
                } else {
                    sel = (area1 < area2) ? i : sel;
                }
            }
        }
        return sel;
    }

    private int findLeastEnlargement(Rectangle rectangle) {
        double area = Double.POSITIVE_INFINITY;
        int sel = -1;

        for (int i = 0; i < usedSpace; i++) {
            double enlargement = datas[i].getUnionRectangle(rectangle).getArea() - datas[i].getArea();
            if (enlargement < area) {
                area = enlargement;
                sel = i;
            } else if (enlargement == area) {
                sel = (datas[sel].getArea() < datas[i].getArea()) ? sel : i;
            }
        }

        return sel;
    }

    public void adjustTree(RTNode node1, RTNode node2) {
        datas[insertIndex] = node1.getNodeRectangle();
        children.set(insertIndex, node1);

        if (node2 != null) {
            insert(node2);

        }
        else if (!isRoot()) {
            RTDirNode parent = (RTDirNode) getParent();
            parent.adjustTree(this, null);
        }
    }

    protected boolean insert(RTNode node) {
        if (usedSpace < rtree.getNodeCapacity()) {
            datas[usedSpace++] = node.getNodeRectangle();
            children.add(node);
            node.parent = this;
            RTDirNode parent = (RTDirNode) getParent();
            if (parent != null)
            {
                parent.adjustTree(this, null);
            }
            return false;
        } else {
            RTDirNode[] a = splitIndex(node);
            RTDirNode n = a[0];
            RTDirNode nn = a[1];

            if (isRoot()) {
                RTDirNode newRoot = new RTDirNode(rtree, Constants.NULL, level + 1);

                newRoot.addData(n.getNodeRectangle());
                newRoot.addData(nn.getNodeRectangle());

                newRoot.children.add(n);
                newRoot.children.add(nn);

                n.parent = newRoot;
                nn.parent = newRoot;

                rtree.setRoot(newRoot);
            } else {
                RTDirNode p = (RTDirNode) getParent();
                p.adjustTree(n, nn);
            }
        }
        return true;
    }

    private RTDirNode[] splitIndex(RTNode node) {
        int[][] group = null;
        switch (rtree.getTreeType()) {
            case Constants.RTREE_LINEAR:
                break;
            case Constants.RTREE_QUADRATIC:
                group = quadraticSplit(node.getNodeRectangle());
                children.add(node);
                node.parent = this;
                break;
            case Constants.RTREE_EXPONENTIAL:
                break;
            case Constants.RSTAR:
                break;
            default:
                throw new IllegalStateException("Invalid tree type.");
        }
        RTDirNode index1 = new RTDirNode(rtree, parent, level);
        RTDirNode index2 = new RTDirNode(rtree, parent, level);

        int[] group1 = group[0];
        int[] group2 = group[1];
        for (int i = 0; i < group1.length; i++) {
            index1.addData(datas[group1[i]]);
            index1.children.add(this.children.get(group1[i]));
            this.children.get(group1[i]).parent = index1;
        }
        for (int i = 0; i < group2.length; i++) {
            index2.addData(datas[group2[i]]);
            index2.children.add(this.children.get(group2[i]));
            this.children.get(group2[i]).parent = index2;
        }
        return new RTDirNode[] { index1, index2 };
    }

    @Override
    protected RTDataNode findLeaf(Rectangle rectangle) {
        for (int i = 0; i < usedSpace; i++) {
            if (datas[i].enclosure(rectangle)) {
                deleteIndex = i;
                RTDataNode leaf = children.get(i).findLeaf(rectangle);
                if (leaf != null)
                    return leaf;
            }
        }
        return null;
    }

}