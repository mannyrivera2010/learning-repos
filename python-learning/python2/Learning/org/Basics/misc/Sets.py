'''
Created on Apr 1, 2013

@author: erivera
'''

set1= set()
set1.add("hello")
set1.add("bye")

print "hela" in set1
print "hello" in set1

print "-----------"

Engineer=set(["tom","ben","kenny"])
Hr=set(["jenny","julia"])
Mgr=set(["tom","julia"])


EngMgr=set.intersection(Engineer,Mgr);
print list(EngMgr) 

allEmployees=set.union(Engineer,Hr,Mgr)
print list(allEmployees)
