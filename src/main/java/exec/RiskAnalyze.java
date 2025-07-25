/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;

import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.risk.RiskEstimateBuilder;
import org.deidentifier.arx.risk.RiskModelPopulationUniqueness;
import org.deidentifier.arx.risk.RiskModelSampleRisks;
import org.deidentifier.arx.risk.RiskModelSampleSummary;
import org.deidentifier.arx.risk.RiskModelSampleUniqueness;

/**
 *
 * @author braully
 */
public class RiskAnalyze {

    /**
     * Entry point.
     *
     * @param args the arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        // Define data
        // Data.DefaultData data = Data.create();
        // data.add("age", "gender", "zipcode");
        // data.add("<50", "female", "81***");
        // data.add("<50", "male", "81***");
        // data.add(">=50", "male", "81***");
        // data.add("*", "*", "81***");
        // data.add("<50", "female", "81***");
        // data.add(">=50", "male", "81***");
        // data.add("<50", "male", "81***");
        //
        // // Define research subset
        // data.getDefinition().setAttributeType("age",
        // AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        // data.getDefinition().setAttributeType("gender",
        // AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        // data.getDefinition().setAttributeType("zipcode",
        // AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        String csvFile = "data/example.csv";
        String[] quasiAtributes = new String[] {
                "education", "workclass", "salary-class"
        };
        String[] insesitiveAttibutes = new String[] {
                "sex", "age", "race", "marital-status", "native-country", "occupation"
        };

        Data data = Data.create(csvFile, StandardCharsets.UTF_8, ';');
        //// Data data = Data.create(csvFile, ';');
        // data.getDefinition().setAttributeType("education",
        // AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        // data.getDefinition().setAttributeType("workclass",
        // AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        // data.getDefinition().setAttributeType("salary-class",
        // AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);

        for (String attibute : quasiAtributes) {
            data.getDefinition().setAttributeType(attibute, AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        }

        for (String attibute : insesitiveAttibutes) {
            data.getDefinition().setAttributeType(attibute, AttributeType.INSENSITIVE_ATTRIBUTE);
        }

        System.out.println("\n - Input data");
        print(data.getHandle().getView());
        System.out.println("\n - Risk analysis:");
        analyzeData(data.getHandle(), 0.2d);
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

        RiskModelSampleRisks samReidModel = builder.getSampleBasedReidentificationRisk();
        RiskModelSampleUniqueness samUniqueModel = builder.getSampleBasedUniquenessRisk();
        RiskModelPopulationUniqueness popUniqueModel = builder.getPopulationBasedUniquenessRisk();

        double lowestRisk = samReidModel.getLowestRisk();
        double fractionOfTuplesAffectedByLowestRisk = samReidModel.getFractionOfRecordsAffectedByLowestRisk();
        double averageRisk = samReidModel.getAverageRisk();
        double highestRisk = samReidModel.getHighestRisk();
        double fractionOfTuplesAffectedByHighestRisk = samReidModel.getFractionOfRecordsAffectedByHighestRisk();
        double estimatedProsecutorRisk = samReidModel.getEstimatedProsecutorRisk();
        double estimatedJournalistRisk = samReidModel.getEstimatedJournalistRisk();
        double estimatedMarketerRisk = samReidModel.getEstimatedMarketerRisk();
        double fractionOfUniqueTuples = samUniqueModel.getFractionOfUniqueRecords();
        double fractionOfUniqueTuplesDankar = popUniqueModel.getFractionOfUniqueTuplesDankar();
        String populationModel = popUniqueModel.getPopulationUniquenessModel().toString();
        String quasiIdentifiers = handle.getDefinition().getQuasiIdentifyingAttributes().toString();

        /*
         * Measure;Value [%]
         * Lowest prosecutor risk;0.00332%
         * Records affected by lowest risk;100%
         * Average prosecutor risk;0.00332%
         * Highest prosecutor risk;0.00332%
         * Records affected by highest risk;100%
         * Estimated prosecutor risk;0.00332%
         * Estimated journalist risk;0.00332%
         * Estimated marketer risk;0.00332%
         * Sample uniques;0%
         * Population uniques;0%
         * Population model;DANKAR
         * Quasi-identifiers;
         */
        System.out.println("Lowest prosecutor risk: " + getPrecent(lowestRisk));
        System.out.println("Records affected by lowest risk: " + getPrecent(fractionOfTuplesAffectedByLowestRisk));
        System.out.println("Average prosecutor risk: " + getPrecent(averageRisk));
        System.out.println("Highest prosecutor risk: " + getPrecent(highestRisk));
        System.out.println("Records affected by highest risk: " + getPrecent(fractionOfTuplesAffectedByHighestRisk));
        System.out.println("Estimated prosecutor risk: " + getPrecent(estimatedProsecutorRisk));
        System.out.println("Estimated journalist risk: " + getPrecent(estimatedJournalistRisk));
        System.out.println("Estimated marketer risk: " + getPrecent(estimatedMarketerRisk));
        System.out.println("Sample uniques: " + getPrecent(fractionOfUniqueTuples));
        System.out.println("Population uniques: " + getPrecent(fractionOfUniqueTuplesDankar));
        System.out.println("Population model: " + populationModel);
        System.out.println("Quasi-identifiers: " + quasiIdentifiers);

    }

    /**
     * Returns a formatted string
     *
     * @param value
     * @return
     */
    private static String getPrecent(double value) {
        Double valStr = value * 100;
        return valStr.toString() + "%";
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
