package lu.pcy113.p4j.util;

import java.util.Map;

public interface Cancellable {
    
    boolean isCancelled();
    void setCancelled(boolean b);
    Map<Object, Boolean> getCallerClasses();

}
