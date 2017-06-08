package il.ac.technion.cs.sd.buy.library;


import java.util.List;
import java.util.Optional;

public interface Storage {
    boolean exists(String id0, String id1);

    Optional<String> getStringByIds(String id0, String id1);

    List<String> getAllStringsById(String id);
}
