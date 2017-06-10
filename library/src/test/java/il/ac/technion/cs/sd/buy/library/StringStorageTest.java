package il.ac.technion.cs.sd.buy.library;

import il.ac.technion.cs.sd.buy.ext.FutureLineStorage;
import il.ac.technion.cs.sd.buy.ext.FutureLineStorageFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class StringStorageTest  {
    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    static final Integer LINE_STORAGE_SIZE = 100;

    //log2(size)+1 iterations + 1 for 2nd compare (to check if "equals")
    static final int BINARY_SEARCH_ITERATIONS = (int)(Math.log(LINE_STORAGE_SIZE)/Math.log(2)) + 2;
    final int AMOUNT_TO_RETURN = 10;

    private static FutureLineStorageFactory setupLineStorageFactoryMock(final FutureLineStorage lineStorage) throws InterruptedException {
        final int SLEEP_DURATION = 100;
        Mockito.doAnswer(invocationOnMock -> {
            try {
                Thread.sleep(SLEEP_DURATION);
            } catch (InterruptedException e) {

            }
            return completedFuture(LINE_STORAGE_SIZE);
        }).when(lineStorage).numberOfLines();


        Mockito.doAnswer(invocationOnMock -> {
            try {
                Thread.sleep(SLEEP_DURATION / 2);
            } catch (InterruptedException e) {

            }
            int i = (int)invocationOnMock.getArguments()[0];
            return completedFuture(String.join(",", "" + i/10, "" + i%10, "" + i%10));
        }).when(lineStorage).read(Mockito.anyInt());

        Mockito.doAnswer(invocationOnMock -> completedFuture(null)).when(lineStorage).appendLine(Mockito.anyString());

        SortedMap<String, String> sortedMap = new TreeMap<>();
        IntStream.range(0, LINE_STORAGE_SIZE)
                .forEach(i -> sortedMap.put("" + i, ""));

        return s -> {
            try {
                Thread.sleep(SLEEP_DURATION);
            } catch (InterruptedException e) {

            }
            return completedFuture(lineStorage);
        };
    }

    private static StringStorage setupStringStorage(final FutureLineStorage lineStorage) throws InterruptedException {
        return new StringStorage(setupLineStorageFactoryMock(lineStorage), "");
    }

    private void existTest(String id0, String id1, boolean exists) throws InterruptedException, ExecutionException {
        FutureLineStorage lineStorage = Mockito.mock(FutureLineStorage.class);
        StringStorage stringStorage = setupStringStorage(lineStorage);

        assertEquals(exists, stringStorage.exists(id0, id1).get().booleanValue());

        Mockito.verify(lineStorage, Mockito.atMost(BINARY_SEARCH_ITERATIONS)).read(Mockito.anyInt());
    }

    private void getSingleStringsByIdTest(String id0, String id1) throws InterruptedException, ExecutionException {
        FutureLineStorage lineStorage = Mockito.mock(FutureLineStorage.class);
        StringStorage stringStorage = setupStringStorage(lineStorage);

        CompletableFuture<Optional<String>> futureResult = stringStorage.getStringByIds(id0, id1);
        Optional<String> result = futureResult.get();
        String[] stringResults = result.get().split(",");

        assertTrue(result.isPresent());
        assertEquals(id0, stringResults[0]);
        assertEquals(id1, stringResults[1]);

        Mockito.verify(lineStorage, Mockito.atMost(BINARY_SEARCH_ITERATIONS)).read(Mockito.anyInt());
    }

    private void getAllStringsByIdTest(String id) throws InterruptedException, ExecutionException {
        FutureLineStorage lineStorage = Mockito.mock(FutureLineStorage.class);
        StringStorage stringStorage = setupStringStorage(lineStorage);

        CompletableFuture<List<String>> futureResult = stringStorage.getAllStringsById(id);
        List<String> resultList = futureResult.get();
        List<String> expectedList = IntStream.range(0, AMOUNT_TO_RETURN)
                .mapToObj(i -> String.join(",", id.toString(), "" + i, "" + i))
                .collect(Collectors.toList());

        assertEquals(expectedList, resultList);
        Mockito.verify(lineStorage, Mockito.atMost(BINARY_SEARCH_ITERATIONS + AMOUNT_TO_RETURN)).read(Mockito.anyInt());
    }

    private void missSingleStringsByIdTest(String id0, String id1) throws InterruptedException, ExecutionException {
        FutureLineStorage lineStorage = Mockito.mock(FutureLineStorage.class);
        StringStorage stringStorage = setupStringStorage(lineStorage);

        CompletableFuture<Optional<String>> futureResult = stringStorage.getStringByIds(id0, id1);
        Optional<String> result = futureResult.get();

        assertFalse(result.isPresent());

        Mockito.verify(lineStorage, Mockito.atMost(BINARY_SEARCH_ITERATIONS)).read(Mockito.anyInt());
    }

    private void missAllStringsByIdTest(String id) throws InterruptedException, ExecutionException {
        FutureLineStorage lineStorage = Mockito.mock(FutureLineStorage.class);
        StringStorage stringStorage = setupStringStorage(lineStorage);

        CompletableFuture<List<String>> futureResult = stringStorage.getAllStringsById(id);
        List<String> resultList = futureResult.get();

        assertTrue(resultList.isEmpty());
        Mockito.verify(lineStorage, Mockito.atMost(BINARY_SEARCH_ITERATIONS + AMOUNT_TO_RETURN)).read(Mockito.anyInt());
    }

    @Test
    public void shouldAppendRightAmountOfLines() throws Exception {
        FutureLineStorage lineStorage = Mockito.mock(FutureLineStorage.class);
        SortedMap<String, String> sortedMap = new TreeMap<>();

        IntStream.range(0, LINE_STORAGE_SIZE)
                .forEach(i -> sortedMap.put("" + i, ""));

        StringStorage stringStorage = new StringStorage(
                setupLineStorageFactoryMock(lineStorage),
                "",
                sortedMap
        );

        Mockito.verify(lineStorage, Mockito.times(LINE_STORAGE_SIZE)).appendLine(Mockito.anyString());
    }

    @Test
    public void shouldExistInStart() throws InterruptedException, ExecutionException {
        existTest("0", "0", true);
    }

    @Test
    public void shouldExistInMiddle() throws InterruptedException, ExecutionException {
        existTest("5" , "5", true);
    }

    @Test
    public void shouldExistInEnd() throws InterruptedException, ExecutionException {
        existTest("9", "9", true);
    }

    @Test
    public void shouldntExistInStart() throws InterruptedException, ExecutionException {
        existTest("", "0", false);
    }

    @Test
    public void shouldntExistInMiddle() throws InterruptedException, ExecutionException {
        existTest("50" , "5", false);
    }

    @Test
    public void shouldntExistInEnd() throws InterruptedException, ExecutionException {
        existTest("9", "999", false);
    }

    @Test
    public void shouldFindSingleInStart() throws InterruptedException, ExecutionException {
        getSingleStringsByIdTest("0", "0");
    }

    @Test
    public void shouldFindSingleInMiddle() throws InterruptedException, ExecutionException {
        getSingleStringsByIdTest("5" , "5");
    }

    @Test
    public void shouldFindSingleInEnd() throws InterruptedException, ExecutionException {
        getSingleStringsByIdTest("9", "9");
    }

    @Test
    public void shouldMissSingleInStart() throws InterruptedException, ExecutionException {
        missSingleStringsByIdTest("", "0");
    }

    @Test
    public void shouldMissSingleInMiddle() throws InterruptedException, ExecutionException {
        missSingleStringsByIdTest("50" , "5");
    }

    @Test
    public void shouldMissSingleInEnd() throws InterruptedException, ExecutionException {
        missSingleStringsByIdTest("9", "999");
    }

    @Test
    public void shouldFindGroupInStart() throws InterruptedException, ExecutionException {
        getAllStringsByIdTest("0");
    }

    @Test
    public void shouldFindGroupInMiddle() throws InterruptedException, ExecutionException {
        getAllStringsByIdTest("5");
    }

    @Test
    public void shouldFindGroupInEnd() throws InterruptedException, ExecutionException {
        getAllStringsByIdTest("9");
    }

    @Test
    public void shouldMissGroupInStart() throws InterruptedException, ExecutionException {
        missAllStringsByIdTest("");
    }

    @Test
    public void shouldMissGroupInMiddle() throws InterruptedException, ExecutionException {
        missAllStringsByIdTest("50");
    }

    @Test
    public void shouldMissGroupInEnd() throws InterruptedException, ExecutionException {
        missAllStringsByIdTest("999");
    }
}
