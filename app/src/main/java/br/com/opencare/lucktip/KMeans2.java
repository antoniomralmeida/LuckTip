package br.com.opencare.lucktip;

/**
 * Created by manoel.ribeiro on 23/03/2017.
 */


import java.util.Random;

public class KMeans2 {

    private double[][] data;
    private int[] bestclustering;
    public int[] clustering;
    private double[][] means;
    public int[] clusterCounts;
    public double SSE = Double.MAX_VALUE;;
    private double min[];
    private double max[];

    public double[][] clustering(double[][] rawData, int numClusters, int nstart) throws Exception {

        // k-means clustering
        // index of return is tuple ID, cell is cluster ID
        // ex: [2 1 0 0 2 2] means tuple 0 is cluster 2, tuple 1 is cluster 1,
        // tuple 2 is cluster 0, tuple 3 is cluster 0, etc.
        // an alternative clustering DS to save space is to use the .NET
        // BitArray class
        data = Normalized(rawData); // so large values don't dominate
        // double[][] means;

        boolean changed = true; // was there a change in at least one cluster
        // assignment?
        boolean success = true; // were all means able to be computed? (no
        // zero-count clusters)
        int seed = 7;

        int ct = 0;
        int maxCount = rawData.length; // sanity check
        double withinss;

        clustering = new int[data.length]; // InitClustering(data.length,
        // numClusters, seed++);
        bestclustering = new int[data.length];
        means = Allocate(numClusters, data[0].length);

        do {
            initMeans(nstart, seed++);
            do {
                ++ct; // k-means typically converges very quickly
                changed = UpdateClustering(); // (re)assign
                // tuples
                // to
                // clusters.
                // no
                // effect
                // if
                // fail
                success = UpdateMeans(); // compute new
                // cluster means
                // if possible.
                // no effect if
                // fail

            } while (changed && success && ct < maxCount);

            withinss = sumSquaresError();

            if (success && withinss < SSE) {
                System.arraycopy(clustering, 0, bestclustering, 0, data.length);
                SSE = withinss;
            }
        } while (nstart-- > 0);
        if (SSE == Double.MAX_VALUE)
            throw new Exception("Bad clustering!");
        clusterCounts = new int[numClusters];
        for (int i = 0; i < data.length; ++i) {
            int cluster = clustering[i];
            ++clusterCounts[cluster];
        }

        return DeNormalized(means);
    }

    private double[][] DeNormalized(double[][] rawData) {
        double[][] result = new double[rawData.length][];
        for (int i = 0; i < rawData.length; ++i) {
            result[i] = new double[rawData[i].length];
        }

        for (int i = 0; i < rawData.length; ++i)
            for (int j = 0; j < result[0].length; ++j) // each col

                result[i][j] = rawData[i][j] * (max[j] - min[j]) + min[j];
        return result;
    }

    private double[][] Normalized(double[][] rawData) {
        // normalize raw data by computing v' = (v - min) / (max - min)

        // make a copy of input data
        double[][] result = new double[rawData.length][];
        for (int i = 0; i < rawData.length; ++i) {
            result[i] = new double[rawData[i].length];
            System.arraycopy(rawData[i], 0, result[i], 0, rawData[i].length);

        }

        min = new double[result[0].length];
        max = new double[result[0].length];

        for (int j = 0; j < result[0].length; ++j) // each col
        {

            min[j] = Double.MAX_VALUE;
            max[j] = Double.MIN_VALUE;
            for (int i = 0; i < result.length; ++i) {
                if (result[i][j] < min[j])
                    min[j] = result[i][j];
                if (result[i][j] > max[j])
                    max[j] = result[i][j];
            }
            double factor = max[j] - min[j];
            for (int i = 0; i < result.length; ++i)
                if (factor != 0)
                    result[i][j] = (result[i][j] - min[j]) / factor;
                else
                    result[i][j] = 0; // make void
        }
        return result;
    }

    private static double[][] Allocate(int numClusters, int numColumns) {
        // convenience matrix allocator for Cluster()
        double[][] result = new double[numClusters][];
        for (int k = 0; k < numClusters; ++k)
            result[k] = new double[numColumns];
        return result;
    }

    private void initMeans(int nstart, int randomSeed) {
        Random random = new Random(randomSeed);
        for (int k = 0; k < means.length; ++k) {
            int i = random.nextInt(data.length);
            for (int j = 0; j < means[k].length; ++j) {
                means[k][j] = data[i][j];
            }
        }
    }

    private boolean UpdateMeans() {
        // returns false if there is a cluster that has no tuples assigned to it
        // parameter means[][] is really a ref parameter

        // check existing cluster counts
        // can omit this check if InitClustering and UpdateClustering
        // both guarantee at least one tuple in each cluster (usually true)
        int numClusters = means.length;
        int[] clusterCounts = new int[numClusters];
        for (int i = 0; i < data.length; ++i) {
            int cluster = clustering[i];
            ++clusterCounts[cluster];
        }

        for (int k = 0; k < numClusters; ++k)
            if (clusterCounts[k] == 0)
                return false;

        // update, zero-out means so it can be used as scratch matrix
        for (int k = 0; k < means.length; ++k)
            for (int j = 0; j < means[k].length; ++j)
                means[k][j] = 0.0;

        for (int i = 0; i < data.length; ++i) {
            int cluster = clustering[i];
            for (int j = 0; j < data[i].length; ++j)
                means[cluster][j] += data[i][j]; // accumulate sum
        }

        for (int k = 0; k < means.length; ++k)
            for (int j = 0; j < means[k].length; ++j)
                means[k][j] /= clusterCounts[k]; // danger of div by 0
        return true;
    }

    private boolean UpdateClustering() {
        // (re)assign each tuple to a cluster (closest mean)
        // returns false if no tuple assignments change OR
        // if the reassignment would result in a clustering where
        // one or more clusters have no tuples.

        int numClusters = means.length;
        boolean changed = false;

        int[] newClustering = new int[clustering.length]; // proposed result
        // Array.Copy(clustering, newClustering, clustering.length);
        System.arraycopy(clustering, 0, newClustering, 0, clustering.length);
        double[] distances = new double[numClusters]; // distances from curr
        // tuple to each mean

        for (int i = 0; i < data.length; ++i) // walk thru each tuple
        {
            for (int k = 0; k < numClusters; ++k)
                distances[k] = Distance(data[i], means[k]); // compute distances
            // from curr tuple
            // to all k means

            int newClusterID = MinIndex(distances); // find closest mean ID
            if (newClusterID != newClustering[i]) {
                changed = true;
                newClustering[i] = newClusterID; // update
            }
        }

        if (changed == false)
            return false; // no change so bail and don't update clustering[][]

        System.arraycopy(newClustering, 0, clustering, 0, newClustering.length);
        // Array.Copy(newClustering, clustering, newClustering.length); //
        // update
        return true; // good clustering and at least one change
    }

    private double Distance(double[] tuple, double[] mean) {
        // Euclidean distance between two vectors for UpdateClustering()
        // consider alternatives such as Manhattan distance
        double sumSquaredDiffs = 0.0;
        for (int j = 0; j < tuple.length; ++j)
            sumSquaredDiffs += Math.pow((tuple[j] - mean[j]), 2);
        return Math.sqrt(sumSquaredDiffs);
    }

    private int MinIndex(double[] distances) {
        // index of smallest value in array
        // helper for UpdateClustering()
        int indexOfMin = 0;
        double smallDist = distances[0];

        for (int k = 0; k < distances.length; ++k) {
            if (distances[k] < smallDist) {

                smallDist = distances[k];
                indexOfMin = k;
            }
        }
        return indexOfMin;
    }

    private double sumSquaresError() {
        double result = 0;
        for (int i = 0; i < data.length; ++i) {
            int c = clustering[i];
            for (int j = 0; j < data[i].length; ++j) {
                result += Math.pow(data[i][j] - means[c][j], 2);
            }
        }
        return result;
    }

}
