package org.wallentines.mcping.modern.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import org.wallentines.mdcfg.serializer.SerializeResult;

public record VarInt(int value) {


    // How many bits are in each segment of a variable-length integer (VarInt)
    public static final int SEGMENT_BITS = 0b01111111;

    // If this bit is set when reading a VarInt, then the next byte should be read as part of the VarInt
    public static final int CONTINUE_BIT = 0b10000000;

    int getWidth() {
        return (31 - Integer.numberOfLeadingZeros(value)) / 7;
    }

    // See https://steinborn.me/posts/performance/how-fast-can-you-write-a-varint//
    public void write(ByteBuf buffer) {

        if ((value & (0xFFFFFFFF << 7)) == 0) {
            buffer.writeByte(value);

        } else if ((value & (0xFFFFFFFF << 14)) == 0) {
            int w = (value & 0x7F | 0x80) << 8 | (value >>> 7);
            buffer.writeShort(w);
        } else if ((value & (0xFFFFFFFF << 21)) == 0) {
            int w = (value & 0x7F | 0x80) << 16 | ((value >>> 7) & 0x7F | 0x80) << 8 | (value >>> 14);
            buffer.writeMedium(w);
        } else if ((value & (0xFFFFFFFF << 28)) == 0) {
            int w = (value & 0x7F | 0x80) << 24 | (((value >>> 7) & 0x7F | 0x80) << 16)
                    | ((value >>> 14) & 0x7F | 0x80) << 8 | (value >>> 21);
            buffer.writeInt(w);
        } else {
            int w = (value & 0x7F | 0x80) << 24 | ((value >>> 7) & 0x7F | 0x80) << 16
                    | ((value >>> 14) & 0x7F | 0x80) << 8 | ((value >>> 21) & 0x7F | 0x80);
            buffer.writeInt(w);
            buffer.writeByte(value >>> 28);
        }

    }

    public static VarInt read(ByteBuf buffer, int limit) {

        int out = 0;
        int position = 0;
        byte currentByte;
        do {
            if(position == limit) {
                throw new DecoderException("VarInt was too big!");
            }
            currentByte = buffer.readByte();
            out |= (currentByte & SEGMENT_BITS) << (7 * position);
            position++;
        } while((currentByte & CONTINUE_BIT) == CONTINUE_BIT);

        return new VarInt(out);
    }

    public static SerializeResult<VarInt> readPartial(ByteBuf buffer, int limit) {

        int out = 0;
        int position = 0;
        byte currentByte;
        do {
            if(position == limit) {
                return SerializeResult.failure("VarInt was too big!");
            }
            if(!buffer.isReadable()) {
                return SerializeResult.failure("Reached the end of the buffer!");
            }
            currentByte = buffer.readByte();
            out |= (currentByte & SEGMENT_BITS) << (7 * position);
            position++;
        } while((currentByte & CONTINUE_BIT) == CONTINUE_BIT);

        return SerializeResult.success(new VarInt(out));
    }

}
