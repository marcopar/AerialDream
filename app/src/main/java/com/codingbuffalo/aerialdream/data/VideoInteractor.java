package com.codingbuffalo.aerialdream.data;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import android.util.Log;
import com.codingbuffalo.aerialdream.data.protium.Interactor;
import com.codingbuffalo.aerialdream.data.protium.ValueTask;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

public class VideoInteractor extends Interactor {
    private Listener listener;
    private List<VideoRepository> repositories = new LinkedList<>();
    private Context context;
    private String source_apple_2019;
    private int videoSource;

    public VideoInteractor(Context context, int videoSource, String source_apple_2019, @NonNull Listener listener) {
        super(Executors.newCachedThreadPool());

        this.context = context.getApplicationContext();
        this.listener = listener;

        this.videoSource = videoSource;
        this.source_apple_2019 = source_apple_2019;
        repositories.add(new Apple2019Repository());
    }

    public void fetchVideos() {
        execute(new FetchVideosTask());
    }

    private class FetchVideosTask extends ValueTask<List<? extends Video>> {
        @Override
        public List<? extends Video> onExecute() throws Exception {
            List<Video> remoteVideos = new ArrayList<>();
            List<String> localVideos = new ArrayList<>();
            List<Video> videos = new ArrayList<>();

            for (VideoRepository repository : repositories) {
                remoteVideos.addAll(repository.fetchVideos(context));
            }

            if (videoSource != VideoSource.REMOTE) {
                localVideos = getAllMedia();
            }

            for (Video video : remoteVideos) {
                Uri remoteUri = video.getUri(source_apple_2019);
                String remoteFilename = remoteUri.getLastPathSegment().toLowerCase();

                if (videoSource == VideoSource.REMOTE) {
                    Log.i("FetchVideosTask","Remote video: " + remoteFilename);
                    videos.add(new SimpleVideo(remoteUri, video.getLocation()));
                    continue;
                }

                if (videoSource != VideoSource.REMOTE) {
                    Uri localUri = findLocalVideo(localVideos, remoteFilename);
                    if(localUri != null) {
                        Log.i("FetchVideosTask","Local video: " + localUri.getLastPathSegment());
                        videos.add(new SimpleVideo(localUri, video.getLocation()));
                    } else if (videoSource == VideoSource.LOCAL_AND_REMOTE) {
                        Log.i("FetchVideosTask","Remote video: " + remoteFilename);
                        videos.add(new SimpleVideo(remoteUri, video.getLocation()));
                    }
                }
            }

            Log.i("FetchVideosTask","Videos found: " + videos.size());
            return videos;
        }

        @Override
        public void onComplete(List<? extends Video> data) {
            listener.onFetch(new VideoPlaylist(data));
        }
    }

    private Uri findLocalVideo(List<String> localVideos, String remoteFilename) {
        for (String localUrl : localVideos ) {
            Uri localUri = Uri.parse(localUrl);
            String localFilename = localUri.getLastPathSegment().toLowerCase();
            if (localFilename.contains(remoteFilename)) {
                return localUri;
            }
        }
        return null;
    }

    private ArrayList<String> getAllMedia() {
        HashSet<String> videoItemHashSet = new HashSet<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String column = "_data";
        String[] projection = {column};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        try {
            cursor.moveToFirst();
            do {
                videoItemHashSet.add((cursor.getString(cursor.getColumnIndexOrThrow(column))));
            } while (cursor.moveToNext());
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<String> downloadedList = new ArrayList<>(videoItemHashSet);
        return downloadedList;
    }

    public interface Listener {
        void onFetch(VideoPlaylist videos);
    }
}
