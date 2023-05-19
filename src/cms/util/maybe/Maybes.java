package cms.util.maybe;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

class Maybes {

    @SuppressWarnings("rawtypes")
    private static final None theNone = new None();

    private static class None<T> extends Maybe<T> {
        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public T get() throws NoMaybeValue {
            throw NoMaybeValue.theException;
        }

        @Override
        public T orElse(T other) {
            return other;
        }
        
        @Override
        public T orElseGet(Supplier<T> other) {
            return other.get();
        }

        @Override
        public <E extends Throwable> T orElseThrow(E throwable) throws E {
            throw throwable;
        }

        @Override
        public <U> Maybe<U> thenMaybe(Function<T, Maybe<U>> f) {
            return Maybe.none();
        }

        @Override
        public <U> Maybe<U> then(Function<T, U> f) {
            return Maybe.none();
        }

        @Override
        public void thenDo(Consumer<T> cons) {
        }

        @Override
        public void thenElse(Consumer<T> consThen, Runnable procElse) {
            procElse.run();
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<>() {
                public T next() { throw new NoSuchElementException(); }
                public boolean hasNext() { return false; }
            };
        }

        @Override
        public boolean contains(T elem) {
            return false;
        }

        @Override
        public Maybe<T> orElseMaybe(Supplier<Maybe<T>> other) {
            return other.get();
        }
        
        @Override
        public String toString() {
            return "none";
        }
        
        @Override
        public int hashCode() {
            return 0;
        }
    }

    private static class Some<T> extends Maybe<T> {
        private final T value;

        /**
         * Should only be called in Maybes.some.
         *
         * @param v Must not be null
         */
        Some(T v) {
            value = v;
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public T get() {
            return value;
        }
        
        @Override
        public T orElse(T other) {
            return value;
        }
        
        @Override
        public T orElseGet(Supplier<T> other) {
            return value;
        }

        @Override
        public <E extends Throwable> T orElseThrow(E throwable) {
            return value;
        }

        @Override
        public <U> Maybe<U> thenMaybe(Function<T, Maybe<U>> f) {
            return f.apply(value);
        }

        @Override
        public <U> Maybe<U> then(Function<T, U> f) {
            return Maybes.some(f.apply(value));
        }

        @Override
        public void thenDo(Consumer<T> cons) {
            cons.accept(value);
        }

        @Override
        public void thenElse(Consumer<T> consThen, Runnable procElse) {
            consThen.accept(value);
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<>() {
                boolean yielded = false;
                public T next() {
                    if (yielded) throw new NoSuchElementException();
                    yielded = true;
                    return value;
                }
                public boolean hasNext() {
                    return !yielded;
                }
            };
        }

        @Override
        public boolean contains(T elem) {
            return value.equals(elem);
        }

        @Override
        public Maybe<T> orElseMaybe(Supplier<Maybe<T>> other) {
            return this;
        }
        
        @Override
        public String toString() {
            return value.toString();
        }
        
        @Override
        public boolean equals(Object o) {
            return ((o instanceof Some<?>)
                && value.equals(((Some<?>)o).value));
        }
        
        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    @SuppressWarnings("unchecked")
    static <T> Maybe<T> none() {
        return (Maybe<T>) Maybes.theNone;
    }

    /** Creates a Maybe from a non-null argument.
     * @param v must be non-null
     * @throws IllegalArgumentException if a null value is passed to it.
     */
    static <T> Maybe<T> some(T v) {
        if (v == null) {
            throw new IllegalArgumentException("Maybe.some() requires a non-null argument");
        }
        return new Maybes.Some<>(v);
    }
}
