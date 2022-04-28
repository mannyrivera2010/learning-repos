'''
Created on Apr 10, 2013

@author: erivera
'''
import unittest
from MyClass import AddingClass

class Test(unittest.TestCase):


    def testName(self):
        pass


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    #unittest.main()
    add=AddingClass()
    
    print add.add(3, 4);
    
    