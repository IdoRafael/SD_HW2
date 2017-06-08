package il.ac.technion.cs.sd.buy.library;

import com.google.inject.assistedinject.Assisted;

public interface StorageFactory {
    Storage create(String fileName);

    Storage create(
            @Assisted("fileName") String fileName,
            @Assisted("xmlString") String xmlString,
            @Assisted("xmlQuery") String xmlQuery,
            @Assisted("swapKeys") boolean swapKeys
    );
}
