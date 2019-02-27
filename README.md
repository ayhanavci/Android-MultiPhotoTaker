## Introduction

This is an Android fragment that adds a custom, mini gallery feature in your project. 

## Features

* Takes photos one by one with a + button.
* Auto saves photos in full resolution into disk with unique ids.
* Resamples taken image bitmaps into lower quality bitmaps to reduce memory consumption and preview them all in one screen.
* User can scroll through previews and select any one of them for a larger preview.
* Can delete saved photos. Deleted photos are removed from disk, large & small previews.
* On Submit & Cancel, sends events to parent with photo uri list.
* On start, can load saved photos into previews using a uri list.
* No external dependencies, just native android sdk.
* Can simply be copy-pasted in any project.

## Usage

The images used in layout file are stock Android vectors. I didn't add them here since it would be pointless. You may just use whatever icons you have.

1. Add the layout & java files in project.
2. Add your own package name on top of the java file.
3. Load the fragment. Here is an example:

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

4. User keeps taking photos.

* User can click the red trash icon on top right to delete the previewed photo.
* Scroll left and right on the nested scroll with the small previews.
* Take more photos using camera+ icon.
* Click submit to send file list to onSubmit event or can cancel.

<img src="https://github.com/ayhanavci/Android-MultiPhotoTaker/blob/master/readme_img/4.png" height="500">

## Licence

Feel free to customise to your needs.

[MIT](https://opensource.org/licenses/MIT)

## Author

Ayhan AVCI 2019

ayhanavci@gmail.com

[lain.run](https://lain.run)