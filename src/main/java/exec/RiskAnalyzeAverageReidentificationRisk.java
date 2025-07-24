/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.criteria.AverageReidentificationRisk;
import org.deidentifier.arx.examples.Example;
import org.deidentifier.arx.risk.RiskEstimateBuilder;
import org.deidentifier.arx.risk.RiskModelSampleRisks;
import org.deidentifier.arx.risk.RiskModelSampleSummary;

/**
 *
 * @author braully
 */
public class RiskAnalyzeAverageReidentificationRisk {

    /**
     * Entry point.
     *
     * @param args the arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        // Define data
//        Data.DefaultData data = Data.create();
//        data.add("age", "gender", "zipcode");
//        data.add("<50", "female", "81***");
//        data.add("<50", "male", "81***");
//        data.add(">=50", "male", "81***");
//        data.add("*", "*", "81***");
//        data.add("<50", "female", "81***");
//        data.add(">=50", "male", "81***");
//        data.add("<50", "male", "81***");
//
//        // Define research subset
//        data.getDefinition().setAttributeType("age", AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
//        data.getDefinition().setAttributeType("gender", AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
//        data.getDefinition().setAttributeType("zipcode", AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        String csvFile = "data/example.csv";
        Data data = Data.create(csvFile, StandardCharsets.UTF_8, ';');
//        Data data = Data.create(csvFile, ';');
        data.getDefinition().setAttributeType("education", AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        data.getDefinition().setAttributeType("workclass", AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        data.getDefinition().setAttributeType("salary-class", AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        

        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new AverageReidentificationRisk(0.5d));
        ARXResult result = anonymizer.anonymize(data, config);
        /*  */
        // Perform risk analysis
        // Create an instance of the anonymizer
        // Perform risk analysis
        System.out.println("\n - Input data");
        print(result.getOutput());
//        print(data.getHandle().getView());

//        System.out.println("\n - Risk analysis:");
//        print(result.getOutput());
        System.out.println("\n - Risk analysis:");
        analyzeData(result.getOutput(), 0.01d);
//        analyzeData(data.getHandle(), 0.5d);
    }

    /**
     * Perform risk analysis
     *
     * @param handle
     */
    private static void analyzeData(DataHandle handle, double THRESHOLD) {

        ARXPopulationModel populationmodel = ARXPopulationModel.create(ARXPopulationModel.Region.USA);
        RiskEstimateBuilder builder = handle.getRiskEstimator(populationmodel);
        RiskModelSampleSummary risks = builder.getSampleBasedRiskSummary(THRESHOLD);

        System.out.println(" * Baseline risk threshold: " + getPrecent(THRESHOLD));
        System.out.println(" * Prosecutor attacker model");
        System.out.println("   - Records at risk: " + getPrecent(risks.getProsecutorRisk().getRecordsAtRisk()));
        System.out.println("   - Highest risk: " + getPrecent(risks.getProsecutorRisk().getHighestRisk()));
        System.out.println("   - Success rate: " + getPrecent(risks.getProsecutorRisk().getSuccessRate()));
        System.out.println(" * Journalist attacker model");
        System.out.println("   - Records at risk: " + getPrecent(risks.getJournalistRisk().getRecordsAtRisk()));
        System.out.println("   - Highest risk: " + getPrecent(risks.getJournalistRisk().getHighestRisk()));
        System.out.println("   - Success rate: " + getPrecent(risks.getJournalistRisk().getSuccessRate()));
        System.out.println(" * Marketer attacker model");
        System.out.println("   - Success rate: " + getPrecent(risks.getMarketerRisk().getSuccessRate()));

        //
        RiskModelSampleRisks reidentificationRisk = builder.getSampleBasedReidentificationRisk();
        System.out.println(" ReidentificationRisk: ");
        System.out.println("       + Average risk     : " + getPrecent(reidentificationRisk.getAverageRisk()));
        System.out.println("       + Lowest risk      : " + getPrecent(reidentificationRisk.getLowestRisk()));
        System.out.println("       + Highest risk     : " + getPrecent(reidentificationRisk.getHighestRisk()));
        System.out.println("       + Tuples affected  : " + getPrecent(reidentificationRisk.getFractionOfRecordsAffectedByHighestRisk()));

        System.out.println("  * Prosecutor re-identification risk: " + getPrecent(reidentificationRisk.getEstimatedProsecutorRisk()));
        System.out.println("  * Journalist re-identification risk: " + getPrecent(reidentificationRisk.getEstimatedJournalistRisk()));
        System.out.println("  * Marketer re-identification risk: " + getPrecent(reidentificationRisk.getEstimatedMarketerRisk()));

    }

    /**
     * Returns a formatted string
     *
     * @param value
     * @return
     */
    private static String getPrecent(double value) {
        return (int) (Math.round(value * 100)) + "%";
    }

    static void print(DataHandle handle) {
        final Iterator<String[]> itHandle = handle.iterator();
        print(itHandle);
    }

    /**
     * Prints a given iterator.
     *
     * @param iterator
     */
    static void print(Iterator<String[]> iterator) {
        while (iterator.hasNext()) {
            System.out.print("   ");
            System.out.println(Arrays.toString(iterator.next()));
        }
    }

}
