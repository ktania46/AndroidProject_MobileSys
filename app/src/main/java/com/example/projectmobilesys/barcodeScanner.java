package com.example.projectmobilesys;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class barcodeScanner extends AppCompatActivity {

    private MaterialButton cameraBtn;
    private MaterialButton galleryBtn;
    private MaterialButton scanBtn;
    private TextView resultTv;
    private ImageView imageIv;

    // to handle the result of camera/gallery permissions in onRequesrPermissionResults
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;

    // array of permissions required to pick image from camera/gallery
    private String[] cameraPermissions;
    private String[] storagePermissions;

    // uri of image that we will take from gallery/camera
    private Uri imageUri = null;

    private BarcodeScannerOptions barcodeScannerOptions;
    private BarcodeScanner barcodeScanner;

    private static final String TAG = "MAIN_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        cameraBtn = findViewById(R.id.cameraBtn);
        galleryBtn = findViewById(R.id.galleryBtn);
        scanBtn = findViewById(R.id.scanBtn);
        resultTv = findViewById(R.id.resultTv);
        imageIv = findViewById(R.id.imageIv);

        // init camera permissions required to pick image from camera/gallery
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        barcodeScannerOptions = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build();
        // init barcodeScanner with BarcodeScanning options
        barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions);

        // handle cameraBtn click, check permissions related to camera( i,e WRITE STORAGE & CAMER) and take image from camera
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkCameraPermission()){
                    // permission required for camera already granted, launch camera intent
                    pickImageCamera();
                }
                else {
                    // permission required for camera not granted, require permissions
                    requestCameraPermission();
                }
            }
        });
        // handle galleryBtn click, check permissions related to Gallery( i,e WRITE STORAGE) and take image from camera
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkStoragePermission()){
                    // permission required for gallery already granted, request permissions
                    pickImageGallery();
                }
                else {
                    // permission required for gallery not granted, require permissions
                    requestStoragePermission();
                }

            }
        });
        // handle scanBtn click, scan bar/QR code from image picked from gallery/camera
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUri == null){
                    Toast.makeText(barcodeScanner.this, "Pick image first", Toast.LENGTH_SHORT).show();
                }
                else{
                    detectResultFromImage();
                }
            }
        });

    }
    private  void detectResultFromImage(){
        try {
            // prepare image from image uri
            InputImage inputImage = InputImage.fromFilePath(this, imageUri);
            // start scanning the Bar/QR code data from image
            Task<List<Barcode>> barcodeResult = barcodeScanner.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            // task completed successfully, we can get detailed info now
                            extractBarCodeQRCodeInfo(barcodes);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // task failed with an exception, we can't get any details
                            Toast.makeText(barcodeScanner.this, "Failed scanning due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
        catch (Exception e){
        // failed with an exception either due to preparing InputImage from where we can pick the image
            Toast.makeText(this, "Failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void extractBarCodeQRCodeInfo(List<Barcode> barcodes){
        // get info from Bar code
        for(Barcode barcode : barcodes){
            Rect bounds = barcode.getBoundingBox();
            Point[] corners = barcode.getCornerPoints();

            // Raw info scanned from Bar code
            String rawValue = barcode.getRawValue();
            Log.d(TAG, "Extract Bar/QR code info: rawValue: "+ rawValue);

            int valueType = barcode.getValueType();

            switch (valueType){
                case Barcode.TYPE_WIFI:{
                    // to get Wifi related to data
                    Barcode.WiFi typeWifi = barcode.getWifi();
                    // to get all info about wifi
                    String ssid = ""+ typeWifi.getSsid();
                    String password = ""+ typeWifi.getPassword();
                    String encryptionType = ""+ typeWifi.getEncryptionType();
                    // show in log
                    Log.d(TAG, "extractBarCodeQRCodeInfo: TYPE_WIFI");
                    Log.d(TAG, "extractBarCodeQRCodeInfo: ssid: "+ ssid);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: password: "+ password);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: encryptionType: "+ encryptionType);
                    // set to textview
                    resultTv.setText("TYPE: TYPE_WIFI \nssid: "+ ssid +"\npassword: "+ password +"\nencryptionType"+ encryptionType +"\nraw value: "+rawValue);

                }
                break;
                case Barcode.TYPE_URL:{
                    // to get URL related to data
                    Barcode.UrlBookmark typeUrl = barcode.getUrl();
                    // to get all info about url
                    String title = ""+ typeUrl.getTitle();
                    String url = ""+ typeUrl.getUrl();
                    // show in log
                    Log.d(TAG, "extractBarCodeQRCodeInfo: TYPE_URL");
                    Log.d(TAG, "extractBarCodeQRCodeInfo: title: "+ title);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: url: "+ url);
                    // set to textview
                    resultTv.setText("TYPE: TYPE_URL \ntitle: "+ title +"\nurl: "+ url +"\nraw value: "+rawValue);


                }
                break;
                case Barcode.TYPE_EMAIL:{
                    // to get email related to data
                    Barcode.Email typeEmail = barcode.getEmail();
                    // to get all info about email
                    String address = ""+ typeEmail.getAddress();
                    String body = ""+ typeEmail.getBody();
                    String subject = ""+ typeEmail.getSubject();
                    // show in log
                    Log.d(TAG, "extractBarCodeQRCodeInfo: TYPE_EMAIL");
                    Log.d(TAG, "extractBarCodeQRCodeInfo: ssid: "+ address);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: body: "+ body);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: subject: "+ subject);
                    // set to textview
                    resultTv.setText("TYPE: TYPE_EMAIL \naddress: "+ address +"\nbody: "+ body +"\nsubject"+ subject +"\nraw value: "+rawValue);

                }
                break;
                case Barcode.TYPE_CONTACT_INFO:{
                    // to get Contact related to data
                    Barcode.ContactInfo typeContact = barcode.getContactInfo();
                    // to get all info about ContactInfo
                    String title = ""+ typeContact.getTitle();
                    String organizer = ""+ typeContact.getOrganization();
                    String name = ""+ typeContact.getName().getFirst()+" "+ typeContact.getName().getLast();
                    String phone = ""+ typeContact.getPhones().get(0).getNumber();
                    // show in log
                    Log.d(TAG, "extractBarCodeQRCodeInfo: TYPE_CONTACT_INFO");
                    Log.d(TAG, "extractBarCodeQRCodeInfo: title: "+ title);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: organizer: "+ organizer);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: name: "+ name);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: phone: "+ phone);
                    // set to textview
                    resultTv.setText("TYPE: TYPE_CONTACT_INFO \ntitle: "+ title +"\norganizer: "+ organizer +"\nname"+ name + "\nphone"+ phone +"\nraw value: "+rawValue);

                }
                break;
                default:{
                    resultTv.setText("raw value " + rawValue);
                }
            }

        }
    }

    private void pickImageGallery(){
        // intent to pick image from galary, will show all resources from where we can pick the image
        Intent intent = new Intent(Intent.ACTION_PICK);
        // Set type of the file we want to pick i.e. image
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);

    }

    private  final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                   // here we will receive image if picked from gallery
                    if (result.getResultCode() == Activity.RESULT_OK){
                        // image picked, get the uri of image picked
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult:imageUri: "+imageUri);
                        // set to imageview
                        imageIv.setImageURI(imageUri);
                    }
                    else {

                        //cancelled
                        Toast.makeText(barcodeScanner.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void pickImageCamera(){

        // get ready the image data to store in Media Store
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Sample Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description");
        // image Uri
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        // intent to launch camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // here we will receive the image, if taken from camera
                    if (result.getResultCode() == Activity.RESULT_OK){
                        // image is taken from camera
                        Intent data = result.getData();
                       // we already have image in imageUri using function pickImageCamera
                        Log.d(TAG, "onActivityResult: imageUri: "+ imageUri);
                        // set to imageview
                        imageIv.setImageURI(imageUri);
                    }
                    else{
                        // cancelled
                        Toast.makeText(barcodeScanner.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private boolean checkStoragePermission(){

        /*check if storage permission is allowed or not
        return true if allowed, false if not allowed
         */
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        // return result true if permission WRITE_EXTERNAL_STORAGE granted or false if denied
        return result;
    }

    private void requestStoragePermission(){

        //request storage permission (for galary image pick)
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        // check if camera permission allowed, true if yes, false if no
        Boolean resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                ==PackageManager.PERMISSION_GRANTED;

        // check if storage permission allowed, true if yes, false if no
        boolean resultStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==PackageManager.PERMISSION_GRANTED;

        // return both results as true/false
        return resultCamera && resultStorage;
    }

    private void requestCameraPermission(){
    //request camera permission (for camera intent)
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                // check if some action from permission dialog performed or not Allowed/Denied
                if (grantResults.length > 0){
                    // check if camera storage permission granted, contains boolean results either true or false
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    // check if both permissions granted or not
                    if(cameraAccepted && storageAccepted){
                        // camera permissions granted, we can launch gallery intent
                        pickImageCamera();
                    }
                    else {
                        // one or both permissions are denied, can't launch camera intent
                        Toast.makeText(this, "Camera & Storage permissions are required", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE:{
                // check if some action from permission dialog performed or not Allowed/Denied
                if(grantResults.length>0){
                    // check if storage permission granted, contains boolean results either true or false
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    // storage permissions granted or not
                    if (storageAccepted){
                        // storage permissions granted, we can launch gallery intent
                        pickImageGallery();
                    }
                    else {
                        // storage permissions are denied, can't launch camera intent
                        Toast.makeText(this, "Storage permission is required...", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }
    }
}
