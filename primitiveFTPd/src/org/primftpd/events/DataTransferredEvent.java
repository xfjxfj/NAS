package org.primftpd.events;

public class DataTransferredEvent {
	private final long timestamp;
	private final long bytes;
	private final boolean isWrite;

	public DataTransferredEvent(long timestamp, long bytes, boolean isWrite) {
		this.timestamp = timestamp;
		this.bytes = bytes;
		this.isWrite = isWrite;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public long getBytes() {
		return bytes;
	}

	public boolean isWrite() {
		return isWrite;
	}
}
