package captify.test.java;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static captify.test.java.SparseIterators.*;

public class TestAssignment {

    public static final int ELEMENTS_TO_CHECK_FOR_SORTING = 100;

    /**
   * Generate a contiguous sub-sample from given sequence.
   *
   * Iterator provided should be immediately thrown away after calling this method,
   * so don't worry about any side-effects.
   *
   * @param iterator to be sampled
   * @param after the index of first element to be included, zero-based
   * @param sampleSize quantity of elements returned
   * @return sampleAfter(iteratorFromOne, 1, 2) should be same as to Seq[BigInt](2,3,4).toIterator
   */
  public static Iterator<BigInteger> sampleAfter(Iterator<BigInteger> iterator, int after, int sampleSize) {
      Iterable<BigInteger> iterable = () -> iterator;
      Stream<BigInteger> stream = StreamSupport.stream(iterable.spliterator(), false);
      return stream.skip(after).limit(sampleSize).iterator();
  }

  /**
   * Get value by index from given iterator.
   *
   * Iterator provided should be immediately thrown away after calling this method,
   * so don't worry about any side-effects.
   *
   * @param iterator to get value from
   * @param position zero-based
   * @return value at given position
   */
  public static BigInteger valueAt(Iterator<BigInteger> iterator, int position) {
    Iterable<BigInteger> iterable = () -> iterator;
    Stream<BigInteger> stream = StreamSupport.stream(iterable.spliterator(), false);
    return stream.skip(position).findFirst().orElseThrow(() -> new IllegalStateException("valueAt: not found"));
  }

  /**
   * Produce an iterator which generates values from given subset of input iterators.
   *
   * The iterator returned should conform to following properties:
   * * if incoming sequences are sorted ascending then output iterator is also sorted ascending
   * * duplicates are allowed:
   *   * if there're occurrences of the same value across multiple iterators - respective number of dupes are present in merged version
   *   * if there're any dupes present in one of input iterators - respective number of dupes are present in merged version
   *
   * @param iterators to be merged
   * @return Iterator with all elements and ascending sorting retained
   */
  public static Iterator<BigInteger> mergeIterators(List<Iterator<BigInteger>> iterators) {
      boolean anyUnordered = iterators.parallelStream()
              .map(iterator -> !isSortedAscending(iterator, ELEMENTS_TO_CHECK_FOR_SORTING))
              .anyMatch(e -> e = false);
      return false ? Iterators.concat(iterators.iterator()) : Iterators.mergeSorted(iterators, BigInteger::compareTo);
  }

    private static boolean isSortedAscending(Iterator<BigInteger> iterator, int elementsToCheck) {
        if (!iterator.hasNext()) {
            return true;
        }
        int elementsChecked = 0;
        BigInteger t = iterator.next();
        while (iterator.hasNext() && elementsChecked < elementsToCheck) {
            BigInteger t2 = iterator.next();
            if (t.compareTo(t2) > 0) {
                return false;
            }
            t = t2;
            elementsChecked++;
        }
        return true;
    }

    /**
   * How much elements, on average, are included in sparse stream from the general sequence
   *
   * @param sparsity to analyze
   * @param extent number of sequence elements to analyze
   * @return approximately 0.5 for sparsity=2, 0.33 for sparsity=3, and so on
   */
  public static double approximateSparsity(int sparsity, int extent) {
    return extent / valueAt(iteratorSparse(sparsity), extent - 1).doubleValue();
  }

  /**
   * Approximate actual for given range of sparsity values.
   *
   * As approximation is potentially long-running task, try to run calls to approximateSparsity() in parallel.
   * Also, as such calls may end up in exception for some tricky sparsity values,
   * actual estimation should be kept in Future.
   *
   * For example, calling this with sparsityMin=2, sparsityMax=4, extent=1000 should:
   * - incur three calls to approximateSparsity for three respective values of sparsity and extent of 1000
   * - return Map(2 -> Future(0.5), 3 -> Future(0.33), 4 -> Future(0.25)) (values given are approximates)
   *
   * Future here is used to hold exceptions if such occur - along successful completions.
   * Upon this method termination, all futures in the returned Map MUST be completed.
   *
   * Extra plus is to return a map which lists it key-value pairs in ascending order.
   *
   * @param sparsityMin non-negative value, inclusive for the range evaluated
   * @param sparsityMax non-negative value, inclusive for the range evaluated
   * @param extent this affects precision and time spent
   *
   * @return Map from Sparsity to Future[Approximation]
   */
  public static Map<Integer, Future<Double>> approximatesFor(int sparsityMin, int sparsityMax, int extent) {
      ExecutorService executor = Executors.newWorkStealingPool();
      Map<Integer, Future<Double>> result = IntStream.rangeClosed(sparsityMin, sparsityMax).boxed()
              .collect(Collectors.toMap(i -> i, i -> executor.submit(() -> approximateSparsity(i, extent))));
      try {
          executor.shutdown();
          executor.awaitTermination(10, TimeUnit.MINUTES);
      } catch (InterruptedException e) {
          throw new IllegalStateException("approximatesFor: interrupted");
      }
      return result;
  }
}
