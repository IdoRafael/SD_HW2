package il.ac.technion.cs.sd.buy.library;


import com.google.inject.assistedinject.Assisted;

import java.util.Comparator;
import java.util.SortedMap;

public interface FutureStorageFactory {
    FutureStorage create(
            String fileName,
            @Assisted("firstIdComparator") Comparator<String> firstIdComparator,
            @Assisted("secondaryIdComparator") Comparator<String> secondaryIdComparator
    );

    FutureStorage create(
            String fileName,
            @Assisted("firstIdComparator") Comparator<String> firstIdComparator,
            @Assisted("secondaryIdComparator") Comparator<String> secondaryIdComparator,
            SortedMap<String, String> sortedMap
    );
}
