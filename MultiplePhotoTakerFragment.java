//Add your package here

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MultiplePhotoTakerFragment extends DialogFragment {
    View view;
    ImageView img_photo_preview;
    ImageButton btn_delete_item;
    LinearLayout layout_photo_buttons;
    HorizontalScrollView photo_buttons_scroll_view;
    Uri photo_uri;
    HashMap<ImageButton, Uri>  button_uri_pair = new HashMap<>();
    ArrayList<Uri> photo_file_paths = new ArrayList<>();
    Uri selected_uri = null;

    Button btn_camera_submit, btn_camera_cancel;

    private MultiplePhotoTakerFragment.IUserActionEvents user_events_listener = null;
    public interface IUserActionEvents {
        void onSubmit(ArrayList<Uri> photo_file_paths);
        void onCancel();
    }
    public void setUserActionEventsListener(MultiplePhotoTakerFragment.IUserActionEvents listener) {user_events_listener  = listener;}

    static final int REQUEST_TAKE_PHOTO = 1000;
    static final int REQUEST_CAMERA_PERMISSION_CODE = 1001;

    public MultiplePhotoTakerFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            view = inflater.inflate(R.layout.multiple_photo_taker_fragment, null);

            if (!checkCameraPermission())
                requestCameraPermission();

            img_photo_preview = view.findViewById(R.id.img_photo_preview);
            btn_delete_item = view.findViewById(R.id.btn_delete_item);
            photo_buttons_scroll_view = view.findViewById(R.id.photo_buttons_scroll_view);
            layout_photo_buttons =  view.findViewById(R.id.layout_photo_buttons);

            loadSavedButtons();
            button_uri_pair.put(createNewPhotoButton(), null);
            btn_delete_item.setOnClickListener(v -> onClickDeletePhoto(v));

            btn_camera_submit = view.findViewById(R.id.btn_camera_submit);
            btn_camera_cancel = view.findViewById(R.id.btn_camera_cancel);
            btn_camera_submit.setOnClickListener(v -> onClickSubmit(v));
            btn_camera_cancel.setOnClickListener(v -> onClickCancel(v));


        }
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), "Camera Permission Granted", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getActivity(), "Camera Permission Denied", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    private void loadSavedButtons() {
        for (int i = 0; i < photo_file_paths.size(); ++i) {
            Uri saved_photo_uri = photo_file_paths.get(i);
            ImageButton new_button = createNewPhotoButton();
            button_uri_pair.put(new_button, saved_photo_uri);
            Bitmap thumbImage = decodeSampledBitmapFromResource(getResources(), saved_photo_uri , 100, 100);
            new_button.setImageBitmap(thumbImage);
            new_button.setOnClickListener(v -> onClickLoadPreview(v));
        }
    }

    private void onClickCancel(View v) {
        if (user_events_listener != null)
            user_events_listener.onCancel();
        if (getActivity() != null)
            getActivity().onBackPressed();
    }
    private void onClickSubmit(View v) {
        photo_file_paths.clear();
        if (user_events_listener != null) {
            for (Map.Entry<ImageButton, Uri> entry : button_uri_pair.entrySet()) {
                Uri uri = entry.getValue();
                if (uri != null)
                    photo_file_paths.add(entry.getValue());
            }

            user_events_listener.onSubmit(photo_file_paths);
        }
        if (getActivity() != null)
            getActivity().onBackPressed();
    }

    private void onClickDeletePhoto(View v) {
        if (selected_uri != null) {
            for (Map.Entry<ImageButton, Uri> entry : button_uri_pair.entrySet()) {
                ImageButton key = entry.getKey();
                Uri value = entry.getValue();
                if (value == selected_uri) {
                    selected_uri = null;
                    button_uri_pair.remove(key);
                    layout_photo_buttons.removeView(key);
                    btn_delete_item.setVisibility(View.GONE);
                    img_photo_preview.setImageResource(R.drawable.ic_preview_photo);
                    if (getActivity() != null) {
                        ContentResolver content_resolver = getActivity().getContentResolver();
                        content_resolver.delete(value, null, null);
                    }

                    break;
                }
            }
        }
    }

    ImageButton createNewPhotoButton() {

        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.MATCH_PARENT);
        layoutParams.leftMargin = 10;
        ImageButton new_button = new ImageButton(getContext());
        new_button.setImageResource(R.drawable.ic_add_photo);
        new_button.setScaleType(ImageView.ScaleType.FIT_CENTER);
        new_button.setAdjustViewBounds(true);
        new_button.setLayoutParams(layoutParams);
        new_button.setOnClickListener(v -> onClickTakePhoto(v));
        new_button.setBackgroundColor(Color.TRANSPARENT);
        new_button.setId(button_uri_pair.size() + 100);

        layout_photo_buttons.addView(new_button);

        return new_button;
    }

    private void onClickTakePhoto(View v) {
        if (!checkCameraPermission()) {
            requestCameraPermission();
            return;
        }

        dispatchTakePictureIntent(UUID.randomUUID().toString());
    }

    String mCurrentPhotoPath;

    private File createImageFile(String image_file_name) throws IOException {
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                image_file_name,  /* prefix */
                ".jpg",  /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void dispatchTakePictureIntent(String image_file_name) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photo_file = null;
            try {
                photo_file = createImageFile(image_file_name);
            } catch (IOException ex) {

            }
            if (photo_file != null) {
                photo_uri = FileProvider.getUriForFile(getActivity(),
                        BuildConfig.APPLICATION_ID + ".provider",photo_file);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photo_uri);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }

        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            Bitmap thumbLargeImage = decodeSampledBitmapFromResource(getResources(), photo_uri, 500, 500);
            img_photo_preview.setImageBitmap(thumbLargeImage);

            for (Map.Entry<ImageButton, Uri> entry : button_uri_pair.entrySet()) {
                ImageButton key = entry.getKey();
                Uri value = entry.getValue();
                if (value == null) {
                    button_uri_pair.put(key, photo_uri);
                    key.setOnClickListener(v -> onClickLoadPreview(v));
                    Bitmap thumb_image = decodeSampledBitmapFromResource(getResources(), photo_uri, 100, 100);
                    key.setImageBitmap(thumb_image);
                    selected_uri = photo_uri;
                    btn_delete_item.setVisibility(View.VISIBLE);
                    button_uri_pair.put(createNewPhotoButton(), null); //Add a new button

                    photo_buttons_scroll_view.postDelayed(() -> {
                        photo_buttons_scroll_view.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                    }, 100L);
                    break;
                }
            }

        }
    }


    public Bitmap decodeSampledBitmapFromResource(Resources res, Uri uri, int reqWidth, int reqHeight) {

        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            ContentResolver contentResolver = getActivity().getContentResolver();
            BitmapFactory.decodeStream(contentResolver.openInputStream(uri), null, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            //return BitmapFactory.decodeResource(res, resId, options);
            return BitmapFactory.decodeStream(contentResolver.openInputStream(uri), null, options);

        }
        catch (FileNotFoundException e) {
            return null;
        }

    }
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int in_sample_size = 1;

        if (height > reqHeight || width > reqWidth) {

            final int half_height = height / 2;
            final int half_width = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((half_height / in_sample_size) >= reqHeight
                    && (half_width / in_sample_size) >= reqWidth) {
                in_sample_size *= 2;
            }
        }

        return in_sample_size;
    }
    private void onClickLoadPreview(View v) {
        ImageButton image_button = (ImageButton) v;
        for (Map.Entry<ImageButton, Uri> entry : button_uri_pair.entrySet()) {
            ImageButton key = entry.getKey();
            Uri value = entry.getValue();
            if (image_button == key) {
                if (value != null) {
                    selected_uri = value;
                    Bitmap thumbImage = decodeSampledBitmapFromResource(getResources(), value, 500, 500);
                    img_photo_preview.setImageBitmap(thumbImage);
                    btn_delete_item.setVisibility(View.VISIBLE);
                }
            }
        }
    }



    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        }, REQUEST_CAMERA_PERMISSION_CODE);
    }

    private boolean checkCameraPermission() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int access_camera_result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED && access_camera_result == PackageManager.PERMISSION_GRANTED;
    }

    public static String ARG_PARAM_PHOTOFILES = "PHOTOFILES";


    public static MultiplePhotoTakerFragment newInstance(ArrayList<Uri> photo_file_paths) {
        MultiplePhotoTakerFragment fragment = new MultiplePhotoTakerFragment();
        final Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_PHOTOFILES, photo_file_paths);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.photo_file_paths = (ArrayList<Uri>) getArguments().getSerializable(ARG_PARAM_PHOTOFILES);
            setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        }
    }
}
