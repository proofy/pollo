package org.outerj.pollo.xmleditor.util;

import java.util.ArrayList;

// NOTE: I found this code on the net without any license mentioned
// Meanwhile extended it to sort ArrayLists too.


public class QuickSort {
    public interface Comparator {
        int compare(Object obj1, Object obj2);
    }

    public class StringComparator implements Comparator
    {
        public int compare(Object obj1, Object obj2)
        {
            return ((Comparable)obj1).compareTo(obj2);
        }
    }

    Comparator itsComparator;

    public QuickSort(Comparator comparator) {
        itsComparator = comparator;
    }

    public QuickSort() {
        itsComparator = new StringComparator();
    }

    /** This is a generic version of C.A.R Hoare's Quick Sort 
     * algorithm.  This will handle arrays that are already
     * sorted, and arrays with duplicate keys.<BR>
     *
     * If you think of a one dimensional array as going from
     * the lowest index on the left to the highest index on the right
     * then the parameters to this function are lowest index or
     * left and highest index or right.  The first time you call
     * this function it will be with the parameters 0, a.length - 1.
     *
     * @param a       an Object array
     * @param lo0     left boundary of array partition
     * @param hi0     right boundary of array partition
     */
    private void qsort(Object[] a, int lo0, int hi0) {
        int lo = lo0;
        int hi = hi0;

        if ( hi0 > lo0) {
            /* Arbitrarily establishing partition element as the midpoint of
             * the array.
             */
            Object mid = a[ ( lo0 + hi0 ) / 2 ];

            // loop through the array until indices cross
            while ( lo <= hi ) {
                /* find the first element that is greater than or equal to 
                 * the partition element starting from the left Index.
                 */
                while (( lo < hi0 ) && ( itsComparator.compare(a[lo], mid) < 0 ))
                    ++lo;

                /* find an element that is smaller than or equal to 
                 * the partition element starting from the right Index.
                 */
                while (( hi > lo0 ) && ( itsComparator.compare(a[hi], mid) > 0 ))
                    --hi;

                // if the indexes have not crossed, swap
                if ( lo <= hi ) {
                    swap(a, lo, hi);

                    ++lo;
                    --hi;
                }
            }

            /* If the right index has not reached the left side of array
             * must now sort the left partition.
             */
            if ( lo0 < hi )
                qsort( a, lo0, hi );

            /* If the left index has not reached the right side of array
             * must now sort the right partition.
             */
            if ( lo < hi0 )
                qsort( a, lo, hi0 );
        }
    }

    private static void swap(Object[] a, int i, int j) {
        Object temp = a[i]; 
        a[i] = a[j];
        a[j] = temp;
    }

    public void sort(Object[] a) {
        qsort(a, 0, a.length - 1);
    }

    public void sortPartial(Object[] a, int start) {
        qsort(a, start, a.length - start);
    }

    public void sortPartial(Object[] a, int start, int end) {
        qsort(a, start, end - start + 1);
    }

    public void sort(Object[] a, int length) {
        qsort(a, 0, length - 1);
    }

    public void sort(ArrayList a) {
        qsort(a, 0, a.size() - 1);
    }

    private void qsort(ArrayList a, int lo0, int hi0) {
        int lo = lo0;
        int hi = hi0;

        if ( hi0 > lo0) {
            /* Arbitrarily establishing partition element as the midpoint of
             * the array.
             */
            Object mid = a.get( ( lo0 + hi0 ) / 2 );

            // loop through the array until indices cross
            while ( lo <= hi ) {
                /* find the first element that is greater than or equal to 
                 * the partition element starting from the left Index.
                 */
                while (( lo < hi0 ) && ( itsComparator.compare(a.get(lo), mid) < 0 ))
                    ++lo;

                /* find an element that is smaller than or equal to 
                 * the partition element starting from the right Index.
                 */
                while (( hi > lo0 ) && ( itsComparator.compare(a.get(hi), mid) > 0 ))
                    --hi;

                // if the indexes have not crossed, swap
                if ( lo <= hi ) {
                    swap(a, lo, hi);

                    ++lo;
                    --hi;
                }
            }

            /* If the right index has not reached the left side of array
             * must now sort the left partition.
             */
            if ( lo0 < hi )
                qsort( a, lo0, hi );

            /* If the left index has not reached the right side of array
             * must now sort the right partition.
             */
            if ( lo < hi0 )
                qsort( a, lo, hi0 );
        }
    }

    private static void swap(ArrayList a, int i, int j) {
        Object temp = a.get(i); 
        a.set(i, a.get(j));
        a.set(j, temp);
    }
}
