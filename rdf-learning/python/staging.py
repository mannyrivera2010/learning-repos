
import requests
from requests_toolbelt.multipart.encoder import MultipartEncoder
from requests.packages.urllib3.exceptions import InsecureRequestWarning

requests.packages.urllib3.disable_warnings(InsecureRequestWarning)

# 3-tuple ``('filename', fileobj, 'content_type')``
multipart_form_data = {
    'additions': ('additions', '[{"@id":"https://mobi.com/ontologies/2/2020/family#Person","@type":["http://www.w3.org/2002/07/owl#Class"],"http://purl.org/dc/terms/title":[{"@value":"person"}]}]', "form-data"),

}


# https://github.com/inovexcorp/mobi/blob/master/com.mobi.catalog.rest/src/main/java/com/mobi/catalog/rest/CatalogRest.java#L2042
r = requests.post(
    "https://home.earasoft.com:8443/mobirest/catalogs/http%3A%2F%2Fmobi.com%2Fcatalog-local/records/https%3A%2F%2Fmobi.com%2Frecords%230916018b-f9d4-41dd-ba48-b5e7a53bbf1a/in-progress-commit",
    files=multipart_form_data,
    headers={'Content-Type': "multipart/form-data; boundary=----WebKitFormBoundaryqZCFyPXb94JYiBvp"},
    cookies= {'mobi_web_token': "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInNjb3BlIjoic2VsZiBcLyoiLCJpc3MiOiJodHRwOlwvXC9tb2JpLmNvbVwvIiwiZXhwIjoxNTgyMTUyODY0fQ.fz3GHmO_3nR6op_NfY8Cm-VdDMXgiGOHaHSMlg8q1B4"},
    verify=False
)

print(r.text)
