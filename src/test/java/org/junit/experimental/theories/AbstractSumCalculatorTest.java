package org.junit.experimental.theories;

import org.junit.Assert;

/**
 * @author rgo
 */
public class AbstractSumCalculatorTest {

    @Theory
    public void testSums(CalcTest calcTest) {
        Assert.assertEquals(calcTest.getExpectedSum(), new SumCalculator().getSum(calcTest.getA(), calcTest.getB()));
    }

    @DataPoints
    public static CalcTest[] provideCalcTestCases() {
        return new CalcTest[]{
                new CalcTest(1, 2, 3),
                new CalcTest(Integer.MAX_VALUE, 10, (long) (Integer.MAX_VALUE) + 10L),
                new CalcTest(0, 10, 10),
                new CalcTest(Integer.MIN_VALUE, -10, (long) (Integer.MIN_VALUE) - 10L)
        };
    }

    private static class CalcTest {
        private final int a;
        private final int b;
        private final long expectedSum;

        private CalcTest(int a, int b, long expectedSum) {
            this.a = a;
            this.b = b;
            this.expectedSum = expectedSum;
        }

        public int getA() {
            return a;
        }

        public int getB() {
            return b;
        }

        public long getExpectedSum() {
            return expectedSum;
        }
    }
}
