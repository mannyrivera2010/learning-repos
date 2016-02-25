'''
Created on Apr 1, 2013

@author: erivera
'''

import hashlib

def sha512(input):
    return str(hashlib.sha512(input).hexdigest()).upper()

hash1=sha512("String 1")
hash2=sha512("String 1")
print hash1
print hash2
print hash1 == hash2