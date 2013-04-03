'''
Created on Apr 2, 2013

@author: erivera
'''
import hashlib

def md5dig(inputStr):
    return str(hashlib.md5(inputStr).hexdigest()).upper()

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


if __name__ == '__main__':
    pass
    print md5Checksum("test.txt")
    #print md5dig("This is a test")
    
    
    