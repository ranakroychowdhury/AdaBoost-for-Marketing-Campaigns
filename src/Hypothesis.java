import java.util.ArrayList;

/**
 * Created by Ranakrc on 19-Apr-18.
 */
public class Hypothesis {

    public double weightOfHypo = 0.0;
    public String attr;
    public ArrayList<Integer> globalMiss = new ArrayList<Integer>(); //keeps track of the id of misclassified samples when classified with the best attribute, so that their weights can be increased
    public ArrayList<Integer> globalHit = new ArrayList<Integer>(); //keeps track of the id of correctly classified samples when classified with the best attribute, so that their weights can be reduced
    public ArrayList<Double> wtVector = new ArrayList<Double>();
    public ArrayList<String> maximumResult = new ArrayList<String>(); ////records the majority result("yes" or "no") for each attribute
    public ArrayList<Integer> matrix = new ArrayList<Integer>();
    public double globalInfoGain = -99999.0;
    public String type;
    public ArrayList<String> categoryString = new ArrayList<String>();
    public ArrayList<Integer> categoryInteger = new ArrayList<Integer>();
    public ArrayList<Double> categoryDouble = new ArrayList<Double>();

    Hypothesis() {}

}
