import random

class Individual:
    def __init__(self, vector):
        self.vector = vector

class Mutator:
    def __init__(self, probability):
        self.probability = probability

    def mutate(self, individual):
        numbers = individual.vector
        i = random.randint(0, len(numbers) - 1)
        numbers[i] = numbers[i] ^ 1
  
        for j in range(len(numbers)):
            if j == i:
                continue
            if random.random() < self.probability:
                numbers[j] = numbers[j] ^ 1
        return Individual(numbers)    
