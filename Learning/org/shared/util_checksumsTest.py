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
        with patch("__builtin__.open") as mock_open:
            
            read_file=StringIOWrapper()
            read_file.write("Data\nLine2\nLine3");
            read_file.seek(0)
             
            mock_open.side_effect=[read_file]
            
            csum=util_checksums.md5Checksum("input.txt")
                        
            #print csum
            self.assertEqual("990F51B299DA10ADE9BFF35416DFE8D4",csum,"File 1")


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()