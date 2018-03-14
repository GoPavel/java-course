package ru.ifmo.rain.golovin.arrayset;

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.List;

public class ReverseListView<E> extends AbstractList<E> {
    private List<E> view;
    private boolean reverseFlag;

    ReverseListView(List<E> otherList) {
        if (otherList instanceof ReverseListView) {
            view = ((ReverseListView) otherList).view;
            reverseFlag = !((ReverseListView) otherList).reverseFlag;
        } else {
            view = otherList;
            reverseFlag = true;
        }
    }

    @Override
    public int size() {
        return view.size();
    }

    @Override
    public E get(int i) {
        return reverseFlag ? view.get(view.size() - 1 - i) : view.get(i);
    }
}