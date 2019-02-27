package edu.berkeley.cdierk.alterwear_camera_v2;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.Button;
import android.view.View;
import android.content.Intent;
import android.provider.MediaStore;
import java.io.File;
import java.io.IOException;
import android.graphics.Color;

import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;
import android.util.Log;

import java.util.Date;
import java.text.SimpleDateFormat;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath;
    private ImageView mImageView;
    private static final String TAG = "MainActivity";
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!OpenCVLoader.initDebug()){
            Toast.makeText(this, "failed to load OpenCV", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "OpenCV Loaded!", Toast.LENGTH_LONG).show();
        }

        this.mImageView = this.findViewById(R.id.imageView1);
        final Button button = findViewById(R.id.button_id);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                } else {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Bitmap photoBW = processingBitmap(photo);

            //edge detection code
            Mat rgba = new Mat();
            Utils.bitmapToMat(photo, rgba);

            Mat edges = new Mat(rgba.size(), CvType.CV_8UC1);
            Imgproc.cvtColor(rgba, edges, Imgproc.COLOR_RGB2GRAY, 4);
            Imgproc.Canny(edges, edges, 80, 100);
            Bitmap photo_edges = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(edges, photo_edges);

            mImageView.setImageBitmap(photo_edges);
        }
    }

    // Convert color photo to black and white
    private Bitmap processingBitmap(Bitmap src){
        Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());

        for (int x = 0; x < src.getWidth(); x++){
            for (int y = 0; y < src.getHeight(); y++){
                int pixelColor = src.getPixel(x, y);
                int pixelAlpha = Color.alpha(pixelColor);
                int pixelRed = Color.red(pixelColor);
                int pixelGreen = Color.green(pixelColor);
                int pixelBlue = Color.blue(pixelColor);

                int pixelBW = (pixelRed + pixelGreen + pixelBlue)/3;
                int newPixel = Color.argb(pixelAlpha, pixelBW, pixelBW, pixelBW);

                dest.setPixel(x, y, newPixel);
            }
        }
        return dest;
    }

}
