'''
Created on Apr 2, 2013

@author: erivera
'''
from obfuscate import Vigenere
from obfuscate import rot47
from obfuscate import Chaff
from hashlib import md5
import org.shared.util_checksums as util_checksums
import os

class VigenereRotException(Exception):
    def __init__(self, code):
        self.code = code
    def __str__(self):
        return repr(self.code)

class VigenereRot(object):
    def __init__(self):
        pass
        
    def __md5dig(self,inputStr):
        return util_checksums.md5dig(inputStr)
    
    def encrypt(self,plaintext,keyinput):
        
        txttemp=plaintext;
        
        for i in range(1,4):
            txttemp=Vigenere.encrypt(rot47(txttemp), self.__md5dig(str(keyinput)+str(i)))
            
        return txttemp
    
    def decrypt(self,ciphertext, keyinput):
        txttemp=ciphertext;
        
        for i in range(3,0,-1):
            txttemp=rot47(Vigenere.decrypt(txttemp, self.__md5dig(str(keyinput)+str(i))))
            
        return txttemp
    
    def encrypt_file(self,input_filename,output_filename,inputkey):
        if(input_filename == output_filename):
            raise VigenereRotException("input and output filename can't be the same")
        
        file_read= open(input_filename,'r') 
        file_write= open(output_filename,'w')
       
        
        ##with open(input_filename,'r') as file_read:
        ##    with open(output_filename,'w') as file_write:
        for line in file_read:   
            enc=self.encrypt(line,inputkey)
            file_write.write(enc)

            
        file_read.close()
        file_write.close()
       
        
    def decrypt_file(self,input_filename,output_filename,inputkey):
        if(input_filename == output_filename):
            raise VigenereRotException("input and output filename can't be the same")
       
        #chaff = Chaff(width=2, stream='.'*10000)

        fileRead = open(input_filename,'r')
        fileWrite = open(output_filename,'w')

        for line in fileRead:    
            #unpaddedline=chaff.unpad(line,key=inputkey)
            enc=self.decrypt(line,inputkey)
            fileWrite.write(enc)
            
        fileRead.close()
        fileWrite.close()
        
#############################
if __name__ == "__main__":
    Vig1=VigenereRot()
    
    plaintextOrg="Hello World! This is python"
    keyOrg="keypass"
    
    print "Orginal   \t" + plaintextOrg
    ciphertxt = Vig1.encrypt(plaintextOrg, keyOrg);
    print "Encrypted\t" + ciphertxt
    decryptedtxt = Vig1.decrypt(ciphertxt, keyOrg)
    print "Decrypted\t" + decryptedtxt
    
    print "---------------"
    
    Vig1.encrypt_file("../file/data/foo.org.txt", "../file/data/foo.en.txt","key")
    print "foo.org.txt\t" + str(util_checksums.md5Checksum("../file/data/foo.org.txt"))
    print "foo.en.txt\t" + str(util_checksums.md5Checksum("../file/data/foo.en.txt"))
    Vig1.decrypt_file("../file/data/foo.en.txt","../file/data/foo.de.txt","key")
    print "foo.de.txt\t" + str(util_checksums.md5Checksum("../file/data/foo.de.txt"))
    print "\nfoo.org.txt and foo.de.txt should have the same md5"
    print "Equals="+str(util_checksums.md5Checksum("../file/data/foo.org.txt")==util_checksums.md5Checksum("../file/data/foo.de.txt"))
    
    print "---------------"
      
    chaff = Chaff(width=2, stream='.'*10000)
    padded=chaff.pad('message45454 jh', key='ROSEBUD')
    print padded
    

    it = chaff.unpad("..m..e...ss...age.4.5454 ...j.h.", 'ROSEBUD')
    print it
