package ru.ifmo.rain.golovin.arrayset;

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.List;

public class ReverseListView<E> extends AbstractList<E> {
    private List<E> view;
    private boolean reverseFlag;

    ReverseListView(List<E> otherList) {
        view = otherList;
        reverseFlag = true;
    }

    ReverseListView(ReverseListView<E> otherList) {
        view = otherList.view;
        reverseFlag = !otherList.reverseFlag;
    }

    public void reverse() {
        reverseFlag = !reverseFlag;
    }

    @Override
    public int size() {
        return view.size();
    }

    @Override
    public E get(int i) {
        return reverseFlag ? view.get(view.size()-1 - i) : view.get(i);
    }
}