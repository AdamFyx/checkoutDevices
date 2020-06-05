package com.example.checkoutdevices.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.checkoutdevices.DB.SQLFunction;
import com.example.checkoutdevices.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class QrcodeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.infos);

        SQLFunction.initTable(QrcodeActivity.this);
        Button insertBtn = findViewById(R.id.insertsql);
        insertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertSql();
            }
        });



//        ImageView img = (ImageView) findViewById(R.id.qrimage);
//        Intent intent = getIntent();
//        Bundle bundle = intent.getExtras();
//        String infos = bundle.getString("devicesinfo");
//        Toast.makeText(this, infos + "11111111111", Toast.LENGTH_LONG).show();
//        Bitmap bitmap = qrcodeBuilt(infos);
//        img.setImageBitmap(bitmap);

    }

    /*
    根据网关的信息生成二维码
     */
    public static Bitmap qrcodeBuilt(String str) {
        //生成二维矩阵,编码时要指定大小,
        //不要生成了图片以后再进行缩放,以防模糊导致识别失败
        try {
            str = new String(str.getBytes("UTF-8"), "ISO-8859-1");
            BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, 240, 240);
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            //  二维矩阵转为一维像素数组（一直横着排）
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (matrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            // 通过像素数组生成bitmap, 具体参考api
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object[] insertSql() {
        /*
        获取当前系统时间
         */
        // SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");// HH:mm:ss
        Date date = new Date(System.currentTimeMillis());
        String time = simpleDateFormat.format(date);
        SQLFunction sqlFunction = new SQLFunction();
        EditText id = findViewById(R.id.d_id);
        EditText ping = findViewById(R.id.ping);
        EditText version = findViewById(R.id.version);
        EditText type = findViewById(R.id.type);
        EditText firm = findViewById(R.id.firm);
        EditText issynchr = findViewById(R.id.issynchr);
        EditText qrcode=findViewById(R.id.qrcode);

        Object[] data = {id.getText().toString(), ping.getText().toString(),
                version.getText().toString(), time,  type.getText().toString(),
                firm.getText().toString(), issynchr.getText().toString(),qrcode.getText().toString()};
        Object[] newData = {
                "网关类型:" + type.getText().toString(),
                "网关厂商:" + firm.getText().toString(),
                "网关id值：" + id.getText().toString(),
                "网关ping值：" + ping.getText().toString(),
                "网关版本号：" + version.getText().toString(),
                "录入时间：" + time,
                "是否同步" + issynchr.getText().toString(),
                "是否关联了二维码" + qrcode.getText().toString()
        };
        Log.d("ManagerActivity", data.toString());
        sqlFunction.insert(QrcodeActivity.this, data);
        return newData;
    }

}