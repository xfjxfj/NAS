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
package org.apache.sshd.common.random;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.Random;
import org.bouncycastle.crypto.prng.RandomGenerator;
import org.bouncycastle.crypto.prng.VMPCRandomGenerator;

import java.security.SecureRandom;

/**
 * BouncyCastle <code>Random</code>.
 * This pseudo random number generator uses the a very fast PRNG from BouncyCastle.
 * The JRE random will be used when creating a new generator to add some random
 * data to the seed.
 *
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
public class BouncyCastleRandom implements Random {

	/**
	 * Named factory for the BouncyCastle <code>Random</code>
	 */
	public static class Factory implements NamedFactory<Random> {

		public String getName() {
			return "bouncycastle";
		}

		public Random create() {
			return new BouncyCastleRandom();
		}
	}

	private final RandomGenerator random;

	public BouncyCastleRandom() {
		this.random = new VMPCRandomGenerator();
		byte[] seed = new SecureRandom().generateSeed(8);
		this.random.addSeedMaterial(seed);
	}

	public void fill(byte[] bytes, int start, int len) {
		this.random.nextBytes(bytes, start, len);
	}

	/**
	 * Returns a pseudo-random uniformly distributed {@code int}
	 * in the half-open range [0, n).
	 */
	public int random(int n) {
		if (n > 0) {
			if ((n & -n) == n) {
				return (int) ((n * (long) next(31)) >> 31);
			}
			int bits, val;
			do {
				bits = next(31);
				val = bits % n;
			} while (bits - val + (n - 1) < 0);
			return val;
		}
		throw new IllegalArgumentException();
	}

	final protected int next(int numBits) {
		int bytes = (numBits + 7) / 8;
		byte[] next = new byte[bytes];
		int ret = 0;
		random.nextBytes(next);
		for (int i = 0; i < bytes; i++) {
			ret = (next[i] & 0xFF) | (ret << 8);
		}
		return ret >>> (bytes * 8 - numBits);
	}
}
