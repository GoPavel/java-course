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
import java.util.Set;
import java.util.SortedSet;

public class ArraySet<E extends Comparable<E> > extends AbstractSet<E> implements NavigableSet<E> {
    private ArrayList<E> elements;
    private Comparator<E> comparator;

    public ArraySet() {
        this.elements = new ArrayList<>();
        this.comparator = Comparator.naturalOrder();
    }

    public ArraySet(Collection<? extends E> c) {
    }

    public ArraySet(Comparator<? super E> comparator) {
        this.elements = new ArrayList<>();
        this.comparator = Comparator.naturalOrder();
    }

    public ArraySet(Collection<? extends E> c, Comparator<? super E> comparator) {
        this.comparator = comparator;
        ArrayList<? extends E> temp = new ArrayList<>(c);
        temp.sort(comparator);

        for (Iterator<? extends E> it = temp.iterator();;) {
            E e = it.next();
            elements.add(it.next());
        }
        //TODO

        elements = new ArrayList<>( new HashSet<>(c));
        elements.sort(Comparator.naturalOrder());
    }

    public ArraySet(Set<E> otherSet) {
        //TODO
    }

    private ArraySet(List<E> arr) {
        elements = new ArrayList<>(arr);
    }

    @Override // from AbstractCollection
    public int size() {
        return elements.size();
    }

    @Override //from AbstractCollection
    public Iterator<E> iterator() {
        return Collections.unmodifiableCollection(elements).iterator();
    }

//    @Override // for performance
//    public boolean contains(Object o) {
//        //TODO   return Collections.binarySearch(elements, (E    ) o) >= 0;
//    }


    @Override  //from SortedSet
    public Comparator<? super E> comparator() {
        //TODO
    }


    @Override //from SortedSet
    public E first() {
        //TODO
    }

    @Override //from SortedSet
    public E last() {
        //TODO
    }

    @Override
    public E ceiling(E e) {
        //TODO
    }

    @Override //from NavigableSet
    public Iterator<E> descendingIterator() {
        //TODO
    }

    @Override //from NavigableSet
    public NavigableSet<E> descendingSet() {
        //TODO
    }

    @Override //from NavigableSet
    public E floor(E e) {
        //TODO
    }

    @Override //from NavigableSet
    public NavigableSet<E> headSet(E toElement) {
        //TODO
    }

    @Override //from NavigableSet
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        //TODO
    }

    @Override //from NavigableSet
    public E higher(E e) {
        //TODO
    }

    @Override //from NavigableSet
    public E lower(E e) {
        //TODO
    }

    @Override //from NavigableSet
    public E pollFirst() {
        //TODO
    }

    @Override //from NavigableSet
    public E pollLast() {
        //TODO
    }

    @Override
    public NavigableSet<E> subSet(E formElement, E toElement) {
        //TODO
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        //TODO
    }


    @Override
    public NavigableSet<E> tailSet(E fromElement) {
        //TODO
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        //TODO
    }
}

///TODO
////  сразу реализовывать навигейт
//// подумать над ошибками приведения
//// подумать над скоростью.