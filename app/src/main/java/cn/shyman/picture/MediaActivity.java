package cn.shyman.picture;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Random;

import cn.shyman.library.picture.picker.SPPicker;
import cn.shyman.library.picture.viewer.SPViewer;
import cn.shyman.library.picture.widget.MediaLayout;

public class MediaActivity extends AppCompatActivity {
	private MediaLayout mediaLayout;
	
	private ArrayList<Object> mediaTagList = new ArrayList<>();
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_media);
		
		this.mediaLayout = findViewById(R.id.mediaLayout);
		this.mediaLayout.setOnMediaInsertListener(new MediaLayout.OnMediaInsertListener() {
			@Override
			public void onInsert() {
				SPPicker.picker()
						.count(9)
						.build(MediaActivity.this, 1);
			}
		});
		this.mediaLayout.setOnMediaEditListener(new MediaLayout.OnMediaEditListener() {
			@Override
			public void onEdit(int viewIndex, Object mediaTag, Uri mediaUri, Uri mediaPictureUri) {
				mediaTagList.remove(mediaTag);
				mediaLayout.removeMediaUri(mediaTag);
			}
		});
		this.mediaLayout.setOnMediaSelectListener(new MediaLayout.OnMediaSelectListener() {
			@Override
			public void onSelected(int viewIndex, Object mediaTag, Uri mediaUri, Uri mediaPictureUri) {
				ArrayList<Uri> pictureUriList = new ArrayList<>();
				pictureUriList.add(mediaUri);
				SPViewer.viewer()
						.uriList(pictureUriList)
						.build(MediaActivity.this);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				ArrayList<Uri> pictureUriList = SPPicker.pickPictureUriList(data);
				for (Uri pictureUri : pictureUriList) {
					Object mediaTag = mediaLayout.addMediaInfo(pictureUri);
					if (mediaTag == null) {
						continue;
					}
					mediaLayout.setMediaInfo(mediaTag, true);
					mediaLayout.setMediaInfo(mediaTag, new Random().nextInt(100));
					mediaTagList.add(mediaTag);
				}
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
