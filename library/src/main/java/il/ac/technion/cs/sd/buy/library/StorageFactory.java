package il.ac.technion.cs.sd.buy.library;


import java.util.SortedMap;

public interface StorageFactory {
    Storage create(String fileName);

    Storage create(
            String fileName,
            SortedMap<String, String> sortedMap
    );
}
