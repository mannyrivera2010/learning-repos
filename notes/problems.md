# Problems
## 1

Problem:  
You have a piece of text, find the number of occurrences per character.
The output should be ordered ascending by the character.

Code should be written so that it is easy to run over multiple inputs.

```
string_1 = '1258641a28755549631bkmmmikdika'
```
Console output should be
```
The value 1 has 3 occurrences
The value 2 has 2 occurrences
The value 3 has 1 occurrences
The value 4 has 2 occurrences
The value 5 has 4 occurrences
The value 6 has 2 occurrences
The value 7 has 1 occurrences
The value 8 has 2 occurrences
The value 9 has 1 occurrences
The value a has 2 occurrences
The value b has 1 occurrences
The value d has 1 occurrences
The value i has 2 occurrences
The value k has 3 occurrences
The value m has 3 occurrences
```

Hint:
```
def print_occurrences(input_string):
    # logic 
    
    
string_1 = '1258641a28755549631bkmmmikdika'
print_occurrences(string_1)
```

## 2

Hint:
```
def upper_case_first_letter(input_string):
    # logic 
    
    
a = 'i want to go to the store'
a_t = upper_case_first_letter(string_1)

print(a_t)
I Want to Go to The Store
```

## 3
Write a method to find number of employees based on role and company name.    
Write a method to find employee names for a particular role in all companies.

```
<?xml version="1.0" encoding="UTF-8"?>
<EmployeeDW>
<Company name="ABCD">
<Employee role="SE">Emp1</Employee>
<Employee role="SE">Emp2</Employee>
<Employee role="TL">Emp3</Employee>
<Employee role="SE">Emp4</Employee>
<Employee role="SSE">Emp11</Employee>
<Employee role="SE">Emp22</Employee>
<Employee role="PM">Emp33</Employee>
<Employee role="SE">Emp44</Employee>
</Company>
<Company name="XYZ">
<Employee role="SE">Emp111</Employee>
<Employee role="SSE">Emp22</Employee>
<Employee role="TL">Emp3</Employee>
<Employee role="SE">Emp4</Employee>
<Employee role="SSE">Emp11</Employee>
<Employee role="PD">Emp22</Employee>
<Employee role="PM">Emp33</Employee>
<Employee role="BA">Emp44</Employee>
</Company>
</EmployeeDW>
```
