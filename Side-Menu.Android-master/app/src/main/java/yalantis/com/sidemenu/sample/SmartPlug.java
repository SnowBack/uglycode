package yalantis.com.sidemenu.sample;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SmartPlug {
    private String IpAdress = null;
    private int port = 9999;
    private String Tag;
    private String ID;
    private String Desc;

    private int Byte2Int(byte i)//fuck Java,好多暗坑。
    {
        int b2;
        if (i >= 0) {
            b2 = (int) i;
        } else {
            b2 = 0x7f & (int) i;
            b2 = b2 + 128;
        }
        return b2;
    }

    private byte[] encrypt(String str) {
        int key = 171;
        ;
        byte b1[] = str.getBytes();
        byte xor[] = new byte[b1.length + 4];
        xor[0] = 0;
        xor[1] = 0;
        xor[2] = 0;
        xor[3] = 0;
        int i = 0;
        byte a;
        int b2;
        for (; i < b1.length; i++) {
            b2 = this.Byte2Int(b1[i]);
            key = (b2 ^ key);
            a = (byte) (0xff & key);
            xor[i + 4] = (byte) a;
        }
        return xor;
    }

    private String decrypt(byte[] b1) {
        int key = 171;
        ;
        byte xor[] = new byte[b1.length];
        int i = 0;
        byte a;
        int b2;
        for (; i < b1.length; i++) {
            b2 = Byte2Int(b1[i]);
            //System.out.println(b2);
            a = (byte) (0xff & (key ^ b2));
            key = b2;
            xor[i] = a;
            //System.out.println(Byte2Int(xor[i]));
        }
        String r = new String(xor);
        return r;
    }
    private String rmTailChar(String origin){
        int n = origin.length()-1;
        for(; n>=0; n--){
            if(origin.charAt(n)=='}')
                break;
        }
        return origin.substring(0, n+1);
    }
    private String talk(String data) {
        String recv = new String("");
        try {
            //System.out.println("连接到主机：" + IpAdress + " ，端口号：" + port);
            Socket client = new Socket(IpAdress, port);
            //System.out.println("远程主机地址：" + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            out.write(encrypt(data));
            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            byte recive[] = new byte[1000];
            byte recive1[] = new byte[1000];
            in.read(recive);
            int i = 4;
            for (; i < recive.length; ++i) {
                recive1[i - 4] = recive[i];
            }
            //System.out.println("服务器响应： "+decrypt(recive1));
            recv = new String(decrypt(recive1));
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return rmTailChar(recv);
    }

    public String getPower() {//当前瞬间功耗
        String command = new String("{\"emeter\":{\"get_realtime\":0}}");
        return talk(command);
    }

    public String getSysInfo() {//所有信息
        String command = new String("{\"system\":{\"get_sysinfo\":{}}}");
        return talk(command);
    }

    public String reboot() {
        String command = new String("{\"system\":{\"reboot\":{\"delay\":1}}}");
        return talk(command);
    }

    public String getTime() {
        String command = new String("{\"time\":{\"get_time\":{}}}");
        return talk(command);
    }

    public String getTimeZone() {
        String command = new String("{\"time\":{\"get_timezone\":{}}}");
        return talk(command);
    }

    public String LedOn() {
        String command = new String("{\"system\":{\"set_relay_state\":{\"state\":1}}}");
        return talk(command);
    }

    public String LedOff() {
        String command = new String("{\"system\":{\"set_relay_state\":{\"state\":0}}}");
        return talk(command);
    }

    public String setDevAlias(String name) {//设置别名
        String command = new String("{\"system\":{\"set_dev_alias\":{\"alias\":\"");
        String commandend = new String("\"}}}");
        return talk(command + name + commandend);

    }

    public String getConsuptionforMonth(String year, String month) {//当月每天用电信息
        return talk("{\"emeter\":{\"get_daystat\":{\"month\":" + month + ",\"year\":" + year + "}}}");
    }

    public String getConsuptionforYear(String year) {//当年每月用电信息
        return talk("{\"emeter\":{\"get_monthstat\":{\"year\":" + year + "}}}");
    }

    public void setIpAdress(String IP) {
        IpAdress = IP;
    }

    public void setTag(String userTag) {
        Tag = userTag;
    }

    public void setDesc(String userDesc) {
        Desc = userDesc;
    }

    public String getDesc(String userDesc) {
        return Desc;
    }

    public SmartPlug(){
        IpAdress = MainActivity.myGetContext().getResources().getString(R.string.device_ip);
        Log.e("IP", IpAdress);
    }
}
