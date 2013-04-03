'''
Created on Apr 1, 2013

@author: erivera
'''
import collections
import re

def occWords(StringInput):
    dictOut=dict()
    
    ListO=re.split(r'[,;  .?!]+', StringInput)
    
    for word in ListO:
        if word in dictOut:
            dictOut[word]=dictOut[word]+1
        else:
            dictOut[word]=1
            
            
    return collections.OrderedDict(sorted(dictOut.items()))


def occ(StringInput):
    dictOut={}
    
    for letter in StringInput:
        if letter in dictOut:
            dictOut[letter] = dictOut[letter]+1
        else:
            dictOut[letter] = 1
            
    return collections.OrderedDict(sorted(dictOut.items()))

dict1=dict()
dict1[1]="One"
dict1["One"]=1 

print dict1.get(1)
print dict1[1]
print type(dict1.get(1))==type(dict1[1])

for k in dict1.iterkeys():
    print k
    
print "-------------------"

dict2={"hello":"Hello","bye":"Bye"}
print dict2.get("hello")
print dict2.get("bon")

StringOcc="Hello my name is Manny, this is a test to check the how many time each letter repeats. Hello"

DictOcc=occ(StringOcc)

for k,v in DictOcc.iteritems():
    print "*" + str(k) + "*" + "-->" + str(v)


print "----------------"
DictWordOcc=occWords(StringOcc)

for k,v in DictWordOcc.iteritems():
    print "*" + str(k) + "*" + "-->" + str(v)
    
print "----------------"
Dict4=dict(hello=1,bye=2)
print Dict4
del Dict4["hello"]
print Dict4


