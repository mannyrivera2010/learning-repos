'''
Created on Jul 29, 2013

@author: manny01
'''

import sub

class main(object):
    '''
    classdocs
    '''
    def __getattr__(self,string):
        if string == 'subA':
            self.subA=sub.subA()
            return self.subA

    def div(self,a,b):
        return self.subA.div(a, b)
        