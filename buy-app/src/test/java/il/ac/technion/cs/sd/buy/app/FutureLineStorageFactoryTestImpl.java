package il.ac.technion.cs.sd.buy.app;

import il.ac.technion.cs.sd.buy.ext.FutureLineStorage;
import il.ac.technion.cs.sd.buy.ext.FutureLineStorageFactory;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class FutureLineStorageFactoryTestImpl implements FutureLineStorageFactory {
    private static final int SLEEP_DURATION = 100;

    private HashMap<String, CompletableFuture<FutureLineStorage>>
            openLineStorages = new HashMap<>();

    @Override
    public CompletableFuture<FutureLineStorage> open(String s) {
        openLineStorages.putIfAbsent(s,
                completedFuture(new FutureLineStorageTestImpl())
        );

        try {
            Thread.sleep(openLineStorages.size() * SLEEP_DURATION);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return openLineStorages.get(s);
    }
}
