import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.Random;
/**
 * Created by Ranakrc on 17-Apr-18.
 */
public class CSVReader {

    public static int stringColumns = 10;
    public static int intColumns = 5;
    public static int doubleColumns = 5;
    public static int columns = stringColumns + intColumns + doubleColumns + 1;
    public static int totNumber = 41188;
    public static int number;
    public static int K = 30; //number of hypotheses
    public static int bins = 10; //number of bins for continuous data
    public static int t = 20; //number of cross validation sets
    public static double range;
    public static int rows, rowsTest;
    public static double posInfDouble = Double.POSITIVE_INFINITY;
    public static double negInfDouble = Double.NEGATIVE_INFINITY;
    public static int posInfInt = (int)Double.POSITIVE_INFINITY;
    public static int negInfInt = (int)Double.NEGATIVE_INFINITY;
    public static String[] attribute = new String[columns - 1];
    //str3 stores the majority result
    public static String str1 = "\"no\"", str2 = "\"yes\"", str3 = "\"no\"", str4 = "\"yes\"";



    static void findF1Score(CrossValidation cv, String[] testResult, String[] actualResult) {

        int tp = 0, fp = 0, fn = 0, tn= 0, i;
        double precision = 0.0, recall = 0.0, F1_Score = 0.0;
        for(i = 0; i < rowsTest; i++) {
            if(testResult[i].equals(actualResult[i]) && testResult[i].equals(str2))
                tp++;
            else if(!(testResult[i].equals(actualResult[i])) && testResult[i].equals(str2))
                fp++;
            else if(!(testResult[i].equals(actualResult[i])) && testResult[i].equals(str1))
                fn++;
            else if(testResult[i].equals(actualResult[i]) && testResult[i].equals(str1))
                tn++;
        }

        /*
        for(i = 0; i < rowsTest; i++) {
            System.out.print("Test Result " + i + ": " + testResult[i]);
            System.out.print(" Actual Result: " + i + ": " + actualResult[i]);
            System.out.println();
        }
        */

        if(tp + fp > 0)
            precision = (double)tp / (double)(tp + fp);
        else
            System.out.println("Precision is undefined");

        if(tp + fn > 0)
            recall = (double)tp / (double)(tp + fn);
        else
            System.out.println("Recall is undefined");

        if(tp + fp > 0 && tp + fn > 0) {
            F1_Score = 2.0 * ((precision * recall) / (precision + recall));
            cv.F1_Score = F1_Score;
        }
        else
            System.out.println("F1 score is undefined");
        //System.out.println("F1_Score is " + cv.F1_Score);
    }



    static void testing(CrossValidation cv, String newString[][], int newInt[][], double newDouble[][], String[] actualResult) {

        int i, j, k;
        String[] testResult = new String[rowsTest];
        for(i = 0; i < rowsTest; i++) {

            double totYes = 0.0, totNo = 0.0;
            for(j = 0; j < cv.hypothesis.size(); j++) {

                //get a hypothesis
                Hypothesis hypo = cv.hypothesis.get(j);
                String varString = "null";
                int varInt = 0;
                double varDouble = 0.0;
                double yes = 0.0, no = 0.0;

                //get the index of the attribute of the hypothesis
                int index = -1;
                for(k = 0; k < columns - 1; k++) {
                    if (attribute[k].equals(hypo.attr))
                        index = k;
                }

                //save the category variable to be compared with the attribute category
                if(hypo.type.equals("string")) {
                    if (index >= 1 && index <= 9)
                        varString = newString[i][index - 1];
                    else if (index == 14)
                        varString = newString[i][9];
                }
                else if (hypo.type.equals("integer")) {
                    if (index == 0)
                        varInt = newInt[i][0];
                    else if (index >= 10 && index <= 13)
                        varInt = newInt[i][index - 9];
                }
                else if(hypo.type.equals("double")) {
                    if (index >= 15 && index <= 19)
                        varDouble = newDouble[i][index - 15];
                }


                //finding the majority vote
                if(hypo.type.equals("string")) {
                    for(k = 0; k < hypo.categoryString.size(); k++) {
                        if(hypo.categoryString.get(k).equals(varString)){
                            if(hypo.maximumResult.get(k).equals(str1))
                                no = hypo.weightOfHypo;
                            else if (hypo.maximumResult.get(k).equals(str2))
                                yes = hypo.weightOfHypo;
                        }
                    }
                }
                else if(hypo.type.equals("integer")) {
                    for(k = 0; k < hypo.categoryInteger.size(); k++) {
                        if(hypo.categoryInteger.get(k) == varInt){
                            if(hypo.maximumResult.get(k).equals(str1))
                                no = hypo.weightOfHypo;
                            else if (hypo.maximumResult.get(k).equals(str2))
                                yes = hypo.weightOfHypo;
                        }
                    }
                }
                else if(hypo.type.equals("double")) {
                    for(k = 0; k < hypo.categoryDouble.size(); k++) {
                        if(hypo.categoryDouble.get(k) == varDouble){
                            if(hypo.maximumResult.get(k).equals(str1))
                                no = hypo.weightOfHypo;
                            else if (hypo.maximumResult.get(k).equals(str2))
                                yes = hypo.weightOfHypo;
                        }
                    }
                }

                totNo += no;
                totYes += yes;
            }
            if(totYes > totNo)
                testResult[i] = str2;
            else if(totNo > totYes)
                testResult[i] = str1;
            else if(totNo == totYes)
                testResult[i] = str3;
            //System.out.println(actualResult[i]);
        }
        findF1Score(cv, testResult, actualResult);
    }



    static void recordClassificationDouble(Hypothesis hypo, double[] train, String[] result) {

        ArrayList<Integer> miss = new ArrayList<Integer>(); //keeps track of the id of misclassified samples so that their weights can be increased
        ArrayList<Integer> hit = new ArrayList<Integer>(); //keeps track of the id of correctly classified samples so that their weights can be reduced
        int i, j, k;

        for(i = 0; i < hypo.categoryDouble.size(); i++) {

            for(j = 0; j < rows; j++) {

                if (train[j] == hypo.categoryDouble.get(i) && hypo.maximumResult.get(i).equals(str1) && result[j].equals(str2))
                    miss.add(j);
                else if (train[j] == hypo.categoryDouble.get(i) && hypo.maximumResult.get(i).equals(str1) && result[j].equals(str1))
                    hit.add(j);
                else if (train[j] == hypo.categoryDouble.get(i) && hypo.maximumResult.get(i).equals(str2) && result[j].equals(str1))
                    miss.add(j);
                else if (train[j] == hypo.categoryDouble.get(i) && hypo.maximumResult.get(i).equals(str2) && result[j].equals(str2))
                    hit.add(j);
            }
        }

        hypo.globalMiss = (ArrayList<Integer>) miss.clone();
        hypo.globalHit = (ArrayList<Integer>) hit.clone();
    }



    static void recordClassificationInt(Hypothesis hypo, int[] train, String[] result) {

        ArrayList<Integer> miss = new ArrayList<Integer>(); //keeps track of the id of misclassified samples so that their weights can be increased
        ArrayList<Integer> hit = new ArrayList<Integer>(); //keeps track of the id of correctly classified samples so that their weights can be reduced
        int i, j, k;

        for(i = 0; i < hypo.categoryInteger.size(); i++) {

            for(j = 0; j < rows; j++) {

                if (train[j] == hypo.categoryInteger.get(i) && hypo.maximumResult.get(i).equals(str1) && result[j].equals(str2))
                    miss.add(j);
                else if (train[j] == hypo.categoryInteger.get(i) && hypo.maximumResult.get(i).equals(str1) && result[j].equals(str1))
                    hit.add(j);
                else if (train[j] == hypo.categoryInteger.get(i) && hypo.maximumResult.get(i).equals(str2) && result[j].equals(str1))
                    miss.add(j);
                else if (train[j] == hypo.categoryInteger.get(i) && hypo.maximumResult.get(i).equals(str2) && result[j].equals(str2))
                    hit.add(j);
            }
        }

        hypo.globalMiss = (ArrayList<Integer>) miss.clone();
        hypo.globalHit = (ArrayList<Integer>) hit.clone();
    }



    static void recordClassificationString(Hypothesis hypo, String[] train, String[] result) {

        ArrayList<Integer> miss = new ArrayList<Integer>(); //keeps track of the id of misclassified samples so that their weights can be increased
        ArrayList<Integer> hit = new ArrayList<Integer>(); //keeps track of the id of correctly classified samples so that their weights can be reduced
        int i, j, k;

        for(i = 0; i < hypo.categoryString.size(); i++) {

            for(j = 0; j < rows; j++) {

                if (train[j].equals(hypo.categoryString.get(i)) && hypo.maximumResult.get(i).equals(str1) && result[j].equals(str2))
                    miss.add(j);
                else if (train[j].equals(hypo.categoryString.get(i)) && hypo.maximumResult.get(i).equals(str1) && result[j].equals(str1))
                    hit.add(j);
                else if (train[j].equals(hypo.categoryString.get(i)) && hypo.maximumResult.get(i).equals(str2) && result[j].equals(str1))
                    miss.add(j);
                else if (train[j].equals(hypo.categoryString.get(i)) && hypo.maximumResult.get(i).equals(str2) && result[j].equals(str2))
                    hit.add(j);
            }
        }

        hypo.globalMiss = (ArrayList<Integer>) miss.clone();
        hypo.globalHit = (ArrayList<Integer>) hit.clone();
    }



    static void learnDouble(Hypothesis hyp, double[] testDouble, String attribute, String[] sampleRes, ArrayList<Double> category, double parentEntropy, int parentNo, int parentYes, double[] train, String[] result) {

        ArrayList<String> maxResult = new ArrayList<String>(); //records the majority result("yes" or "no") for each attribute
        ArrayList<Integer> mat = new ArrayList<>();

        int i, j, k;
        int no, yes, total;
        double entropySumChild = 0.0, entropyChild, fracNo, fracYes, frac1 = 0.0, frac2 = 0.0;

        for(i = 0; i < category.size(); i++) {

            no = 0;
            yes = 0;
            for (j = 0; j < rows; j++) {
                if (category.get(i) == testDouble[j] && sampleRes[j].equals(str1))
                    no++;
                if (category.get(i) == testDouble[j] && sampleRes[j].equals(str2))
                    yes++;
            }
            total = no + yes;
            if (total > 0) {
                fracNo = (double) no / (double) total;
                fracYes = (double) yes / (double) total;
                if (no > 0)
                    frac1 = -fracNo * (Math.log10(fracNo) / Math.log10(2));
                if (yes > 0)
                    frac2 = -fracYes * (Math.log10(fracYes) / Math.log10(2));
                entropyChild = frac1 + frac2;
                entropySumChild += ((double) total / (double) rows) * entropyChild;
            }
            mat.add(no);
            mat.add(yes);
            if (no > yes)
                maxResult.add(str1);
            else if (yes > no)
                maxResult.add(str2);
            else if (no == yes) {
                if (parentNo > parentYes)
                    maxResult.add(str1);
                else if (parentYes > parentNo)
                    maxResult.add(str2);
                else if (parentNo == parentYes)
                    maxResult.add(str3);
            }
        }

        double infoGain = parentEntropy - entropySumChild;
        //System.out.println("Infogain is " + infoGain);
        //Update the variable of each hypothesis of each fold
        if(infoGain > hyp.globalInfoGain) {
            hyp.globalInfoGain = infoGain;
            hyp.attr = attribute;
            hyp.type = "double";
            hyp.categoryDouble = (ArrayList<Double>) category.clone();
            hyp.maximumResult = (ArrayList<String>) maxResult.clone();
            hyp.matrix = (ArrayList<Integer>) mat.clone();
            recordClassificationDouble(hyp, train, result);
        }
    }



    static void learnInteger(Hypothesis hyp, int[] testInteger, String attribute, String[] sampleRes, ArrayList<Integer> category, double parentEntropy, int parentNo, int parentYes, int[] train, String[] result) {

        ArrayList<String> maxResult = new ArrayList<String>(); //records the majority result("yes" or "no") for each attribute
        ArrayList<Integer> mat = new ArrayList<>();

        int i, j, k;
        int no, yes, total;
        double entropySumChild = 0.0, entropyChild, fracNo, fracYes, frac1 = 0.0, frac2 = 0.0;

        for(i = 0; i < category.size(); i++) {

            no = 0;
            yes = 0;
            for (j = 0; j < rows; j++) {
                if (category.get(i) == testInteger[j] && sampleRes[j].equals(str1))
                    no++;
                if (category.get(i) == testInteger[j] && sampleRes[j].equals(str2))
                    yes++;
            }
            total = no + yes;
            if (total > 0) {
                fracNo = (double) no / (double) total;
                fracYes = (double) yes / (double) total;
                if (no > 0)
                    frac1 = -fracNo * (Math.log10(fracNo) / Math.log10(2));
                if (yes > 0)
                    frac2 = -fracYes * (Math.log10(fracYes) / Math.log10(2));
                entropyChild = frac1 + frac2;
                entropySumChild += ((double) total / (double) rows) * entropyChild;
            }
            mat.add(no);
            mat.add(yes);
            if (no > yes)
                maxResult.add(str1);
            else if (yes > no)
                maxResult.add(str2);
            else if (no == yes) {
                if (parentNo > parentYes)
                    maxResult.add(str1);
                else if (parentYes > parentNo)
                    maxResult.add(str2);
                else if (parentNo == parentYes)
                    maxResult.add(str3);
            }
        }

        double infoGain = parentEntropy - entropySumChild;
        //System.out.println("Infogain is " + infoGain);
        //Update the variable of each hypothesis of each fold
        if(infoGain > hyp.globalInfoGain) {
            hyp.globalInfoGain = infoGain;
            hyp.attr = attribute;
            hyp.type = "integer";
            hyp.categoryInteger = (ArrayList<Integer>) category.clone();
            hyp.maximumResult = (ArrayList<String>) maxResult.clone();
            hyp.matrix = (ArrayList<Integer>) mat.clone();
            recordClassificationInt(hyp, train, result);
        }
    }



    static void learnString(Hypothesis hyp, String[] testString, String attribute, String[] sampleRes, ArrayList<String> category, double parentEntropy, int parentNo, int parentYes, String[] train, String[] result) {

        ArrayList<String> maxResult = new ArrayList<String>(); //records the majority result("yes" or "no") for each attribute
        ArrayList<Integer> mat = new ArrayList<>();
        int i, j, k;
        int no, yes, total;
        double entropySumChild = 0.0, entropyChild, fracNo, fracYes, frac1 = 0.0, frac2 = 0.0;

        for(i = 0; i < category.size(); i++) {

            no = 0;
            yes = 0;
            for (j = 0; j < rows; j++) {
                if (category.get(i).equals(testString[j]) && sampleRes[j].equals(str1))
                    no++;
                if (category.get(i).equals(testString[j]) && sampleRes[j].equals(str2))
                    yes++;
            }
            total = no + yes;
            if (total > 0) {
                fracNo = (double) no / (double) total;
                fracYes = (double) yes / (double) total;
                if (no > 0)
                    frac1 = -fracNo * (Math.log10(fracNo) / Math.log10(2));
                if (yes > 0)
                    frac2 = -fracYes * (Math.log10(fracYes) / Math.log10(2));
                entropyChild = frac1 + frac2;
                entropySumChild += ((double) total / (double) rows) * entropyChild;
            }
            mat.add(no);
            mat.add(yes);
            if (no > yes)
                maxResult.add(str1);
            else if (yes > no)
                maxResult.add(str2);
            else if (no == yes) {
                if (parentNo > parentYes)
                    maxResult.add(str1);
                else if (parentYes > parentNo)
                    maxResult.add(str2);
                else if (parentNo == parentYes)
                    maxResult.add(str3);
            }
        }

        double infoGain = parentEntropy - entropySumChild;
        //System.out.println("Infogain is " + infoGain);
        //Update the variable of each hypothesis of each fold
        if(infoGain > hyp.globalInfoGain) {
            hyp.globalInfoGain = infoGain;
            hyp.attr = attribute;
            hyp.type = "string";
            hyp.categoryString = (ArrayList<String>) category.clone();
            hyp.maximumResult = (ArrayList<String>) maxResult.clone();
            hyp.matrix = (ArrayList<Integer>) mat.clone();
            recordClassificationString(hyp, train, result);
        }
    }



    static void readFile(String newString[][], int newInt[][], double newDouble[][], String result[]) {

        String csvFile = "bank-additional-full.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ";";
        int cnt = -1;


        //read and parse file into string, float and integers
        try {

            br = new BufferedReader(new FileReader(csvFile));
            int j;

            while ((line = br.readLine()) != null) {

                String[] country = line.split(cvsSplitBy);
                int cntInt = 0, cntString = 0, cntDouble = 0;

                // use semicolon as separator
                if(cnt > -1) {
                    for (j = 0; j < columns; j++) {
                        if(j == columns - 1)
                            result[cnt] = country[j];
                        else if(j == 0 || j == 10 || j == 11 || j == 12 || j == 13) {
                            newInt[cnt][cntInt] = Integer.parseInt(country[j]);
                            cntInt++;
                        }
                        else if((j >= 1 && j <= 9) || j == 14) {
                            newString[cnt][cntString] = country[j];
                            cntString++;
                        }
                        else {
                            newDouble[cnt][cntDouble] = Double.parseDouble(country[j]);
                            cntDouble++;
                        }
                    }

                }
                else {
                    for (j = 0; j < columns - 1; j++)
                        attribute[j] = country[j];
                }
                cnt++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /*
        int i, j;
        for(i=0; i<rows; i++) {
            System.out.println("No# " + i);
            for(j=0; j<intColumns; j++)
                System.out.print(newInt[i][j] + " ");
            System.out.println();
        }

        for(i=0; i<rows; i++) {
            System.out.println("No# " + i);
            for(j=0; j<stringColumns; j++)
                System.out.print(newString[i][j] + " ");
            System.out.println();
        }

        for(i=0; i<rows; i++) {
            System.out.println("No# " + i);
            for(j=0; j<doubleColumns; j++)
                System.out.print(newDouble[i][j] + " ");
            System.out.println();
            System.out.println();
        }

        for(i=0; i<rows; i++) {
            if (result[i].equals("\"no\""))
                System.out.print(result[i]);
        }
        System.out.println();
        */
    }



    static void ADABOOST(CrossValidation cv, String newString[][], int prepSampInt[][], double prepSampDouble[][], String result[]) {

        int i, j, k, numHypo = 0;
        double error;
        double[] weight = new double[rows];
        double[] cumulativeSumMin = new double[rows]; //records the minimum weight to take a sample
        double[] cumulativeSumMax = new double[rows]; //records the maximum weight to take a sample

        //Initialize the weight vector
        for (i = 0; i < rows; i++)
            weight[i] = 1.0 / rows;


        while(numHypo < K) {
            //System.out.println("numHypo is " + numHypo) ;
            //System.out.println(rows);
            Hypothesis hypo = new Hypothesis();
            //System.out.println("Hypothesis No: " + numHypo);

            int[] sampleSet = new int[rows];
            String[] sampleRes = new String[rows];

            error = 0.0;
            cumulativeSumMin[0] = 0.0;
            cumulativeSumMax[0] = weight[0];

            for (i = 1; i < rows; i++) {
                cumulativeSumMin[i] = cumulativeSumMax[i - 1];
                cumulativeSumMax[i] = cumulativeSumMin[i] + weight[i];
            }

            /*
            for(i = 0; i < rows; i++) {
                System.out.print(cumulativeSumMin[i] + " " + cumulativeSumMax[i]);
                System.out.println();
            }*/


            double sampleWtSum = 0.0;
            //System.out.print("Chosen sample ");
            for (i = 0; i < rows; i++) {
                Random generator = new Random();
                double number = generator.nextDouble();
                for (j = 0; j < rows; j++) {
                    if (number >= cumulativeSumMin[j] && number < cumulativeSumMax[j]) {
                        sampleSet[i] = j;
                        sampleRes[i] = result[j];
                        sampleWtSum += weight[j];
                        //System.out.print(j + " ");
                    }
                }
            }

            //Generate the matrices to calculate entropy from the sampled set
            int no = 0;
            int yes = 0;
            int total = 0;
            double entropySumChild = 0.0;
            double parentEntropy = 0.0;
            double fracNo = 0.0;
            double fracYes = 0.0;

            //Calculate entropy for parent
            for (i = 0; i < rows; i++) {
                if (sampleRes[i].equals(str1))
                    no++;
                else if (sampleRes[i].equals(str2))
                    yes++;
            }
            total = no + yes;
            fracNo = (double) no / (double) total;
            fracYes = (double) yes / (double) total;
            parentEntropy = -fracNo * (Math.log10(fracNo) / Math.log10(2)) - fracYes * (Math.log10(fracYes) / Math.log10(2));

            //Set the weight vector for each hypothesis
            for (j = 0; j < rows; j++)
                hypo.wtVector.add(weight[j]);


            //Work with strings
            for (j = 0; j < stringColumns; j++) {
                ArrayList<String> category = new ArrayList<String>(); //records all the category of an attribute

                // Finding the number of distinct categories of an attribute
                for (i = 0; i < rows; i++) {
                    // Check if the picked element
                    // is already printed
                    for (k = 0; k < i; k++) {
                        if (newString[i][j].equals(newString[k][j]))
                            break;
                    }

                    //when a new category is encountered for the first time, enter it in category
                    if (i == k)
                        category.add(newString[i][j]);
                }

                String[] testString = new String[rows];
                for (i = 0; i < rows; i++)
                    testString[i] = newString[sampleSet[i]][j];

                String[] prepString = new String[rows];
                for (i = 0; i < rows; i++)
                    prepString[i] = newString[i][j];

                //HARDCODED
                if (j == 9)
                    learnString(hypo, testString, attribute[j + 5], sampleRes, category, parentEntropy, no, yes, prepString, result);
                else
                    learnString(hypo, testString, attribute[j + 1], sampleRes, category, parentEntropy, no, yes, prepString, result);
            }


            //Work with integers
            for (j = 0; j < intColumns; j++) {
                ArrayList<Integer> category = new ArrayList<Integer>(); //records all the category of an attribute

                //Generate category for the attribute
                for (i = 0; i < bins; i++)
                    category.add(i);

                int[] testInt = new int[rows];
                for (i = 0; i < rows; i++)
                    testInt[i] = prepSampInt[sampleSet[i]][j];

                int[] trainInt = new int[rows];
                for(i = 0; i < rows; i++)
                    trainInt[i] = prepSampInt[i][j];

                //HARDCODED
                if (j == 0)
                    learnInteger(hypo, testInt, attribute[0], sampleRes, category, parentEntropy, no, yes, trainInt, result);
                 else
                    learnInteger(hypo, testInt, attribute[j + 9], sampleRes, category, parentEntropy, no, yes, trainInt, result);
            }



            //Work with double
            for (j = 0; j < doubleColumns; j++) {
                ArrayList<Double> category = new ArrayList<Double>(); //records all the category of an attribute

                //Generate category for the attribute
                for (i = 0; i < bins; i++)
                    category.add((double) i);

                double[] testDouble = new double[rows];
                for (i = 0; i < rows; i++)
                    testDouble[i] = prepSampDouble[sampleSet[i]][j];

                double[] trainDouble = new double[rows];
                for(i = 0; i < rows; i++)
                    trainDouble[i] = prepSampDouble[i][j];

                learnDouble(hypo, testDouble, attribute[j + 15], sampleRes, category, parentEntropy, no, yes, trainDouble, result);
            }

            /*
            //print the globalMin & its corresponding attribute
            System.out.println("Global Info Gain is " + hypo.globalInfoGain + " and corresponding attribute is " + hypo.attr);
            System.out.println(hypo.attr + " type is " + hypo.type);

            if(hypo.type.equals("string")) {
                for(i = 0; i < hypo.categoryString.size(); i++) {
                    System.out.print("Category " + i + ": " + hypo.categoryString.get(i));
                    System.out.print(" No: " + hypo.matrix.get(2 * i) + " Yes: " + hypo.matrix.get(2 * i + 1));
                    System.out.print(" Majority result is " + hypo.maximumResult.get(i));
                    System.out.println();
                }
            }
            else if(hypo.type.equals("integer")) {
                for(i = 0; i < hypo.categoryInteger.size(); i++) {
                    System.out.print("Category " + i + ": " + hypo.categoryInteger.get(i));
                    System.out.print(" No: " + hypo.matrix.get(2 * i) + " Yes: " + hypo.matrix.get(2 * i + 1));
                    System.out.print(" Majority result is " + hypo.maximumResult.get(i));
                    System.out.println();
                }
            }
            else if(hypo.type.equals("double")) {
                for(i = 0; i < hypo.categoryDouble.size(); i++) {
                    System.out.print("Category " + i + ": " + hypo.categoryDouble.get(i));
                    System.out.print(" No: " + hypo.matrix.get(2 * i) + " Yes: " + hypo.matrix.get(2 * i + 1));
                    System.out.print(" Majority result is " + hypo.maximumResult.get(i));
                    System.out.println();
                }
            }


            System.out.println();
            for(i = 0; i < hypo.globalMiss.size(); i++) {
                System.out.print(i + ":");
                System.out.print(hypo.globalMiss.get(i) + ", ");
            }

            System.out.println();
            System.out.println(hypo.globalMiss.size() + " have been misclassified");
            System.out.println(hypo.globalHit.size() + " have been correctly classified");
            */

            for (i = 0; i < hypo.globalMiss.size(); i++)
                error += weight[hypo.globalMiss.get(i)];
            //System.out.println("Error is" + error);

            //Check if this is a valid hypothesis
            if (error >= 0.5)
                continue;

            numHypo++;

            for (i = 0; i < hypo.globalHit.size(); i++)
                weight[hypo.globalHit.get(i)] = weight[hypo.globalHit.get(i)] * (error / (1 - error));

            double sumW = 0.0;
            for (i = 0; i < rows; i++)
                sumW += weight[i];

            for (i = 0; i < rows; i++)
                weight[i] = weight[i] / sumW;


            hypo.weightOfHypo = Math.log10((1.0 - error) / error);
            //System.out.println("Weight is " + hypo.weightOfHypo);
            cv.hypothesis.add(hypo);
        }
    }



    static void prepDataSet(Universe unv) {

        int i, j, k, totNo = 0, totYes = 0, minResult = 0;
        String[][] fileString = new String[totNumber][stringColumns];
        int[][] fileInt = new int[totNumber][intColumns];
        double[][] fileDouble = new double[totNumber][doubleColumns];
        String[] fileResult = new String[totNumber];

        readFile(fileString, fileInt, fileDouble, fileResult);

        for(i = 0; i < totNumber; i++) {
            if(fileResult[i].equals(str1))
                totNo++;
            else if(fileResult[i].equals(str2))
                totYes++;
        }

        if(totNo > totYes) {
            minResult = totYes;
            str3 = str1;
            str4 = str2;
        }
        else if(totYes > totNo) {
            minResult = totNo;
            str3 = str2;
            str4 = str1;
        }
        else if(totNo == totYes) {
            minResult = totNo;
            str3 = str2;
            str4 = str1;
        }

        number = 2 * minResult;
        //System.out.println("Minresult: " + minResult);
        range = Math.ceil((double)number/(double)t);
        //System.out.println(number + " " + range);

        String[][] readyString = new String[number][stringColumns];
        int[][] readyInt = new int[number][intColumns];
        double[][] readyDouble = new double[number][doubleColumns];
        String[] readyResult = new String[number];
        int mino = 0, majo = 0;
        ArrayList<Integer> fill = new ArrayList<>();
        Random rand = new Random();

        //Prepare the dataset for the minority samples
        for(i = 0; i < totNumber; i++) {
            if (fileResult[i].equals(str4) && mino < minResult) {
                while (true) {
                    int n = rand.nextInt(number - 1);
                    if (!fill.contains(n)) {
                        for (j = 0; j < stringColumns; j++)
                            readyString[n][j] = fileString[i][j];
                        for (j = 0; j < intColumns; j++)
                            readyInt[n][j] = fileInt[i][j];
                        for (j = 0; j < doubleColumns; j++)
                            readyDouble[n][j] = fileDouble[i][j];
                        readyResult[n] = fileResult[i];
                        fill.add(n);
                        mino++;
                        break;
                    }
                }
            }
        }

        //Prepare the dataset for the majority samples
        //System.out.println(fill.size());
        int[] arr = new int[minResult];
        i = 0;
        for(k = 0; k < number; k++) {
            if(!fill.contains(k)) {
                arr[i] = k;
                i++;
                fill.add(k);
            }
        }

        //Prepare the dataset for the majority samples
        for(i = 0; i < totNumber; i++) {
            Random r = new Random();
            int idx = r.nextInt(totNumber);
            if(fileResult[idx].equals(str3) && majo < minResult) {
                for (j = 0; j < stringColumns; j++)
                    readyString[arr[majo]][j] = fileString[idx][j];
                for (j = 0; j < intColumns; j++)
                    readyInt[arr[majo]][j] = fileInt[idx][j];
                for (j = 0; j < doubleColumns; j++)
                    readyDouble[arr[majo]][j] = fileDouble[idx][j];
                readyResult[arr[majo]] = fileResult[idx];
                majo++;
            }
        }

        /*
        int NO = 0, YES = 0;
        for(i = 0; i < number; i++) {
            System.out.print(i + ": ");
            for(j = 0; j < stringColumns; j++)
                System.out.print(readyString[i][j] + " ");
            for(j = 0; j < intColumns; j++)
                System.out.print(readyInt[i][j] + " ");
            for(j = 0; j < doubleColumns; j++)
                System.out.print(readyDouble[i][j] + " ");
            System.out.print(readyResult[i]);
            if(readyResult[i].equals(str1))
                NO++;
            else
                YES++;
            System.out.println();
        }
        */
        //System.out.println(maxCount);
        //printDistinct(index, number);
        //Find the number of distinct elements in the index array


        for(k = 0; k < t; k++) {
            CrossValidation cv = new CrossValidation();
            int[][] maxBinInt = new int[intColumns][bins];
            double[][] maxBinDouble = new double[doubleColumns][bins];
            //TEST SET
            if (k != t - 1) {
                //cv.startTest = 18858;
                //cv.endTest = 18858;
                //rowsTest = 1;
                //cv.startTest = (int) range;
                //cv.endTest = number - 1;
                //rowsTest = number - cv.startTest;
                cv.startTest = k * (int) range;
                cv.endTest = (k + 1) * (int) range - 1;
                rowsTest = (int) range;
            } else {
                cv.startTest = k * (int) range;
                cv.endTest = number - 1;
                rowsTest = number - (t - 1) * (int) range;
            }

            //TRAINING SET
            if (k == 0) {
                //cv.startSet1 = 0;
                //cv.endSet1 = 9;
                //rows = 10;
                cv.startSet1 = (int) range;
                cv.endSet1 = number - 1;
                cv.startSet2 = -1;
                cv.endSet2 = -1;
                rows = number - cv.startSet1;
            } else if (k == t - 1) {
                cv.startSet1 = -1;
                cv.endSet1 = -1;
                cv.startSet2 = 0;
                cv.endSet2 = k * (int) range - 1;
                rows = cv.endSet2 + 1;
            } else {
                cv.startSet1 = 0;
                cv.endSet1 = cv.startTest - 1;
                cv.startSet2 = cv.endTest + 1;
                cv.endSet2 = number - 1;
                rows = cv.endSet1 + 1 + number - cv.startSet2;
            }

            //System.out.println("Rows: " + cv.startSet1 + " " + cv.endSet1 + " " + cv.startSet2 + " " + cv.endSet2 + ": RowsTest " + cv.startTest + " " + cv.endTest);
            //TRAINING DATASET
            String[][] newString = new String[rows][stringColumns];
            int[][] newInt = new int[rows][intColumns];
            int[][] prepSampInt = new int[rows][intColumns];
            double[][] newDouble = new double[rows][doubleColumns];
            double[][] prepSampDouble = new double[rows][doubleColumns];
            String[] result = new String[rows];

            //TESTING DATASET
            String[][] testString = new String[rowsTest][stringColumns];
            int[][] testInt = new int[rowsTest][intColumns];
            int[][] testPrepInt = new int[rowsTest][intColumns];
            double[][] testDouble = new double[rowsTest][doubleColumns];
            double[][] testPrepDouble = new double[rowsTest][doubleColumns];
            String[] testResult = new String[rowsTest];

            int cntRows = 0, cntRowsTest = 0;
            for(i = 0; i < number; i++) {

                if((i >= cv.startSet1 && i <= cv.endSet1) || (i >= cv.startSet2 && i <= cv.endSet2)) {
                    for(j = 0; j < stringColumns; j++)
                        newString[cntRows][j] = readyString[i][j];
                    for(j = 0; j < intColumns; j++)
                        newInt[cntRows][j] = readyInt[i][j];
                    for(j = 0; j < doubleColumns; j++)
                        newDouble[cntRows][j] = readyDouble[i][j];
                    result[cntRows] = readyResult[i];
                    cntRows++;
                }
                if(i >= cv.startTest && i <= cv.endTest) {
                    for(j = 0; j < stringColumns; j++)
                        testString[cntRowsTest][j] = readyString[i][j];
                    for(j = 0; j < intColumns; j++)
                        testInt[cntRowsTest][j] = readyInt[i][j];
                    for(j = 0; j < doubleColumns; j++)
                        testDouble[cntRowsTest][j] = readyDouble[i][j];
                    testResult[cntRowsTest] = readyResult[i];
                    cntRowsTest++;
                }
            }

            /*
            for(i = 0; i < rowsTest; i++) {
                for(j = 0; j < stringColumns; j++)
                    System.out.print(testString[i][j] + " ");
                for(j = 0; j < intColumns; j++)
                    System.out.print(testInt[i][j] + " ");
                for(j = 0; j < doubleColumns; j++)
                    System.out.print(testDouble[i][j] + " ");
                System.out.print(testResult[i]);
                System.out.println();
            }
            */
            //System.out.println(cntRows + " " +cntRowsTest);


            //Classify integer and double data into bins for training
            for (j = 0; j < intColumns; j++) {
                int globalMax, globalMin;
                globalMax = negInfInt;
                globalMin = posInfInt;

                //Finding min and max integers of an attribute
                for (i = 0; i < rows; i++) {
                    if (newInt[i][j] < globalMin)
                        globalMin = newInt[i][j];
                }

                for (i = 0; i < rows; i++) {
                    if (newInt[i][j] > globalMax)
                        globalMax = newInt[i][j];
                }

                double range = Math.ceil(((double) globalMax - (double) globalMin) / (double) bins);

                //Set the upper limit for each bin of each attribute
                for (i = 0; i < bins - 1; i++)
                    maxBinInt[j][i] = globalMin + (i + 1) * (int) range;
                maxBinInt[j][bins - 1] = posInfInt;

                //Update arr[] according to the bins
                int b;
                for (i = 0; i < rows; i++) {
                    if (newInt[i][j] > negInfInt && newInt[i][j] < globalMin + (int) range)
                        prepSampInt[i][j] = 0;
                    for (b = 1; b < bins - 1; b++) {
                        if (newInt[i][j] >= globalMin + b * (int) range && newInt[i][j] < globalMin + (b + 1) * (int) range)
                            prepSampInt[i][j] = b;
                    }
                    if (newInt[i][j] >= globalMin + (bins - 1) * (int) range && newInt[i][j] <= posInfInt)
                        prepSampInt[i][j] = bins - 1;
                }
            }


            for (j = 0; j < doubleColumns; j++) {

                double globalMax, globalMin;
                globalMax = negInfDouble;
                globalMin = posInfDouble;

                //Finding min and max integers of an attribute
                for (i = 0; i < rows; i++) {
                    if (newDouble[i][j] < globalMin)
                        globalMin = newDouble[i][j];
                }

                for (i = 0; i < rows; i++) {
                    if (newDouble[i][j] > globalMax)
                        globalMax = newDouble[i][j];
                }

                double range = Math.ceil(((double) globalMax - (double) globalMin) / (double) bins);

                //Set the upper limit for each bin of each attribute
                for (i = 0; i < bins - 1; i++)
                    maxBinDouble[j][i] = globalMin + (double) (i + 1) * range;
                maxBinDouble[j][bins - 1] = posInfDouble;

                //Update arr[] according to the bins
                int b;
                for (i = 0; i < rows; i++) {
                    if (newDouble[i][j] > negInfDouble && newDouble[i][j] < globalMin + range)
                        prepSampDouble[i][j] = 0.0;
                    for (b = 1; b < bins - 1; b++) {
                        if (newDouble[i][j] >= globalMin + (double) b * range && newDouble[i][j] < globalMin + (double) (b + 1) * range)
                            prepSampDouble[i][j] = (double) b;
                    }
                    if (newDouble[i][j] >= globalMin + (double) (bins - 1) * range && newDouble[i][j] <= posInfDouble)
                        prepSampDouble[i][j] = (double) (bins - 1);
                }
            }

            /*
            for(i = 0; i < rows; i++) {
                for(j = 0; j < stringColumns; j++)
                    System.out.print(newString[i][j] + " ");
                for(j = 0; j < intColumns; j++)
                    System.out.print(prepSampInt[i][j] + " ");
                for(j = 0; j < doubleColumns; j++)
                    System.out.print(prepSampDouble[i][j] + " ");
                System.out.print(result[i]);
                System.out.println();
            }
            */

            ADABOOST(cv, newString, prepSampInt, prepSampDouble, result);


            //Classify integer and double data into bins for testing
            int b;
            for (j = 0; j < intColumns; j++) {

                for (i = 0; i < rowsTest; i++) {
                    int a = testInt[i][j];

                    if (a < maxBinInt[j][0])
                        testPrepInt[i][j] = 0;
                    for (b = 1; b < bins - 1; b++) {
                        if (a < maxBinInt[j][b])
                            testPrepInt[i][j] = b;
                    }
                    if (a >= maxBinInt[j][bins -1])
                        testPrepInt[i][j] = bins - 1;
                }
            }

            for (j = 0; j < doubleColumns; j++) {

                for (i = 0; i < rowsTest; i++) {
                    double a = testDouble[i][j];

                    if (a < maxBinDouble[j][0])
                        testPrepDouble[i][j] = 0.0;
                    for (b = 1; b < bins - 1; b++) {
                        if (a < maxBinDouble[j][b])
                            testPrepDouble[i][j] = (double)b;
                    }
                    if (a >= maxBinDouble[j][bins -1])
                        testPrepDouble[i][j] = (double)(bins - 1);
                }
            }


            testing(cv, testString, testPrepInt, testPrepDouble, testResult);
            unv.cvArr.add(cv);
        }

    }



    public static void main(String[] args) {

        Universe unv = new Universe();
        prepDataSet(unv);
        int i;
        double sum_F1 = 0.0, avg_F1 = 0.0;
        for (i = 0; i < unv.cvArr.size(); i++) {
            System.out.println("F1_Score of cross validation set " + i + " is " + unv.cvArr.get(i).F1_Score);
            sum_F1 += unv.cvArr.get(i).F1_Score;
        }
        avg_F1 = sum_F1 / (double) t;
        System.out.println("Average F1_SCORE is " + avg_F1);
    }
}

