package ocrserver;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ocrserver.Client;

public class OCRServer {

	public static ExecutorService threadPool;
	public static Vector<Client> clients = new Vector<Client>();
	public static Vector<String> logs = new Vector<String>();

	ServerSocketChannel serverSocketChannel = null;
	String str;

	public void startServer(int port) {
		try {
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(true);
			serverSocketChannel.bind(new InetSocketAddress(port));
		} catch (Exception e) {
			e.printStackTrace();
			if (serverSocketChannel.isOpen()) {
				stopServer();
			}
			return;
		}
		Runnable thread = new Runnable() {
			public void run() {
				while (true) {
					try {
						SocketChannel socketChannel = serverSocketChannel.accept();
						InetSocketAddress isa = (InetSocketAddress) socketChannel.getRemoteAddress();
						System.out.println("connected." + isa.getHostName());
						clients.add(new Client(socketChannel, isa.getHostName()));
					} catch (Exception e) {
						if (!serverSocketChannel.isOpen()) {
							stopServer();
						}
						break;
					}
				}
			}
		};
		threadPool = Executors.newCachedThreadPool();
		threadPool.submit(thread);
		System.out.println("Server Started");
	}

	public void stopServer() {
		System.out.println("Server Stopped");
		try {
			Iterator<Client> iterator = clients.iterator();
			while (iterator.hasNext()) {
				Client client = iterator.next();
				client.socketChannel.close();
				iterator.remove();
			}
			if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
				serverSocketChannel.close();
			}
			if (threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		OCRServer mainServer = new OCRServer();
		mainServer.startServer(19032);
	}
}
