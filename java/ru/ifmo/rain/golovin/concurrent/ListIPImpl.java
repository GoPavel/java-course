package ru.ifmo.rain.golovin.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        private Function<? super T, MapT> mapper;
        private BinaryOperator<MapT> operator;
        private int indexForResult;
        private ArrayList<MapT> result;

        Folding(List<? extends T> list, Function<? super T, MapT> map, BinaryOperator<MapT> op, ArrayList<MapT> res, int resIndex) {
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

    private <T, MapT> MapT runThread(int cntThreads, List<? extends T> values, Function<? super T, MapT> map, BinaryOperator<MapT> op)
        throws  InterruptedException {
        int lenPart = (values.size() + cntThreads - 1) / cntThreads;

        Thread[] threads = new Thread[cntThreads];
        int cntRunned = 0;

        ArrayList<MapT> results = new ArrayList<>(Collections.nCopies(cntThreads, null));

        for (int l = 0, r = lenPart; l < values.size(); l = r, r += lenPart, cntRunned++) {
            threads[cntRunned] = new Thread(new Folding<T, MapT>(values.subList(l, r > values.size() ? values.size() : r), map, op, results, cntRunned));
            threads[cntRunned].start();
        }

        for (int i = 0; i < cntRunned; ++i) {
            try {
                if (threads[i] == null) {
                    System.out.println("NULL " + i);
                }
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
        return runThread(threads, values, o -> o, (o1, o2) -> (Comparator.nullsFirst(comparator).compare(o1, o2) >= 0) ? o1 : o2);
    }

    @Override
    public <T> T
    minimum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        return runThread(threads, values, o -> o, (o1, o2) -> (Comparator.nullsLast(comparator).compare(o1, o2) <= 0) ? o1 : o2);
    }

    @Override
    public <T> boolean
    all(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return this.<T, Boolean>runThread(threads, values, o -> (o != null ? predicate.test(o) : true ), (b1, b2) -> b1 && b2 );
    }

    @Override
    public <T> boolean
    any(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return this.<T, Boolean>runThread(threads, values, o -> (o != null ? predicate.test(o) : false ), (b1, b2) -> b1 || b2);
    }

    @Override
    public String
    join(int threads, List<?> values)
            throws InterruptedException {
        return runThread(threads, values, Object::toString, (s, s2) -> s + s2);
    }

    @Override
    public <T> List<T>
    filter(final int threads, final List<? extends T> values, final Predicate<? super T> predicate)
            throws InterruptedException {
        return runThread(threads, values, o -> predicate.test(o) ? Arrays.asList(o) : Collections.emptyList(),
                (ts, ts2) -> {
                    ts.addAll(ts2);
                    return ts;
                });
    }

    @Override
    public <T, U> List<U>
    map(final int threads, final List<? extends T> values, final Function<? super T, ? extends U> f)
            throws InterruptedException {
        return this.<T, List<U>>runThread(threads, values, o -> Arrays.asList(f.apply(o)),
                (ts, ts2) -> {
                    ts.addAll(ts2);
                    return ts;
                });
    }
}