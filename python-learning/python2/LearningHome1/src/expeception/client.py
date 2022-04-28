'''.

Created on Jul 29, 2013

@author: manny01
'''
import exceptions
import traceback 
import main

main_class = main.main()

try:
    print (main_class.div(10, 0))
except exceptions.ArithmeticError:
    print "Error in Value"
except Exception as E:
    print traceback.print_stack(E)