/**
 * Class that implements a red-black tree which implements the MyMap interface.
 * @author Brian S. Borowski
 * @version 1.2.1 March 5, 2024
 */
public class RBTreeMap<K extends Comparable<K>, V> extends BSTreeMap<K, V>
        implements MyMap<K, V> {

    /**
     * Creates an empty red-black tree map.
     */
    public RBTreeMap() { }

    /**
     * Creates a red-black tree map from the array of key-value pairs.
     * @param elements an array of key-value pairs
     */
    public RBTreeMap(Pair<K, V>[] elements) {
        insertElements(elements);
    }

    /**
     * Creates a red-black tree map of the given key-value pairs. If
     * sorted is true, a balanced tree will be created via a divide-and-conquer
     * approach. If sorted is false, the pairs will be inserted in the order
     * they are received, and the tree will be rotated to maintain the red-black
     * tree properties.
     * @param elements an array of key-value pairs
     */
    public RBTreeMap(Pair<K, V>[] elements, boolean sorted) {
        if (!sorted) {
            insertElements(elements);
        } else {
            root = createBST(elements, 0, elements.length - 1);
        }
    }

    /**
     * Recursively constructs a balanced binary search tree by inserting the
     * elements via a divide-snd-conquer approach. The middle element in the
     * array becomes the root. The middle of the left half becomes the root's
     * left child. The middle element of the right half becomes the root's right
     * child. This process continues until low > high, at which point the
     * method returns a null Node.
     * All nodes in the tree are black down to and including the deepest full
     * level. Nodes below that deepest full level are red. This scheme ensures
     * that all paths from the root to the nulls contain the same number of
     * black nodes.
     * @param pairs an array of <K, V> pairs sorted by key
     * @param low   the low index of the array of elements
     * @param high  the high index of the array of elements
     * @return      the root of the balanced tree of pairs
     */
    @Override
    protected Node<K, V> createBST(Pair<K, V>[] pairs, int low, int high)
    {
        return createRBT(pairs, low, high, 0);
    }

    private RBNode<K, V> createRBT(Pair<K, V>[] pairs, int low, int high, int depth)
    {
        if (low > high) { return null; }

        int mid = low + (high - low) / 2;
        RBNode<K, V> current = new RBNode<>(pairs[mid].key, pairs[mid].value);
        if (depth < (int) (Math.log(pairs.length + 1) / Math.log(2))) { current.color = RBNode.BLACK; }

        RBNode<K, V> leftChild = createRBT(pairs, low, mid - 1, depth + 1);
        RBNode<K, V> rightChild = createRBT(pairs, mid + 1, high, depth + 1);

        if (leftChild != null) { leftChild.setParent(current); }
        if (rightChild != null) { rightChild.setParent(current); }
        current.setLeft(leftChild);
        current.setRight(rightChild);

        size++;
        return current;
    }

    /**
     * Associates the specified value with the specified key in this map. If the
     * map previously contained a mapping for the key, the old value is replaced
     * by the specified value.
     * @param key   the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @return the previous value associated with key, or null if there was no
     *         mapping for key
     */
    @Override
    public V put(K key, V value)
    {
        RBNode<K, V> entry = new RBNode<>(key, value);
        if (root == null)
        {
            root = entry;
        }
        else
        {
            RBNode<K, V> current = (RBNode<K, V>) root;
            RBNode<K, V> previous = null;
            int childDirection = 0;

            while (current != null)
            {
                if (key.compareTo(current.key) == 0)
                {
                    V oldValue = current.value;
                    current.value = value;
                    return oldValue;
                }

                previous = current;
                if (key.compareTo(current.key) < 0)
                {
                    current = current.getLeft();
                    childDirection = -1;
                }
                else if (key.compareTo(current.key) > 0)
                {
                    current = current.getRight();
                    childDirection = 1;
                }
            }

            if (childDirection == -1)
            {
                entry.setParent(previous);
                previous.setLeft(entry);
            }
            if (childDirection == 1)
            {
                entry.setParent(previous);
                previous.setRight(entry);
            }
        }

        entry.color = RBNode.RED;
        insertFixup(entry);

        size++;
        return null;
    }

    /**
     * Removes the mapping for a key from this map if it is present.
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with key, or null if there was no
     *         mapping for key
     */
    @Override
    public V remove(K key)
    {
        RBNode<K, V> z = (RBNode<K, V>) iterativeSearch(key);
        if (z == null) { return null; }

        RBNode<K, V> y = z;
        RBNode<K, V> x;
        byte yColor = y.color;
        if (z.getLeft() == null)
        {
            x = z.getRight();
            if (x == null)
            {
                x = new RBNode(null, null);
                x.color = RBNode.BLACK;
                x.setParent(z.getParent());
            }
            transplant(z, z.getRight());
        }
        else if (z.getRight() == null)
        {
            x = z.getLeft();
            if (x == null)
            {
                x = new RBNode(null, null);
                x.color = RBNode.BLACK;
                x.setParent(z.getParent());
            }
            transplant(z, z.getLeft());
        }
        else
        {
            y = (RBNode<K, V>) treeMinimum(z.getRight());
            yColor = y.color;
            x = y.getRight();
            if (!y.equals(z.getRight()))
            {
                if (x == null)
                {
                    x = new RBNode(null, null);
                    x.color = RBNode.BLACK;
                    x.setParent(y.getParent());
                }
                transplant(y, y.getRight());
                y.setRight(z.getRight());
                y.getRight().setParent(y);
            }
            else if (x != null)
            {
                x.setParent(y);
            }
            else
            {
                x = new RBNode(null, null);
                x.color = RBNode.BLACK;
                x.setParent(y);
            }
            transplant(z, y);
            y.setLeft(z.getLeft());
            y.getLeft().setParent(y);
            y.color = z.color;
        }
        if (yColor == RBNode.BLACK && size > 1)
        {
            deleteFixup(x);
        }

        size--;
        return z.value;
    }

    /**
     * Fixup method described on p. 339 of CLRS, 4e.
     */
    private void insertFixup(RBNode<K, V> z)
    {
        while (z.getParent() != null && z.getParent().color == RBNode.RED)
        {
            if (z.getParent().getParent() != null && z.getParent().equals(z.getParent().getParent().getLeft()))
            {
                RBNode<K, V> y = z.getParent().getParent().getRight();
                if(y != null && y.color == RBNode.RED)
                {
                    z.getParent().color = RBNode.BLACK;
                    y.color = RBNode.BLACK;
                    z.getParent().getParent().color = RBNode.RED;
                    z = z.getParent().getParent();
                }
                else
                {
                    if (z.equals(z.getParent().getRight()))
                    {
                        z = z.getParent();
                        leftRotate(z);
                    }
                    z.getParent().color = RBNode.BLACK;
                    z.getParent().getParent().color = RBNode.RED;
                    rightRotate(z.getParent().getParent());
                }
            }
            else if (z.getParent().getParent() != null)
            {
                RBNode<K, V> y = z.getParent().getParent().getLeft();
                if(y != null && y.color == RBNode.RED)
                {
                    z.getParent().color = RBNode.BLACK;
                    y.color = RBNode.BLACK;
                    z.getParent().getParent().color = RBNode.RED;
                    z = z.getParent().getParent();
                }
                else
                {
                    if (z.equals(z.getParent().getLeft()))
                    {
                        z = z.getParent();
                        rightRotate(z);
                    }
                    z.getParent().color = RBNode.BLACK;
                    z.getParent().getParent().color = RBNode.RED;
                    leftRotate(z.getParent().getParent());
                }
            }
            else { break; }
        }
        ((RBNode<K, V>) root).color = RBNode.BLACK;
    }

    /**
     * Fixup method described on p. 351 of CLRS, 4e.
     */
    private void deleteFixup(RBNode<K, V> x)
    {
        while (!x.equals(root) && x.color == RBNode.BLACK)
        {
            if (x.equals(x.getParent().getLeft()) || (x.getParent().getLeft() == null && !x.equals(x.getParent().getRight())))
            {
                RBNode<K, V> w = x.getParent().getRight();
                if (w.color == RBNode.RED)
                {
                    w.color = RBNode.BLACK;
                    x.getParent().color = RBNode.RED;
                    leftRotate(x.getParent());
                    w = x.getParent().getRight();
                }
                if (!((w.getLeft() != null && w.getLeft().color == RBNode.RED) || (w.getRight() != null && w.getRight().color == RBNode.RED)))
                {
                    w.color = RBNode.RED;
                    x = x.getParent();
                }
                else
                {
                    if (w.getRight() == null || w.getRight().color == RBNode.BLACK)
                    {
                        w.getLeft().color = RBNode.BLACK;
                        w.color = RBNode.RED;
                        rightRotate(w);
                        w = x.getParent().getRight();
                    }
                    w.color = x.getParent().color;
                    x.getParent().color = RBNode.BLACK;
                    if (w.getRight() != null)
                    {
                        w.getRight().color = RBNode.BLACK;
                    }
                    leftRotate(x.getParent());
                    x = (RBNode<K, V>) root;
                }
            }
            else if (x.equals(x.getParent().getRight()) || (x.getParent().getRight() == null && !x.equals(x.getParent().getLeft())))
            {
                RBNode<K, V> w = x.getParent().getLeft();
                if (w.color == RBNode.RED)
                {
                    w.color = RBNode.BLACK;
                    x.getParent().color = RBNode.RED;
                    rightRotate(x.getParent());
                    w = x.getParent().getLeft();
                }
                if (!((w.getLeft() != null && w.getLeft().color == RBNode.RED) || (w.getRight() != null && w.getRight().color == RBNode.RED)))
                {
                    w.color = RBNode.RED;
                    x = x.getParent();
                }
                else {
                    if (w.getLeft() == null || w.getLeft().color == RBNode.BLACK) {
                        w.getRight().color = RBNode.BLACK;
                        w.color = RBNode.RED;
                        leftRotate(w);
                        w = x.getParent().getLeft();
                    }
                    w.color = x.getParent().color;
                    x.getParent().color = RBNode.BLACK;
                    if (w.getLeft() != null) {
                        w.getLeft().color = RBNode.BLACK;
                    }
                    rightRotate(x.getParent());
                    x = (RBNode<K, V>) root;
                }
            }
        }
        x.color = RBNode.BLACK;
    }

    /**
     * Left-rotate method described on p. 336 of CLRS, 4e.
     */
    private void leftRotate(Node<K, V> x)
    {
        Node<K, V> y = x.getRight();
        x.setRight(y.getLeft());

        if (y.getLeft() != null)
        {
            y.getLeft().setParent(x);
        }
        y.setParent(x.getParent());

        if (x.getParent() == null)
        {
            root = y;
        }
        else if (x.equals(x.getParent().getLeft()))
        {
            x.getParent().setLeft(y);
        }
        else
        {
            x.getParent().setRight(y);
        }

        y.setLeft(x);
        x.setParent(y);
    }

    /**
     * Right-rotate method described on p. 336 of CLRS, 4e.
     */
    private void rightRotate(Node<K, V> x)
    {
        Node<K, V> y = x.getLeft();
        x.setLeft(y.getRight());

        if (y.getRight() != null)
        {
            y.getRight().setParent(x);
        }
        y.setParent(x.getParent());

        if (x.getParent() == null)
        {
            root = y;
        }
        else if (x.equals(x.getParent().getLeft()))
        {
            x.getParent().setLeft(y);
        }
        else
        {
            x.getParent().setRight(y);
        }

        y.setRight(x);
        x.setParent(y);
    }
}
