import gzip
import glob
import argparse
import sys

def merge():

    with gzip.open("ngram.merged.txt.gz", 'w') as file_write_handler:

        files = glob.glob("pubmed*.txt.gz")
        for current_file in files:
            print("processing: {}".format(current_file))
            with gzip.open(current_file, 'rb') as file_open_handler:
                for line in file_open_handler:

                    file_write_handler.write(line)

def sort_merged_file():
    pass

if __name__ == '__main__':
    print('Number of arguments:', len(sys.argv), 'arguments.')
    print('Argument List:', str(sys.argv))


    lista = []

    with gzip.open("ngram.merged_sorted.gz", 'rb') as file_open_handler:
        for line in file_open_handler:
            line_split = line.decode("utf-8").strip().split("\t")

            lista.append([line_split[0], int(line_split[1])])

    lista = sorted(lista, key=lambda line_split: line_split[1], reverse=True)



    with gzip.open("ngram.merged_sorted1.txt.gz", 'w') as file_write_handler:
        with gzip.open("ngram.merged_sorted2.txt.gz", 'w') as file_write2_handler:
            for line_split in lista:
                temp_str = "\t".join(str(x) for x in line_split)

                if len(line_split[0].split(" ")) == 1:
                    file_write_handler.write("{}\n".format(temp_str).encode("utf-8"))
                else:
                    file_write2_handler.write("{}\n".format(temp_str).encode("utf-8"))

    print("*-*-*")


    # parser = argparse.ArgumentParser(description='utils')
    # parser.add_argument('integers', metavar='N', type=int, nargs='+',
    #                    help='an integer for the accumulator')
    # parser.add_argument('--sum', dest='accumulate', action='store_const',
    #                    const=sum, default=max,
    #                    help='sum the integers (default: find the max)')
    #
    # args = parser.parse_args()
    # # print(args.accumulate(args.integers))
