package org.cwilt.search.utils.basic;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

// PairingHeap class
//
// CONSTRUCTION: with no initializer
//
// ******************PUBLIC OPERATIONS*********************
// Position insert( x )   --> Insert x, return position
// Comparable deleteMin( )--> Return and remove smallest item
// Comparable findMin( )  --> Return smallest item
// boolean isEmpty( )     --> Return true if empty; else false
// int size( )            --> Return size of priority queue
// void makeEmpty( )      --> Remove all items
// void decreaseKey( Position p, newVal )
//                        --> Decrease value in node p
// ******************ERRORS********************************
// Exceptions thrown for various operations

/**
 * Implements a pairing heap.
 * Supports a decreaseKey operation.
 * Note that all "matching" is based on the compareTo method.
 * @author Mark Allen Weiss
 * @param <AnyType> 
 */
public class PairingHeap<AnyType extends PairingHeapable<AnyType>> implements Queue<AnyType>
{
	private final Comparator<AnyType> comparator;
	
    /**
     * The Position interface represents a type that can
     * be used for the decreaseKey operation.
     * @param <AnyType> 
     */
    public interface Position<AnyType>
    {
        /**
         * Returns the value stored at this position.
         * @return the value stored at this position.
         */
        AnyType getValue( );
    }
    
    /**
     * Construct the pairing heap.
     * @param c 
     */
    public PairingHeap ( Comparator<AnyType> c)
    {
    	this.comparator = c;
    	
        root = null;
        theSize = 0;
    }

    /**
     * Insert into the priority queue, and return a Position
     * that can be used by decreaseKey.
     * Duplicates are allowed.
     * @param x the item to insert.
     * @return the node containing the newly inserted item.
     */
    public boolean add( AnyType x )
    {
        PairNode<AnyType> newNode = new PairNode<AnyType>( x );

        if( root == null )
            root = newNode;
        else
            root = compareAndLink( root, newNode );
            
        theSize++;
        
        x.setPosition(newNode);
        
        return true;
    }

    /**
     * Find the smallest item in the priority queue.
     * @return the smallest item.
     * @throws NoSuchElementException if pairing heap is empty.
     */
    public AnyType peek( )
    {
        if( isEmpty( ) )
            throw new NoSuchElementException( );
        return root.element;
    }

    /**
     * Remove the smallest item from the priority queue.
     * @return the smallest item.
     */
    public AnyType remove( )
    {
        if( isEmpty( ) )
            throw new NoSuchElementException( );
        AnyType x = peek( );
        if( root.leftChild == null )
            root = null;
        else
            root = combineSiblings( root.leftChild );
        theSize--;
        x.setPosition(null);
        return x;
    }
    
    

    
	@Override
	public boolean remove(Object o) {
		@SuppressWarnings("unchecked")
		AnyType a = (AnyType) o;
		Position<AnyType> node = a.getPosition();
		if(node == null)
			return false;

        PairNode<AnyType> p = (PairNode<AnyType>) node;
		
        if( p != root )
        {
            if( p.nextSibling != null )
                p.nextSibling.prev = p.prev;
            if( p.prev.leftChild == p )
                p.prev.leftChild = p.nextSibling;
            else
                p.prev.nextSibling = p.nextSibling;

            p.nextSibling = null;
            root = compareAndLink( root, p );
        }
        return true;
	}

    /**
     * Change the value of the item stored in the pairing heap.
     * @param pos any Position returned by insert.
     * @param newVal the new value, which must be smaller
     *    than the currently stored value.
     * @throws IllegalArgumentException if pos is null.
     * @throws IllegalArgumentException if new value is larger than old.
     */
    public void decreaseKey( Position<AnyType> pos, AnyType newVal )
    {
        if( pos == null )
            throw new IllegalArgumentException( "null Position passed to decreaseKey" );

        PairNode<AnyType> p = (PairNode<AnyType>) pos;
        
        if( p.element == null )
            throw new IllegalArgumentException( "pos already deleted" );
        if( comparator.compare(p.element, newVal ) < 0 )
            throw new IllegalArgumentException( "newVal/oldval: " + newVal + " /" + p.element );
        p.element.setPosition(null);
        p.element = newVal;
        newVal.setPosition(p);
        if( p != root )
        {
            if( p.nextSibling != null )
                p.nextSibling.prev = p.prev;
            if( p.prev.leftChild == p )
                p.prev.leftChild = p.nextSibling;
            else
                p.prev.nextSibling = p.nextSibling;

            p.nextSibling = null;
            root = compareAndLink( root, p );
        }
    }

    /**
     * Test if the priority queue is logically empty.
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty( )
    {
        return root == null;
    }

    /**
     * Returns number of items stored in the priority queue.
     * @return size of the priority queue.
     */
    public int size( )
    {
        return theSize;
    }

    /**
     * Make the priority queue logically empty.
     */
    public void clear( )
    {
        root = null;
        theSize = 0;
    }
    
    /**
     * Private static class for use with PairingHeap.
     */
    private static class PairNode<AnyType> implements Position<AnyType> 
    {
        /**
         * Construct the PairNode.
         * @param theElement the value stored in the node.
         */
        public PairNode( AnyType theElement )
        {
            element     = theElement;
            leftChild   = null;
            nextSibling = null;
            prev        = null;
        }

        /**
         * Returns the value stored at this position.
         * @return the value stored at this position.
         */
        public AnyType getValue( )
        {
            return element;
        }
        
            // Friendly data; accessible by other package routines
        public AnyType    element;
        public PairNode<AnyType>   leftChild;
        public PairNode<AnyType>   nextSibling;
        public PairNode<AnyType>   prev;
    }

    private PairNode<AnyType> root;
    private int theSize;

    /**
     * Internal method that is the basic operation to maintain order.
     * Links first and second together to satisfy heap order.
     * @param first root of tree 1, which may not be null.
     *    first.nextSibling MUST be null on entry.
     * @param second root of tree 2, which may be null.
     * @return result of the tree merge.
     */
    private PairNode<AnyType> compareAndLink( PairNode<AnyType> first, PairNode<AnyType> second )
    {
        if( second == null )
            return first;

        if( comparator.compare(second.element, first.element ) < 0 )
        {
            // Attach first as leftmost child of second
            second.prev = first.prev;
            first.prev = second;
            first.nextSibling = second.leftChild;
            if( first.nextSibling != null )
                first.nextSibling.prev = first;
            second.leftChild = first;
            return second;
        }
        else
        {
            // Attach second as leftmost child of first
            second.prev = first;
            first.nextSibling = second.nextSibling;
            if( first.nextSibling != null )
                first.nextSibling.prev = first;
            second.nextSibling = first.leftChild;
            if( second.nextSibling != null )
                second.nextSibling.prev = second;
            first.leftChild = second;
            return first;
        }
    }

    @SuppressWarnings("unchecked")
	private PairNode<AnyType> [ ] doubleIfFull( PairNode<AnyType> [ ] array, int index )
    {
        if( index == array.length )
        {
            PairNode<AnyType> [ ] oldArray = array;

            array = new PairNode[ index * 2 ];
            for( int i = 0; i < index; i++ )
                array[ i ] = oldArray[ i ];
        }
        return array;
    }
   
        // The tree array for combineSiblings
    @SuppressWarnings("unchecked")
	private PairNode<AnyType> [ ] treeArray =  new PairNode[ 5 ];

    /**
     * Internal method that implements two-pass merging.
     * @param firstSibling the root of the conglomerate;
     *     assumed not null.
     */
    private PairNode<AnyType> combineSiblings( PairNode<AnyType> firstSibling )
    {
        if( firstSibling.nextSibling == null )
            return firstSibling;

            // Store the subtrees in an array
        int numSiblings = 0;
        for( ; firstSibling != null; numSiblings++ )
        {
            treeArray = doubleIfFull( treeArray, numSiblings );
            treeArray[ numSiblings ] = firstSibling;
            firstSibling.prev.nextSibling = null;  // break links
            firstSibling = firstSibling.nextSibling;
        }
        treeArray = doubleIfFull( treeArray, numSiblings );
        treeArray[ numSiblings ] = null;

            // Combine subtrees two at a time, going left to right
        int i = 0;
        for( ; i + 1 < numSiblings; i += 2 )
            treeArray[ i ] = compareAndLink( treeArray[ i ], treeArray[ i + 1 ] );

            // j has the result of last compareAndLink.
            // If an odd number of trees, get the last one.
        int j = i - 2;
        if( j == numSiblings - 3 )
            treeArray[ j ] = compareAndLink( treeArray[ j ], treeArray[ j + 2 ] );

            // Now go right to left, merging last tree with
            // next to last. The result becomes the new last.
        for( ; j >= 2; j -= 2 )
            treeArray[ j - 2 ] = compareAndLink( treeArray[ j - 2 ], treeArray[ j ] );

        return (PairNode<AnyType>) treeArray[ 0 ];
    }

    
	@Override
	public boolean addAll(Collection<? extends AnyType> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<AnyType> iterator() {
		throw new UnsupportedOperationException();
	}


	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public AnyType element() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean offer(AnyType e) {
		add(e);
		return true;
	}

	
    /**
     * Remove the smallest item from the priority queue.
     * @return the smallest item.
     */
    public AnyType poll( )
    {
        if( isEmpty( ) )
        	return null;
        AnyType x = peek( );
        if( root.leftChild == null )
            root = null;
        else
            root = combineSiblings( root.leftChild );
        theSize--;
        x.setPosition(null);
        return x;
    }



}