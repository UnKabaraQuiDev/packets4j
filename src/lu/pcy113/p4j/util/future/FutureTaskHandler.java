package lu.pcy113.p4j.util.future;

import java.util.LinkedHashMap;

public class FutureTaskHandler extends Thread {
    
    private LinkedHashMap<Object, FutureTask<?, ?>> queue = new LinkedHashMap<>();

    public void <I> append(FutureTask<I, O> ft, I in) {
        queue.put(in, ft);
    }

}
