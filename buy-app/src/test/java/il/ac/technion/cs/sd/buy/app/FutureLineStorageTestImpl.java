package il.ac.technion.cs.sd.buy.app;

import il.ac.technion.cs.sd.buy.ext.FutureLineStorage;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class FutureLineStorageTestImpl implements FutureLineStorage{
    ArrayList<String> lines = new ArrayList<>();

    private static final int NUMBER_OF_LINES_SLEEP_DURATION = 100;

    @Override
    public CompletableFuture<Void> appendLine(String s) {
        lines.add(s);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<String> read(int i) {
        String toReturn = lines.get(i);
        try {
            Thread.sleep(toReturn.length());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(toReturn);
    }

    @Override
    public CompletableFuture<Integer> numberOfLines() {
        try {
            Thread.sleep(NUMBER_OF_LINES_SLEEP_DURATION);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(lines.size());
    }
}
