import gzip
import glob
import time
import json

import requests

INDEX_NAME = "pubmed_author_json"
INDEX_TYPE = "_doc"
API_ENDPOINT = "http://127.0.0.1:9200/{}/_bulk".format(INDEX_NAME)


def document_type(index, type, doc_id):
    return { "index" : { "_index" : index, "_type" : type, "_id" : doc_id } }


def flush(bulk_buffer):
    headers = {'content-type': 'application/x-ndjson'}
    response = requests.post(url=API_ENDPOINT, data="".join(bulk_buffer), headers=headers)
    return response

file_count = 0

files = glob.glob("pubmed*.json.gz")

for current_file in files:
    file_count+=1

    print(current_file)

    batch_counter = 0
    #'pubmed-00000-of-00010.pmidAuthor.txt.gz'
    with gzip.open(current_file, 'rb') as file_open_handler:
        bulk_buffer = []

        record_counter = 0
        for line in file_open_handler:
            line = line.decode("utf-8")
            # skip line
            if len(line) == 0:
                continue

            record_counter += 1

            line_json = json.loads(line)

            pmid = line_json["medlineCitation"]["PMID"]

            # print(pmid)
            # print(authors_json)
            # print('-------')

            bulk_buffer.append("{}\n".format(json.dumps(document_type(INDEX_NAME, "_doc", pmid))))
            bulk_buffer.append("{}\n".format(json.dumps(line_json)))

            if len(bulk_buffer) == 300 * 2:
                batch_counter += 1
                print("{:,}\t{:,}\t{:,}".format(file_count, batch_counter, record_counter))
                response = flush(bulk_buffer)
                # print(response.json())

                bulk_buffer=[]

        response = flush(bulk_buffer)



# POST _bulk
# { "index" : { "_index" : "test", "_type" : "_doc", "_id" : "1" } }
# { "field1" : "value1" }
# { "delete" : { "_index" : "test", "_type" : "_doc", "_id" : "2" } }
# { "create" : { "_index" : "test", "_type" : "_doc", "_id" : "3" } }
# { "field1" : "value3" }
# { "update" : {"_id" : "1", "_type" : "_doc", "_index" : "test"} }
# { "doc" : {"field2" : "value2"} }
