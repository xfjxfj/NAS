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

/**
 * This interface defines constants for the SSH protocol.
 *
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
public interface SshConstants {

	//
	// SSH message identifiers
	//

	byte SSH_MSG_DISCONNECT = 1;
	byte SSH_MSG_IGNORE = 2;
	byte SSH_MSG_UNIMPLEMENTED = 3;
	byte SSH_MSG_DEBUG = 4;
	byte SSH_MSG_SERVICE_REQUEST = 5;
	byte SSH_MSG_SERVICE_ACCEPT = 6;
	byte SSH_MSG_KEXINIT = 20;
	byte SSH_MSG_NEWKEYS = 21;

	byte SSH_MSG_KEX_FIRST = 30;
	byte SSH_MSG_KEX_LAST = 49;

	byte SSH_MSG_KEXDH_INIT = 30;
	byte SSH_MSG_KEXDH_REPLY = 31;

	byte SSH_MSG_KEX_DH_GEX_REQUEST_OLD = 30;
	byte SSH_MSG_KEX_DH_GEX_GROUP = 31;
	byte SSH_MSG_KEX_DH_GEX_INIT = 32;
	byte SSH_MSG_KEX_DH_GEX_REPLY = 33;
	byte SSH_MSG_KEX_DH_GEX_REQUEST = 34;

	byte SSH_MSG_USERAUTH_REQUEST = 50;
	byte SSH_MSG_USERAUTH_FAILURE = 51;
	byte SSH_MSG_USERAUTH_SUCCESS = 52;
	byte SSH_MSG_USERAUTH_BANNER = 53;

	byte SSH_MSG_USERAUTH_INFO_REQUEST = 60;
	byte SSH_MSG_USERAUTH_INFO_RESPONSE = 61;

	byte SSH_MSG_USERAUTH_PK_OK = 60;

	byte SSH_MSG_USERAUTH_PASSWD_CHANGEREQ = 60;

	byte SSH_MSG_USERAUTH_GSSAPI_MIC = 66;

	byte SSH_MSG_GLOBAL_REQUEST = 80;
	byte SSH_MSG_REQUEST_SUCCESS = 81;
	byte SSH_MSG_REQUEST_FAILURE = 82;
	byte SSH_MSG_CHANNEL_OPEN = 90;
	byte SSH_MSG_CHANNEL_OPEN_CONFIRMATION = 91;
	byte SSH_MSG_CHANNEL_OPEN_FAILURE = 92;
	byte SSH_MSG_CHANNEL_WINDOW_ADJUST = 93;
	byte SSH_MSG_CHANNEL_DATA = 94;
	byte SSH_MSG_CHANNEL_EXTENDED_DATA = 95;
	byte SSH_MSG_CHANNEL_EOF = 96;
	byte SSH_MSG_CHANNEL_CLOSE = 97;
	byte SSH_MSG_CHANNEL_REQUEST = 98;
	byte SSH_MSG_CHANNEL_SUCCESS = 99;
	byte SSH_MSG_CHANNEL_FAILURE = 100;

	//
	// Values for the algorithms negotiation
	//

	int PROPOSAL_KEX_ALGS = 0;
	int PROPOSAL_SERVER_HOST_KEY_ALGS = 1;
	int PROPOSAL_ENC_ALGS_CTOS = 2;
	int PROPOSAL_ENC_ALGS_STOC = 3;
	int PROPOSAL_MAC_ALGS_CTOS = 4;
	int PROPOSAL_MAC_ALGS_STOC = 5;
	int PROPOSAL_COMP_ALGS_CTOS = 6;
	int PROPOSAL_COMP_ALGS_STOC = 7;
	int PROPOSAL_LANG_CTOS = 8;
	int PROPOSAL_LANG_STOC = 9;
	int PROPOSAL_MAX = 10;

	/**
	 * User-friendly names for the KEX algorithms negotiation items - the
	 * list index matches the {@code PROPOSAL_XXX} constant
	 *
	 * @see <A HREF="http://tools.ietf.org/html/rfc4253#section-7.1">RFC-4253 - section 7.1</A>
	 */
	String[] PROPOSAL_KEX_NAMES = {"kex algorithms", "server host key algorithms", "encryption algorithms (client to server)", "encryption algorithms (server to client)", "mac algorithms (client to server)", "mac algorithms (server to client)", "compression algorithms (client to server)", "compression algorithms (server to client)", "languages (client to server)", "languages (server to client)"};

	//
	// Disconnect error codes
	//
	int SSH2_DISCONNECT_HOST_NOT_ALLOWED_TO_CONNECT = 1;
	int SSH2_DISCONNECT_PROTOCOL_ERROR = 2;
	int SSH2_DISCONNECT_KEY_EXCHANGE_FAILED = 3;
	int SSH2_DISCONNECT_HOST_AUTHENTICATION_FAILED = 4;
	int SSH2_DISCONNECT_RESERVED = 4;
	int SSH2_DISCONNECT_MAC_ERROR = 5;
	int SSH2_DISCONNECT_COMPRESSION_ERROR = 6;
	int SSH2_DISCONNECT_SERVICE_NOT_AVAILABLE = 7;
	int SSH2_DISCONNECT_PROTOCOL_VERSION_NOT_SUPPORTED = 8;
	int SSH2_DISCONNECT_HOST_KEY_NOT_VERIFIABLE = 9;
	int SSH2_DISCONNECT_CONNECTION_LOST = 10;
	int SSH2_DISCONNECT_BY_APPLICATION = 11;
	int SSH2_DISCONNECT_TOO_MANY_CONNECTIONS = 12;
	int SSH2_DISCONNECT_AUTH_CANCELLED_BY_USER = 13;
	int SSH2_DISCONNECT_NO_MORE_AUTH_METHODS_AVAILABLE = 14;
	int SSH2_DISCONNECT_ILLEGAL_USER_NAME = 15;

	//
	// Open error codes
	//

	int SSH_OPEN_ADMINISTRATIVELY_PROHIBITED = 1;
	int SSH_OPEN_CONNECT_FAILED = 2;
	int SSH_OPEN_UNKNOWN_CHANNEL_TYPE = 3;
	int SSH_OPEN_RESOURCE_SHORTAGE = 4;
}
