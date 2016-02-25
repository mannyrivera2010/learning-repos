'''
Created on Mar 28, 2013

@author: erivera
'''
from BasicUtil import BasicUtil
import math

BasicUtilObj=BasicUtil()

if __name__ == '__main__':
    pass


#Hello World
print("hello")

f1=float(1.3)


print(str(f1) + " is integer:" + str(f1.is_integer()))

print("5".isalpha() or int("4")>8)

even_numbers = [2,4,6,8]
odd_numbers = (1,3,5,7)

print type(even_numbers)
print type(odd_numbers)

all_numbers = list(odd_numbers) + even_numbers
print type(all_numbers)


print(all_numbers)


for cur in all_numbers:
    print cur
    
print BasicUtilObj.sumList(all_numbers)

print "--------" 
listB=[100,81]
print BasicUtilObj.sumList(listB)
avgListB=BasicUtilObj.avgList(listB)
print type(avgListB)==type(float()) #compare Types
print avgListB
print round(avgListB,3)
print float("%.3f" % avgListB)

print "--------"
listC=["Hello","lower"]
print listC
listC=BasicUtilObj.upperCaseList(listC)
print listC
print len(listC)
print len(str(457))
print "--------"
print math.factorial(10)

print cmp("hello", "Hello".lower())
print cmp("A", "B") # -1 > A is less than B
print cmp("C", "B") # 1 > C is greater than B
print cmp("by", "by") # 0 > Same

print "--------"
Obj1="obj1";
print id(Obj1)
Obj2=Obj1
print id(Obj2)
Obj2="obj1";
print id(Obj2)
print "--------"
 



