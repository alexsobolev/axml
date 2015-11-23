package com.example.myapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyActivity extends Activity {


    public static final short RES_XML_TYPE = 3;
    public static final short RES_STRING_POOL_TYPE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        putShort(outputStream, RES_XML_TYPE); // type [XML]
        putShort(outputStream, (short) 8); // header size
        putInt(outputStream, 708);       // chunk size
        writeStringPool(outputStream);

        View view = LayoutInflater.from(this).inflate(R.layout.main, null);
        setContentView(view);
    }

    private static void putShort(ByteArrayOutputStream outputStream, short data){
        try {
            outputStream.write(ByteBuffer.allocate(Short.SIZE/Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN).putShort(data).array());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void putInt(ByteArrayOutputStream outputStream, int data){
        try {
            outputStream.write(ByteBuffer.allocate(Integer.SIZE/Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN).putInt(data).array());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void putByteArray(ByteArrayOutputStream outputStream, byte[] data){
        try {
            outputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeStringPool(ByteArrayOutputStream outputStream){
        StringPool stringPool = new StringPool();
        stringPool.pushStringIntoPool("orientation");
        stringPool.pushStringIntoPool("layout_width");
        stringPool.pushStringIntoPool("layout_height");
        stringPool.pushStringIntoPool("text");
        stringPool.pushStringIntoPool("android");
        stringPool.pushStringIntoPool("http://schemas.android.com/apk/res/android");
        stringPool.pushStringIntoPool("");
        stringPool.pushStringIntoPool("LinearLayout");
        stringPool.pushStringIntoPool("TextView");
        stringPool.pushStringIntoPool("Hello World, PendragonActivity");

        stringPool.writeTo(outputStream);
        //Log.d("Result: ", "0" + new BigInteger(outputStream.toByteArray()).toString(16));
    }

    static class StringPool {
        private Map<String, byte[]> strings = new HashMap<String, byte[]>();
        private List<String> stringsIndex = new ArrayList<String>();
        private int headerSize = 28;

        public StringPool() {
        }

        public int pushStringIntoPool(String text) {
            int index = stringsIndex.indexOf(text);
            if(index > -1)
                return index;

            byte[] data = text.getBytes();
            ByteBuffer byteBuffer = ByteBuffer.allocate(data.length + 4)
                    .put((byte) data.length)
                    .put((byte) data.length)
                    .put(data)
                    .put((byte) 0);
            strings.put(text, byteBuffer.array());
            stringsIndex.add(text);
            return strings.size() - 1;
        }

        public void writeTo(ByteArrayOutputStream outputStream){
            int size = 0;
            ByteArrayOutputStream chunkBodyOutputStream = new ByteArrayOutputStream();
            for(String index : stringsIndex){
                putInt(outputStream, size);
                size += strings.get(index).length;
                try {
                    chunkBodyOutputStream.write(strings.get(index));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                putShort(outputStream, RES_STRING_POOL_TYPE);
                putInt(outputStream, headerSize); // размер заголовка
                putInt(outputStream, size + headerSize); // размер всего чанка

                putInt(outputStream, size()); // кол-во элементов в пуле
                putInt(outputStream, 0); // кол-во стилей в массиве стилей
                putInt(outputStream, 0); // флаги
                putInt(outputStream, size()*4+headerSize); // начало строк
                putInt(outputStream, 0); // начало стилей

                chunkBodyOutputStream.writeTo(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public int size(){
            return stringsIndex.size();
        }
    }

    public static void main(String[] args) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        putShort(outputStream, RES_XML_TYPE); // type [XML]
        putShort(outputStream, (short) 8); // header size
        putInt(outputStream, 708);       // chunk size
        writeStringPool(outputStream);

        try {
            new FileOutputStream(new File("/home/sobolev/main.xml")).write(outputStream.toByteArray());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
