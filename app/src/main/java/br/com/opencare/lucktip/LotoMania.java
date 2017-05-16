package br.com.opencare.lucktip;


import android.os.Environment;

import java.io.BufferedReader;
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


/**
 * Created by manoel.ribeiro on 21/03/2017.
 */

public class LotoMania extends Caixa {
    private final Predict p = new Predict();

    private final float base = 1.5f;

    private final int numDezenas[] = { 50 };
    private final int numJogos[] = new int[numDezenas.length];
    private final float valorAposta[] = { 1.5f };


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


    public LotoMania() {

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


        final String ZipFile = tempDir + "lotomania.zip";
        final String OutputDir = tempDir;
        final String Mega = "http://www1.caixa.gov.br/loterias/_arquivos/loterias/D_lotoma.zip";
        final String HTMLmega = tempDir + "D_LOTMAN.htm";
        final String CACHEmega = tempDir + "lotomania.txt";

        try {
            System.out.println("Using " + tempDir + "...");
            UnZip unZip = new UnZip();
            if (saveUrl(ZipFile, Mega)) {
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

        int start = games <= 0 ? 0 : Math.max(history.size() - games * 20, 0);
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int i = 0; i < 100; i++)
            map.put(i, 0);

        for (int i = start; i < history.size(); i++)
            map.put(history.get(i), map.get(history.get(i)) + 1);

        ValueComparator bvc = new ValueComparator(map);
        TreeMap<Integer, Integer> hs = new TreeMap<Integer, Integer>(bvc);
        hs.putAll(map);
        return hs;
    }

    public ArrayList<ArrayList<Integer>> Learning() {
        final int maxiter = 3;
        ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
        try {
            TreeMap<Integer, Integer> hist = new TreeMap<Integer, Integer>();

            System.out.print("Learning...");

            hist = Hist(history.size()*90/100);

            System.out.println(hist);

            double[][] _data = new double[100][1];
            for (int i = 0; i < hist.size(); i++) {
                _data[i][0] = (int) hist.values().toArray()[i];
            }
            // System.out.println(hist);
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
                for (int i = 0; i < 100; i++) {
                    if (KM.clustering[i ] == n)
                        map.put(i, (int) (1000 * Math.abs((int) hist.values().toArray()[i] - _centroids[n][0])));
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
        }
        catch (Exception e) {

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

            for (int k = numDezenas[l] + 2; k < 100; k++) {

                long combinacao = fat(k, numDezenas[l]) / fat(k - numDezenas[l]);
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
                        ERROR = true;
                        break;
                    }

                if (!ERROR) {
                    games.add(g);
                    x++;
                }
            }

         }
        System.out.println("Done.");
        return games;
    }

    private boolean LoadTable(String html) {

        try {

            String content = "";

            System.out.print("Parsing...");
            Scanner scanner = new Scanner( new File(html), "UTF-8" );
            content = scanner.useDelimiter("\\A").next();
            scanner.close(); // Put this call in a finally block
            content = content.replaceAll("(\\r|\\n)", "");


            Document doc = Jsoup.parse(content);
            Elements tableElements = doc.select("table");
            // Elements tableHeaderEles = tableElements.select("thead tr th");
            // System.out.print("header:");
            // for (int i = 0; i < tableHeaderEles.size(); i++) {
            // System.out.print(tableHeaderEles.get(i).text()+";");
            // }
            // System.out.println();

            Elements tableRowElements = tableElements.select(":not(thead) tr");

            for (int i = 0; i < tableRowElements.size(); i++) {
                Element row = tableRowElements.get(i);
                // System.out.print("row:");
                Elements rowItems = row.select("td");
                for (int j = 0; j < rowItems.size(); j++) {
                    if (j >= 2 && j <= 21) {
                        history.add(Integer.parseInt(rowItems.get(j).text()));
                    }
                }
                // System.out.println();

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
