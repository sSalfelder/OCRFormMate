package com.github.ssalfelder.ocrformmate.init;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.javacpp.Loader;

public class OpenCvLoader {
    private static boolean loaded = false;

    public static boolean init() {
        if (!loaded) {
            Loader.load(opencv_core.class);
            loaded = true;
            System.out.println("OpenCV via Bytedeco erfolgreich geladen.");
        }
        return loaded;
    }
}
