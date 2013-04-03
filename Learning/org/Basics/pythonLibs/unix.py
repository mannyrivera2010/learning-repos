'''
Created on Apr 1, 2013

@author: erivera
'''
import grp
for gp in grp.getgrall():
    print str(gp.gr_name) + "\t" + str(gp.gr_gid)

print "---------------------"

import crypt
print crypt.crypt("password", "3kd");

print "---------------------"

import pwd

for pg in pwd.getpwall():
    print pg
    