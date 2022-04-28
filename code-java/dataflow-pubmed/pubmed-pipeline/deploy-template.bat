
set GOOGLE_APPLICATION_CREDENTIALS=%userprofile%\creds\dataflow-cred.json

mvn clean compile exec:java ^
-Dexec.mainClass=com.earasoft.beam.pubmed.PubmedDriver ^
-Dexec.args="--runner=DataflowRunner --project=cosc603riv-webwords --stagingLocation=gs://dataflow-123456789/staging/ --templateLocation=gs://dataflow-123456789/templates/pubmed --gcpTempLocation=gs://dataflow-123456789/temp/"
