# Solutions
## 1

```
string_1 = '1258641a28755549631bkmmmikdika'

def count_value(input_string):
    d = {}
    for i in input_string:
        if i in d:
            d[i] = d[i] + 1
        else:
            d[i] = 1
    for k in sorted(d.keys()):
        print('The value {} has {} occurrences'.format(k, d[k]))

count_value(string_1)
```
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

## 2
```
def tranform(s):
  return ' '.join([(s if s in ['a','to'] else s.title()) for s in s.split(' ')])

a = 'i want to go to the store'

a_t = tranform(a)
print(a_t)
```

