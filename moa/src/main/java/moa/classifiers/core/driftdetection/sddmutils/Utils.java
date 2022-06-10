package moa.classifiers.core.driftdetection.sddmutils;

//**********************************HELPER Methods*****************************************//
public class Utils {
    private static double kullback_leibler_divergence(double[] arr1, double[] arr2) {
        double kld = 0;
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] == 0 || arr2[i] == 0)
                continue;
            kld += arr1[i] * Math.log(arr1[i] / arr2[i]);

        }
        return kld / Math.log(2);


    }
}
