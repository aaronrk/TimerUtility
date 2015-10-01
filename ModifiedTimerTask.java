/*
 * Copyright 2015 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package project.somepackagename;

import java.util.Date;

/**
 * A task that can be scheduled for one-time or repeated execution by a Timer.
 *
 * @author Josh Bloch
 * @see java.util.Timer
 * @since 1.3
 */

public class TimerTask extends java.util.TimerTask {
    /**
     * This object is used to control access to the TimerTask internals.
     */
    final Object lock = new Object();

    /**
     * The internal object of the conventional TimerTask from java.util.
     */
    java.util.TimerTask internalTask;
    /**
     * The state of this task, chosen from the constants below.
     */
    int state = VIRGIN;

    /**
     * This task has not yet been scheduled.
     */
    static final int VIRGIN = 0;

    /**
     * This task is scheduled for execution.  If it is a non-repeating task,
     * it has not yet been executed.
     */
    static final int SCHEDULED = 1;

    /**
     * This non-repeating task has already executed (or is currently
     * executing) and has not been cancelled.
     */
    static final int EXECUTED = 2;

    /**
     * This task has been cancelled (with a call to TimerTask.cancel).
     */
    static final int CANCELLED = 3;

    /**
     * Next execution time for this task in the format returned by
     * System.currentTimeMillis, assuming this task is scheduled for execution.
     * For repeating tasks, this field is updated prior to each task execution.
     */
    long nextExecutionTime;

    /**
     * Period in milliseconds for repeating tasks.  A positive value indicates
     * fixed-rate execution.  A negative value indicates fixed-delay execution.
     * A value of 0 indicates a non-repeating task.
     */
    long period = 0;

    /**
     * Creates a new timer task.
     */
    protected TimerTask(java.util.TimerTask internalTask) {
        this.internalTask = internalTask;
    }

    /**
     * The action to be performed by this timer task.
     */
    @Override
    public void run() {
        //FIXME cannot find another way to access the internal state so this is the only way to check if the inner task
        //has been cancelled this is why no repeated execution is allowed in manuallyAdvancingTimer.
        System.out.println("Run called");
        if(internalTask.cancel() == true) {
            internalTask.run();
            System.out.println("Execution proceeded as planned.");
            return;
        } else {
            System.out.println("Execution was skipped.");
            return;
        }
    }

    /**
     * Cancels this timer task.  If the task has been scheduled for one-time
     * execution and has not yet run, or has not yet been scheduled, it will
     * never run.  If the task has been scheduled for repeated execution, it
     * will never run again.  (If the task is running when this call occurs,
     * the task will run to completion, but will never run again.)
     * <p/>
     * <p>Note that calling this method from within the <tt>run</tt> method of
     * a repeating timer task absolutely guarantees that the timer task will
     * not run again.
     * <p/>
     * <p>This method may be called repeatedly; the second and subsequent
     * calls have no effect.
     *
     * @return true if this task is scheduled for one-time execution and has
     * not yet run, or this task is scheduled for repeated execution.
     * Returns false if the task was scheduled for one-time execution
     * and has already run, or if the task was never scheduled, or if
     * the task was already cancelled.  (Loosely speaking, this method
     * returns <tt>true</tt> if it prevents one or more scheduled
     * executions from taking place.)
     */
    @Override
    public boolean cancel() {
        synchronized (lock) {
            boolean result = (state == SCHEDULED);
            state = CANCELLED;
            internalTask.cancel();
            return result;
        }
    }

    /**
     * Returns the <i>scheduled</i> execution time of the most recent
     * <i>actual</i> execution of this task.  (If this method is invoked
     * while task execution is in progress, the return value is the scheduled
     * execution time of the ongoing task execution.)
     * <p/>
     * <p>This method is typically invoked from within a task's run method, to
     * determine whether the current execution of the task is sufficiently
     * timely to warrant performing the scheduled activity:
     * <pre>{@code
     *   public void run() {
     *       if (System.currentTimeMillis() - scheduledExecutionTime() >=
     *           MAX_TARDINESS)
     *               return;  // Too late; skip this execution.
     *       // Perform the task
     *   }
     * }</pre>
     * This method is typically <i>not</i> used in conjunction with
     * <i>fixed-delay execution</i> repeating tasks, as their scheduled
     * execution times are allowed to drift over time, and so are not terribly
     * significant.
     *
     * @return the time at which the most recent execution of this task was
     * scheduled to occur, in the format returned by Date.getTime().
     * The return value is undefined if the task has yet to commence
     * its first execution.
     * @see Date#getTime()
     */
    @Override
    public long scheduledExecutionTime() {
        synchronized (lock) {
            return (period < 0 ? nextExecutionTime + period
                    : nextExecutionTime - period);
        }
    }
}
