package com.czp.transport.message;

public class MessageFormatConstant {

    public final static byte[] MAGIC="prpc".getBytes();
    public final static byte VERSION=1;
    public final static short HEADER_LENGTH=(byte)(MAGIC.length+1+2+4+1+1+1+8);

    public final static short CURRENT_HEADER_LENGTH=2;
    public final static int MAX_FRAME_LENGTH=1024*1024;
    public static final int VERSION_LENGTH = 1;
    //头部长度占用的字节数
    public static final int HEADER_LENGTH_LENGTH = 2;
    //总长度占用的字节
    public static final int FULL_LENGTH_LENGTH = 4;
}
