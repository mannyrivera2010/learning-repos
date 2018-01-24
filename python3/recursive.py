

def print_debug(key, is_base_boolean, is_dict_boolean, is_list_boolean, has_key):
    print('--key:{}\tb:{}\td:{}\tl:{}\thk:{}--'.format(key,
                                                    is_base_boolean,
                                                    is_dict_boolean,
                                                    is_list_boolean,
                                                    has_key))


def shorthand_dict(input_object, key=''):
    is_base_boolean = isinstance(input_object, (int,str,float))
    is_dict_boolean = isinstance(input_object, dict)
    is_list_boolean = isinstance(input_object, list)
    has_key = True if key else False

    if is_list_boolean and not has_key:
        return [shorthand_dict(ob) for ob in input_object]
    elif is_list_boolean and has_key:
        return '[{}]'.format(','.join([shorthand_dict(ob) for ob in input_object]))
    elif is_dict_boolean:
        output = []
        sorted_key = sorted(input_object.keys())
        output = ['{}:{}'.format(key, shorthand_dict(input_object[key], key)) for key in sorted_key]
        return '({})'.format(','.join(output))
    elif is_base_boolean:
        return '{}'.format(input_object)


ex_1 = {'b':{'d':'d', 'c':{'f':'f','e':'e'}},'a':'a'}

ex_2 = [{'_score': {'Baseline': {'raw_score': 8.0, 'weight': 1.0},
                'Bookmark Collaborative Filtering': {'raw_score': 1.0, 'weight': 5.0},
                 '_sort_score': 13.0},
    'title': 'White Horse'}]

ex_3 = {'b':{'d':'d', 'c':[1,2,3]},'a':'a'}


import json

print(json.dumps(shorthand_dict(ex_1),indent=2))
# "(a:a,b:(c:(e:e,f:f),d:d))"
print(json.dumps(shorthand_dict(ex_2),indent=2))
# [
#   "(_score:(Baseline:(raw_score:8.0,weight:1.0),Bookmark Collaborative Filtering:(raw_score:1.0,weight:5.0),_sort_score:13.0),title:White Horse)"
# ]
print(json.dumps(shorthand_dict(ex_3),indent=2))
# "(a:a,b:(c:[1,2,3],d:d))"
