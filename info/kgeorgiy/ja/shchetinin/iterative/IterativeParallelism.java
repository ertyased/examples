package info.kgeorgiy.ja.shchetinin.iterative;

import info.kgeorgiy.java.advanced.iterative.AdvancedIP;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.*;

public class IterativeParallelism implements AdvancedIP {

    private <T, U> U reduce0(int amThreads, List<T> values, Supplier<U> identity, BiFunction<U, T, U> function, BinaryOperator<U> reduceReduce, int step) throws InterruptedException {
        // :NOTE: Функция слишком много чего делает, стоит упростить
        if (values.isEmpty() || amThreads < 1 || step < 1) {
            // :NOTE: maybe throw?
            return identity.get();
        }
        int up = values.size() / amThreads;
        List<AbstractThread<T, U>> threads = new ArrayList<>();
        int left = 0;
        for (int i = 0; i < amThreads; i++) {
            int right = left + up;
            if (i < values.size() % amThreads) {
                right++;
            }
            right = Math.min(values.size(), right);
            if (left % step != 0) {
                left += (step - left % step);
            }
            left = Math.min(right, left);
            // :NOTE: Красиво
            threads.add(new AbstractThread<>(values.subList(left, right), step, identity.get(), function));
            threads.get(i).start();
            left = right;
        }
        List<U> accumulators = new ArrayList<>();
        for (int i = 0; i < amThreads; ++i) {
            AbstractThread<T, U> thread = threads.get(i);
            try {
                thread.join();
            } catch (InterruptedException e) {
                for (Thread thread1 : threads) {
                    thread1.interrupt();
                }
                Thread.currentThread().interrupt();
                // :NOTE: thread leak
                throw new InterruptedException("Error while join threads");
            }
            accumulators.add(thread.getAccumulator());
        }
        U result = accumulators.get(0);
        for (int i = 1; i < accumulators.size(); ++i) {
            result = reduceReduce.apply(result, accumulators.get(i));
        }
        return result;
    }

    private <T, U> U reduce1(int amThreads, List<T> values, U identity, BiFunction<U, T, U> function, BinaryOperator<U> reduceReduce, int step) throws InterruptedException {
        return reduce0(amThreads, values, () -> identity, function, reduceReduce, step);
    }

    @Override
    public <T> T reduce(int threads, List<T> values, T identity, BinaryOperator<T> operator, int step) throws InterruptedException {
        return reduce1(threads, values, identity, operator, operator, step);
    }

    @Override
    public <T, R> R mapReduce(int threads, List<T> values, Function<T, R> lift, R identity, BinaryOperator<R> operator, int step) throws InterruptedException {
        return reduce1(threads, values, identity, (r, t) -> operator.apply(r, lift.apply(t)), operator, step);
    }

    @Override
    public String join(int threads, List<?> values, int step) throws InterruptedException {
        return reduce0(threads, values, StringBuilder::new,
                (stringBuilder, o) -> {
                    stringBuilder.append(o);
                    return stringBuilder;
                },
                (stringBuilder, stringBuilder2) -> {
                    stringBuilder.append(stringBuilder2);
                    return stringBuilder;
                }, step).toString();
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate, int step) throws InterruptedException {
        return reduce0(threads, values, ArrayList::new,
                (us, t) -> {
                    if (predicate.test(t)) {
                        us.add(t);
                    }
                    return us;
                },
                (us, us2) -> {
                    us.addAll(us2);
                    return us;
                }, step);
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f, int step) throws InterruptedException {
        return reduce0(threads, values, ArrayList::new,
                (us, t) -> {
                    us.add(f.apply(t));
                    return us;
                },
                (us, us2) -> {
                    us.addAll(us2);
                    return us;
                }, step);
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator, int step) throws InterruptedException {
        return reduce(threads, values, null, (t1, t2) -> {
            if (t1 == null) {
                return t2;
            }
            if (t2 == null) {
                return t1;
            }
            return comparator.compare(t1, t2) >= 0 ? t1 : t2;
        }, step);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator, int step) throws InterruptedException {
        return reduce(threads, values, null, (t1, t2) -> {
            if (t1 == null) {
                return t2;
            }
            if (t2 == null) {
                return t1;
            }
            return comparator.compare(t1, t2) <= 0 ? t1 : t2;
        }, step);
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate, int step) throws InterruptedException {
        // :NOTE: !any(predicate.negate())
        return reduce1(threads, values, Boolean.TRUE, (u, t1) -> u & predicate.test(t1), Boolean::logicalAnd, step);
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate, int step) throws InterruptedException {
        return reduce1(threads, values, Boolean.FALSE, (u, t1) -> u | predicate.test(t1), Boolean::logicalOr, step);
    }

    @Override
    public <T> int count(int threads, List<? extends T> values, Predicate<? super T> predicate, int step) throws InterruptedException {
        return reduce1(threads, values, 0, (u, t1) -> {
            if (predicate.test(t1)) {
                u++;
            }
            return u;
        }, Integer::sum, step);
    }

}
