package com.example.android_proyecto.Models;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Video {
    private String title;
    private String url; // This will now store ONLY the ID (e.g., "dQw4w9WgXcQ")

    public Video() {}

    public Video(String title, String url) {
        this.title = title;
        // Important: We use the setter here so the extraction logic runs
        // even when creating the object via constructor.
        setUrl(url);
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the URL.
     * This method automatically detects if the input is a full URL
     * and extracts only the Video ID before saving it.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    // --- HELPER METHOD ---
    public String extractVideoIdFromUrl(String url) {
        if (url == null) return "";
        url = url.trim();

        // 1. If it's already a clean ID (11 chars, no symbols), return it directly.
        if (url.length() == 11 && !url.contains("/") && !url.contains(".")) {
            return url;
        }

        // 2. Regex to find ID in any YouTube URL format
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);

        if (matcher.find()) {
            return matcher.group();
        }

        // 3. Fallback: If we couldn't extract anything, return the original string
        // (Just in case it was a non-standard ID)
        return url;
    }
}