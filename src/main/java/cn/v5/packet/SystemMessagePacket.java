package cn.v5.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 二进制包消息
 */
public class SystemMessagePacket {
    public static byte[] writePacket(byte[] json) {
        ByteBuf buffer = Unpooled.buffer();
        try {
            buffer.writeByte(0xb7);
            buffer.writeByte(3); //version
            buffer.writeByte(8); //packet type
            buffer.writeByte(0); //secret
            long timestamp = System.currentTimeMillis();
            buffer.writeShort((int)(timestamp >> 32));
            buffer.writeInt((int) timestamp);
            buffer.writeShort(0);
            buffer.writeShort(json != null ? json.length : 0);
            buffer.writeShort(0);
            if(json != null) buffer.writeBytes(json);

            byte[] result = new byte[buffer.readableBytes()];
            buffer.readBytes(result);
            return result;
        }finally {
            buffer.release();
        }

    }

    public static void main(String[] args) {
        System.out.println(writePacket("1".getBytes()).length);
    }
}
