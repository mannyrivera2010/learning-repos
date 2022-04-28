import requests
from requests.packages.urllib3.exceptions import InsecureRequestWarning

requests.packages.urllib3.disable_warnings(InsecureRequestWarning)

from urllib.parse import quote


def encode(input):
    input = input.replace(":", "%3A")
    input = input.replace("/", "%2F")
    input = input.replace("#", "%23")
    return input


def extract_title_id(ontologies_raw):
    return dict([[i.get("http://purl.org/dc/terms/title")[0]["@value"],
                        i.get("http://mobi.com/ontologies/ontology-editor#ontologyIRI")[0]["@id"]] for i in ontologies_raw])


class MobiClient:

    def __init__(self, host , username, password, ssl_verify=False):
        self.host = host
        self.username = username
        self.password = password
        self.ssl_verify = ssl_verify

    def set_mobi_web_token(self):
        uri = "{}/mobirest/session?username={}&password={}".format(self.host, self.username, self.password)
        response = requests.post(uri, verify=self.ssl_verify)

        if response.status_code != 200:
            raise Exception("Unauthorized")

        self.mobi_web_token = response.cookies['mobi_web_token']

    def get_ontologies(self):
        catalog_id = encode("http://mobi.com/catalog-local")
        sort = encode("http://purl.org/dc/terms/title")
        type = encode("http://mobi.com/ontologies/ontology-editor#OntologyRecord")
        uri_post = "catalogs/{}/records?ascending=true&limit=10&offset=0&sort={}&type={}".format(catalog_id, sort, type)
        uri = "{}/mobirest/{}".format(self.host, uri_post)
        headers = {'content-type': 'application/json'}
        cookies = {'mobi_web_token': self.mobi_web_token}
        response = requests.get(uri, verify=self.ssl_verify,  headers=headers, cookies=cookies)

        if response.status_code != 200:
            raise Exception("Error")

        return response.json()

    def create_ontology(self, ontology_name):
        uri = "{}/mobirest/ontologies?title={}".format(self.host, ontology_name)
        headers = {'content-type': 'application/json'}
        cookies = {'mobi_web_token': self.mobi_web_token}

        payload = {
            "@id":"https://mobi.com/ontologies/2/2020/{}".format(ontology_name), # id can be custom
            "@type":["http://www.w3.org/2002/07/owl#Ontology"],
            "http://purl.org/dc/terms/title":[{"@value":ontology_name}]
        }

        response = requests.post(uri, verify=self.ssl_verify, headers=headers, cookies=cookies, json=payload)

        if response.status_code != 201:
            raise Exception(response.text)

        return response.json()


mobi_client = MobiClient("https://home.earasoft.com:8443", "admin", "pizza123!")
mobi_client.set_mobi_web_token()

ontologies_dict = extract_title_id(mobi_client.get_ontologies())
print(ontologies_dict)
# {}

# create 1st ontology
ontology_1 = "family"

if ontology_1 not in ontologies_dict:
    mobi_client.create_ontology(ontology_1)

# check
ontologies_dict = extract_title_id(mobi_client.get_ontologies())
print(ontologies_dict)
# {'family': 'https://mobi.com/ontologies/2/2020/family'}

# create 2st ontology
ontology_2 = "exampleOntology"

if ontology_2 not in ontologies_dict:
    mobi_client.create_ontology(ontology_2)

# check
ontologies_dict = extract_title_id(mobi_client.get_ontologies())
print(ontologies_dict)
# {'exampleOntology': 'https://mobi.com/ontologies/2/2020/exampleOntology', 'family': 'https://mobi.com/ontologies/2/2020/family'}
