package bingosoft.hrhelper.common;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import java.util.Date;
import java.util.Properties;

/**
 * @创建人 chenwx
 * @功能描述 邮件发送工具
 * @创建时间 2018-07-18 14:08:08
 */
public class MailUtil {
    //发件人地址
    private String senderAddress;
    //发件人账户名
    private String senderAccount;
    //发件人账户密码
    private String senderPassword;
    //收件人地址
    private String[] recipientAddresses;
    //抄送人地址
    private String[] copyToAddresses;
    //邮件标题
    private String subject;
    //邮件正文
    private String content;
    //附件路径
    private String[] attachmentPaths;

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public void setSenderAccount(String senderAccount) {
        this.senderAccount = senderAccount;
    }

    public void setSenderPassword(String senderPassword) {
        this.senderPassword = senderPassword;
    }

    public void setRecipientAddresses(String[] recipientAddresses) {
        this.recipientAddresses = recipientAddresses;
    }

    public void setCopyToAddresses(String[] copyToAddresses) {
        this.copyToAddresses = copyToAddresses;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAttachmentPaths(String[] attachmentPaths) {
        this.attachmentPaths = attachmentPaths;
    }

    /**
     * 邮件发送
     * @throws Exception
     */
    public void sendMail() throws Exception {
        // 连接邮件服务器的参数配置
        Properties props = new Properties();
        // 设置用户的认证方式
        props.setProperty("mail.smtp.auth", "true");
        // 设置传输协议
        props.setProperty("mail.transport.protocol", "smtp");
        // 设置发件人的SMTP服务器地址
        props.setProperty("mail.smtp.host", "GW-MAIL-01.bingosoft.local");
        props.setProperty("mail.smtp.port", "587");
        // 创建定义整个应用程序所需的环境信息的 Session 对象
        Session session = Session.getInstance(props);
        // 设置调试信息在控制台打印出来
        session.setDebug(true);
        // 创建邮件的实例对象
        Message msg = getMimeMessage(session);
        // 根据session对象获取邮件传输对象Transport
        Transport transport = session.getTransport();
        // 设置发件人的账户名和密码
        transport.connect(senderAccount, senderPassword);
        // 发送邮件，并发送到所有收件人地址，message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
        transport.sendMessage(msg,msg.getAllRecipients());

        // 关闭邮件连接
        transport.close();
    }

    /**
     * 获取邮件的实例对象
     * @param session
     * @return MimeMessage
     * @throws MessagingException
     * @throws AddressException
     */
    private MimeMessage getMimeMessage(Session session) throws Exception{
        // 创建一封邮件的实例对象
        MimeMessage msg = new MimeMessage(session);

        // 设置发件人地址
        msg.setFrom(new InternetAddress(senderAddress));
        /**
         * 设置收件人地址（可以增加多个收件人、抄送、密送），即下面这一行代码书写多行
         * MimeMessage.RecipientType.TO:发送
         * MimeMessage.RecipientType.CC：抄送
         * MimeMessage.RecipientType.BCC：密送
         */
        if (recipientAddresses!=null && recipientAddresses.length>0){
            for (String recipientAddress: recipientAddresses) {
                msg.setRecipient(MimeMessage.RecipientType.TO,new InternetAddress(recipientAddress));
            }
        }else{
            throw new ParamException("收件人为空");
        }
        if (copyToAddresses!=null && copyToAddresses.length>0){
            for (String copyToAddress: copyToAddresses) {
                msg.setRecipient(MimeMessage.RecipientType.CC,new InternetAddress(copyToAddress));
            }
        }
        // 设置邮件主题
        msg.setSubject(subject,"UTF-8");
        //下面是设置邮件正文

        // 设置（文本+图片）和 附件 的关系（合成一个大的混合"节点" / Multipart ）
        MimeMultipart mm = new MimeMultipart();
        // 混合关系
        mm.setSubType("mixed");
        // 创建附件"节点"
        MimeBodyPart body = new MimeBodyPart();
        body.setContent(content, "text/html;charset=UTF-8");
        mm.addBodyPart(body);     // 如果有多个附件，可以创建多个多次添加
        if(attachmentPaths!=null && attachmentPaths.length>0){
            for (String attachmentPath: attachmentPaths) {
                // 9. 创建附件"节点"
                MimeBodyPart attachment = new MimeBodyPart();
                // 读取本地文件
                DataHandler dh2 = new DataHandler(new FileDataSource(attachmentPath));
                // 将附件数据添加到"节点"
                attachment.setDataHandler(dh2);
                // 设置附件的文件名（需要编码）
                attachment.setFileName(MimeUtility.encodeText(dh2.getName()));
                mm.addBodyPart(attachment);     // 如果有多个附件，可以创建多个多次添加
            }
        }
        // 设置整个邮件的关系（将最终的混合"节点"作为邮件的内容添加到邮件对象）
        msg.setContent(mm);
        // 设置邮件的发送时间,默认立即发送
        msg.setSentDate(new Date());

        return msg;
    }

}
