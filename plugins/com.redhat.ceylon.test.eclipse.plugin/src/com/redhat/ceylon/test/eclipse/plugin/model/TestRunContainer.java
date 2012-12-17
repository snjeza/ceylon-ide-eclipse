package com.redhat.ceylon.test.eclipse.plugin.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.ILaunch;

public class TestRunContainer {
    
    private static final int MAX_RUNS_COUNT = 10;

    private final List<TestRun> testRuns = new ArrayList<TestRun>();
    private final List<TestRunListener> testRunListeners = new ArrayList<TestRunListener>();

    public void addTestRunListener(TestRunListener testRunListener) {
        testRunListeners.add(testRunListener);
    }

    public void removeTestRunListener(TestRunListener testRunListener) {
        testRunListeners.remove(testRunListener);
    }
    
    public List<TestRunListener> getTestRunListeners() {
        return testRunListeners;
    }
    
    public TestRun getTestRun(ILaunch launch) {
        TestRun result = null;

        for (TestRun testRun : testRuns) {
            if (testRun.getLaunch() == launch) {
                result = testRun;
                break;
            }
        }

        return result;
    }

    public TestRun getOrCreateTestRun(ILaunch launch) {
        TestRun result = getTestRun(launch);

        if (result == null) {
            result = new TestRun(launch);
            testRuns.add(0, result);
            fireTestRunAdded(result);
        
            if (testRuns.size() > MAX_RUNS_COUNT) {
                List<TestRun> obsoleteRuns = testRuns.subList(MAX_RUNS_COUNT, testRuns.size());
                for (TestRun obsoleteRun : obsoleteRuns) {
                    if (!obsoleteRun.isRunning()) {
                        testRuns.remove(obsoleteRun);
                        fireTestRunRemoved(obsoleteRun);
                    }
                }
            }
        }

        return result;
    }
    
    private void fireTestRunAdded(TestRun testRun) {
        for (TestRunListener testRunListener : testRunListeners) {
            testRunListener.testRunAdded(testRun);
        }
    }
    
    private void fireTestRunRemoved(TestRun testRun) {
        for (TestRunListener testRunListener : testRunListeners) {
            testRunListener.testRunRemoved(testRun);
        }
    }

}