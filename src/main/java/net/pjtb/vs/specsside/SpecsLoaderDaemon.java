package net.pjtb.vs.specsside;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import net.pjtb.vs.shared.EventAddresses;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

public abstract class SpecsLoaderDaemon extends Verticle {
	private static class SimpleAsyncResult<T> implements AsyncResult<T> {
		private final T success;
		private final Throwable failure;

		public SimpleAsyncResult(T success) {
			this.success = success;
			this.failure = null;
		}

		public SimpleAsyncResult(Throwable failure) {
			this.success = null;
			this.failure = failure;
		}

		@Override
		public Throwable cause() {
			return failure;
		}

		@Override
		public boolean failed() {
			return failure != null;
		}

		@Override
		public T result() {
			return success;
		}

		@Override
		public boolean succeeded() {
			return success != null;
		}
	}

	protected void getVertxBufferOutputStream(String fileName, Handler<AsyncResult<InputStream>> doneHandler) {
		vertx.fileSystem().open(fileName, null, false, false, false, result -> {
			if (result.failed()) {
				doneHandler.handle(new SimpleAsyncResult<>(result.cause()));
				return;
			}

			PipedOutputStream os = new PipedOutputStream();
			try {
				InputStream is = new PipedInputStream(os);
				doneHandler.handle(new SimpleAsyncResult<>(is));
			} catch (Exception e) {
				doneHandler.handle(new SimpleAsyncResult<>(e));
				return;
			}
			result.result().dataHandler(data -> {
				try {
					os.write(data.getBytes());
				} catch (Exception e) {
					container.logger().warn("Failed to read " + fileName, e);
					try {
						//os.close();
						result.result().close();
					} catch (Exception ex) {
						container.logger().debug("Failed to close stream after reading " + fileName, e);
					}
				}
			});
			result.result().endHandler(v -> {
				try {
					os.close();
				} catch (Exception e) {
					container.logger().debug("Failed to close stream after reading " + fileName, e);
				}
			});
		});
	}

	protected abstract void handleMapSpecsRequest(Message<Buffer> message);
	protected abstract void handleReactorSpecsRequest(Message<Buffer> message);

	@Override
	public void start() {
		vertx.eventBus().registerLocalHandler(String.format(EventAddresses.SPECS_LOADED_NOTIFICATION, "map"), this::handleMapSpecsRequest);
		vertx.eventBus().registerLocalHandler(String.format(EventAddresses.SPECS_LOADED_NOTIFICATION, "reactor"), this::handleReactorSpecsRequest);
	}
}
