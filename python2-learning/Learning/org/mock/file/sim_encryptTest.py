'''
Created on Apr 2, 2013

@author: erivera
'''
import unittest
from mock import MagicMock, patch
from sim_encrypt import VigenereRot
from sim_encrypt import VigenereRotException
import StringIO

class StringIOWrapper(StringIO.StringIO):
    def close(self):
        pass
    
    
    
class VigenereRotTest(unittest.TestCase):

    def setUp(self):
        self.Vig1=VigenereRot();

    def tearDown(self):
        pass

    def test_encrypt(self):
        ciphertxt= self.Vig1.encrypt("Hello this is a test", "key");
        self.assertEqual("~;CE? F:;D 7H M N6G7",ciphertxt, "Encryption Test 1")
        
        ciphertxt= self.Vig1.encrypt("hello", "LongerKeyWithCase123");
        self.assertEqual("88?EF",ciphertxt, "Encryption Test 2")
        
        ciphertxt= self.Vig1.encrypt("hello\nbye", "LongerKeyWithCase123");
        self.assertEqual("88?EF\nH3:",ciphertxt, "Encryption Test 3")
        
        ciphertxt= self.Vig1.encrypt("", "KeyPassword");
        self.assertEqual("",ciphertxt, "Encryption Test 4")
        
    def test_decrypt(self):
        plaintxt= self.Vig1.decrypt("~;CE? F:;D 7H M N6G7", "key");
        self.assertEqual("Hello this is a test",plaintxt, "Decryption Test 1")
        
        plaintxt= self.Vig1.decrypt("88?EF", "LongerKeyWithCase123");
        self.assertEqual("hello",plaintxt, "Decryption Test 2")
        
        plaintxt= self.Vig1.decrypt("88?EF\nH3:", "LongerKeyWithCase123");
        self.assertEqual("hello\nbye", plaintxt, "Decryption Test 3")
        
        plaintxt= self.Vig1.decrypt("", "KeyPassword");
        self.assertEqual("",plaintxt, "Decryption Test 4")
    
    def test_encrypt_file(self):        
        with patch("__builtin__.open") as mock_open:
            
            read_file=StringIOWrapper()
            read_file.write("Test");
            
            read_file.seek(0)
             
            write_file=StringIOWrapper()
            mock_open.side_effect=[read_file, write_file]
            
            self.Vig1.encrypt_file("input.txt", "output.txt", "key")
            
            #print write_file.getvalue()
            self.assertEqual(");3I",write_file.getvalue(),"File 1")
            
    def test_encrypt_file_exception(self):        
        with patch("__builtin__.open") as mock_open:
            
            read_file=StringIOWrapper()
            read_file.write("Test");
            
            read_file.seek(0)
             
            write_file=StringIOWrapper()
            mock_open.side_effect=[read_file, write_file]
            
            
            thrown=False
            
            try:
                self.Vig1.encrypt_file("input.txt", "input.txt", "key")
            except VigenereRotException:
                thrown=True
            except Exception, e:
                self.fail('Not the ExpectedException:' + str(e))
            else:
                self.fail('ExpectedException not thrown')
                
                
            #print write_file.getvalue()
            self.assertEqual(True,thrown,"Encrypt File Exception Test")
      
    def test_decrypt_file(self):
        pass
        #self.fail("Need to Implement")

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()