package com.siahmsoft.soundroid.sdk7;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;




public class ImageListActivity extends ListActivity{

    public static int REQUEST_CODE_OK_IMAGE = 112;

    static String[] projection = {
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.TITLE,
        MediaStore.Images.Media.SIZE,
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media.MINI_THUMB_MAGIC
    };

    static String[] from = new String[] {
        MediaStore.Images.Media.TITLE,
        MediaStore.Images.Media.MINI_THUMB_MAGIC,
        MediaStore.Images.Media.SIZE
    };

    String[] fromThumbnails = new String[] {
            MediaStore.Images.Thumbnails._ID,
            MediaStore.Images.Thumbnails.DATA
    };

    static int[] to = new int[] {
        R.id.file_photo_name,
        R.id.file_photo,
        R.id.file_photo_size
    };

    static Options o;

    static{
        o = new Options();
        o.inSampleSize = 8;
        o.inTempStorage = new byte[16384];
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Cursor cursor = getContentResolver().query(	MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,//MediaStore.Images.Media.SIZE + " < " +  2147484,
                null,
                MediaStore.Images.Media.TITLE);

        startManagingCursor(cursor);

        // Used to map notes entries from the database to views
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(	this,
                R.layout.image_browser,
                cursor,
                from,
                to);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(final View view, final Cursor cursor, int columnIndex) {

                boolean retval = false;

                if(columnIndex == 3){
                    String size = cursor.getString(columnIndex);

                    String sizef = android.text.format.Formatter.formatFileSize(ImageListActivity.this, Long.valueOf(size));

                    TextView fileSize = (TextView)view;
                    fileSize.setText("Size: " + sizef);

                    retval = true;
                }

                if (columnIndex == 5) {

                    String miniThumId = cursor.getString(columnIndex);

                    long idOriginal = cursor.getLong(0);

                    Cursor c = MediaStore.Images.Thumbnails.queryMiniThumbnail(getContentResolver(), idOriginal, MediaStore.Images.Thumbnails.MINI_KIND, fromThumbnails);

                    if(c.moveToFirst()){
                        long id = c.getLong(0);
                        String data = c.getString(1);

                        Bitmap bm = BitmapFactory.decodeFile(data, o);

                        ImageView photo = (ImageView) view;
                        photo.setImageBitmap(bm);
                        retval = true;


                    }else{
                        //En este caso no haya thumbnail de la imagen, tengo que procesarla a mano
                        String size = cursor.getString(3);

                        if(Integer.valueOf(size) > 1048576/2){

                            String path = cursor.getString(4);

                            Bitmap bm = BitmapFactory.decodeFile(path, o);
                            ImageView photo = (ImageView) view;
                            photo.setImageBitmap(bm);
                            retval = true;

                        }else{

                            ImageView photo = (ImageView) view;
                            String id = cursor.getString(0);

                            photo.setImageURI(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id));
                            retval = true;
                        }
                    }
                }

                return retval;
            }
        });

        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE
        };

        Cursor c = getContentResolver().query(	uri,
                projection,
                null,
                null,
                MediaStore.Audio.Media.TITLE );

        startManagingCursor(c);

        c.moveToFirst();

        String filePath = c.getString(1);
        String fileName = c.getString(2);
        String size = c.getString(3);

        if(Integer.valueOf(size) <= 2147484){

            int a = fileName.lastIndexOf(".");
            int b = fileName.length();

            String extension = fileName.substring(a + 1, b).toLowerCase();

            if("jpg".equals(extension) || "png".equals(extension) || "gif".equals(extension) || "tiff".equals(extension)){
                Intent i = new Intent(ImageListActivity.this, ImageListActivity.class);
                i.putExtra("fileName", fileName);
                i.putExtra("filePath", filePath);

                setResult(RESULT_OK, i);

                ImageListActivity.this.finish();
            }else{
                Toast.makeText(ImageListActivity.this, "You only can upload files with extension: jpg, png, gif or tiff", Toast.LENGTH_LONG).show();
            }

        }else{
            Toast.makeText(ImageListActivity.this, "The selected image can not be greater than 2Mb", Toast.LENGTH_LONG).show();
        }
    }
}


/*
 adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {


			private String path;
			private Options o;
			private Bitmap bm;
			private Handler mHandler;
			boolean retval = false;

			@Override
			public boolean setViewValue(final View view, final Cursor cursor, int columnIndex) {

				mHandler = new Handler(){

					@Override
					public void handleMessage(Message msg) {
						//super.handleMessage(msg);

						switch(msg.what){
							case 0:
								ImageView photo = (ImageView) view;
								photo.setImageBitmap(bm);
								retval = true;
								break;
						}
					}
				};

				if (columnIndex == 5) {

					String size = cursor.getString(3);

					if(Integer.valueOf(size) > 1048576/2){

						path = cursor.getString(4);

						o = new Options();
						o.inSampleSize = 8;
						o.inTempStorage = new byte[16384];

						new Runnable(){

							@Override
							public void run() {
								Bitmap bm = BitmapFactory.decodeFile(path, o);
								mHandler.sendEmptyMessage(0);
							}

						}.run();
//						String path = cursor.getString(4);
//
//						Options o = new Options();
//						o.inSampleSize = 8;
//						o.inTempStorage = new byte[16384];
//
//						Bitmap bm = BitmapFactory.decodeFile(path, o);
//						ImageView photo = (ImageView) view;
//						photo.setImageBitmap(bm);
//						retval = true;
					}else{

						ImageView photo = (ImageView) view;
						String id = cursor.getString(0);

						photo.setImageURI(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id));
						retval = true;
					}
				}

				return retval;
			}
		});
 */



/*package com.siahmsoft.soundroid.activities;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.siahmsoft.soundroid.R;


public class ImageListActivity extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String[] projection = {
								MediaStore.Images.Thumbnails._ID,
								MediaStore.Images.Thumbnails.IMAGE_ID,
								MediaStore.Images.Thumbnails.KIND
								};

		Cursor cursor = getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
												projection,
												null,
												null,
												MediaStore.Images.Media._ID);

		startManagingCursor(cursor);

		// Used to map notes entries from the database to views
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(	this,
																R.layout.file_list,
																cursor,
																new String[] { 	MediaStore.Images.Thumbnails._ID,
																				MediaStore.Images.Thumbnails._ID},
																new int[] {	R.id.file_photo_name,
																			R.id.file_photo//,
																			R.id.file_size });

		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				boolean retval = false;

//				if (columnIndex == 5) {
//					String size = cursor.getString(columnIndex);
//
//					int s = Integer.valueOf(size);
//
//					TextView textSize = (TextView) view;
//					textSize.setText(Double.valueOf(s/1048576) + "Mb");
//
//					retval = true;
//				}

				//if (columnIndex == 1 || columnIndex == 0) {
//					MediaStore.Images.Media._ID,
//					MediaStore.Images.Media.DISPLAY_NAME,
//					MediaStore.Images.Media.TITLE,
//					MediaStore.Images.Media.SIZE,
//					MediaStore.Images.Media.MINI_THUMB_MAGIC

					if(columnIndex == 1){
						ImageView v = (ImageView) view;
						String id = cursor.getString(0); //minithumb magic
						v.setImageURI(Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, id));
					}else if(columnIndex == 0){

						String[] projection = {
								MediaStore.Images.ImageColumns._ID,
								MediaStore.Images.ImageColumns.DISPLAY_NAME,
								MediaStore.Images.ImageColumns.TITLE };

						String imgId = cursor.getString(1);

						String selection = MediaStore.Images.ImageColumns._ID + " = " + imgId;

						Cursor cursor2 = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								projection,
								selection,
								null,
								MediaStore.Images.Media.TITLE);


						cursor2.moveToFirst();

						String idImage = cursor2.getString(0);

						TextView v = (TextView) view;
						String text = cursor2.getString(2);
						v.setText(text);
					}


					retval = true;

//					try {
//
//						ImageView photo = (ImageView) view;
//						Bitmap bm;
//						String id = cursor.getString(0);
						//MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//						bm = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, id));
//
//						photo.setImageBitmap(bm);
//
//						retval = true;
//					} catch (FileNotFoundException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}


			//	}

				return retval;
			}
		});

		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

		String[] projection = {
				MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.TITLE };

		Cursor c = getContentResolver().query(	uri,
				projection,
				null,
				null,
				MediaStore.Audio.Media.TITLE );

		startManagingCursor(c);

		c.moveToFirst();

		String filePath = c.getString(1);
		String fileName = c.getString(2);

		Intent i = new Intent(ImageListActivity.this, ImageListActivity.class);
		i.putExtra("fileName", fileName);
		i.putExtra("filePath", filePath);

		setResult(1, i);

		ImageListActivity.this.finish();
	}
}*/