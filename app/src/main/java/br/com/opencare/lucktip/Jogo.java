package br.com.opencare.lucktip;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by manoel.ribeiro on 03/03/2017.
 */

public class Jogo {
    public List<Integer> dezena = new ArrayList<Integer>();

    @Override
    public String toString() {
        String r = "";
        DecimalFormat df = new DecimalFormat("00");

        String sep = "";
        for (int i=0;i<100;i++) {
            if (dezena.contains(i)) {
                r = r + sep + df.format(i);
                sep = ",";
            }
        }
        return r + "\n";
    }

    public boolean contains(Jogo j) {

        if (dezena.size() >= j.dezena.size()) {

            int ci = 0;
            for (int i = 0; i < j.dezena.size(); i++) {
                for (int k = 0; k < dezena.size(); k++)
                    if (dezena.get(k) == j.dezena.get(i))
                        ci++;
            }
            if (j.dezena.size() == ci)
                return true;
        }

        return false;
    }
}
