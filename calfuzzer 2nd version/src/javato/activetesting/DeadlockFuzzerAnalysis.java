package javato.activetesting;

import javato.activetesting.activechecker.ActiveChecker;
import javato.activetesting.analysis.CheckerAnalysisImpl;
import javato.activetesting.common.Parameters;
import javato.activetesting.igoodlock.DeadlockCycleInfo;
import javato.activetesting.igoodlock.Node;
import javato.activetesting.lockset.LockSetTracker;
import javato.activetesting.reentrant.IgnoreRentrantLock;

import java.util.List;

/**
 * Copyright (c) 2007-2008,
 * Koushik Sen    <ksen@cs.berkeley.edu>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * <p/>
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * <p/>
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class DeadlockFuzzerAnalysis extends CheckerAnalysisImpl {
    private LockSetTracker lsTracker;
    private IgnoreRentrantLock ignoreRentrantLock;
    private List<Node> deadlockingCycle;

    public void initialize() {
        synchronized (ActiveChecker.lock) {
            lsTracker = new LockSetTracker();
            ignoreRentrantLock = new IgnoreRentrantLock();
            DeadlockCycleInfo cycles = DeadlockCycleInfo.read();
            deadlockingCycle = cycles.getCycles().get(Parameters.errorId - 1);
            System.out.println("cycle " + deadlockingCycle);
        }
    }

    private boolean needToPause(List<Integer> lockSet) {
        for (Node node : deadlockingCycle) {
            List<Integer> tupleLs = node.getContext();
            if (lockSet.equals(tupleLs)) {
                return true;
            }
        }
        return false;
    }

    private boolean needToYieldOthers(List<Integer> lockSet) {
        for (Node node : deadlockingCycle) {
            List<Integer> tupleLs = node.getContext();
            if (lockSet.size() == 1 && lockSet.get(0).equals(tupleLs.get(0))) {
                return true;
            }
        }
        return false;
    }


    public void lockBefore(Integer iid, Integer thread, Integer lock) {
        synchronized (ActiveChecker.lock) {
            if (ignoreRentrantLock.lockBefore(thread, lock)) {
                boolean isDeadlock = lsTracker.lockBefore(iid, thread, lock);
                if (isDeadlock) {
                    Runtime.getRuntime().halt(1);
                } else {
                    List<Integer> lockSet = lsTracker.getLockSetIids(thread);
                    if (needToYieldOthers(lockSet)) {
                        (new ActiveChecker()).check(30);
                    } else if (needToPause(lockSet)) {
                        (new ActiveChecker()).check();
                    }
                }
            }
        }
        ActiveChecker.blockIfRequired();
    }

    public void unlockAfter(Integer iid, Integer thread, Integer lock) {
        synchronized (ActiveChecker.lock) {
            if (ignoreRentrantLock.unlockAfter(thread, lock)) {
                lsTracker.unlockAfter(thread);
            }
        }
    }

    public void newExprAfter(Integer iid, Integer object, Integer objOnWhichMethodIsInvoked) {
    }

    public void methodEnterBefore(Integer iid) {
    }

    public void methodExitAfter(Integer iid) {
    }

    public void startBefore(Integer iid, Integer parent, Integer child) {
    }

    public void waitAfter(Integer iid, Integer thread, Integer lock) {
    }

    public void notifyBefore(Integer iid, Integer thread, Integer lock) {
    }

    public void notifyAllBefore(Integer iid, Integer thread, Integer lock) {
    }

    public void joinAfter(Integer iid, Integer parent, Integer child) {
    }

    public void readBefore(Integer iid, Integer thread, Long memory) {
    }

    public void writeBefore(Integer iid, Integer thread, Long memory) {
    }

    public void finish() {
    }
}
