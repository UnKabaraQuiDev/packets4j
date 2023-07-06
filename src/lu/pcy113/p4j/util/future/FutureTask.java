package lu.pcy113.p4j.util.future;

@FunctionalInterface
public interface FutureTask<I, O> {
    O complete(I in);

    default <T> FutureTask<I, T> then(FutureTask<O, T> ft) {
        return (in) -> ft.complete(complete(in));
    }
}
