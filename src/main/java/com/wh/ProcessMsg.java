package com.wh;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class ProcessMsg {
	public interface IMsg {
		void onMsg(int command, String value);
	}

	String name;


	public ProcessMsg(String name) {
		this.name = name;
	}

	protected File getFile() throws IOException {
		File file = File.createTempFile("a~~~", "b~~~~");
		return new File(file.getParentFile(), name);
	}

	interface IBufferOper {
		void onOper(FileChannel channel, MappedByteBuffer buffer) throws  Exception;
	}

	void doMemory(IBufferOper onBufferOper) throws Exception {
		try (RandomAccessFile accessFile = new RandomAccessFile(getFile(), "rw");
			 FileChannel channel = accessFile.getChannel();) {
			MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 1024);
			onBufferOper.onOper(channel, buffer);
		}
	}

	String getCharset(String charset) {
		return charset == null || charset.isEmpty() ? "utf8" : charset;
	}

	public void write(int command, String value, String charset) throws Exception {
		doMemory(new IBufferOper() {
			@Override
			public void onOper(FileChannel channel, MappedByteBuffer buffer) throws IOException {
				FileLock lock = channel.lock();
				try {
					buffer.position(0);
					byte[] dataBuffer = value.getBytes(getCharset(charset));
					buffer.putInt(command);
					buffer.putInt(dataBuffer.length);
					buffer.put(dataBuffer);
				} finally {
					lock.release();
				}
			}
		});
	}

	public void read(String charset, IMsg onMsg) throws Exception {
		doMemory(new IBufferOper() {
			@Override
			public void onOper(FileChannel channel, MappedByteBuffer buffer) throws Exception {
				while (!Thread.interrupted()) {
					FileLock lock = channel.lock();
					try {
						buffer.position(0);
						int command = buffer.getInt();
						if (command == 0) {
							lock.release();
							Thread.sleep(1000);
							continue;
						}
						int len = buffer.getInt();
						byte[] dataBuffer = new byte[len];
						buffer.get(dataBuffer);
						buffer.position(0);
						buffer.putInt(0);
						onMsg.onMsg(command, new String(dataBuffer, getCharset(charset)));
					} finally {
						lock.release();
					}
				}
			}
		});
	}
}
