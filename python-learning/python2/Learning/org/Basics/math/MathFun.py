'''
Created on Mar 28, 2013

@author: erivera
'''

class SimpleMath(object):
    '''
    classdocs
    '''
    def __init__(self):
        '''
        Constructor
        '''
        pass
    
    def sum(self,x,y):
        '''
        Add numbers x and y
        '''
        return x+y
    
    def mult(self,x,y):
        '''
        Multiply numbers x and y
        '''
        return x*y
    
    def div(self,x,y):
        if not float(x) or not float(y):
            #print "x:" + str(x) + "\t" + "y:" + str(y)
            raise RuntimeError("Error Message-X or Y can't be zero")
            
        return float(x)/float(y)
    
    if __name__ == "__main__":
        print("MAIN")
    
    
        