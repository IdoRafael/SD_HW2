package il.ac.technion.cs.sd.buy.library;


import java.util.Comparator;
import java.util.SortedMap;

public interface FutureStorageFactory {
    FutureStorage create(
            String fileName,
            Comparator<String> firstIdComparator,
            Comparator<String> secondaryIdComparator
    );

    FutureStorage create(
            String fileName,
            Comparator<String> firstIdComparator,
            Comparator<String> secondaryIdComparator,
            SortedMap<String, String> sortedMap
    );
}
