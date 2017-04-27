package com.company;

//import com.flickr4java.flickr.Flickr;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photos.SearchParameters;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

/**
 * Created by Clara on 4/27/17.
 * Uses Flickr4Java to collect images on a subject for a GUI.
 */
public class FlickrImages {

    public void get320ImageURL(String subject, FlickrListener listener) {
        new FlickrWorker(subject, listener).execute();
    }


    public interface FlickrListener {
        void imageSaved(String filename);
    }


    private class FlickrWorker extends SwingWorker<String, Void> {

        private String subject;
        private FlickrListener listener;

        FlickrWorker(String subject, FlickrListener listener) {
            this.subject = subject;
            this.listener = listener;
        }

        @Override
        protected String doInBackground() throws Exception {

            String api_key = Keys.FLICKR_API_KEY;             // todo set these
            String sharedSecret = Keys.FLICKR_SHARED_SECRET;   // todo set these
            
            Flickr f = new Flickr(api_key, sharedSecret, new REST());
            PhotosInterface photos = f.getPhotosInterface();

            SearchParameters params = new SearchParameters();
            params.setText(subject);

            PhotoList<Photo> photoList = photos.search(params, 1, 1);

            if (photoList.size() >= 1) {
                Photo photo = photoList.get(0);

                // Get URL of small image
                URL url = new URL(photo.getSmall320Url());

                // Save to file. Create a stream of data from the URL...
                InputStream stream = url.openStream();
                // ... and a path to a file on this computer ....
                String filename = "temp.jpg";   // TODO use unique filenames if you need to store many files
                Path filePath = new File(filename).toPath();

                // ... delete the file, if it exists, ignore otherwise ...
                try {
                    Files.delete(filePath);
                } catch (NoSuchFileException nsfe) {
                    //ignore!
                    System.out.println("There was no temp file to delete. Probably not a problem.");
                }

                // .. and copy the URL stream to the file.
                Files.copy(stream, filePath);

                // ... close stream.
                stream.close();

                return filename;
            }

            return null;   // If no photos found
        }

        @Override
        protected void done(){

            try {
                String filename = get();
                listener.imageSaved(filename);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



    }





}
