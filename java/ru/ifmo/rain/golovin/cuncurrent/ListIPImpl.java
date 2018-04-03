package ru.ifmo.rain.golovin.cuncurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ListIPImpl implements ListIP {

    static private class Folding<T, MapT> implements Runnable {

        private Stream<? extends T> values;
        private Function<T, MapT> mapper;
        private BinaryOperator<MapT> operator;
        private int indexForResult;
        private ArrayList<MapT> result;

        Folding(List<? extends T> list, Function<T, MapT> map, BinaryOperator<MapT> op, ArrayList<MapT> res, int resIndex) {
            values = list.stream();
            mapper = map;
            operator = op;
            indexForResult = resIndex;
            result = res;
        }

        @Override
        public void run() {
            result.set(indexForResult, values.map(mapper).reduce(operator).orElse(null));
        }
    }

    private <T, MapT> MapT runThread(int cntThreads, List<? extends T> values, Function<T, MapT> map, BinaryOperator<MapT> op)
        throws  InterruptedException {
        int lenPart = (values.size() + cntThreads - 1) / cntThreads;

        Thread[] threads = new Thread[cntThreads];

        ArrayList<MapT> results = new ArrayList<>(cntThreads);

        for (int l = 0, r = lenPart, i = 0; l < values.size(); l = r, r += lenPart, ++i) {
            threads[i] = new Thread(new Folding<T, MapT>(values.subList(l, r), map, op, results, i));
            threads[i].start();
        }

        for (int i = 0; i < cntThreads; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                throw e;
            }
        }

        return results.stream().reduce(op).orElse(null);
    }

    @Override
    public <T> T
    maximum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        return runThread(threads, values, o -> o, (o1, o2) -> (comparator.compare(o1, o2) >= 0) ? o1 : o2);
    }

    @Override
    public <T> T
    minimum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        return runThread(threads, values, o -> o, (o1, o2) -> (comparator.compare(o1, o2) <= 0) ? o1 : o2);
    }

    @Override
    public <T> boolean
    all(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return runThread(threads, values, predicate, );
    }

    @Override
    public <T> boolean
    any(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {

    }


    @Override
    public String
    join(int threads, List<?> values)
            throws InterruptedException { }

    @Override
    public <T> List<T>
    filter(final int threads, final List<? extends T> values, final Predicate<? super T> predicate)
            throws InterruptedException { }

    @Override
    public <T, U> List<U>
    map(final int threads, final List<? extends T> values, final Function<? super T, ? extends U> f)
            throws InterruptedException { }
}