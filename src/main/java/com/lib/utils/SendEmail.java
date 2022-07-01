package com.lib.utils;

import com.lib.enums.Const;
import com.sun.mail.util.MailSSLSocketFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SendEmail {

    public static String HOST = null;
    public static String PROTOCOL = "smtp";
    public static int PORT = 0;
    public static String FROM = null;// 发件人的email
    public static String PWD = null;// 发件人密码


    static {
        Properties prop = new Properties();
        InputStream in = Const.class.getResourceAsStream("/jdbc.properties");
        try {
            prop.load(in);
            HOST = prop.getProperty("mail_host").trim();
            PORT = Integer.valueOf(prop.getProperty("mail_port"));
            FROM = prop.getProperty("mail_user").trim();
            PWD = prop.getProperty("mail_pwd").trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        send("universsky@163.com", "test");
    }

    public static void send(String toEmail, String content) {
        System.out.println("邮件内容:" + content);

        try {

            //创建一个配置文件并保存
            Properties properties = new Properties();

            properties.setProperty("mail.host", "smtp.163.com");

            properties.setProperty("mail.transport.protocol", "smtp");

            properties.setProperty("mail.smtp.auth", "true");

            //设置SSL加密
            MailSSLSocketFactory sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.ssl.socketFactory", sf);

            //创建一个session对象
            Session session = Session.getDefaultInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM, PWD);
                }
            });

            //开启debug模式
            session.setDebug(true);

            //获取连接对象
            Transport transport = session.getTransport();

            //连接服务器
            transport.connect(HOST, FROM, PWD);

            //创建邮件对象
            MimeMessage mimeMessage = new MimeMessage(session);

            //邮件发送人
            mimeMessage.setFrom(new InternetAddress(FROM));

            //邮件接收人
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));

            //邮件标题
            mimeMessage.setSubject("知识管理系统KMS");

            //邮件内容
            mimeMessage.setContent(content, "text/html;charset=UTF-8");

            //发送邮件
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());

            //关闭连接
            transport.close();

        } catch (Exception mex) {
            mex.printStackTrace();
        }
    }

}
