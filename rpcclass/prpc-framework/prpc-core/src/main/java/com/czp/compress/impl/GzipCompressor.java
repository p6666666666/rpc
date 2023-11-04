package com.czp.compress.impl;

import com.czp.compress.Compressor;
import com.czp.exception.CompressException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 使用gzip进行压缩
 */
@Slf4j
public class GzipCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) {

        try(
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos);
                ) {
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
            byte[] result = baos.toByteArray();
            if (log.isDebugEnabled()){
                log.debug("对字节数据进行了压缩，长度由【{}】压缩至【{}】.",bytes.length,result.length);
            }
            return result;
        } catch (IOException e) {
            log.error("对字节数组进行压缩时发生异常",e);
            throw new CompressException(e);
        }



    }

    @Override
    public byte[] decompress(byte[] bytes) {
        try (
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                GZIPInputStream gzipInputStream = new GZIPInputStream(bais);
        ) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipInputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            byte[] result = baos.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("对字节数据进行了解压缩,长度由【{}】到【{}】",bytes.length,result.length);
            }
            return result;
        } catch (IOException e) {
            log.error("对字节数组进行解压缩时发生异常", e);
            throw new CompressException(e);
        }
    }

}
