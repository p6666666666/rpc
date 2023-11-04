package com.czp;

import com.czp.utils.DateUtil;

import javax.xml.crypto.Data;
import java.util.concurrent.atomic.LongAdder;

/**
 * id生成器（雪花算法）
 * 机房号（5bit）+机器号（5bit）+时间戳(long 减少到32位)+序列号（12bit）
 */
public class IdGenerator {
    //起始时间戳
    public static final long START_STAMP= DateUtil.get("2022-1-1").getTime();

    public static final  long DATA_CENTER_BIT=5L;
    public static final  long MACHINE_BIT=5L;
    public static final  long SEQUENCE_BIT=12L;
    //最大值(2的5次方减一)
    public static final  long DATA_CENTER_MAX=~(-1L<<DATA_CENTER_BIT);
    public static final  long MACHINE_MAX=~(-1L<<MACHINE_BIT);
    public static final  long SEQUENCE_MAX=~(-1L<<SEQUENCE_BIT);

    public static final  long TIMESTAMP_LEFT=DATA_CENTER_BIT+MACHINE_BIT+SEQUENCE_BIT;
    public static final  long DATA_CENTER_LEFT=MACHINE_BIT+SEQUENCE_BIT;
    public static final  long MACHINE_LEFT=DATA_CENTER_BIT;

    private long dataCenterId;
    private long machineId;
    private LongAdder sequenceId=new LongAdder();
    //时钟回拨问题
    private long lastTimeStamp=-1L;

    public IdGenerator(long dataCenterId, long machineId) {
        //判断是否合法
        if (dataCenterId>DATA_CENTER_MAX ||machineId>MACHINE_MAX){
            throw new IllegalArgumentException("传入的数据中心编号不合法");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }
    public  long getId(){
        //处理时间戳
        long currentTime=System.currentTimeMillis();
        long timeStamp=currentTime-START_STAMP;

        //判断时钟回拨
        if (timeStamp<lastTimeStamp){
            throw new RuntimeException("你的服务器进行了时钟回调");
        }
        //处理同一时间并发问题
        if (timeStamp==lastTimeStamp){
            sequenceId.increment();
            if (sequenceId.sum()==SEQUENCE_MAX){
                timeStamp=getNextTimeStamp();
            }
        }else {
            sequenceId.reset();
        }
        lastTimeStamp=timeStamp;
        long sequence=sequenceId.sum();
        return timeStamp<<TIMESTAMP_LEFT| dataCenterId<<DATA_CENTER_LEFT|machineId<<MACHINE_LEFT|sequence;
    }

    private long getNextTimeStamp() {
        long current = System.currentTimeMillis() - START_STAMP;
        while(current==lastTimeStamp){
            current = System.currentTimeMillis() - START_STAMP;
        }
        return current;
    }

    public static void main(String[] args) {
        IdGenerator idGenerator=new IdGenerator(1,2);
        for (int i = 0; i <1000; i++) {
            new Thread(()-> System.out.println(idGenerator.getId())).start();
        }
    }
}
