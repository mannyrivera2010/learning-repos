'''
Created on Apr 3, 2013

@author: erivera
'''
import unittest
import util_checksums
import StringIO
from mock import MagicMock, patch

class StringIOWrapper(StringIO.StringIO):
    def close(self):
        pass
    
class Test(unittest.TestCase):

    def setUp(self):
        pass

    def tearDown(self):
        pass

    def test_md5dig(self):
        csum=util_checksums.md5dig("This is a test")
        self.assertEqual("CE114E4501D2F4E2DCEA3E17B546F339", csum, "test_md5dig test 1")
    
    def test_md5Checksum(self):
        pass


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()