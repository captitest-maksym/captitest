package captify.test.java;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestAssignmentTest {
    @Test
    public void testValueAt() {
        List<BigInteger> list = Arrays.asList(BigInteger.ONE, BigInteger.TEN);
        assertEquals(BigInteger.TEN, TestAssignment.valueAt(list.iterator(), 1));
    }

    @Test
    public void testSampleAfter() {
        List<BigInteger> list = Arrays.asList(BigInteger.ZERO, BigInteger.ONE, BigInteger.TEN);
        List<BigInteger> listToCompare = Arrays.asList(BigInteger.ONE, BigInteger.TEN);
        Iterator<BigInteger> iterator = TestAssignment.sampleAfter(list.iterator(), 1, 2);
        assertEquals(listToCompare.get(0), iterator.next());
        assertEquals(listToCompare.get(1), iterator.next());
    }
}
