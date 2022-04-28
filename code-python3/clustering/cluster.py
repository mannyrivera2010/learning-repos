
import io
import pprint

input_string_1 = """6,10,12
1,2
3,4
10,5
8,20
1,4
25,26,27
28,29,30
27,30,31
31,28
"""


def strings_to_list_set(input_string):
    output_list = []

    for raw_line in io.StringIO(input_string):
        line_set = set(raw_line.strip().split(','))
        output_list.append(line_set)

    return output_list

input_data = strings_to_list_set(input_string_1)

"""
[{'12', '6', '10'},
 {'1', '2'},
 {'3', '4'},
 {'10', '5'},
 {'8', '20'},
 {'1', '4'},
 {'26', '27', '25'},
 {'28', '29', '30'},
 {'31', '27', '30'},
 {'28', '31'}]
"""
pprint.pprint(input_data)



def clutering(left_list, right_list, out=True, level=0):
    result = []


    #
    # if out:
    #     print('{0} ----before out----left_list: {1}'.format('\t'*level, left_list))
    #     print('{0} ----before out----right_list: {1}'.format('\t'*level, right_list))
    # else:
    #     print('{0} ----before in----left_list: {1}'.format('\t'*level, left_list))
    #     print('{0} ----before in----right_list: {1}'.format('\t'*level, right_list))

    while len(left_list) != 0 and len(right_list) != 0:
        if left_list[0].intersection(right_list[0]):
            result.append(left_list[0].union(right_list[0]))
            del left_list[0]
            del right_list[0]
        else:
            result.append(left_list[0])
            result.append(right_list[0])
            del left_list[0]
            del right_list[0]

    if len(left_list) == 0:
        result += right_list
    else:
        result += left_list

    # if out:
    #     print('{0} ----after out----left_list: {1}'.format('\t'*level, left_list))
    #     print('{0} ----after out----right_list: {1}'.format('\t'*level, right_list))
    #     print('{0} ----after out----results: {1}'.format('\t'*level, result))
    # else:
    #     print('{0} ----after in----left_list: {1}'.format('\t'*level, left_list))
    #     print('{0} ----after in----right_list: {1}'.format('\t'*level, right_list))
    #     print('{0} ----after in----results: {1}'.format('\t'*level, result))

    if out:
        middle2 = int(len(result)/2)
        left2 = mergeclutering(result[:middle2])
        right2 = mergeclutering(result[middle2:])
        return clutering(left2, right2, out=False, level=level+1)
    else:
        print(str('\t'*level)+str(level)+' - '+str(result))
        return result


def mergeclutering(input_list, level=1):
    if len(input_list) < 2:
        return input_list

    middle = int(len(input_list)/2)

    left_current_list = input_list[:middle]
    right_current_list = input_list[middle:]



    left = mergeclutering(left_current_list, level=level+1)
    right = mergeclutering(right_current_list, level=level+1)
    print('{0}{1} left: {2}'.format('\t' * level, level, left_current_list))
    print('{0}{1} right: {2}'.format('\t' * level, level, right_current_list))
    return clutering(left, right, level=level)

results = mergeclutering(input_data)
print('---')
pprint.pprint(results)
#print("=== {0}".format(results))

# def mergesort2(list):
#     if len(list) < 2:
#         return list
#
#     middle = int(len(list)/2)
#     left = mergesort2(list[:middle])
#     right = mergesort2(list[middle:])
#
#     return merge2(left, right)
