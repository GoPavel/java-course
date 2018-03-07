package ru.ifmo.rain.golovin.arrayset;

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.List;

public class ReverseListView<E> extends AbstractList<E> {
    List<E> view;

    ReverseListView(List<E> otherList) {
        view = otherList;
    }

    @Override
    public int size() {
        return view.size();
    }

    @Override
    public E get(int i) {
        return view.get(view.size()-1 - i);
    }
}