package com.amazonaws.kinesisvideo.demoapp.activity;

import android.content.Context;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

public class FileHelper {

    // Define the file name
    private static final String FILE_NAME = "IngenicTest_NoPatch_WiFi_2.txt";

    // Method to append a line to a file
    public static void appendLineToFile(Context context, String lineToAppend) {
        File file = new File(context.getFilesDir(), FILE_NAME); // Get the file directory

        try {
            // Create the file if it doesn't exist
            if (!file.exists()) {
                file.createNewFile();
            }

            // Append a line to the file
            FileWriter writer = new FileWriter(file, true);
            writer.append(lineToAppend + "\n"); // Append the line and add a newline character
            writer.close();

            System.out.println("Line appended to file successfully!");

            printFileContents(file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to print the contents of a file
    private static void printFileContents(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            System.out.println("Current file contents:");
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}