"""
https://docs.python.org/3/library/multiprocessing.html
"""

import io
import gzip
import os

import time
import json

import requests

import multiprocessing
from multiprocessing import Pool
from multiprocessing import Process
from multiprocessing import Queue

import traceback

from google.cloud import storage
from google.cloud.storage.blob import Blob
from google.cloud.storage.bucket import Bucket

INDEX_NAME = "pubmed_author_json"
INDEX_TYPE = "_doc"
API_ENDPOINT = "http://127.0.0.1:9200/{}/_bulk".format(INDEX_NAME)

BUCKET_NAME = "pubmed-output"


def document_type(index, type, doc_id):
    return { "index" : { "_index" : index, "_type" : type, "_id" : doc_id } }


def flush(bulk_buffer):
    headers = {'content-type': 'application/x-ndjson'}
    response = requests.post(url=API_ENDPOINT, data="".join(bulk_buffer), headers=headers)
    return response


def blob_task(status_queue, bucket_name, blob_name, file_count):
    storage_client = storage.Client()

    bucket = Bucket(storage_client, name=bucket_name)
    blob = Blob(blob_name, bucket)

    print("{}: start".format(blob_name))

    blob_stream = io.BytesIO()
    blob.download_to_file(blob_stream)
    # reset steam to position 0
    blob_stream.seek(0)

    record_counter = 0
    batch_counter = 0

    bulk_buffer = []

    with gzip.open(blob_stream, 'rb') as file_open_handler:
        for line in file_open_handler:
            line = line.decode("utf-8")
            line = line.strip()

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

                status_queue.put("{}\t{:,}\t{:,}\t{:,}".format(blob_name, file_count, batch_counter, record_counter))

                response = flush(bulk_buffer)
                # print(response.json())

                bulk_buffer=[]

    response = flush(bulk_buffer)

    print("{}: end".format(blob_name))

    return "None"


if __name__ == '__main__':
    cpu_count = multiprocessing.cpu_count()
    print("cpu_count:{}".format(cpu_count))
    pool = Pool(cpu_count)

    # Instantiates a client
    storage_client = storage.Client()

    # Note: Client.list_blobs requires at least package version 1.17.0.
    blobs = storage_client.list_blobs(BUCKET_NAME, prefix="pubmed")

    file_count = 0

    blob_task_dict = {}

    multiprocessing_manager = multiprocessing.Manager()
    # status_queue: used to get when es batches are done
    status_queue = multiprocessing_manager.Queue()

    for blob in blobs:
        blob_name = blob.name

        # wait while current tasks in blob_task_dict is less than cpu_count
        while len(blob_task_dict) >= cpu_count:
            time.sleep(100.0 / 1000.0)
            print("find ready tasks [{}]: {}".format(file_count, str([key for key in blob_task_dict])))

            ready_keys = []
            for key in blob_task_dict:
                async_result = blob_task_dict[key]

                if async_result.ready():
                    ready_keys.append(key)

                    if not async_result.successful():
                        print("key:{} was not successful".format(key))
                        try:
                            print(async_result.get())
                        except Exception as e:
                            traceback.print_exc()

            # delete async results that finished
            for key in ready_keys:
                del blob_task_dict[key]

            # empty status queue
            print("status_queue_qsize: {}".format(status_queue.qsize()))
            while status_queue.qsize() > 0:
                record = status_queue.get()
                print(record)

        print("Adding {} to blob_task_dict".format(blob_name))
        file_count+=1
        res = pool.apply_async(blob_task, (status_queue, BUCKET_NAME, blob_name, file_count, ))      # runs in *only* one process
        blob_task_dict[blob_name] = res
