package il.ac.technion.cs.sd.buy.library;


import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FutureStorage {
    CompletableFuture<FutureStorage> getFuture();

    CompletableFuture<String> get(int index);

    CompletableFuture<Integer> size();

    CompletableFuture<Boolean> exists(String id0, String id1);

    CompletableFuture<Boolean> existsBySingleId(String id0);

    CompletableFuture<Optional<String>> getStringByIds(String id0, String id1);

    CompletableFuture<Optional<String>> getSomeStringBySingleId(String id0);

    CompletableFuture<List<String>> getAllStringsById(String id);
}
