package cn.v5.v5protocol;

import com.handwin.protocal.v5.codec.V5CodecException;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5GenericPacketDecodeAndEncoder;
import com.handwin.protocal.v5.codec.V5PacketHead;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Created by piguangtao on 15/12/17.
 */
@Service
public class V5protocolUtil {

    @Value("${local.idc.region}")
    private String localRegion;

    private V5GenericPacketDecodeAndEncoder decodeAndEncoder = new V5GenericPacketDecodeAndEncoder();

    public byte[] encode(GroupMessage message) {
        if (null == message) return null;
        V5GenericPacket v5GenericPacket = new V5GenericPacket();
        V5PacketHead v5PacketHead = new V5PacketHead();
        v5GenericPacket.setPacketHead(v5PacketHead);

        v5PacketHead.setTimestamp(System.currentTimeMillis());

        if (StringUtils.isBlank(message.getSendId())) {
            throw new RuntimeException("[GroupMessage] send id should not be blank");
        }
        v5PacketHead.setFrom(message.getSendId());

        if (null == message.getReceivers() || message.getReceivers().size() < 1) {
            throw new RuntimeException("[GroupMessage] no receiver");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < message.getReceivers().size(); i++) {
            String receiver = message.getReceivers().get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append(receiver);
        }
        v5PacketHead.setTo(sb.toString());

        //本区发送和接受
        v5PacketHead.setFromRegion(localRegion);
        v5PacketHead.setToRegion(localRegion);

        //消息的来源区
        v5PacketHead.setVia("coreServer");

        //coreServer-->bizServer不需要确认
        v5PacketHead.setServerReceivedConfirm(Boolean.FALSE);
        v5PacketHead.setStore(message.getNeedStore());
        v5PacketHead.setPush(message.getNeedPush());
        if (message.getNeedPush()) {
            if (StringUtils.isBlank(message.getPushContent())) {
                throw new RuntimeException("[GroupMessage] push content should not be blank when need push");
            }
            v5PacketHead.setPushContent(message.getPushContent());
        }
        v5PacketHead.setPushIncr(message.getPushIncr());
        if (message.getNeedStore()) {
            v5PacketHead.setClientReceivedConfirm(true);
        }
        v5PacketHead.setEnsureArrive(message.getEnsureArrive());
        v5PacketHead.setMessageID(UUID.randomUUID().toString());
        if (StringUtils.isNotBlank(message.getTraceId())) {
            v5PacketHead.setTraceId(message.getTraceId());
        }

        v5PacketHead.setService("/group/system/notify");
        if (StringUtils.isBlank(message.getGroupId())) {
            throw new RuntimeException("[GroupMessage] groupId should not be blank.");
        }
        v5PacketHead.addHead("group_id", message.getGroupId());
        v5PacketHead.addHead("enter_conversation", message.getEnterConversationForPush());

        if (StringUtils.isBlank(message.getMsgBody())) {
            throw new RuntimeException("[GroupMessage] msg body should not be blank");
        }
        v5GenericPacket.setBodySrcBytes(message.getMsgBody().getBytes(StandardCharsets.UTF_8));

        byte[] result;
        try {
            ByteBuf byteBuf = decodeAndEncoder.encode(v5GenericPacket);
            result = byteBuf.array();
        } catch (V5CodecException e) {
            throw new RuntimeException(String.format("[GroupMessage] fails to encode group message. %s", message), e);
        }

        return result;
    }
}
