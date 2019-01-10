package com.samsung.android.sdk.pen.pg.example2_6;

/**
 * Created by josem on 6/8/2017.
 */

import java.util.LinkedList;

public class Cluster {

    public LinkedList<FeatureVectorClass> points;
    public FeatureVectorClass centroid;
    public int id;

    //Creates a new Cluster
    public Cluster(int id) {
        this.id = id;
        this.points = new LinkedList<>();
        this.centroid = null;
    }

    public LinkedList<FeatureVectorClass> getPoints() {
        return points;
    }

    public void addPoint(FeatureVectorClass point) {
        points.add(point);
    }

    public void setPoints(LinkedList<FeatureVectorClass> points) {
        this.points.addAll(points);
    }

    public FeatureVectorClass getCentroid() {
        return centroid;
    }

    public void setCentroid(FeatureVectorClass centroid) {
        this.centroid = centroid;
    }

    public int getId() {
        return id;
    }

    public void clear() {
        //Clears the point of the given Cluster
        points.clear();
    }

    public void plotCluster() {
        System.out.println("[Cluster: " + id+"]");
        System.out.println("[Centroid: " + centroid + "]");
        System.out.println("[Points: \n");
        for(FeatureVectorClass p : points) {
            System.out.println(p);
        }
        System.out.println("]");
    }

}