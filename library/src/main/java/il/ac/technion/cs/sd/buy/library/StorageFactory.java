package il.ac.technion.cs.sd.buy.library;


import com.google.inject.assistedinject.Assisted;

import java.util.SortedMap;

public interface StorageFactory {
    Storage create(String fileName);

    Storage create(
            String fileName,
            SortedMap<String, String> sortedMap
    );
}
