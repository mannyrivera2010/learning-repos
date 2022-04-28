'''
Created on Jul 29, 2013

@author: manny01
'''

import exceptions

class subA(object):
    '''
    classdocs
    '''
    
    def div(self,a,b):
        try:
            print a
            print b
            return float(float(a)/float(b)) 
        except exceptions.ValueError:
            raise exceptions.ArithmeticError("ehllo")
        except Exception as E:
            print 'hello'
            raise exceptions.ZeroDivisionError("hello")