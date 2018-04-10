package ru.ifmo.rain.golovin.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.Integer.min;

public class ListIPImpl implements ListIP {

    private String emptyString = "";

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

    private <T, MapT> MapT runThread(int _cntThreads, List<? extends T> values, Function<? super T, MapT> map, BinaryOperator<MapT> op)
            throws InterruptedException {
        int cntThreads = min(_cntThreads, values.size());
        int lenPart = values.size() / cntThreads;

        Thread[] threads = new Thread[cntThreads];
        int cntRun = 0;

        ArrayList<MapT> results = new ArrayList<>(Collections.nCopies(cntThreads, null));

        int rem = values.size() % cntThreads;
        if (rem > 0) {
            lenPart++;
        }
        for (int l = 0, r = lenPart; l < values.size(); l = r, r += lenPart, cntRun++) {
            threads[cntRun] = new Thread(new Folding<T, MapT>(values.subList(l, r > values.size() ? values.size() : r), map, op, results, cntRun));
            threads[cntRun].start();
            if (rem != 0 && rem == cntRun + 1) {
                lenPart--;
            }
        }

        InterruptedException anyInterruptedException = null;
        for (int i = 0; i < cntRun; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                if (anyInterruptedException == null) {
                    anyInterruptedException = e;
                } else {
                    anyInterruptedException.addSuppressed(e);
                }
            }
        }

        if (anyInterruptedException != null) {
            throw anyInterruptedException;
        }

        return results.stream().filter(Predicate.isEqual(null).negate()).reduce(op).orElse(null);
    }

    @Override
    public <T> T
    maximum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        Objects.requireNonNull(values);
        Objects.requireNonNull(comparator);
        return runThread(threads, values, Function.identity(), (o1, o2) -> (comparator.compare(o1, o2) >= 0) ? o1 : o2);
    }

    @Override
    public <T> T
    minimum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        Objects.requireNonNull(values);
        Objects.requireNonNull(comparator);
        return runThread(threads, values, Function.identity(), (o1, o2) -> (comparator.compare(o1, o2) <= 0) ? o1 : o2);
    }

    @Override
    public <T> boolean
    all(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        Objects.requireNonNull(values);
        Objects.requireNonNull(predicate);
        return this.<T, Boolean>runThread(threads, values, predicate::test, (b1, b2) -> b1 && b2);
    }

    @Override
    public <T> boolean
    any(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        Objects.requireNonNull(values);
        Objects.requireNonNull(predicate);
        return this.<T, Boolean>runThread(threads, values, predicate::test, (b1, b2) -> b1 || b2);
    }

    @Override
    public String
    join(int threads, List<?> values)
            throws InterruptedException {
        Objects.requireNonNull(values);
        return this.<Object, String>runThread(threads, values, o -> o != null ? o.toString() : emptyString, (s1, s2) -> s1 + s2);
    }

    @Override
    public <T> List<T>
    filter(final int threads, final List<? extends T> values, final Predicate<? super T> predicate)
            throws InterruptedException {
        Objects.requireNonNull(values);
        Objects.requireNonNull(predicate);
        return runThread(threads, values, o -> new LinkedList<T>(predicate.test(o) ? Collections.singletonList(o) : Collections.emptyList()),
                (ts1, ts2) -> {
                    ts1.addAll(ts2);
                    return ts1;
                });
    }

    @Override
    public <T, U> List<U>
    map(final int threads, final List<? extends T> values, final Function<? super T, ? extends U> f)
            throws InterruptedException {
        Objects.requireNonNull(values);
        Objects.requireNonNull(f);
        return this.<T, List<U>>runThread(threads, values, o -> new LinkedList<>(Collections.singletonList(f.apply(o))),
                (ts1, ts2) -> {
                    ts1.addAll(ts2);
                    return ts1;
                });
    }
}