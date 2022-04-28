'''
Created on Mar 28, 2013

@author: erivera
'''
import unittest
from MathFun import SimpleMath

class Test(unittest.TestCase):

    def setUp(self):
        self.SimpleMathObj=SimpleMath()

    def tearDown(self):
        pass

    def testSum(self):
        value=self.SimpleMathObj.sum(5, 5)
        self.assertEqual(10, value, "5+5=10")
    
    def testMult(self):
        value=self.SimpleMathObj.mult(2, 50)
        self.assertEqual(100, value, "2*50=100")
        
    def testdiv(self):
        #Subtest 1
        throw=False
        try:
            value=self.SimpleMathObj.div(10, 100) 
        except Exception, e:
            if str(e).find("X or Y can't be zero"):
                throw=True
            
        self.assertEqual(.1, value, "10/100=.1")
        self.assertEqual(False, throw, "Check Throw")
        
        #Subtest 2
        throw=False
        try:
            value=self.SimpleMathObj.div(100, 10) 
        except Exception, e:
            if str(e).find("X or Y can't be zero"):
                throw=True
            
        self.assertEqual(10, value, "100/10=")
        self.assertEqual(False, throw, "Check Throw")
        
        #Subtest 3
        throw=False
        try:
            value=self.SimpleMathObj.div(10, 0) 
        except Exception, e:
            if str(e).find("X or Y can't be zero"):
                throw=True
            
        self.assertEqual(True, throw, "Check Throw")
        
        #Subtest 4
        throw=False
        try:
            value=self.SimpleMathObj.div(0, 100) 
        except Exception, e:
            if str(e).find("X or Y can't be zero"):
                throw=True
            
        self.assertEqual(True, throw, "Check Throw")
        

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()