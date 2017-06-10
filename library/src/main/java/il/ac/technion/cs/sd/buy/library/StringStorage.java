package il.ac.technion.cs.sd.buy.library;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import il.ac.technion.cs.sd.buy.ext.FutureLineStorage;
import il.ac.technion.cs.sd.buy.ext.FutureLineStorageFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.completedFuture;


public class StringStorage implements Storage {
    private CompletableFuture<FutureLineStorage> futureLineStorage;

    private static final String DELIMITER = ",";

    @AssistedInject
    public StringStorage(
            FutureLineStorageFactory lineStorageFactory,
            @Assisted String fileName
    ) {
        this.futureLineStorage = lineStorageFactory.open(fileName);
    }

    @AssistedInject
    public StringStorage(
            FutureLineStorageFactory lineStorageFactory,
            @Assisted String fileName,
            @Assisted SortedMap<String, String> sortedMap
    ) {
        this(lineStorageFactory, fileName);

        CompletableFuture<Void> w = completedFuture(null);

        for(Map.Entry<String,String> entry : sortedMap.entrySet()) {
            w = w.thenCompose(x -> futureLineStorage)
                    .thenCompose(ls -> ls.appendLine(String.join(DELIMITER, entry.getKey(), entry.getValue())));
        }
    }

    public CompletableFuture<String> get(int index) {
        return futureLineStorage.thenCompose(ls -> ls.read(index));
    }

    public CompletableFuture<Integer> size() {
        return futureLineStorage.thenCompose(ls -> ls.numberOfLines());
    }

    @Override
    public CompletableFuture<Boolean> exists(String id0, String id1) {
        return getStringByIds(id0, id1)
                .thenApply(Optional::isPresent);
    }

    @Override
    public CompletableFuture<Optional<String>> getStringByIds(String id0, String id1) {
        return findIndexByTwoKeys(id0, id1)
                .thenCompose(indexFound -> {
                    if (indexFound.isPresent()) {
                        return get(indexFound.getAsInt()).thenApply(Optional::of);
                    } else {
                        return completedFuture(Optional.empty());
                    }
                });
    }

    @Override
    public CompletableFuture<List<String>> getAllStringsById(String id) {
        return findIndexBySingleKey(id).thenCompose(indexFound -> {
            if (indexFound.isPresent()) {
                CompletableFuture<LinkedList<String>> before = getAllBeforeWithSameKey(indexFound.getAsInt(), id);
                CompletableFuture<LinkedList<String>> after = getAllAfterWithSameKey(indexFound.getAsInt(), id);
                return before.thenCombine(after, (beforeList, afterList) -> {
                    beforeList.addAll(afterList);
                    return beforeList;
                });
            }
            return completedFuture(new LinkedList<>());
        });
    }

    private CompletableFuture<LinkedList<String>> getAllBeforeWithSameKey(int i, String key) {
        if (i < 0) {
            return completedFuture(new LinkedList<>());
        }
        return get(i).thenCompose(found -> {
            if (found.split(DELIMITER)[0].equals(key)) {
                return getAllBeforeWithSameKey(i-1, key).thenApply(list -> {list.addLast(found); return list;});
            } else {
                return completedFuture(new LinkedList<>());
            }
        });
    }

    private CompletableFuture<LinkedList<String>> getAllAfterWithSameKey(int i, String key) {
        return size().thenCompose(size -> {
            if (i >= size) {
                return completedFuture(new LinkedList<>());
            } else {
                return get(i).thenCompose(found -> {
                    if (found.split(DELIMITER)[0].equals(key)) {
                        return getAllAfterWithSameKey(i+1, key).thenApply(list -> {list.addFirst(found); return list;});
                    } else {
                        return completedFuture(new LinkedList<>());
                    }
                });
            }
        });
    }

    private CompletableFuture<OptionalInt> findIndexByTwoKeys(String key0, String key1) {
        return binarySearch(
                String.join(DELIMITER, key0, key1),
                Comparator.comparing((String s) -> s.split(DELIMITER)[0])
                        .thenComparing((String s)-> s.split(DELIMITER)[1])
        );
    }

    private CompletableFuture<OptionalInt> findIndexBySingleKey(String key) {
        return binarySearch(
                key,
                Comparator.comparing((String s) -> s.split(DELIMITER)[0])
        );

    }

    private CompletableFuture<OptionalInt> binarySearch(String target, Comparator comparator) {
        return size()
                .thenCompose(size -> binarySearchAux(0, size-1, target, comparator));
    }

    private CompletableFuture<OptionalInt> binarySearchAux(int start, int end, String target, Comparator comparator) {
        int middle = (start + end) / 2;
        if(end < start) {
            return completedFuture(OptionalInt.empty());
        }

        return get(middle)
                .thenCompose( middleValue -> {
                    int comparison = comparator.compare(target, middleValue);

                    if(comparison == 0) {
                        return completedFuture(OptionalInt.of(middle));
                    } else if(comparison < 0) {
                        return binarySearchAux(start, middle - 1, target, comparator);
                    } else {
                        return binarySearchAux(middle + 1, end, target, comparator);
                    }
                }
        );

    }
}
