package lordsemi.kmeans;

import java.util.LinkedList;
import java.util.Random;

/**
 * Created by josem on 6/1/2017.
 */

public class FeatureVectorClass {

    private float word; //Numero de palabra
    private double X_coordinate; //Coordenadas
    private double Y_coordinate;
    private double writing_direction_X; //Direccion de escritura
    private double writing_direction_Y;
    private double curvature_X; //Curvatura
    private double curvature_Y;
    private double vicinity_aspect; //Aspect
    private double vicinity_slope_X; //Slope
    private double vicinity_slope_Y;
    private double vicinity_curliness; //Curliness
    private double vicinity_linearity; //Linearity
    private int cluster;

    public FeatureVectorClass(double X, double Y, double start_writing_direction_X, double start_writing_direction_Y, double start_curvature_X,
                              double start_curvature_Y, double start_vicinity_aspect, double start_vicinity_slope_X, double start_vicinity_slope_Y,
                              double start_vicinity_curliness, double start_vicinity_linearity) {
//        word = word_number;
        X_coordinate = X;
        Y_coordinate = Y;
        writing_direction_X = start_writing_direction_X;
        writing_direction_Y = start_writing_direction_Y;
        curvature_X = start_curvature_X;
        curvature_Y = start_curvature_Y;
        vicinity_aspect = start_vicinity_aspect;
        vicinity_slope_X = start_vicinity_slope_X;
        vicinity_slope_Y = start_vicinity_slope_Y;
        vicinity_curliness = start_vicinity_curliness;
        vicinity_linearity = start_vicinity_linearity;
//        cluster = cluster_number;
    }

    public FeatureVectorClass() {
    }

    public int getCluster() {
        return cluster;
    }

    public void setCluster(int cluster) {
        this.cluster = cluster;
    }

    public float getWord() {
        return word;
    }

    public void setWord(float word) {
        this.word = word;
    }

    public double getWriting_direction_X() {
        return writing_direction_X;
    }

    public void setWriting_direction_X(double writing_direction_X) {
        this.writing_direction_X = writing_direction_X;
    }

    public double getWriting_direction_Y() {
        return writing_direction_Y;
    }

    public void setWriting_direction_Y(double writing_direction_Y) {
        this.writing_direction_Y = writing_direction_Y;
    }

    public double getCurvature_X() {
        return curvature_X;
    }

    public void setCurvature_X(double curvature_X) {
        this.curvature_X = curvature_X;
    }

    public double getCurvature_Y() {
        return curvature_Y;
    }

    public void setCurvature_Y(double curvature_Y) {
        this.curvature_Y = curvature_Y;
    }

    public double getX_coordinate() {
        return X_coordinate;
    }

    public void setX_coordinate(double x_coordinate) {
        X_coordinate = x_coordinate;
    }

    public double getY_coordinate() {
        return Y_coordinate;
    }

    public void setY_coordinate(double y_coordinate) {
        Y_coordinate = y_coordinate;
    }

    public double getvicinity_aspect() {
        return vicinity_aspect;
    }

    public void setvicinity_aspect(double vicinity_aspect) {
        this.vicinity_aspect = vicinity_aspect;
    }

    public double getVicinity_slope_X() {
        return vicinity_slope_X;
    }

    public void setVicinity_slope_X(double vicinity_slope_X) {
        this.vicinity_slope_X = vicinity_slope_X;
    }

    public double getVicinity_slope_Y() {
        return vicinity_slope_Y;
    }

    public void setVicinity_slope_Y(double vicinity_slope_Y) {
        this.vicinity_slope_Y = vicinity_slope_Y;
    }

    public double getvicinity_curliness() {
        return vicinity_curliness;
    }

    public void setvicinity_curliness(double vicinity_curliness) {
        this.vicinity_curliness = vicinity_curliness;
    }

    public double getvicinity_linearity() {
        return vicinity_linearity;
    }

    public void setvicinity_linearity(double vicinity_linearity) {
        this.vicinity_linearity = vicinity_linearity;
    }

    //Calculates the distance between two points.
    protected static double distance(FeatureVectorClass p, FeatureVectorClass centroid) {
        double dX = Math.pow(p.getX_coordinate() - centroid.getX_coordinate(), 2);
        double dY = Math.pow(p.getY_coordinate() - centroid.getY_coordinate(), 2);
        double dWritingX = Math.pow(p.getWriting_direction_X() - centroid.getWriting_direction_X(), 2);
        double dWritingY = Math.pow(p.getWriting_direction_Y() - centroid.getWriting_direction_Y(), 2);
        double dCurvatureX = Math.pow(p.getCurvature_X() - centroid.getCurvature_X(), 2);
        double dCurvatureY = Math.pow(p.getCurvature_Y() - centroid.getCurvature_Y(), 2);
        double dAspect = Math.pow(p.getvicinity_aspect() - centroid.getvicinity_aspect(), 2);
        double dSlopeX = Math.pow(p.getVicinity_slope_X() - centroid.getVicinity_slope_X(), 2);
        double dSlopeY = Math.pow(p.getVicinity_slope_Y() - centroid.getVicinity_slope_Y(), 2);
        double dCurliness = Math.pow(p.getvicinity_curliness() - centroid.getvicinity_curliness(), 2);
        double dLinearity = Math.pow(p.getvicinity_linearity() - centroid.getvicinity_linearity(), 2);
        double result = dX + dY + dWritingX + dWritingY + dCurvatureX + dCurvatureY + dAspect + dSlopeX + dSlopeY + dCurliness + dLinearity;

        double Distance = Math.sqrt(result);
        return Distance;

    }

    //
    protected static LinkedList<FeatureVectorClass> createRandomCentroids(LinkedList<FeatureVectorClass> Features, int k_number) {
        Random r = new Random();
        LinkedList<Integer> Done = new LinkedList<>();
        //List that will be returned with the random centroids initialized
        LinkedList<FeatureVectorClass> RandomK = new LinkedList<>();
        for (int i = 0; i < k_number; i++) {
            int aux = r.nextInt(Features.size());
            if (Done.size() > 0) {
                for (int j = 0; j < Done.size(); j++) {
                    if (Done.get(j) == aux) {
                        aux = r.nextInt(Features.size());
                        j = 0;
                    }
                }
                Done.add(aux);
                FeatureVectorClass Centroid = Features.get(aux);
                RandomK.add(Centroid);
            } else {
                Done.add(aux);
                FeatureVectorClass Centroid = Features.get(aux);
                RandomK.add(Centroid);
            }
        }
        return RandomK;
    }

}
