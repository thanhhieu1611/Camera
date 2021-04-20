package com.example.takephoto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import jp.wasabeef.picasso.transformations.BlurTransformation;
import jp.wasabeef.picasso.transformations.gpu.ContrastFilterTransformation;
import jp.wasabeef.picasso.transformations.gpu.InvertFilterTransformation;
import jp.wasabeef.picasso.transformations.gpu.KuwaharaFilterTransformation;
import jp.wasabeef.picasso.transformations.gpu.PixelationFilterTransformation;
import jp.wasabeef.picasso.transformations.gpu.SepiaFilterTransformation;
import jp.wasabeef.picasso.transformations.gpu.SketchFilterTransformation;
import jp.wasabeef.picasso.transformations.gpu.SwirlFilterTransformation;
import jp.wasabeef.picasso.transformations.gpu.ToonFilterTransformation;
import jp.wasabeef.picasso.transformations.gpu.VignetteFilterTransformation;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentPhotoPath;

    // Declare objects
    Button takePhoto;
    ImageView photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get objects
        takePhoto = findViewById(R.id.btnTakePhoto);
        photo = findViewById(R.id.imgPhotoTaken);

        // Add Click event to takephoto button object
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

    }

    //Take photo with a camera app
    private void dispatchTakePictureIntent(){
        // Check device's camera availability and number of cameras
        checkCameraHardware(MainActivity.this);

        // A quick way to enable taking pictures or videos in your application without a lot of extra code
        // is to use an Intent to invoke an existing Android camera application
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            // Create a file where the photo should go
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }catch(IOException ex){
                Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            if(photoFile != null){
                // We are using getUriForFile(Context, String, File) which returns a content URI.
                // For more recent apps targeting Android 7.0 (API level 24) and higher,
                // passing a file: URI across a package boundary causes a FileUriExposedException.
                // Therefore, we now present a more generic way of storing images using a FileProvider.
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // Create collision-resistant file name
    private File createImageFile() throws IOException{
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        );

        // Save a file; path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        //Toast.makeText(MainActivity.this, currentPhotoPath,Toast.LENGTH_SHORT).show();
        return image;
    }

    // Get the thumbnail of image captured and store in photo object
    // The Android Camera application encodes the photo in the return Intent
    // delivered to onActivityResult() as a small Bitmap in the extras, under the key "data"
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            /*Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            photo.setImageBitmap(imageBitmap);*/

            // Invoke media'scanner
            // galleryAddPic();

            setPic();
        }
    }

    // Invoke the system's media scanner to add your photo to the Media Provider's database
    // Make it available in the Android Gallery application and to other apps
    //  If you saved your photo to the directory provided by getExternalFilesDir(),
    //  the media scanner cannot access the files because they are private to your app.
    /*private void galleryAddPic(){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }*/

    //Managing multiple full-sized images can be tricky with limited memory.
    // If you find your application running out of memory after displaying just a few images,
    // you can dramatically reduce the amount of dynamic heap used by expanding the JPEG
    // into a memory array that's already scaled to match the size of the destination view.
    private void setPic() {
        // Get the dimensions of the View
        int targetW = photo.getWidth();
        int targetH = photo.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        photo.setImageBitmap(bitmap);
    }

    // Check if this device has a camera
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            Toast.makeText(context, "This device has a camera", Toast.LENGTH_SHORT).show();
            // Check how many camera does it have
            Toast.makeText(context, "This device has " + String.valueOf(Camera.getNumberOfCameras() + " cameras."), Toast.LENGTH_SHORT).show();
            return true;
        } else {
            // no camera on this device
            Toast.makeText(context, "This device has NO camera", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // Button handle to transform picture
    public void buttonHandler(View v) {
        if (currentPhotoPath == null) return;
        switch (v.getId()){
            case R.id.button1:
                Picasso.get().load(new File(currentPhotoPath)).transform(new VignetteFilterTransformation(this)).into((ImageView) findViewById(R.id.imgPhotoTaken));
                break;
            case R.id.button2:
                Picasso.get().load(new File(currentPhotoPath)).transform(new ToonFilterTransformation(this)).into((ImageView) findViewById(R.id.imgPhotoTaken));
                break;
            case R.id.button3:
                Picasso.get().load(new File(currentPhotoPath)).transform(new SepiaFilterTransformation(this)).into((ImageView) findViewById(R.id.imgPhotoTaken));
                break;
            case R.id.button4:
                Picasso.get().load(new File(currentPhotoPath)).transform(new ContrastFilterTransformation(this)).into((ImageView) findViewById(R.id.imgPhotoTaken));
                break;
            case R.id.button5:
                Picasso.get().load(new File(currentPhotoPath)).transform(new InvertFilterTransformation(this)).into((ImageView) findViewById(R.id.imgPhotoTaken));
                break;
            case R.id.button6:
                Picasso.get().load(new File(currentPhotoPath)).transform(new PixelationFilterTransformation(this)).into((ImageView) findViewById(R.id.imgPhotoTaken));
                break;
            case R.id.button7:
                Picasso.get().load(new File(currentPhotoPath)).transform(new SketchFilterTransformation(this)).into((ImageView) findViewById(R.id.imgPhotoTaken));
                break;
            case R.id.button8:
                Picasso.get().load(new File(currentPhotoPath)).transform(new SwirlFilterTransformation(this)).into((ImageView) findViewById(R.id.imgPhotoTaken));
                break;
            case R.id.button9:
                Picasso.get().load(new File(currentPhotoPath)).transform(new KuwaharaFilterTransformation(this)).into((ImageView) findViewById(R.id.imgPhotoTaken));
                break;
        }
    }
}