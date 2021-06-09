package com.laoying.sciot.util;

import java.io.UnsupportedEncodingException;

public class CRC16Check {
//    /**
//     *
//     * @param parityData 待校验的字符串
//     * @return crc16字符串。。。
//     */
//    public static String calcCrc16( String parityData) throws UnsupportedEncodingException {
//        byte[] data = parityData.getBytes();//bug原因:未指定编码格式，默认的是跟随系统
//
//        int crc = 0xFFFF;
//        int dxs = 0xA001;
//        int hibyte;
//        int sbit;
//        for (int i = 0; i < data.length; i++) {
//            hibyte = crc >> 8;
//            crc = hibyte ^ data[i];
//            for (int j = 0; j < 8; j++) {
//                sbit = crc & 0x0001;
//                crc = crc >> 1;
//                if (sbit == 1)
//                    crc ^= dxs;
//            }
//        }
//        String crcstr = Integer.toHexString(crc & 0xFFFF);
//        if (crcstr.length() < 4) {
//            crcstr = "0" + crcstr;
//        }
//        return crcstr.toUpperCase();
//    }
//    public static String  bytesToHex(byte[] bytes){
//        StringBuffer stringBuffer = new StringBuffer();
//        for (int i=0;i<bytes.length;i++){
//            String hex = Integer.toHexString(bytes[i] & 0xFF);
//            if (hex.length()<2){
//                stringBuffer.append(0);
//            }
//            stringBuffer.append(hex);
//        }
//        return stringBuffer.toString();
//    }
//
//    /**
//     *
//     * @param src	数据
//     * @param len	数据长度
//     * @return
//     */
//    public static String crc16(String src, int len) {
//        int crc = 0x0000FFFF;
//        short tc;
//        char sbit;
//        for (int i = 0; i < len; i++) {
//            tc = (short) (crc >>> 8);
//            crc = ((tc ^ src.charAt(i)) & 0x00FF);
//            for (int r = 0; r < 8; r++) {
//                sbit = (char) (crc & 0x01);
//                crc >>>= 1;
//                if (sbit != 0)
//                    crc = (crc ^ 0xA001) & 0x0000FFFF;
//            }
//        }
//        String str=Integer.toHexString(crc);
//        if(str.length()==3){
//            return "0"+str.toUpperCase();
//        }else if(str.length()==2){
//            return "00"+str.toUpperCase();
//        }else if(str.length()==1){
//            return "000"+str.toUpperCase();
//        }
//        return str.toUpperCase();
//    }

    /**
     * @param data 待校验的字节数组
     * @return
     */
    public static String getCrc(byte[] data) {
        int high;
        int flag;

        // 16位寄存器，所有数位均为1
        int wcrc = 0xffff;
        for (int i = 0; i < data.length; i++) {
            // 16 位寄存器的高位字节
            high = wcrc >> 8;
            // 取被校验串的一个字节与 16 位寄存器的高位字节进行“异或”运算
            wcrc = high ^ data[i];

            for (int j = 0; j < 8; j++) {
                flag = wcrc & 0x0001;
                // 把这个 16 寄存器向右移一位
                wcrc = wcrc >> 1;
                // 若向右(标记位)移出的数位是 1,则生成多项式 1010 0000 0000 0001 和这个寄存器进行“异或”运算
                if (flag == 1)
                    wcrc ^= 0xa001;
            }
        }
        String str = Integer.toHexString(wcrc);
        if (str.length() == 3) {
            return "0" + str.toUpperCase();
        } else if (str.length() == 2) {
            return "00" + str.toUpperCase();
        } else if (str.length() == 1) {
            return "000" + str.toUpperCase();
        }
        return str;
    }
}
