package lordsemi.kmeans;

/**
 * Created by josem on 6/8/2017.
 */

import java.io.File;
import java.util.LinkedList;


public class KMeans {
    //Creating the file we will return
    String string = "Handwritten search data";
    File file = new File(string);
    //Number of Clusters. This metric should be related to the number of points
    private int NUM_CLUSTERS = 150;
    //list of centroids
    LinkedList<FeatureVectorClass> ListOfCentroids = new LinkedList<>();
    LinkedList<FeatureVectorClass> lastCentroids = new LinkedList<>();
    LinkedList<FeatureVectorClass> currentCentroids = new LinkedList<>();
    //Min and Max X and Y
    private static final int MIN_COORDINATE = 0;
    private static final int MAX_COORDINATE = 10;

    private LinkedList<FeatureVectorClass> points;
    private LinkedList<Cluster> clusters;

    public KMeans() {
        this.points = new LinkedList<>();
        this.clusters = new LinkedList<>();
    }

    public static void main(LinkedList<FeatureVectorClass> Sample) {

        KMeans kmeans = new KMeans();
        kmeans.init(Sample);
        kmeans.calculate();
    }

    public void GetFile(){
        SpenNoteDoc tmpSpenNoteDoc = new SpenNoteDoc(mContext, strFilePath, mScreenRect.width(),
                SpenNoteDoc.MODE_WRITABLE, true);
    }
    //Initializes the process
    public void init(LinkedList<FeatureVectorClass> Sample) {
        //Clean list
        ListOfCentroids.clear();
        //Create Points
        points.addAll(Sample);

        //Create Clusters and Centroids
        LinkedList<Cluster> ListOfClusters = new LinkedList<>();
        ListOfCentroids = FeatureVectorClass.createRandomCentroids(Sample, NUM_CLUSTERS);
        //Set Random Centroids
        for (int i = 0; i < NUM_CLUSTERS; i++) {
            //First we create a cluster
            Cluster C = new Cluster(i);
            //then we set to the centroid we are going to assign, its cluster
            ListOfCentroids.get(i).setCluster(i);
            //then we assignt the centroid to the cluster
            C.setCentroid(ListOfCentroids.get(i));
            //finally we add the cluster to the cliusters list
            ListOfClusters.add(C);
        }
        clusters.addAll(ListOfClusters);


        //Print Initial state
//        plotClusters();
    }

    private void plotClusters() {
        for (int i = 0; i < NUM_CLUSTERS; i++) {
            Cluster c = clusters.get(i);
            c.plotCluster();
        }
    }

    //The process to calculate the K Means, with iterating method.
    public void calculate() {
        boolean finish = false;
        int iteration = 0;

        // Add in new data, one at a time, recalculating centroids with each new one.
        while (!finish) {
            //Clear cluster state
            clearClusters();
            lastCentroids.clear();
            currentCentroids.clear();

            lastCentroids = getCentroids();

            //Assign points to the closer cluster
            assignCluster();

            //Calculate new centroids.
            calculateCentroids();

            iteration++;

//            currentCentroids = getCentroids();

            //Calculates total distance between new and old Centroids
            double distance = 0;
            for (int i = 0; i < lastCentroids.size(); i++) {
                distance += FeatureVectorClass.distance(lastCentroids.get(i), currentCentroids.get(i));
            }
            for (FeatureVectorClass centroid : currentCentroids) {
                clusters.get(centroid.getCluster()).setCentroid(centroid);
            }

//            System.out.println("#################");
            System.out.println("Iteration: " + iteration);
            System.out.println(" Distance: " + distance);

//            System.out.println("Centroid distances: " + distance);
//            plotClusters();

            if (distance == 0) {
                finish = true;
            }
        }
    }

    private void clearClusters() {
        for (Cluster cluster : clusters) {
            cluster.clear();
        }
    }

    private LinkedList<FeatureVectorClass> getCentroids() {
        LinkedList<FeatureVectorClass> centroids = new LinkedList<>();
        for (Cluster cluster : clusters) {
            FeatureVectorClass aux = cluster.getCentroid();
            centroids.add(aux);
        }
        return centroids;
    }

    private void assignCluster() {
        double max = Double.MAX_VALUE;
        double min;
        int cluster = 0;
        double distance;
//        double j = 0;
//        double progress = 0;

        for (FeatureVectorClass point : points) {
            min = max;
            for (int i = 0; i < NUM_CLUSTERS; i++) {
                Cluster c = clusters.get(i);
                distance = FeatureVectorClass.distance(point, c.getCentroid());
                if (distance < min) {
                    min = distance;
                    cluster = i;
                }
            }
            point.setCluster(cluster);
            clusters.get(cluster).addPoint(point);
//            j++;
//            if(j==10000){
//                progress = (j/progress) * 100;
//                System.out.println("Progress: " + progress);
//            }

        }
    }

    private void calculateCentroids() {
        for (Cluster cluster : clusters) {
            double sumX = 0;
            double sumY = 0;
            double sumWDirectionX = 0;
            double sumWDirectionY = 0;
            double sumCurvX = 0;
            double sumCurvY = 0;
            double sumAspect = 0;
            double sumSlopeX = 0;
            double sumSlopeY = 0;
            double sumCurliness = 0;
            double sumLinearity = 0;
            int i = 0;
            LinkedList<FeatureVectorClass> list = cluster.getPoints();
            int n_points = list.size();
            //First we calculate the summatory of each feature
            for (FeatureVectorClass point : list) {
                sumX += point.getX_coordinate();
                sumY += point.getY_coordinate();
                sumWDirectionX += point.getWriting_direction_X();
                sumWDirectionY += point.getWriting_direction_Y();
                sumCurvX += point.getCurvature_X();
                sumCurvY += point.getCurvature_Y();
                sumAspect += point.getvicinity_aspect();
                sumSlopeX += point.getVicinity_slope_X();
                sumSlopeY += point.getVicinity_slope_Y();
                sumCurliness += point.getvicinity_curliness();
                sumLinearity += point.getvicinity_linearity();
            }

//            FeatureVectorClass centroid = cluster.getCentroid();
            FeatureVectorClass centroid = new FeatureVectorClass();
            if (n_points > 0) {
                double newX = sumX / n_points;
                double newY = sumY / n_points;
                double newWDirectionX = sumWDirectionX / n_points;
                double newWDirectionY = sumWDirectionY / n_points;
                double newCurvX = sumCurvX / n_points;
                double newCurvY = sumCurvY / n_points;
                double newAspect = sumAspect / n_points;
                double newSlopeX = sumSlopeX / n_points;
                double newSlopeY = sumSlopeY / n_points;
                double newCurliness = sumCurliness / n_points;
                double newLinearity = sumLinearity / n_points;

                centroid.setX_coordinate(newX);
                centroid.setY_coordinate(newY);
                centroid.setWriting_direction_X(newWDirectionX);
                centroid.setWriting_direction_Y(newWDirectionY);
                centroid.setCurvature_X(newCurvX);
                centroid.setCurvature_Y(newCurvY);
                centroid.setvicinity_aspect(newAspect);
                centroid.setVicinity_slope_X(newSlopeX);
                centroid.setVicinity_slope_Y(newSlopeY);
                centroid.setvicinity_curliness(newCurliness);
                centroid.setvicinity_linearity(newLinearity);
                centroid.setCluster(cluster.getId());
            }
            currentCentroids.add(centroid);
        }
    }
}
