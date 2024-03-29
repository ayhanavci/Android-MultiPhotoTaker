## Introduction

This is an Android fragment that adds a custom, mini gallery feature in your project. [Article on my site](https://lain.run/library/Android-Multi-Photo-Taker/)

## Features

* Takes photos one by one with a + button.
* Auto saves photos in full resolution onto disk with unique ids.
* Resamples taken image bitmaps into lower quality bitmaps to reduce memory consumption and preview them all in one screen.
* Users can scroll through previews and select any one of them for a larger preview.
* Can delete saved photos. Deleted photos are removed from disk, large & small previews.
* On Submit & Cancel, sends events to parent with photo URI list.
* On start, can load saved photos into previews using URI list.
* No external dependencies, just native android sdk.
* Can simply be copy-pasted in any project.
* The preview image buttons are added dynamically and as it is, there is no limit to it. (You may want to edit the Java code to limit button creation according to your needs)

## MultiplePhotoTakerFragment Class

| Function | What it does  |
| ---------- | -------------------- |
| setUserActionEventsListener | The calling activity sets its event handler IUserActionEvents  |
| onCreateView | Framework override. Sets up button event handlers and loads photos (if any) into previews  |
| onRequestPermissionsResult | Framework override. Only called on first start, called after user allows camera usage. |
| loadSavedButtons | If parent activity passed URI list, load them into previews. |
| onClickCancel | User clicked Cancel. Notify the parent and press back |
| onClickSubmit | User clicked Submit. Notify the parent with taken photo URI list and press back |
| onClickDeletePhoto | User clicked red trash icon. Delete the photo and update variables & UI |
| createNewPhotoButton | Adds a new camera+ button to right-most. Called after user takes a photo. |
| onClickTakePhoto | User clicked camera+ button. Start camera intent |
| dispatchTakePictureIntent | Start camera intent and save the taken photo onto disk |
| onActivityResult | Framework override. When the photo is successfully taken, update variables & UI. Called after camera intent succeeds. |
| decodeSampledBitmapFromResource | This one resamples the taken photo, essentially shrinking it into less memory consuming bitmaps to be used in previews. |
| calculateInSampleSize | Used by decodeSampledBitmapFromResource. Calculates what dimensions the shrinked photo should have without harming the ratio. |
| onClickLoadPreview | User clicked one of the preview buttons. It is displayed on large preview |
| requestCameraPermission |  Only called on the very first start. Asks user if usage of camera is allowed |
| checkCameraPermission | Called each time app is started to check if the app has camera permissions. |
| newInstance | Factory pattern for fragment creation as recommended by Google. |
| onCreate | Framework override. Doesn't do much |

## Usage

The images used in layout file are stock Android vectors. I didn't add them here since it would be pointless. You may just use whatever icons you have.

1. Add the layout & java files in your project. ([MultiplePhotoTakerFragment.java](https://github.com/ayhanavci/Android-MultiPhotoTaker/blob/master/MultiplePhotoTakerFragment.java) & [multiple_photo_taker_fragment.xml](https://github.com/ayhanavci/Android-MultiPhotoTaker/blob/master/multiple_photo_taker_fragment.xml))
2. Add your own package name on top of the java file.
3. Edit your AndroidManifest.xml for Camera & Storage permissions. Don't forget to add a FileProvider too. Check Android documentation on it.
4. Load the fragment. Here is an example:

```Java
  ArrayList<Uri> photo_files = new ArrayList<>();
  ...
  //You can fill photo_files with uri list to previously saved images. This is passed on "newInstance". 
  //Below, same variable is also used to get submitted photos, which obviously you don't need to.
  ...  
  MultiplePhotoTakerFragment multiplePhotoTakerFragment = MultiplePhotoTakerFragment.newInstance(photo_files);
  multiplePhotoTakerFragment.setUserActionEventsListener(new MultiplePhotoTakerFragment.IUserActionEvents() {
    @Override
    public void onSubmit(ArrayList<Uri> photo_file_paths) {
      //User clicked submit
      photo_files = photo_file_paths; //Save submitted photo uri list.
    }

    @Override
    public void onCancel() {
      //User clicked cancel
    }
  });
  FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
  ft.replace(R.id.content_frame, multiplePhotoTakerFragment);
  ft.addToBackStack(null);
  ft.commit();
```

## Screenshots

1. Fragment starts
<img src="https://github.com/ayhanavci/Android-MultiPhotoTaker/blob/master/readme_img/1.png" height="500">

2. User clicks the photo+ icon. Camera intent starts, user takes a photo
<img src="https://github.com/ayhanavci/Android-MultiPhotoTaker/blob/master/readme_img/2.png" height="500">

3. Back to fragment screen. One photo taken
<img src="https://github.com/ayhanavci/Android-MultiPhotoTaker/blob/master/readme_img/3.png" height="500">

4. User keeps taking photos. The buttons are dynamic and there is no hard limit to it. (The limit is the memory!)

* User can click the red trash icon on top right to delete the previewed photo.
* Scroll left and right on the nested scroll with the small previews.
* Take more photos using camera+ icon.
* Click submit to send file list to onSubmit event or can cancel.

<img src="https://github.com/ayhanavci/Android-MultiPhotoTaker/blob/master/readme_img/4.png" height="500">

## Licence

Feel free to customise according to your needs.

[MIT](https://opensource.org/licenses/MIT)

## Author

Ayhan AVCI 2019

ayhanavci@gmail.com

[ayhanavci.xyz](https://ayhanavci.xyz)
