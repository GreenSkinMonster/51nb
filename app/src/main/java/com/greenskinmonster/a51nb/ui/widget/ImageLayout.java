package com.greenskinmonster.a51nb.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.Target;
import com.greenskinmonster.a51nb.R;
import com.greenskinmonster.a51nb.bean.ContentImg;
import com.greenskinmonster.a51nb.bean.HiSettingsHelper;
import com.greenskinmonster.a51nb.cache.ImageContainer;
import com.greenskinmonster.a51nb.cache.ImageInfo;
import com.greenskinmonster.a51nb.glide.GlideHelper;
import com.greenskinmonster.a51nb.glide.GlideImageEvent;
import com.greenskinmonster.a51nb.job.GlideImageJob;
import com.greenskinmonster.a51nb.job.JobMgr;
import com.greenskinmonster.a51nb.ui.ImageViewerActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * Created by GreenSkinMonster on 2017-08-11.
 */

public class ImageLayout extends RelativeLayout {

    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private String mUrl;
    private RequestManager mGlide;

    private int mIndex = -1;
    private ArrayList<ContentImg> mContentImgs;

    public ImageLayout(Context context) {
        super(context);
        init();
    }

    public ImageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_image, this, true);
        mImageView = (ImageView) findViewById(R.id.image_view);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mImageView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
                if (imageInfo.getStatus() == ImageInfo.IDLE || imageInfo.getStatus() == ImageInfo.FAIL) {
                    JobMgr.addJob(new GlideImageJob(mUrl, JobMgr.PRIORITY_LOW, "", true));
                } else if (imageInfo.getStatus() == ImageInfo.SUCCESS) {
                    startImageGallery();
                }
            }
        });
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public void setGlide(RequestManager glide) {
        mGlide = glide;
    }

    public void setImageInfo(int imageIndex, ArrayList<ContentImg> imgs) {
        mIndex = imageIndex;
        mContentImgs = imgs;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        EventBus.getDefault().register(this);

        ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        if (imageInfo.getStatus() == ImageInfo.SUCCESS) {
            loadImage();
            mProgressBar.setVisibility(View.GONE);
        } else if (imageInfo.getStatus() == ImageInfo.FAIL) {
            mImageView.setImageResource(R.drawable.image_broken);
            mProgressBar.setVisibility(View.GONE);
        } else if (imageInfo.getStatus() == ImageInfo.IN_PROGRESS) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(imageInfo.getProgress());
        } else {
            mImageView.setImageResource(R.drawable.ic_action_image);
            boolean autoload = HiSettingsHelper.getInstance().isImageLoadableByNetwork();
            JobMgr.addJob(new GlideImageJob(
                    mUrl,
                    JobMgr.PRIORITY_LOW,
                    "",
                    autoload));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        EventBus.getDefault().unregister(this);
        Glide.clear(mImageView);
        super.onDetachedFromWindow();
    }

    private void loadImage() {
        if (mGlide != null) {
            mGlide.load(mUrl)
                    .crossFade()
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .error(R.drawable.image_broken)
                    .into(mImageView);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GlideImageEvent event) {
        if (!event.getImageUrl().equals(mUrl))
            return;
        final ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        imageInfo.setMessage(event.getMessage());

        if (event.getStatus() == ImageInfo.IN_PROGRESS
                && imageInfo.getStatus() != ImageInfo.SUCCESS) {
            if (mProgressBar.getVisibility() != View.VISIBLE)
                mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(event.getProgress());

            imageInfo.setProgress(event.getProgress());
            imageInfo.setStatus(ImageInfo.IN_PROGRESS);
        } else if (event.getStatus() == ImageInfo.SUCCESS) {
            if (mProgressBar.getVisibility() == View.VISIBLE)
                mProgressBar.setVisibility(View.GONE);
            if (GlideHelper.isOkToLoad(getContext()))
                loadImage();
        } else if (event.getStatus() == ImageInfo.FAIL) {
            mProgressBar.setVisibility(GONE);
            mImageView.setImageResource(R.drawable.image_broken);
        } else if (event.getStatus() == ImageInfo.IDLE) {
            mProgressBar.setVisibility(GONE);
        }
    }

    public void startImageGallery() {
        if (mContentImgs != null && mContentImgs.size() > 0) {
            Intent intent = new Intent(getContext(), ImageViewerActivity.class);
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeScaleUpAnimation(mImageView, 0, 0, mImageView.getMeasuredWidth(), mImageView.getMeasuredHeight());
            intent.putExtra(ImageViewerActivity.KEY_IMAGE_INDEX, mIndex);
            intent.putParcelableArrayListExtra(ImageViewerActivity.KEY_IMAGES, mContentImgs);
            ActivityCompat.startActivity(getContext(), intent, options.toBundle());
        }
    }

}
