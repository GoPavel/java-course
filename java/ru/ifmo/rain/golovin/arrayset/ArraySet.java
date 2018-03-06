package ru.ifmo.rain.golovin.arrayset;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class ArraySet<E extends Comparable<E> > extends AbstractSet<E> implements NavigableSet<E> {
    private List<E> elements;
    private Comparator<? super E> comparatorOfSet;

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Collection<? extends E> c) {
        this(c, Comparator.naturalOrder());
    }

    public ArraySet(Comparator<? super E> comparator) {
        this(Collections.emptyList(), comparator);
    }

    public ArraySet(Collection<? extends E> c, Comparator<? super E> comparator) {
        comparatorOfSet = comparator == null ? Comparator.naturalOrder() : comparator;
        Objects.requireNonNull(c); //TODO ask about with statement
        if (!c.isEmpty()) {
            ArrayList<? extends E> temp = new ArrayList<>(c);
            temp.sort(comparator);
            Iterator<? extends E> it = temp.iterator();
            elements.add(it.next());
            while(it.hasNext()) {
                E e = it.next();
                if(comparator.compare(e, elements.get(elements.size()-1)) != 0) {
                    elements.add(e);
                }
            }
        } else {
            elements = Collections.emptyList();
        }
    }

    public ArraySet(Set<E> otherSet) {
        this(otherSet, Comparator.naturalOrder());
    }

    private ArraySet(List<E> listView, Comparator<? super E> comparator) { // for create view
        elements = listView;
        comparatorOfSet = comparator;
    }

    @Override // from AbstractCollection
    public int size() {
        return elements.size();
    }

    @Override //from AbstractCollection
    public Iterator<E> iterator() {
        return Collections.unmodifiableCollection(elements).iterator();
    }

    @SuppressWarnings("UncheckedCast")
    @Override // for performance
    public boolean contains(Object o) {
        return (Collections.binarySearch(elements, (E)o,comparatorOfSet) >= 0);
        // TODO may be error with emtpyList
    }

    @Override  //from SortedSet
    public Comparator<? super E> comparator() {
        return comparatorOfSet;
    }


    @Override //from SortedSet
    public E first() {
        return elements.get(0);
    }

    @Override //from SortedSet
    public E last() {
        return elements.get(elements.size() - 1);
    }

    private int ceilingIndex(E e) { // find >= e else size()
        int index = Collections.binarySearch(elements, e, comparatorOfSet);
        return index >= 0 ? index : (-index - 1);
    }

    private int floorIndex(E e) { //find <= e else -1
        int index = Collections.binarySearch(elements, e, comparatorOfSet);
        return index >= 0 ? index : (-index - 1) - 1;
    }

    private int higherIndex(E e) { // find > e else size()
        int index = Collections.binarySearch(elements, e, comparatorOfSet);
        return index >= 0 ? index+1 : (-index - 1);
    }

    private int lowerIndex(E e) { // find < e else -1
        int index = Collections.binarySearch(elements, e, comparatorOfSet);
        return index >= 0 ? index-1 : (-index - 1) - 1;
    }

    @Override
    public E ceiling(E e) { // find >= e else null
        int index = ceilingIndex(e);
        return index == elements.size() ? null : elements.get(index);
    }

    @Override //from NavigableSet
    public E floor(E e) { // find <= e else null
        int index = floorIndex(e);
        return index == -1 ? null : elements.get(index);
    }

    @Override //from NavigableSet
    public E higher(E e) { // ind > e else null
        int index = higherIndex(e);
        return (index == elements.size() ? null : elements.get(index));
    }

    @Override //from NavigableSet
    public E lower(E e) { //find < e else null
        int index = lowerIndex(e);
        return index == -1 ? null : elements.get(index);
    }

    @Override //from NavigableSet
    public Iterator<E> descendingIterator() {
        //TODO read doc
    }

    @Override //from NavigableSet
    public NavigableSet<E> descendingSet() {
        //TODO read doc
    }

    @Override //from NavigableSet
    public NavigableSet<E> headSet(E toElement) {
        return subSet(first(), true, toElement, false);
    }

    @Override //from NavigableSet
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return subSet(first(), true, toElement, inclusive);
    }

    @Override //from NavigableSet
    public E pollFirst() {
        throw new UnsupportedOperationException();
    }
    @Override //from NavigableSet
    public E pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<E> subSet(E formElement, E toElement) {
        return subSet(formElement, true, toElement, false);
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        if (comparatorOfSet.compare(fromElement, toElement) > 0)
            throw new IllegalArgumentException("fromKey > toKey");
        int fromIndex = fromInclusive ? ceilingIndex(fromElement) : higherIndex(fromElement); // [0..size()]
        int toIndex = toInclusive ? floorIndex(toElement) : lowerIndex(toElement); // [-1..size()-1]
        return new ArraySet<>(elements.subList(fromIndex, toIndex + 1), comparatorOfSet); // fromIndex -- inclusive, toIndex -- exclusive
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement) {
        return subSet(fromElement, false, last(), true);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return subSet(fromElement, false, last(), inclusive);
    }
}

//TODO
// +- null check
// +- outOfBoundExc