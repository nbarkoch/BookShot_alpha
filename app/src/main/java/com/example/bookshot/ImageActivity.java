package com.example.bookshot;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.bookshot.aws.S3Uploader;
import com.example.bookshot.aws.S3Utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageActivity extends AppCompatActivity {
    String AUTHORITY = "com.example.bookshot.fileprovider";
    Button bt_upload, bt_select, bt_camera;
    ImageView imageView;
    S3Uploader s3uploaderObj;
    String urlFromS3 = null;
    static Uri imageUri;
    private String TAG = ImageActivity.class.getCanonicalName();
    public static final int CAMERA_PERM_CODE = 101;
    public static final int GALLERY_PERM_CODE = 103;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 105;
    String currentPhotoPath;
    public static boolean isImageFromGallery;
    ProgressDialog progressDialog;
    Switch switchMultiple;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        s3uploaderObj = new S3Uploader(ImageActivity.this);
        progressDialog = new ProgressDialog(ImageActivity.this);
        bt_upload = findViewById(R.id.bt_upload);
        bt_camera = findViewById(R.id.bt_camera);
        bt_select = findViewById(R.id.bt_select);
        imageView = findViewById(R.id.image);
        switchMultiple = findViewById(R.id.switch_multiple);

        bt_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStoragePermissionGranted();
            }
        });
        bt_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCameraPermissionGranted();
            }
        });
        bt_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (count > 0) {
                    uploadImageTos3(imageUri);
                } else {
                    Toast.makeText(ImageActivity.this, "Choose image first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void isCameraPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERM_CODE);
            }
        } else {
            Log.v(TAG, "Permission is granted");
            dispatchTakePictureIntent();
        }
    }

    public void isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                chooseImage();
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, GALLERY_PERM_CODE);
            }
        } else {
            Log.v(TAG, "Permission is granted");
            chooseImage();
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST_CODE);
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.i("exception:", ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        AUTHORITY,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == GALLERY_PERM_CODE) {
            chooseImage();
            Log.e(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == CAMERA_PERM_CODE) {
            dispatchTakePictureIntent();
            Log.e(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
        } else {
            Log.e(TAG, "Please click again and select allow to choose profile picture");
        }
    }

    private void uploadImageTos3(Uri imageUri) {
        String filePath;
        if (isImageFromGallery)
            filePath = getFilePathFromURI(imageUri);
        else
            filePath = getFilePathFromProvider(imageUri);
        Log.d("", "Chosen path = " + filePath);
        final String path = filePath;
        if (path != null) {
            showLoading();
            s3uploaderObj.initUpload(path);
            s3uploaderObj.setOns3UploadDone(new S3Uploader.S3UploadInterface() {
                @Override
                public void onUploadSuccess(String response) {
                    if (response.equalsIgnoreCase("Success")) {
                        hideLoading();
                        urlFromS3 = S3Utils.generates3ShareUrl(getApplicationContext(), path);
                        if (!TextUtils.isEmpty(urlFromS3)) {
                            Toast.makeText(ImageActivity.this, "Uploaded Successfully!!", Toast.LENGTH_SHORT).show();
                            boolean switchValue = switchMultiple.isChecked();
                            if (switchValue)
                                moveToMultiCatalogActivity(path);
                            else
                                moveToCatalogActivity(path);
                        }
                    }
                }

                @Override
                public void onUploadError(String response) {
                    hideLoading();
                    Log.e(TAG, "Error Uploading");
                }
            });
        } else {
            Toast.makeText(this, "Null Path", Toast.LENGTH_SHORT).show();
        }
    }


    private void showLoading() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.setMessage("Uploading Image !!");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                File f = new File(currentPhotoPath);
                imageView.setImageURI(Uri.fromFile(f));
                Log.d("tag", "Absolute Url of Image is " + Uri.fromFile(f));
                Uri contentUri = Uri.fromFile(f);
                imageUri = contentUri;
                isImageFromGallery = false;
                count++;

            }
        }
        if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri contentUri = data.getData();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "." + getFileExt(contentUri);
                Log.d("tag", "onActivityResult: Gallery Image Uri:  " + imageFileName);
                imageView.setImageURI(contentUri);
                imageUri = contentUri;
                isImageFromGallery = true;
                count++;
            }
        }
    }

    private String getFileExt(Uri contentUri) {
        ContentResolver c = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void moveToCatalogActivity(String path) {
        Intent intent = new Intent(this, CatalogActivity.class);
        String keyIdentifier = path.substring(path.lastIndexOf("/") + 1);
        intent.putExtra("fileName", keyIdentifier);
        intent.putExtra("switchMultiple", false);
        startActivity(intent);
    }

    private void moveToMultiCatalogActivity(String path) {
        Intent intent = new Intent(this, MultiCatalogActivity.class);
        String keyIdentifier = path.substring(path.lastIndexOf("/") + 1);
        intent.putExtra("fileName", keyIdentifier);
        startActivity(intent);
    }


    private String getFilePathFromURI(Uri selectedImageUri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(selectedImageUri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);

        assert cursor != null;
        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    private String getFilePathFromProvider(Uri cameraImageUri) {
        String filePath;
        Log.d("", "URI = " + cameraImageUri);
        if (cameraImageUri != null && "content".equals(cameraImageUri.getScheme())) {
            Cursor cursor = this.getContentResolver().query(cameraImageUri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            cursor.moveToFirst();
            filePath = cursor.getString(0);
            cursor.close();
        } else {
            filePath = cameraImageUri.getPath();
        }
        return filePath;
    }


}
