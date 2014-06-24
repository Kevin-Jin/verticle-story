package net.pjtb.vs.playerside;

import net.pjtb.vs.util.LittleEndianReader;

import org.vertx.java.platform.Verticle;

public class GameSideDaemon extends Verticle {
	@Override
	public void start() {
		GameSidePacketProcessor pp = new GameSidePacketProcessor() {
			@Override
			public void process(LittleEndianReader reader) {
				System.out.println(reader);
			}
		};

		vertx.createNetServer().connectHandler(socket -> {
			container.logger().info(socket.remoteAddress() + " connected");
			//subscribe to vertx buffer packets sent to gamemap, party, buddy list, etc https://github.com/vert-x/vertx-examples/blob/master/src/raw/java/fanout/FanoutServer.java
			new ClientSession(socket, pp, vertx.eventBus()).init();
		}).listen(8484);
	}
}
