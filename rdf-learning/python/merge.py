import glob

file_list = glob.glob("data/*.csv")

for file_name in file_list:
    with open(file_name, 'r') as open_file:

        for inner_line in open_file:
            if "gender" not in inner_line:
                print(inner_line.strip())
