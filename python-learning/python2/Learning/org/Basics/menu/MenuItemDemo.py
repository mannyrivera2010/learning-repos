'''
Created on Mar 28, 2013

@author: erivera
'''

import MenuItem

list1=[];

Chicken=MenuItem.MenuItem("Chicken",1.55);
print(Chicken)


print("---------")
list1.append(Chicken);
list1.append(MenuItem.MenuItem("Rice",1.80))
list1.append(MenuItem.MenuItem("Rice with Chicken",2.80))
list1.append(MenuItem.MenuItem("Rice with Beef",3.80))
list1.append(MenuItem.MenuItem("Rice with Vegs",5.80))
list1.append(MenuItem.MenuItem())
list1.append(MenuItem.MenuItem("Free"))

for s in list1:
    print s
    

