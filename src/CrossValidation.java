import java.util.ArrayList;

/**
 * Created by Ranakrc on 19-Apr-18.
 */
public class CrossValidation {

    public int startSet1, endSet1, startSet2, endSet2, startTest, endTest;
    public int K = 5; //number of hypotheses
    public ArrayList<Hypothesis> hypothesis = new ArrayList<Hypothesis>();
    public double F1_Score;

    CrossValidation() {}
}
