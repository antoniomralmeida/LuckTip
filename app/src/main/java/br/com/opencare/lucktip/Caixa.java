package br.com.opencare.lucktip;

import android.os.StrictMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * Created by manoel.ribeiro on 10/03/2017.
 */

public class Caixa {
    protected final List<Integer> history = new ArrayList<Integer>();



    public boolean saveUrl(final String filename, final String urlString) throws MalformedURLException, IOException {

        System.out.print("Download...");

        try {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            // First set the default cookie manager.
            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

            File file = new File(filename);
            if (file.exists()) {
                Date lastModDate = new Date(file.lastModified());
                Date now = new Date();
                long diffHours = (now.getTime() - lastModDate.getTime()) / (60*60*1000);
                if (diffHours < 12)
                    return false;
            }


            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            InputStream in = conn.getInputStream();
            OutputStream out = new FileOutputStream(file, false);
            int i = 0;
            while ((i = in.read()) != -1) {
                out.write(i);
            }
            in.close();
            out.close();
            System.out.println("OK");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public long fat(int n) {
        if (n < 2)
            return 1;
        else
            return n * fat(n - 1);
    }

    public long fat(int n, int k) {
        if (n < 2 || n == k)
            return 1;
        else
            return n * fat(n - 1, k);

    }

    public void save(String fileName) throws FileNotFoundException {

        FileOutputStream fileIn = new FileOutputStream(fileName);
        PrintWriter pw;
        pw = new PrintWriter(new FileOutputStream(fileName));
        for (int i : history)
            pw.println(i);
        pw.close();

    }

    public boolean load(String fileName) throws FileNotFoundException {
        try {
            FileInputStream fileIn = new FileInputStream(fileName);
            Scanner scan = new Scanner(fileIn);
            while (scan.hasNextInt()) {
                int loaded = scan.nextInt();
                history.add(loaded);
            }
            scan.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
