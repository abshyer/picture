package cn.shyman.picture;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

import cn.shyman.library.picture.picker.PictureInfo;
import cn.shyman.library.picture.picker.SPPicker;
import cn.shyman.library.picture.viewer.SPViewer;

public class SplashActivity extends AppCompatActivity {
	private TextView tvContent;
	private SimpleDraweeView simpleDraweeView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		this.tvContent = (TextView) findViewById(R.id.tvContent);
		simpleDraweeView = (SimpleDraweeView) findViewById(R.id.simpleDraweeView);
		simpleDraweeView.setImageURI(Uri.parse("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1505539245598&di=7270119e470db490d51dd4a53d4844ca&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimgad%2Fpic%2Fitem%2F9825bc315c6034a81c94dd93c1134954092376a9.jpg"));
		simpleDraweeView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				turnDetails();
			}
		});
		// turn();
		
		findViewById(R.id.btnCount).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SPPicker.picker()
						.count(9)
						.compress(80)
						.build(SplashActivity.this, 1);
			}
		});
		findViewById(R.id.btnEditable).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SPPicker.picker()
						.count(9)
						.editable(true)
						.build(SplashActivity.this, 1);
			}
		});
		findViewById(R.id.btnAspect).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SPPicker.picker()
						.count(9)
						.editable(true)
						.aspectRatio(1, 1)
						.build(SplashActivity.this, 1);
			}
		});
	}
	
	private void turnDetails() {
		simpleDraweeView.setLegacyVisibilityHandlingEnabled(true);
		ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
				this, simpleDraweeView, SPViewer.TRANSITION_NAME
		);
		
		ArrayList<String> pictureUrlList = new ArrayList<>();
		pictureUrlList.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1505539245598&di=7270119e470db490d51dd4a53d4844ca&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimgad%2Fpic%2Fitem%2F9825bc315c6034a81c94dd93c1134954092376a9.jpg");
		pictureUrlList.add("https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=197144486,1264112589&fm=11&gp=0.jpg");
		pictureUrlList.add("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=1798203804,1445173353&fm=11&gp=0.jpg");
		pictureUrlList.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1505539246124&di=93b58feb2505a6e3335a502dde677336&imgtype=0&src=http%3A%2F%2Fpic38.nipic.com%2F20140222%2F2656254_095504906000_2.jpg");
		pictureUrlList.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1505539246123&di=c3f6dd9da72a19bbb2d6b6c2cb3fe6ec&imgtype=0&src=http%3A%2F%2Fimg2.niutuku.com%2Fdesk%2F1207%2F1100%2Fbizhi-1100-7201.jpg");
		pictureUrlList.add("https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=517748425,2883022337&fm=11&gp=0.jpg");
		SPViewer.viewer()
				.urlList(pictureUrlList)
				.editable(true)
				.build(this, options.toBundle());
	}
	
	private void turn() {
		ActivityOptionsCompat options =
				ActivityOptionsCompat.makeScaleUpAnimation(simpleDraweeView,
						simpleDraweeView.getWidth() / 2, simpleDraweeView.getHeight() / 2,
						0, 0);
		
		ArrayList<String> pictureUrlList = new ArrayList<>();
		pictureUrlList.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1505539245598&di=7270119e470db490d51dd4a53d4844ca&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimgad%2Fpic%2Fitem%2F9825bc315c6034a81c94dd93c1134954092376a9.jpg");
		pictureUrlList.add("https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=197144486,1264112589&fm=11&gp=0.jpg");
		pictureUrlList.add("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=1798203804,1445173353&fm=11&gp=0.jpg");
		pictureUrlList.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1505539246124&di=93b58feb2505a6e3335a502dde677336&imgtype=0&src=http%3A%2F%2Fpic38.nipic.com%2F20140222%2F2656254_095504906000_2.jpg");
		pictureUrlList.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1505539246123&di=c3f6dd9da72a19bbb2d6b6c2cb3fe6ec&imgtype=0&src=http%3A%2F%2Fimg2.niutuku.com%2Fdesk%2F1207%2F1100%2Fbizhi-1100-7201.jpg");
		pictureUrlList.add("https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=517748425,2883022337&fm=11&gp=0.jpg");
		SPViewer.viewer()
				.urlList(pictureUrlList)
				.editable(true)
				.build(this, options.toBundle());
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				ArrayList<PictureInfo> pictureInfoList = SPPicker.pickPictureInfoList(data);
				StringBuilder builder = new StringBuilder();
				for (PictureInfo pictureInfo : pictureInfoList) {
					builder.append(pictureInfo.getPictureUri())
							.append("\n")
							.append(pictureInfo.getPictureWidth())
							.append(" ")
							.append(pictureInfo.getPictureHeight())
							.append("\n");
				}
				this.tvContent.setText(builder);
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
