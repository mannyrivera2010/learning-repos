'''
Created on Apr 2, 2013

@author: erivera
'''
import hashlib

class checksums(object):
    @staticmethod
    def md5dig(inputStr):
        return str(hashlib.md5(inputStr).hexdigest()).upper()
    
    @staticmethod
    def sha512dig(inputStr):        
        return str(hashlib.sha512(inputStr).hexdigest()).upper()
    
    @staticmethod
    def md5Checksum(filePath):
        fh = open(filePath, 'rb')
        m = hashlib.md5()
        while True:
            data = fh.read(8192)
            #print "*" + str(data)
            if not data:
                break
            m.update(data)
            
        return str(m.hexdigest()).upper()
    
    @staticmethod
    def sha512Checksum(filePath):
        fh = open(filePath, 'rb')
        m = hashlib.sha512()
        while True:
            data = fh.read(8192)
            #print "*" + str(data)
            if not data:
                break
            m.update(data)
            
        return str(m.hexdigest()).upper()


if __name__ == '__main__':
    pass
    print checksums.md5Checksum("test.txt")
    
    print "-----------------------------"
    print checksums.sha512dig("This is a test")
    
    
    