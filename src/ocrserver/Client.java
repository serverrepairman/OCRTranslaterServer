package ocrserver;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import com.google.protobuf.ByteString;

public class Client {
	
	SocketChannel socketChannel;
	String userNum;
	public Client(SocketChannel socketChannel,String userNum) {
		this.socketChannel = socketChannel;
		this.userNum = userNum;
		receive();
	}
	
	public void receive() {
		Runnable thread = new Runnable() {
			ByteBuffer byteBuffer = null;
			Charset charset = Charset.forName("UTF-8");
			public void run() {
				try {
					while(true) {
						byteBuffer = ByteBuffer.allocate(10000);
						int byteCount = socketChannel.read(byteBuffer);
						byteBuffer.flip();
						ByteString request = ByteString.copyFrom(byteBuffer);
						System.out.println("recieved : " + request);
						String response = OCR.Request(request);
						send(response);
					}
				} catch(Exception e) {
					try {
						System.out.println("messege got error "
								+ socketChannel.getRemoteAddress()
								+ ": " + Thread.currentThread().getName());
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		OCRServer.threadPool.submit(thread);
	}
	public void send(String message) {
		ByteBuffer byteBuffer = null;
		Charset charset = Charset.forName("UTF-8");
		byteBuffer = charset.encode(message);
		try {
			socketChannel.write(byteBuffer);
		} catch(Exception e) {
			System.out.println("sending failed...");
			return;
		}
		System.out.println("sended: " + message);
	}
}
