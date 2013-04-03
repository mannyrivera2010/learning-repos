'''
Created on Apr 2, 2013

@author: erivera
'''

import StringIO

import fileinput


if __name__ == '__main__':
    #EXAMPLE 1
    output = StringIO.StringIO()
    output.write('First line.\n')
    output.write('Second Line')
    
    # Retrieve file contents -- this will be
    # 'First line.\nSecond line.\n'
    contents = output.getvalue()
    print contents
    

    # Close object and discard memory buffer --
    # .getvalue() will now raise an exception.
       
    output.close()
    
    print "------------"
    print type(output)
    
    f = open(output, 'r')
     


    
    
    