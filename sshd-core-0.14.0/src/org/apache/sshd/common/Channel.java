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
package org.apache.sshd.common;

import org.apache.sshd.client.future.OpenFuture;
import org.apache.sshd.common.channel.Window;
import org.apache.sshd.common.future.CloseFuture;
import org.apache.sshd.common.session.ConnectionService;
import org.apache.sshd.common.util.Buffer;

import java.io.IOException;

/**
 * TODO Add javadoc
 *
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
public interface Channel extends Closeable {

	int getId();

	int getRecipient();

	Window getLocalWindow();

	Window getRemoteWindow();

	Session getSession();

	void handleClose() throws IOException;

	void handleWindowAdjust(Buffer buffer) throws IOException;

	void handleRequest(Buffer buffer) throws IOException;

	void handleData(Buffer buffer) throws IOException;

	void handleExtendedData(Buffer buffer) throws IOException;

	void handleEof() throws IOException;

	void handleFailure() throws IOException;

	CloseFuture close(boolean immediately);

	void init(ConnectionService service, Session session, int id) throws IOException;

	/**
	 * For a server channel, this method will actually open the channel
	 */
	OpenFuture open(int recipient, int rwsize, int rmpsize, Buffer buffer);

	/**
	 * For a client channel, this method will be called internally by the session when the confirmation
	 * has been received.
	 */
	void handleOpenSuccess(int recipient, int rwsize, int rmpsize, Buffer buffer) throws IOException;

	/**
	 * For a client channel, this method will be called internally by the session when
	 * the server has rejected this channel opening.
	 */
	void handleOpenFailure(Buffer buffer) throws IOException;
}
