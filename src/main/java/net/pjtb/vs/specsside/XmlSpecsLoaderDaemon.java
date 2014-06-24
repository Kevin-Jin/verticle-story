package net.pjtb.vs.specsside;

import java.util.Map;

import net.pjtb.vs.shared.MapSpecs;
import net.pjtb.vs.shared.MapSpecsFactory;
import net.pjtb.vs.shared.ReactorSpecs;
import net.pjtb.vs.shared.ReactorSpecsFactory;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;

import com.jetdrone.vertx.mods.bson.BSON;

public class XmlSpecsLoaderDaemon extends SpecsLoaderDaemon {
	//TODO: using blocking IO with woodstox in a worker verticle will probably be faster
	@Override
	protected void handleMapSpecsRequest(Message<Buffer> message) {
		Map<String, Object> rxBson = BSON.decode(message.body());
		int id = ((Number) rxBson.get("id")).intValue();
		MapSpecs specs = MapSpecsFactory.getInstance().getSpecsShell(id);
		if (specs == null || specs.isLoaded()) {
			container.logger().warn("Requested to load map data into invalid specs shell");
			message.fail(1, "Requested to load map data into invalid specs shell");
			return;
		}
		String fileName = String.format("./Map.wz/Map%d/%09d.xml", id / 1000000, id);
		this.getVertxBufferOutputStream(fileName, result -> {
			if (result.failed()) {
				container.logger().warn("Failed to open " + fileName, result.cause());
				message.fail(2, "Failed to open " + fileName);
				return;
			}

			//TODO: feed InputStream into Woodstox, then load map
			result.result();

			message.reply();
		});
	}


	@Override
	protected void handleReactorSpecsRequest(Message<Buffer> message) {
		Map<String, Object> rxBson = BSON.decode(message.body());
		String scriptName = (String) rxBson.get("scriptName");
		ReactorSpecs specs = ReactorSpecsFactory.getInstance().getSpecsShell(scriptName);
		if (specs == null || specs.isLoaded()) {
			container.logger().warn("Requested to load reactor data into invalid specs shell");
			message.fail(1, "Requested to load map data into invalid specs shell");
			return;
		}
		//TODO: send over reactor id instead
		String fileName = String.format("./Reactor.wz/%09d.xml", 34234123);
		this.getVertxBufferOutputStream(fileName, result -> {
			if (result.failed()) {
				container.logger().warn("Failed to open " + fileName, result.cause());
				message.fail(2, "Failed to open " + fileName);
				return;
			}

			//TODO: feed InputStream into Woodstox, then load map
			result.result();

			message.reply();
		});
	}
}
