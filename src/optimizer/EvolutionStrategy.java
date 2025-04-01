package optimizer;

import java.util.List;

/**
 * Author：Milu
 * 严重落后进度啦......
 * date：2024/2/29
 * project:matsimParamCalibration
 */
public interface EvolutionStrategy {
    // Initialize the population
    void initializePopulation();
    // Evaluate the fitness of each individual in the population
    void evaluatePopulation();
    // Select parents for reproduction
    List<Individual> selectParents();
    // Reproduce and create offspring
    List<Individual> reproduce(List<Individual> parents);
    // Mutate the offspring
    void mutate(List<Individual> offspring);
    // Replace the old population with the new population
    void replacePopulation(List<Individual> newPopulation);
    // Run the evolution strategy for a specified number of generations
    void evolve(int numGenerations, double stopFitness);
}
