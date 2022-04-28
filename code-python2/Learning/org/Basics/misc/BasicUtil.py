'''
Created on Apr 1, 2013

@author: erivera
'''

    
    
class BasicUtil(object):
    '''
    classdocs
    '''


    def __init__(self):
        '''
        Constructor
        '''
        pass
        
    def sortList(self,inputlist):
        return inputlist.sort();
    
    def upperCaseList(self,inputList):
        listOut=[]
        for cur in inputList:
            cur= cur.upper()
            listOut.append(cur);
                        
        return listOut
        
    def sumList(self,inputList):
        sum=0
        
        for cur in inputList:
            sum+=cur
        
        return sum
    
    def avgList(self,inputList):
        sum=0
        count=float(len(inputList))
        
        for cur in inputList:
            sum+=cur
            
        if count==0:
            return None
        else:
            return sum/count