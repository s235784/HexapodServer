import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {
        System.out.println("等待连接");
        try {
            ServerSocket server = new ServerSocket(23578);
            Socket client = null;
            while (true) {
                client = server.accept();
                System.out.println("连接成功");
                new Thread(new ServerThread(server,client)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("错误：" + e.toString());
        }
    }
}

class ServerThread implements Runnable {

    private Socket client;
    private ServerSocket server;
    private Runtime run;
    private Process p;

    public static final int CODE_COLSE = 0;
    public static final int CODE_COLSE_SERVER = 1;
    public static final int CODE_AUTOM = 2;
    public static final int CODE_MANUAL = 3;
    public static final int CODE_MANUAL_RIGHT = 301;
    public static final int CODE_MANUAL_LEFT = 302;
    public static final int CODE_MANUAL_BACK = 303;
    public static final int CODE_MANUAL_GO = 304;

    public ServerThread(ServerSocket server, Socket client) {
        this.client = client;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            //输出流
            PrintStream out = new PrintStream(client.getOutputStream());
            //输入流
            BufferedReader buf = new BufferedReader(new InputStreamReader(client.getInputStream()));
            run = Runtime.getRuntime();
            boolean flag = true;
            while (flag) {
                //接收从客户端发送过来的数据
                String str = buf.readLine();
                if (str == null || "".equals(str)) {
                    flag = false;
                } else {
                    System.out.println(str);
                    out.println("已接收");
                    switch (Integer.valueOf(str)) {
                        case CODE_COLSE:
                            out.println("0");
                            client.close();
                            System.out.println("连接已关闭");
                            flag = false;
                            break;
                        case CODE_COLSE_SERVER:
                            out.println("01");
                            client.close();
                            server.close();
                            System.out.println("连接已关闭");
                            System.out.println("进程已关闭");
                            System.exit(0);
                            break;
                        case CODE_AUTOM:
                            out.println("自动模式");
                            StartPy(out,"a", null);
                            break;
                        case CODE_MANUAL:
                            out.println("02");
                            if (p != null)
                                p.destroy();
                            break;
                        case CODE_MANUAL_RIGHT:
                            out.println("右转");
                            StartPy(out,"m", "r");
                            break;
                        case CODE_MANUAL_LEFT:
                            out.println("左转");
                            StartPy(out,"m", "l");
                            break;
                        case CODE_MANUAL_BACK:
                            out.println("后退");
                            StartPy(out,"m", "b");
                            break;
                        case CODE_MANUAL_GO:
                            out.println("前进");
                            StartPy(out,"m", "f");
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("错误：" + e.toString());
        }
    }

    private void StartPy(PrintStream out, String command, String command2) {
        try {
            if (command2 == null) {
                p = run.exec(new String[]{"python", "hexapod.py", command});
            } else {
                p = run.exec(new String[]{"python", "hexapod.py", command, command2});
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        p.destroy();
                        out.println("1");
                    }
                }).start();
            }
        }catch (IOException e){
            e.printStackTrace();
            out.println("错误：" + e.toString());
        }
    }
}