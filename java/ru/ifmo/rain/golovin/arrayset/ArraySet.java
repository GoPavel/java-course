package ru.ifmo.rain.golovin.arrayset;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public class ArraySet<E extends Comparable<E> > extends AbstractSet<E> implements SortedSet<E> {
    private ArrayList<E> elements;

    public ArraySet(final Set<E> otherSet) {

        HashSet<E> hashSet = new HashSet<>(otherSet);
        elements = new ArrayList<>(hashSet);
        elements.sort(Comparator.naturalOrder());
    }

    private ArraySet(final List<E> arr) {
        elements = new ArrayList<>(arr);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableCollection(elements).iterator();
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(elements, (E    ) o) >= 0;
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return new ArraySet<E>(elements.subList(0, (Collections.binarySearch(elements, toElement))));
    }

    @Override
    public SortedSet<E> tailSet()

}

///TODO
//  сразу реализовывать навигейт
// подумать над ошибками приведения
// подумать над скоростью.