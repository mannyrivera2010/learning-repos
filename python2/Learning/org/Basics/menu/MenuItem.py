'''
Created on Mar 28, 2013

@author: erivera
'''

class MenuItem(object):
    '''
    This class represents a Item in a Menu
    '''    
    #__name=""
    #__price=0.0
    
    def __init__(self,strName="null",flPrice=0.0):
        '''
        Constructor
        '''
        self.__name=strName
        self.__price=flPrice
        
        
    def __str__(self):
        '''
        Prints out the Name and Price with a tab
        '''
        return str(self.__Name) + "\t" + str(self.__Price)
    
    @property
    def name(self):
        return self.__Name
    
    @property
    def price(self):
        return self.__Price    