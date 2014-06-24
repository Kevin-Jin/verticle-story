package net.pjtb.vs.playerside;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.pjtb.vs.util.LittleEndianByteArrayReader;
import net.pjtb.vs.util.LittleEndianByteArrayWriter;
import net.pjtb.vs.util.Rng;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.net.NetSocket;

import com.jetdrone.vertx.mods.bson.BSON;

/**
 * Guaranteed to be accessible by only one thread.
 *
 * @author Kevin Jin
 */
public class ClientSession {
	public enum MessageType { HEADER, BODY }

	private static final int HEADER_LENGTH = 4;

	public static final byte
		CLIENT_DISTRIBUTION_JAPAN = 3,
		CLIENT_DISTRIBUTION_TEST = 5,
		CLIENT_DISTRIBUTION_SEA = 7,
		CLIENT_DISTRIBUTION_GLOBAL = 8,
		CLIENT_DISTRIBUTION_BRAZIL = 9
	;

	private static final Buffer EMPTY_BUFFER = new Buffer();

	private final NetSocket socket;
	private byte[] rxIv, txIv;
	private Buffer lastRxBuf;
	private MessageType rxType;
	private int rxLen;

	private final GameSidePacketProcessor pp;
	private final EventBus eventBus;
	private final Map<String, Handler<? extends Message<?>>> eventBusHandlers;
	private boolean closed;

	private WorldCharacter c;

	public ClientSession(NetSocket socket, GameSidePacketProcessor pp, EventBus eventBus) {
		this.socket = socket;
		this.pp = pp;
		this.eventBus = eventBus;
		this.eventBusHandlers = new HashMap<>();
	}

	public void init() {
		socket.dataHandler(buffer -> {
			receive(buffer);
		});
		socket.endHandler(v -> {
			close("Received EOF from client");
		});

		rxLen = HEADER_LENGTH;
		rxType = MessageType.HEADER;
		lastRxBuf = EMPTY_BUFFER;

		rxIv = new byte[4];
		txIv = new byte[4];
		Random generator = Rng.getGenerator();
		generator.nextBytes(rxIv);
		generator.nextBytes(txIv);

		LittleEndianByteArrayWriter lew = new LittleEndianByteArrayWriter(13);
		lew.writeShort(GlobalConstants.MAPLE_VERSION);
		lew.writeLengthPrefixedString("");
		lew.writeBytes(rxIv);
		lew.writeBytes(txIv);
		lew.writeByte(CLIENT_DISTRIBUTION_GLOBAL);
		byte[] body = lew.getBytes();
		//vert.x Buffer class doesn't support appending little endian short...
		lew = new LittleEndianByteArrayWriter(2 + body.length);
		lew.writeShort((short) body.length);
		lew.writeBytes(body);
		socket.write(new Buffer(lew.getBytes()));
	}

	private void decryptBytes(byte[] body) {
		ClientEncryption.aesOfbCrypt(body, rxIv);
		ClientEncryption.mapleDecrypt(body);

		rxIv = ClientEncryption.nextIv(rxIv);
	}

	private void receive(Buffer buf) {
		int cursor;
		int lastBufLength = lastRxBuf.length();

		//keep processing packet header/body as long as we have enough bytes to read it in full
		for (cursor = 0; cursor - lastBufLength + rxLen <= buf.length(); ) {
			byte[] message;
			if (cursor - lastBufLength >= 0) {
				//lastBuf is exhausted
				message = buf.getBytes(cursor - lastBufLength, cursor - lastBufLength + rxLen);
				cursor += rxLen;
			} else {
				//first finishes off lastBuf if it's not empty, then goes to buf
				int readFromLastBuf = Math.min(lastBufLength - cursor, rxLen);
				message = new byte[rxLen];
				System.arraycopy(lastRxBuf.getBytes(cursor, readFromLastBuf), 0, message, 0, readFromLastBuf);
				cursor += rxLen;
				rxLen -= readFromLastBuf;
				if (rxLen > 0)
					System.arraycopy(buf.getBytes(0, rxLen), 0, message, readFromLastBuf, rxLen);
			}
			switch (rxType) {
				case HEADER: {
					if (!ClientEncryption.checkPacket(message, rxIv)) {
						close("Failed packet test");
						return;
					}
					rxLen = ClientEncryption.getPacketLength(message);
					rxType = MessageType.BODY;
					break;
				}
				case BODY: {
					rxLen = HEADER_LENGTH;
					rxType = MessageType.HEADER;
					decryptBytes(message);
					pp.process(new LittleEndianByteArrayReader(message));
					break;
				}
			}
		}

		//check if buffer contains more than we can read. if so, keep a copy of the remaining bytes
		if (cursor - lastBufLength < buf.length())
			lastRxBuf = buf.getBuffer(cursor - lastBufLength, buf.length());
		else
			lastRxBuf = EMPTY_BUFFER;
	}

	private Buffer getEncryptedBuffer(byte[] message) {
		//make a copy in case the caller reuses the passed array
		byte[] input = new byte[message.length];
		System.arraycopy(message, 0, input, 0, message.length);

		//encrypt the input
		ClientEncryption.mapleEncrypt(input);
		ClientEncryption.aesOfbCrypt(input, txIv);

		//prepend the input with a header
		byte[] header = ClientEncryption.makePacketHeader(input.length, txIv);
		byte[] output = new byte[header.length + input.length];
		System.arraycopy(header, 0, output, 0, header.length);
		System.arraycopy(input, 0, output, header.length, input.length);

		//increment sendIv for the next message
		txIv = ClientEncryption.nextIv(txIv);
		return new Buffer(output);
	}

	public void send(byte[] message) {
		socket.write(getEncryptedBuffer(message));
	}

	public void subscribe(String key, Handler<? extends Message<?>> handler) {
		eventBus.registerHandler(key, handler);
		eventBusHandlers.put(key, handler);
	}

	public void subscribe(String key) {
		subscribe(key, (Message<Buffer> packet) -> {
			Map<String, Object> rxBson = BSON.decode(packet.body());
			Integer excludeCharacterId = (Integer) rxBson.get("exclude");
			if (excludeCharacterId == null || c == null || excludeCharacterId.intValue() != c.getId())
				send((byte[]) rxBson.get("packet"));
		});
	}

	public void unsubscribe(String key) {
		Handler<? extends Message<?>> handler = eventBusHandlers.remove(key);
		if (handler != null)
			eventBus.unregisterHandler(key, handler);
	}

	public void publish(String key, Buffer b) {
		eventBus.publish(key, b);
	}

	public void publish(String key, byte[] packet, int excludeCharacterId) {
		Map<String, Object> txBson;
		if (excludeCharacterId <= 0) {
			txBson = Collections.singletonMap("packet", packet);
		} else {
			txBson = new HashMap<>();
			txBson.put("packet", packet);
			txBson.put("exclude", Integer.valueOf(excludeCharacterId));
		}
		publish(key, BSON.encode(txBson));
	}

	public void close(String message) {
		if (!closed) {
			closed = true;
			socket.close();
			for (Map.Entry<String, Handler<? extends Message<?>>> handler : eventBusHandlers.entrySet())
				eventBus.unregisterHandler(handler.getKey(), handler.getValue());
			System.out.println("Client closed " + message);
		}
	}
}
