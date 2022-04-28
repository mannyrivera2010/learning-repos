import random
import time

def get_unsorted_list(size):
    """
    Initialize code
    """
    return [random.randint(0,1000000) for i in range(size)]


def merge2(left, right):
    if not len(left) or not len(right):
        return left or right

    result = []
    i, j = 0, 0
    while (len(result) < len(left) + len(right)):
        if left[i] < right[j]:
            result.append(left[i])
            i+= 1
        else:
            result.append(right[j])
            j+= 1
        if i == len(left) or j == len(right):
            result.extend(left[i:] or right[j:])
            break
    return result


def mergesort2(list):
    if len(list) < 2:
        return list

    middle = int(len(list)/2)
    left = mergesort2(list[:middle])
    right = mergesort2(list[middle:])

    return merge2(left, right)

if __name__ == "__main__":
    my_randoms = get_unsorted_list(500000)


    beg_ts = time.time()
    sorted(my_randoms)
    end_ts = time.time()
    print("builtin sorted py3: elapsed time: %f" % (end_ts - beg_ts))

    beg_ts = time.time()
    mergesort2(my_randoms)
    end_ts = time.time()
    print("Merge Sort 2: elapsed time: %f" % (end_ts - beg_ts))
