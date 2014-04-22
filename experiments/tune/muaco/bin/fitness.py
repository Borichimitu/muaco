from individual import Individual

class Fitness:
    def __init__(self, target):
        self.target = target
   
    def getFitness(self, individual):
        return sum([int(self.target[i] == individual.vector[i]) for i in range(len(self.target))])

    def getDesiredFitness(self):
        return len(self.target)
