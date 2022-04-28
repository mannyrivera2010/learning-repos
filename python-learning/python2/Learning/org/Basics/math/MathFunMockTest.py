'''
Created on Apr 1, 2013

@author: erivera
'''


from mock import MagicMock, call, patch


mock = MagicMock(side_effect=Exception("error"))

mock()



