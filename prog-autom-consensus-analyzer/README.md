# consensus-analyzer

This repo contains code for analyzation consensus protocols.
User provides set of consensus members/organizations, and for each organization the probability to accept 
transaction. One can take the probability from the historical data set. If there is not enough data, it's recommended
to used beta distribution - http://varianceexplained.org/r/empirical_bayes_baseball/
As a consensus condition user provides a boolean formula. Each member of the formula represents name if the consensus
member. Examples locate in resources.

The tool converts formula to DNF form and displays it before the computation. Further, the tool defines the probability 
that members reach the consensus. The tool works in two options:
* without backward transitions - just one round of confirmation
* with backward transition. If some member rejects the transaction, then transaction will be sent for confirmation
again to all members. The tool computes probability to reach the consensus for all possible combinations of backward
transitions.

In the output one gets the probability and expected number of messages for each model that corresponds to the unique 
combinations of backward transitions. A user chooses a proper combination of expected messages vs probability, and 
passes further the results file to consensus-scheduler. Also, tool creates a text representation of discrete-time
Markov chain for each model. One can see the analyzation result in results/results.json, and dtmc models in 
results/models.

## Installation instructions

Download a copy of PRISM and build it

* ``git clone https://github.com/prismmodelchecker/prism prism``
* ``cd prism/prism``
* ``make``


Download the ``consensus-analyzer`` repo and build the examples

* ``cd ../..``
* ``git clone https://github.com/1vanan/prog-autom-consensus-analyzer.git``
* ``cd consensus-analyzer``
* ``make``

The second part of the above assumes that PRISM is in a directory called ``prism`` one level up.
If you want to use a PRISM distribution located elsewhere, build like this:

* ``make PRISM_DIR=/some/copy/of/prism``

## Usage instructions
Go to the consensus-analyzer directory. Compile: run "make" command in this directory.

Run ``bin/run``with parameters: 
* path to configuration file
* path to the output file
* flag true if running with backward transitions, false otherwise

Configuration example: ./resources/cnf_simple.yaml

Run example: bin/run ./resources/cnf_simple.yaml ./results false
Run example with backward transitions: bin/run ./resources/cnf_simple.yaml ./results true

Configuration example with 2 distinct files for configuration and specification: 
* names of organizations and probabilities: ./resources/cnf-1/cnf_1_properties.yaml
* consensus condition: ./resources/cnf-1/cnf_2_spec.yaml

Run example: bin/run ./resources/cnf-1/cnf_1_properties.yaml  ./resources/cnf-1/cnf_simple.yaml ./results false
Run example with backward transitions: bin/run ./resources/cnf-1/cnf_1_properties.yaml  ./resources/cnf-1/cnf_1_spec.yaml ./results true

TODO:
* how to add prism precision
* figure out how to connect prism with maven and build the project as a single jar with fat jar
