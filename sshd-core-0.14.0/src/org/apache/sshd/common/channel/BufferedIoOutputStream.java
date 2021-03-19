/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sshd.common.channel;

import org.apache.sshd.common.Closeable;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.common.io.IoWriteFuture;
import org.apache.sshd.common.util.Buffer;
import org.apache.sshd.common.util.CloseableUtils;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An IoOutputStream capable of queuing write requests
 */
public class BufferedIoOutputStream extends CloseableUtils.AbstractInnerCloseable implements IoOutputStream {

	private final IoOutputStream out;
	private final Queue<ChannelAsyncOutputStream.IoWriteFutureImpl> writes = new ConcurrentLinkedQueue<ChannelAsyncOutputStream.IoWriteFutureImpl>();
	private final AtomicReference<ChannelAsyncOutputStream.IoWriteFutureImpl> currentWrite = new AtomicReference<ChannelAsyncOutputStream.IoWriteFutureImpl>();

	public BufferedIoOutputStream(IoOutputStream out) {
		this.out = out;
	}

	public IoWriteFuture write(Buffer buffer) {
		final ChannelAsyncOutputStream.IoWriteFutureImpl future = new ChannelAsyncOutputStream.IoWriteFutureImpl(buffer);
		if (isClosing()) {
			future.setValue(new IOException("Closed"));
		} else {
			writes.add(future);
			startWriting();
		}
		return future;
	}

	private void startWriting() {
		final ChannelAsyncOutputStream.IoWriteFutureImpl future = writes.peek();
		if (future != null) {
			if (currentWrite.compareAndSet(null, future)) {
				out.write(future.getBuffer()).addListener(new SshFutureListener<IoWriteFuture>() {
					public void operationComplete(IoWriteFuture f) {
						if (f.isWritten()) {
							future.setValue(true);
						} else {
							future.setValue(f.getException());
						}
						finishWrite();
					}

					private void finishWrite() {
						writes.remove(future);
						currentWrite.compareAndSet(future, null);
						startWriting();
					}
				});
			}
		}
	}

	@Override
	protected Closeable getInnerCloseable() {
		return builder().when(writes).close(out).build();
	}

	@Override
	public String toString() {
		return "BufferedIoOutputStream[" + out + "]";
	}
}
