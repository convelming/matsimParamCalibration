package optimizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Individual implements Comparable<Individual> {
    private static final Logger log = LogManager.getLogger(Individual.class);

    private double[] genes; // Representation of the individual's solution
    private double fitness; // Fitness value of the individual
    // Constructor, getters, and setters
    // Other methods as needed
    public Individual(double[] genes) {
        this.genes = genes;
        this.fitness = -Double.MAX_VALUE;
    }

    public Individual(double[] genes, double fitness) {
        this.genes = genes;
        this.fitness = fitness;
    }

    public Individual rndProduceIndividuals(double scale) {
        Random random = new Random();
        // each parameters must have a range to be randomly
        double[] similarGenes = new double[this.genes.length];
        for (int i = 0; i < this.genes.length; i++) {
            similarGenes[i] = genes[i] - Math.abs(genes[i]) * scale / 2 + Math.abs(genes[i]) * scale * random.nextDouble();
        }
        return new Individual(similarGenes, -Double.MAX_VALUE);
    }
    public Individual rndProduceIndividualsInBounds(double[][] bounds,double scale){
        Random random = new Random();
        double[] similarGenes = new double[this.genes.length];
        for (int i = 0; i < this.genes.length; i++) {
            if (bounds[i][0]>genes[i]||genes[i]>bounds[i][1]) log.warn("One of the parameters is out of default bounds, please check!");
            if (this.genes[i]!=0.0 && (bounds[i][0]<= genes[i]&&genes[i]<=bounds[i][1])){
                similarGenes[i] = genes[i] - Math.abs(genes[i]) * scale / 2 + Math.abs(genes[i]) * scale * random.nextDouble();
                while (similarGenes[i]<bounds[i][0]||similarGenes[i]>bounds[i][1]){
                    similarGenes[i] = genes[i] - Math.abs(genes[i]) * scale / 2 + Math.abs(genes[i]) * scale * random.nextDouble();
                }
            }else{
                similarGenes[i] = bounds[i][0] + (bounds[i][1]-bounds[i][0]) * random.nextDouble();
            }
        }
        return new Individual(similarGenes, -Double.MAX_VALUE);
    }

    public List<Individual> genRndIndividualList(int numIndividuals, double scale) {
        List<Individual> individuals = new ArrayList<>();
        for (int i = 0; i < numIndividuals; i++) {
            individuals.add(rndProduceIndividuals(scale));
        }
        return individuals;
    }
    public List<Individual> genRndIndividualList(int numIndividuals, double[][] bounds,double scale) {
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < numIndividuals; i++) {
            population.add(rndProduceIndividualsInBounds(bounds,scale));
        }
        return population;
    }
    @Override
    public int compareTo(Individual o) {
        return Double.compare(o.getFitness(), this.getFitness());// for descent
    }

    public double[] getGenes() {
        return genes;
    }

    public double getFitness() {
        return fitness;
    }

    public void setGenes(double[] genes) {
        this.genes = genes;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public static Individual rndIndividual(int geneSize) {
        double[] genes = new double[geneSize];
        for (int i = 0; i < geneSize; i++) {
            // if gene i by default is zero, then mutate from -1000~1000; else from -50% to 50%
            if (genes[i]==0.0){
                genes[i] = -1000. + (new Random()).nextDouble() * 2000.;
            }else {
                genes[i] = -genes[i]*0.5+genes[i]*(new Random()).nextDouble();
            }

        }
        return new Individual(genes, -Double.MAX_VALUE);
    }

    public static void main(String[] args) {
        Individual i1 = new Individual(new double[]{2.0, 3.0}, 1.0);
        Individual i2 = new Individual(new double[]{2.0, 3.0}, 4.0);
        Individual i3 = new Individual(new double[]{2.0, 3.0}, 2.0);
        List<Individual> test = new ArrayList<>();
        test.add(i1);
        test.add(i2);
        test.add(i3);
        test.add(i3);
        test.forEach(individual -> System.out.println(individual.fitness));
        Collections.sort(test);
        System.out.println();
        test.forEach(individual -> System.out.println(individual.fitness));
    }
}
