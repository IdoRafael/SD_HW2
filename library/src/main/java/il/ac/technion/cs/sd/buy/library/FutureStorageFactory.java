package il.ac.technion.cs.sd.buy.library;


import java.util.SortedMap;

public interface FutureStorageFactory {
    FutureStorage create(String fileName);

    FutureStorage create(
            String fileName,
            SortedMap<String, String> sortedMap
    );
}
