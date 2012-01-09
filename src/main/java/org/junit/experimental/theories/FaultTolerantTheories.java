package org.junit.experimental.theories;

import org.junit.Assert;
import org.junit.experimental.theories.internal.ParameterizedAssertionError;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.util.LinkedList;
import java.util.List;

/**
 * @author rgo
 */
public class FaultTolerantTheories extends Theories {

    public FaultTolerantTheories(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    public Statement methodBlock(FrameworkMethod method) {
        return new FaultTolerantAnchor(method, getTestClass());
    }

    class FaultTolerantAnchor extends TheoryAnchor {

        private final List<Throwable> failures = new LinkedList<Throwable>();
        private final String methodName;

        private int testsCount;

        FaultTolerantAnchor(FrameworkMethod method, TestClass testClass) {
            super(method, testClass);
            this.methodName = method.getName();
        }

        @Override
        public void evaluate() throws Throwable {
            super.evaluate();
            if (!failures.isEmpty()) {
                printFailures();
                Assert.fail("Theory '" + getTestClass().getJavaClass().getSimpleName() + "." + methodName + "' failed for " + failures.size() +
                            " scenario" + (failures.size() > 1 ? "s" : "") + " out of " + testsCount + ".");
            }
        }

        @Override
        protected void reportParameterizedError(Throwable e, Object... params) throws Throwable {
            if (params.length == 0) {
				failures.add(e);
            } else {
                failures.add(new ParameterizedAssertionError(e, methodName, params));
            }
            handleDataPointSuccess();
        }

        @Override
        protected void handleDataPointSuccess() {
            super.handleDataPointSuccess();
            testsCount++;
        }

        private void printFailures() throws Throwable {
            for (final Throwable failure : failures) {
                Thread errorMessagePrinter = new Thread(
                    new ThreadGroup("FailurePrinters") {
                        @Override
                        public void uncaughtException(Thread t, Throwable e) {
                            Thread.UncaughtExceptionHandler ueh = Thread.getDefaultUncaughtExceptionHandler();
                            if (ueh != null) {
                                ueh.uncaughtException(t, e);
                            } else if (!(e instanceof ThreadDeath)) {
                                e.printStackTrace(System.err);
                            }
                        }
                    },
                    new Runnable() {
                        public void run() {
                            if (failure instanceof Error) {
                                throw (Error)failure;
                            }
                            if (failure instanceof RuntimeException) {
                                throw (RuntimeException)failure;
                            }
                            throw new RuntimeException(failure);
                        }
                    }
                );
                errorMessagePrinter.start();
                errorMessagePrinter.join();
            }
        }
    }
}
