package com.wuwei.nettyServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author wuwei
 * @since 2018/7/24 17:44
 * 监听客户端连接的传统Socket服务端
 */
public class SocketServer {

    private static final Logger logger = Logger.getLogger(SocketServer.class.getName());

    public static void main(String[] args) {
        startServerSocket();
    }

    /**
     * 开启服务端socket监听
     */
    public static void startServerSocket() {
        try {
            ServerSocket ss = new ServerSocket(8888);
            //循环监听
            while (true) {
                //监听客户端上线，阻塞等待
                Socket socket = ss.accept();
                //启动新的线程，监听客户端的在线状态
                new Thread(() -> listenClientConnection(socket)).start();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    /**
     * 监听客户端连接状态，如果客户端断开，移除该连接
     *
     * @param socket
     */
    public static void listenClientConnection(Socket socket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            while (true) {
                String msg = null;
                //接收客户端消息
                if ((msg = reader.readLine()) != null) {
                    System.out.println("Socket服务端收到的数据：" + msg);
                    pw.println("服务端收到消息：" + msg);
                    pw.flush();
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }
}
