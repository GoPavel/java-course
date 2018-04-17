package ru.ifmo.rain.golovin.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

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

    private ParallelMapper parallelMapper;

    public ListIPImpl() {
        parallelMapper = null;
    }

    public ListIPImpl(ParallelMapper mapper) {
        parallelMapper = mapper;
    }

    private String emptyString = "";

    static private class Folding<T, MapT> implements Runnable {

        private Stream<? extends T> values;
        private Function<? super T, MapT> mapper;
        private BinaryOperator<MapT> operator;
        private int indexForResult;

        private List<MapT> result;

        Folding(List<? extends T> list, Function<? super T, MapT> map, BinaryOperator<MapT> op, List<MapT> res, int resIndex) {
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

    private <T> List<List<? extends T>> splitListOnThreads(int _cntThreads, List<? extends T> values) {
        int cntThread = min(_cntThreads, values.size());
        int lenPart = values.size() / cntThread;

        LinkedList<List<? extends T>> groups = new LinkedList<>();

        int rem = values.size() % cntThread;
        if (rem > 0)
            lenPart++;
        for (int l = 0, r = lenPart, cntRun = 0; l < values.size(); l = r, r += lenPart, cntRun++) {
            groups.add(values.subList(l, r > values.size() ? values.size() : r));
            if (rem != 0 && rem == cntRun + 1) {
                lenPart--;
            }
        }

        return groups;
    }

    private <T, MapT> MapT runThread(int _cntThreads, List<? extends T> values, Function<? super T, MapT> map, BinaryOperator<MapT> op)
            throws InterruptedException {
        List<List<? extends T>> groups = splitListOnThreads(_cntThreads, values);
        assert (groups.size() <= _cntThreads);

        List<MapT> results;

        if (parallelMapper == null) {

            ArrayList<Thread> threads = new ArrayList<>();

            results = new ArrayList<>(Collections.nCopies(groups.size(), null));

            for (int i = 0; i < groups.size(); ++i) {
                Thread thread = new Thread(new Folding<T, MapT>(groups.get(i), map, op, results, i));
                thread.start();
                threads.add(thread);
            }

            InterruptedException anyInterruptedException = null;
            for (int i = 0; i < groups.size(); ++i) {
                try {
                    threads.get(i).join();
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

        } else {
            results = parallelMapper.map(group -> {
                ArrayList<MapT> res = new ArrayList<>(Collections.nCopies(1, null));
                Folding<T, MapT> f = new Folding<>(group, map, op, res, 0);
                f.run();
                return res.get(0);
            }, groups);
        }
        return results.stream().filter(Predicate.isEqual(null).negate()).reduce(op).orElse(null);
    }

    @Override
    public <T> T
    maximum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        if (!checkNonNullArgument(values)) {
            throw new IllegalArgumentException();
        }
        return runThread(threads, values, Function.identity(), (o1, o2) -> (comparator.compare(o1, o2) >= 0) ? o1 : o2);
    }

    @Override
    public <T> T
    minimum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        if (!checkNonNullArgument(values, comparator)) {
            throw new IllegalArgumentException();
        }
        return runThread(threads, values, Function.identity(), (o1, o2) -> (comparator.compare(o1, o2) <= 0) ? o1 : o2);
    }

    @Override
    public <T> boolean
    all(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        if (!checkNonNullArgument(values, predicate)) {
            throw new IllegalArgumentException();
        }
        return this.<T, Boolean>runThread(threads, values, predicate::test, (b1, b2) -> (b1 == null) ? false : b1 && (b2 == null ? false : b2));
    }

    @Override
    public <T> boolean
    any(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        if (!checkNonNullArgument(values, predicate)) {
            throw new IllegalArgumentException();
        }
        return this.<T, Boolean>runThread(threads, values, predicate::test, (b1, b2) -> b1 || b2);
    }

    @Override
    public String
    join(int threads, List<?> values)
            throws InterruptedException {
        if (!checkNonNullArgument(values)) {
            throw new IllegalArgumentException();
        }
        return this.<Object, String>runThread(threads, values, o -> o != null ? o.toString() : emptyString, (s1, s2) -> s1 + s2);
    }

    @Override
    public <T> List<T>
    filter(final int threads, final List<? extends T> values, final Predicate<? super T> predicate)
            throws InterruptedException {
        if (!checkNonNullArgument(values, predicate)) {
            throw new IllegalArgumentException();
        }
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
        if (!checkNonNullArgument(values, f)) {
            throw new IllegalArgumentException();
        }
        return this.<T, List<U>>runThread(threads, values, o -> new LinkedList<>(Collections.singletonList(f.apply(o))),
                (ts1, ts2) -> {
                    ts1.addAll(ts2);
                    return ts1;
                });
    }

    private <T1, T2>boolean checkNonNullArgument(T1 t1, T2 t2) {
        return  t1 != null && t2 != null;
    }
    private <T1>boolean checkNonNullArgument(T1 t1) {
        return  t1 != null;
    }
}
