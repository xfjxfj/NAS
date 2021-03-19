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
package org.apache.sshd.server.channel;

import org.apache.sshd.common.Channel;
import org.apache.sshd.common.channel.ChannelAsyncInputStream;
import org.apache.sshd.common.io.IoInputStream;
import org.apache.sshd.common.util.Buffer;

import java.io.IOException;

public class AsyncDataReceiver implements ChannelDataReceiver {

	private final ChannelAsyncInputStream in;

	public AsyncDataReceiver(Channel channel) {
		in = new ChannelAsyncInputStream(channel);
	}

	public IoInputStream getIn() {
		return in;
	}

	public int data(ChannelSession channel, byte[] buf, int start, int len) throws IOException {
		in.write(new Buffer(buf, start, len));
		return 0;
	}

	public void close() throws IOException {
		in.close(false);
	}
}
