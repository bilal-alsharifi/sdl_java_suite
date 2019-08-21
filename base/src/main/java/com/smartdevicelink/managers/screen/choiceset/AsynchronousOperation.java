/*
 * Copyright (c)  2019 Livio, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the Livio Inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Created by Nicole Yarroch on 7/25/19 8:43 AM
 */

package com.smartdevicelink.managers.screen.choiceset;

import com.smartdevicelink.util.DebugTool;

class AsynchronousOperation implements Runnable {
    private static final String TAG = "AsynchronousOperation - ";
    private Thread thread;
    private Object lock;
    private Boolean blocked;
    private Boolean executing;
    private Boolean finished;

    AsynchronousOperation() {
        blocked = false;
        executing = false;
        finished = false;
    }

    @Override
    public void run() {
        thread = Thread.currentThread();
        lock = new Object();
        DebugTool.logInfo(TAG + "Starting: " + toString());
        if (isCancelled()) {
            finished = true;
            DebugTool.logInfo(TAG + "Operation was cancelled: " + toString());
            return;
        }

        executing = true;
    }

    void finishOperation() {
        unblock();
        executing = false;
        finished = true;
        DebugTool.logInfo(TAG + "Finishing: " + toString());
    }

    Boolean isExecuting() {
        return executing;
    }

    Boolean isFinished() {
        return finished;
    }

    void cancel(){
        thread.interrupt();
    }

    Boolean isCancelled() {
        return thread.isInterrupted();
    }

    void block(){
        if (!blocked && !finished) {
            blocked = true;
            DebugTool.logInfo(TAG + "Blocking: " + toString());
            try {
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void unblock(){
        if (blocked) {
            blocked = false;
            DebugTool.logInfo(TAG + "Unblocking: " + toString());
            thread.interrupt();
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " (OpThread:" + thread.getName() + ", currentThread:" + Thread.currentThread().getName() + ", blocked:" + blocked + ", executing:" + executing + ", finished:" + finished + ")";
    }
}