package br.com.opencare.lucktip;

/**
 * Created by manoel.ribeiro on 24/02/2017.
 */
import android.Manifest;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Megasena extends Caixa  {
    private final Predict p = new Predict();

    private final float base = 3.5f;
    private final int apostaBase = 6;

    private final int apostas[] = new int[60];
    private final int numDezenas[] = { 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
    private final int numJogos[] = new int[numDezenas.length];
    private final float valorAposta[] = new float[numDezenas.length];



    class ValueComparatorDouble implements Comparator<Double> {
        @Override
        public int compare(Double a, Double b) {
            if (a >= b) {
                return 1;
            } else {
                return -1;
            } // returning 0 would merge keys
        }
    }

    class ValueComparator implements Comparator<Integer> {
        Map<Integer, Integer> base;

        public ValueComparator(Map<Integer, Integer> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with
        // equals.
        @Override
        public int compare(Integer a, Integer b) {
            if (base.get(a) >= base.get(b)) {
                return 1;
            } else {
                return -1;
            } // returning 0 would merge keys
        }
    }


    public Megasena() {

        for (int i = 0; i < numDezenas.length; i++)
            valorAposta[i] = base * fat(numDezenas[i]) / (fat(numDezenas[i] - apostaBase) * fat(apostaBase));

        for (int i = 0; i < apostas.length; i++)
            apostas[i] = i + 1;

        //String property = "java.io.tmpdir";
        String tempDir = "/";
        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/LuckTip");
        boolean isPresent = true;
        if (!docsFolder.exists()) {
            isPresent = docsFolder.mkdir();
        }
        if (isPresent) {
            tempDir = docsFolder.getAbsolutePath() + "/";
        }


        final String ZipFile = tempDir + "mega.zip";
        final String OutputDir = tempDir;
        final String URL = "http://www1.caixa.gov.br/loterias/_arquivos/loterias/D_mgsasc.zip";
        final String HTMLmega = tempDir + "d_megasc.htm";
        final String CACHEmega = tempDir + "megasena.txt";


        try {
            System.out.println("Using " + tempDir + "...");
            UnZip unZip = new UnZip();
            if (saveUrl(ZipFile, URL)) {
                unZip.unZipIt(ZipFile, OutputDir);
                if (LoadTable(HTMLmega))
                    save(CACHEmega);
                else
                    load(CACHEmega);
            } else
                if (!load(CACHEmega)) {
                    File file = new File(ZipFile);
                    file.delete();
                }

            p.setPredict(Learning());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    private TreeMap<Integer, Integer> Hist(int games) {

        int start = games <= 0 ? 0 : Math.max(history.size() - games * apostaBase, 0);
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int i : apostas)
            map.put(i, 0);

        for (int i = start; i < history.size(); i++)
            map.put(history.get(i), map.get(history.get(i)) + i);

        ValueComparator bvc = new ValueComparator(map);
        TreeMap<Integer, Integer> hs = new TreeMap<Integer, Integer>(bvc);
        hs.putAll(map);
        return hs;
    }



    public ArrayList<ArrayList<Integer>> Learning() {
        final int maxiter = 3;

        TreeMap<Integer, Integer> hist = new TreeMap<Integer, Integer>();
        ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();

        System.out.print("Learning...");

        try {


            hist = Hist(history.size());

            double[][] _data = new double[apostas.length][1];
            for (int i = 0; i < hist.size(); i++) {
                _data[i][0] = (int) hist.values().toArray()[i];
            }

            KMeans2 KM = new KMeans2();
            int sturges = (int) (Math.log(history.size()) / Math.log(2) + 1);

            int clusters = 2;
            double SSE = Double.MAX_VALUE;
            for (int c = 2; c <= sturges; c++) {

                KM.clustering(_data, c, maxiter);

                if (KM.SSE < SSE) {
                    SSE = KM.SSE;
                    clusters = c;
                } else
                    break;
            }

            double[][] _centroids = KM.clustering(_data, clusters, maxiter);

            for (int n = 0; n < clusters; n++) {
                if (KM.clusterCounts[n] == 0)
                    continue;
                Map<Integer, Integer> map = new HashMap<Integer, Integer>();
                for (int i : apostas) {
                    if (KM.clustering[i - 1] == n)
                        map.put(i, (int) (1000 * Math.abs((int) hist.values().toArray()[i - 1] - _centroids[n][0])));
                }

                ValueComparator bvc = new ValueComparator(map);
                TreeMap<Integer, Integer> hs = new TreeMap<Integer, Integer>(bvc);
                hs.putAll(map);

                ArrayList<Integer> linha = new ArrayList<Integer>();
                for (Map.Entry<Integer, Integer> e : hs.entrySet())
                    linha.add(e.getKey());
                result.add(linha);
                System.out.println(linha);
            }
            System.out.println("OK[" + result.size() +"]");

        } catch (Exception e) {
            // TODO: handle exception
        }
        return result;

    }


    public List<Jogo> Jogar(float valor) {

        for (int l = numJogos.length - 1; l >= 0; l--)
            numJogos[l] = 0;

        int x = numJogos.length - 1;
        float v = valor;
        while (x >= 0) {
            if (Math.round(v / valorAposta[x]) >= 1) {
                numJogos[x] = (int) (v / valorAposta[x]);
                v = v - numJogos[x] * valorAposta[x];
            }
            x--;
        }


        List<Jogo> games = new ArrayList<Jogo>();

        for (int l = numJogos.length - 1; l >= 0; l--) {
            int i_ini = games.size();

            if (numJogos[l] == 0)
                continue;
            int n = numDezenas[l];

            for (int k = numDezenas[l] + 2; k <= apostas.length; k++) {
                long combinacao;
                if ((k - numDezenas[l]) > numDezenas[l])
                    combinacao = fat(k, k - numDezenas[l]) / fat(numDezenas[l]);
                else
                    combinacao = fat(k, numDezenas[l]) / fat(k - numDezenas[l]);

                if (combinacao >= numJogos[l] && (n <= k)) {
                    n = k;
                    break;
                }
            }

            List<Integer> numbs = new ArrayList<Integer>();
            p.reset();

            int j = 0;
            while (j < n) {
                int z = p.next();
                if (!numbs.contains(z)) {
                    numbs.add(z);
                    j++;
                }
            }
            System.out.println(numbs);
            x = 0;
            while (x < numJogos[l]) {
                Collections.shuffle(numbs);
                Jogo g = new Jogo();
                boolean ERROR = false;

                for (int k = 0; k < numDezenas[l]; k++) {
                    g.dezena.add(numbs.get(k));
                }

                for (int i = 0; i < games.size(); i++)
                    if (games.get(i).contains(g)) {
                        System.out.println("dup");
                        ERROR = true;
                        break;
                    }

                if (!ERROR) {
                    games.add(g);
                    x++;
                }
            }
        }

        return games;
    }

    private boolean LoadTable(String html) {

        try {

            String content = "";

            System.out.print("Parsing...");
            Scanner scanner = new Scanner( new File(html), "UTF-8" );
            String text = scanner.useDelimiter("\\A").next();
            scanner.close(); // Put this call in a finally block
            text = text.replaceAll("(\\r|\\n)", "");

            Document doc = Jsoup.parse(text);
            Elements tableElements = doc.select("table");
            Elements tableRowElements = tableElements.select(":not(thead) tr");

            for (int i = 0; i < tableRowElements.size(); i++) {
                Element row = tableRowElements.get(i);
                // System.out.print("row:");
                Elements rowItems = row.select("td");
                for (int j = 0; j < rowItems.size(); j++) {

                    // System.out.print("j="+j+":"+rowItems.get(j).text()+";");
                    if (j >= 2 && j <= 7)
                        history.add(Integer.parseInt(rowItems.get(j).text()));

                }

            }
            System.out.println("OK");
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * @return the base
     */
    public float getBase() {
        return base;
    }

}
