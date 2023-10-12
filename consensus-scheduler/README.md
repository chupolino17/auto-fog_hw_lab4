# consensus-scheduler

The tool reads analyzation result that produced consensus-analyzer, and based on the scheduling strategy sends a
transaction for confirmation. As results file can contain multiple models, one needs to choose one for sending a
transaction. Scheduler strategy uses different models choice:

* model with a minimal number of expected messages
* model with a max probability of confirmation
* model specified by id
* model specified by probability/expected messages pair

A user shall implement SenderService interface. There shall be a logic for sending transaction, and scheduler
properties. Then, provider SenderService to SendingConfirmationService and choose the method to send based on scheduler
strategy. One can find examples in SendingConfirmationServiceImplTest.

As an output SendingConfirmationService writes a log with sending transaction history and returns a boolean value.
Return value indicates a success of a transaction confirmation.

TODO:

* timeout for sending: if the message was sent some number of times to the same org, then go further
* waiting time for backward transitions
* take the closest number to given probability message pair
* make an example with hyperledger