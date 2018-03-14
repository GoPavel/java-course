package ru.ifmo.rain.golovin.arrayset;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private List<E> elements;
    private Comparator<? super E> comparatorOfSet;

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Collection<? extends E> c) {
        this(c, null);
    }

    public ArraySet(Comparator<? super E> comparator) {
        this(Collections.emptyList(), comparator);
    }

    public ArraySet(Collection<? extends E> c, Comparator<? super E> comparator) {
        comparatorOfSet = comparator;
        Objects.requireNonNull(c);
        if (!c.isEmpty()) {
            ArrayList<? extends E> temp = new ArrayList<>(c);
            temp.sort(comparatorOfSet);
            Iterator<? extends E> it = temp.iterator();
            elements = new ArrayList<>();
            elements.add(it.next());
            while (it.hasNext()) {
                E e = it.next();
                if (compare(e, elements.get(elements.size() - 1)) != 0) {
                    elements.add(e);
                }
            }
        } else {
            elements = Collections.emptyList();
        }
    }

    public ArraySet(Set<E> otherSet) {
        this(otherSet, null);
    }

    private ArraySet(List<E> listView, Comparator<? super E> comparator) { // for create view
        elements = listView;
        comparatorOfSet = comparator;
    }

    private int compare(E e1, E e2) {
        if (e1 instanceof Comparable && e2 instanceof Comparable) {
//            return comparatorOfSet == null ? Comparator.<Comparable>naturalOrder().compare((Comparable)e1, (Comparable)e2) : comparatorOfSet.compare(e1, e2);
            return comparatorOfSet == null ? ((Comparable)e1).compareTo((Comparable)(e2)) : comparatorOfSet.compare(e1, e2);
        } else {
            throw new ClassCastException("E isn't Comparable, then it haven't natural order. Please, use custom comparator");
        }
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
        return (Collections.binarySearch(elements, (E) o, comparatorOfSet) >= 0);
    }

    @Override  //from SortedSet
    public Comparator<? super E> comparator() {
        return comparatorOfSet;
    }


    @Override //from SortedSet
    public E first() {
        if (elements.isEmpty())
            throw new NoSuchElementException();
        return elements.get(0);
    }

    @Override //from SortedSet
    public E last() {
        if (elements.isEmpty())
            throw new NoSuchElementException();
        return elements.get(elements.size() - 1);
    }

    private int binarySearch(E e) {
        return Collections.binarySearch(elements, e, comparatorOfSet);
    }

    private int ceilingIndex(E e) { // find >= e else size()
        int index = binarySearch(e);
        return index >= 0 ? index : (-index - 1);
    }

    private int floorIndex(E e) { //find <= e else -1
        int index = binarySearch(e);
        return index >= 0 ? index : (-index - 1) - 1;
    }

    private int higherIndex(E e) { // find > e else size()
        int index = binarySearch(e);
        return index >= 0 ? index + 1 : (-index - 1);
    }

    private int lowerIndex(E e) { // find < e else -1
        int index = binarySearch(e);
        return index >= 0 ? index - 1 : (-index - 1) - 1;
    }

    @Override
    public E ceiling(E e) { // find >= e else null
        int index = ceilingIndex(Objects.requireNonNull(e));
        return index == elements.size() ? null : elements.get(index);
    }

    @Override //from NavigableSet
    public E floor(E e) { // find <= e else null
        int index = floorIndex(Objects.requireNonNull(e));
        return index == -1 ? null : elements.get(index);
    }

    @Override //from NavigableSet
    public E higher(E e) { // ind > e else null
        int index = higherIndex(Objects.requireNonNull(e));
        return (index == elements.size() ? null : elements.get(index));
    }

    @Override //from NavigableSet
    public E lower(E e) { //find < e else null
        int index = lowerIndex(Objects.requireNonNull(e));
        return index == -1 ? null : elements.get(index);
    }

    @Override //from NavigableSet
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override //from NavigableSet
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(new ReverseListView<>(elements), Collections.reverseOrder(comparatorOfSet));
    }

    @Override //from NavigableSet
    public NavigableSet<E> headSet(E toElement) {
        Objects.requireNonNull(toElement);
        if (isEmpty())
            return new ArraySet<E>(Collections.emptyList(), comparatorOfSet);
        return subSet(first(), true, toElement, false);
    }

    @Override //from NavigableSet
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        Objects.requireNonNull(toElement);
        if (isEmpty())
            return new ArraySet<E>(Collections.emptyList(), comparatorOfSet);
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
    public NavigableSet<E> subSet(E fromElement, E toElement) {
        Objects.requireNonNull(fromElement);
        Objects.requireNonNull(toElement);
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        Objects.requireNonNull(fromElement);
        Objects.requireNonNull(toElement);
        if (isEmpty() ||
                compare(fromElement, toElement) > 0 ||
                (compare(fromElement, toElement) == 0 && (!fromInclusive || !toInclusive))) {
            return new ArraySet<E>(Collections.emptyList(), comparatorOfSet);
        }
        int fromIndex = fromInclusive ? ceilingIndex(fromElement) : higherIndex(fromElement); // [0..size()]
        int toIndex = toInclusive ? floorIndex(toElement) : lowerIndex(toElement); // [-1..size()-1]
        return new ArraySet<>(elements.subList(fromIndex, toIndex + 1), comparatorOfSet); // fromIndex -- inclusive, toIndex -- exclusive
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement) {
        Objects.requireNonNull(fromElement);
        if (isEmpty())
            return new ArraySet<E>(Collections.emptyList(), comparatorOfSet);
        return subSet(fromElement, true, last(), true);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        Objects.requireNonNull(fromElement);
        if (isEmpty())
            return new ArraySet<E>(Collections.emptyList(), comparatorOfSet);
        return subSet(fromElement, inclusive, last(), true);
    }
}