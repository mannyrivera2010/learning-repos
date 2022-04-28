'''
Created on Mar 28, 2013

@author: erivera
'''
import unittest
from MenuItem import MenuItem

class MenuItemTest(unittest.TestCase):    
    #MenuItem1=MenuItem();  #java = Private Static variable
    
    def setUp(self):
        self.menuitem1 = MenuItem("Chicken",1.55)

    def tearDown(self):
        pass

    def testName(self):
        pass
    
    def test__str__(self):
        StringTest="Chicken\t1.55"
        self.assertEqual(StringTest,str(self.menuitem1))
        
    def testgetName(self):
        StringTest="Chicken"
        self.assertEqual(StringTest, self.menuitem1.getName())
    
    def testgetPrice(self):
        FloatNumber=1.55;
        self.assertEqual(FloatNumber,self.menuitem1.getPrice())
        
if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()
    