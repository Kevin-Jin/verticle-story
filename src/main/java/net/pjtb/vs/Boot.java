package net.pjtb.vs;

import net.pjtb.vs.mapside.MapDaemon;
import net.pjtb.vs.playerside.GameSideDaemon;
import net.pjtb.vs.specsside.XmlSpecsLoaderDaemon;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class Boot extends Verticle {
	private void deployMapServer(int startChannel, int endChannel) {
		JsonObject config = new JsonObject();
		config.putNumber("world", Integer.valueOf(0));
		config.putNumber("startChannel", Integer.valueOf(startChannel));
		config.putNumber("endChannel", Integer.valueOf(endChannel));
		container.deployVerticle(MapDaemon.class.getName(), config);
	}

	public void start() {
		int cores = Runtime.getRuntime().availableProcessors();

		//each instance listening on the same TCP port does not conflict with one another
		//http://vertx.io/core_manual_java.html#scaling-tcp-servers
		container.deployVerticle(GameSideDaemon.class.getName(), cores);

		//if channels is not divisible by mapServers, the earlier map servers serve fewer channels
		//this is fair because channel 1 is usually more crowded than channel 20
		final int channels = 7;
		final int mapServers = Math.max(cores / 2, 1);
		final int channelsPerMapServer = channels / mapServers;
		int startChannel = 1, endChannel;
		for (int i = 0; i < mapServers - 1; i++, startChannel = endChannel + 1) {
			endChannel = startChannel + channelsPerMapServer - 1;
			deployMapServer(startChannel, endChannel);
		}
		deployMapServer(startChannel, channels);

		//any SpecsLoaderDaemon implementation that relies on IO without using vertx.fileSystem()
		//or a vert.x optimized database driver should instead use container.deployWorkerVerticle()
		container.deployVerticle(XmlSpecsLoaderDaemon.class.getName(), Math.max(cores / 4, 1));
	}
}
