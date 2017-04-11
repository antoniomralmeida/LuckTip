package br.com.opencare.lucktip;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private int position=0;
    private Megasena m = null;
    private Quina q = null;
    private LotoMania l = null;
    private List<Jogo> jogos;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                                          @Override
                                          public void onTabSelected(TabLayout.Tab tab) {
                                              position = tab.getPosition();
                                          }

                                          @Override
                                          public void onTabUnselected(TabLayout.Tab tab) {

                                          }

                                          @Override
                                          public void onTabReselected(TabLayout.Tab tab) {

                                          }
                                      });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                View rootView = mViewPager.getFocusedChild();


                EditText  ed_bettors = (EditText) rootView.findViewById(R.id.edBettors);
                EditText ed_value = (EditText) rootView.findViewById(R.id.edValue);
                TextView result = (TextView) rootView.findViewById(R.id.result);

                System.out.println("Position: " + mViewPager.getCurrentItem());

                final ProgressDialog progress = new ProgressDialog(view.getContext());
                progress.setTitle("Calculando");
                progress.setMessage("Espere enquanto gera as apostas...");
                progress.setCancelable(false); // disable dismiss by tapping outside of the dialog


                int bettors = Integer.parseInt(ed_bettors.getText().toString());
                float value = Float.parseFloat(ed_value.getText().toString());

                progress.show();

                MyTask task = new MyTask(value, bettors, progress, result);
                task.execute();
               }
        });


    }

    private class MyTask extends AsyncTask<Void, Void, Void> {
        private String res;
        private float value;
        private int bettors;
        private TextView result;
        private ProgressDialog progress;

        public MyTask(float value, int bettors, ProgressDialog progress, TextView result) {
            this.value = value;
            this.bettors = bettors;
            this.progress = progress;
            this.result = result;
        }

        @Override
        protected void onPostExecute(Void unused) {
            result.setText(res);
            progress.dismiss();
        }

        protected Void doInBackground(Void... param) {
            //Do some work

            switch (position) {
                case 0:
                    if (m == null)
                        m = new Megasena();
                    if (value < m.getBase() && bettors == 1)
                        res = "O valor abaixo da aposta mínima ";
                    else if (value < 4 && bettors > 1)
                        res = "O valor da aposta para bolão deve ser maior que R$ 4,00";
                    else if (bettors > 1 && (int) (value * bettors / m.getBase()) * m.getBase() / bettors < 4) {
                        res = "O valor da aposta efetiva ficou em "
                                + (int) (value * bettors / m.getBase()) * m.getBase() / bettors;

                    } else {


                        jogos = m.Jogar(value * bettors);
                        res = "APOSTA(S):\n";
                        for (Jogo j : jogos) {
                            res = res + j.toString();
                        }
                        float aposta = (int) (value * bettors / m.getBase()) * m.getBase() / bettors;
                        res = res + "\nO valor da cota ficou em R$ " + aposta + "\n";
                        if (value > aposta)
                            res = res + "O troco foi de R$ " + (value * bettors - aposta * bettors);

                        System.out.println(res);

                    }
                    break;
                case 1:

                    if (q == null)
                        q = new Quina();
                    if (value < 3 && bettors > 1)
                        res = "O valor da aposta para bolão deve ser maior que R$ 3,00 ";

                    else if (bettors > 1 && (int) (value * bettors / q.getBase()) * q.getBase() / bettors < 3) {
                        res = "O valor da aposta efetiva ficou em "
                                + (int) (value * bettors / q.getBase()) * q.getBase() / bettors;

                    } else {
                        jogos = q.Jogar(value * bettors);
                        res = "APOSTA(S):\n";
                        for (Jogo j : jogos) {
                            res = res + j.toString();
                        }
                        float aposta = (int) (value * bettors / q.getBase()) * q.getBase() / bettors;
                        res = res + "\nO valor da cota ficou em R$ " + aposta + "\n";
                        if (value > aposta)
                            res = res + "O troco foi de R$ " + (value * bettors - aposta * bettors);

                        System.out.println(res);
                    }

                    break;

                case 2:


                    if (l == null)
                        l = new LotoMania();

                    jogos = l.Jogar(value * bettors);
                    res = "APOSTA(S):\n";
                    for (Jogo j : jogos) {
                        res = res + j.toString();
                    }
                    float aposta = (int) (value * bettors / l.getBase()) * l.getBase() / bettors;
                    res = res + "\nO valor da cota ficou em R$ " + aposta + "\n";
                    if (value > aposta)
                        res = res + "O troco foi de R$ " + (value * bettors - aposta * bettors);

                    System.out.println(res);
                    break;


            }
        return null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            return PlaceholderFragment.newInstance(position );
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "MEGASENA";
                case 1:
                    return "QUINA";
                case 2:
                    return "LOTOMANIA";
            }
            return null;
        }


    }
}
