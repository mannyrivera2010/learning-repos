'''
Created on Mar 29, 2013

@author: erivera
'''
from MathFun import SimpleMath

SimpleMathObj=SimpleMath()

x = raw_input('X > ')
y = raw_input('Y > ')

value=None

throw=False
try:
    value=SimpleMathObj.div(x, y) 
except Exception, e:
    print e
    throw=True
    
print value
            