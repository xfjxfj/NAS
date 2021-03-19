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
package org.apache.sshd.server.auth;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.SshConstants;
import org.apache.sshd.common.SshException;
import org.apache.sshd.common.util.Buffer;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.session.ServerSession;

/**
 * TODO Add javadoc
 *
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
public class UserAuthKeyboardInteractive extends AbstractUserAuth {

	public static class Factory implements NamedFactory<UserAuth> {
		public String getName() {
			return "keyboard-interactive";
		}

		public UserAuth create() {
			return new UserAuthKeyboardInteractive();
		}
	}

	@Override
	protected Boolean doAuth(Buffer buffer, boolean init) throws Exception {
		if (init) {
			// Prompt for password
			buffer = session.createBuffer(SshConstants.SSH_MSG_USERAUTH_INFO_REQUEST);
			buffer.putString("Password authentication");
			buffer.putString("");
			buffer.putString("en-US");
			buffer.putInt(1);
			buffer.putString("Password: ");
			buffer.putBoolean(false);
			session.writePacket(buffer);
			return null;
		} else {
			byte cmd = buffer.getByte();
			if (cmd != SshConstants.SSH_MSG_USERAUTH_INFO_RESPONSE) {
				throw new SshException("Received unexpected message: " + cmd);
			}
			int num = buffer.getInt();
			if (num != 1) {
				throw new SshException("Expected 1 response from user but received " + num);
			}
			String password = buffer.getString();
			return checkPassword(session, username, password);
		}
	}

	private boolean checkPassword(ServerSession session, String username, String password) throws Exception {
		PasswordAuthenticator auth = session.getFactoryManager().getPasswordAuthenticator();
		if (auth != null) {
			return auth.authenticate(username, password, session);
		}
		throw new Exception("No PasswordAuthenticator configured");
	}
}
