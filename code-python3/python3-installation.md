Python3 Installation    
How to compile python 3.x in Debian-Based Linux    
1) Download Preq    
```shell
$ sudo apt-get install libpq-dev python-dev libssl-dev openssl libsqlite3-dev libjpeg-dev libjpeg8-dev
```
2) Download Python From Website    
https://www.python.org/downloads/    

3) Compile Python Source
```shell
$ ./configure --with-ensurepip=install
$ make
```

4)Making a virtual enviroment 
````
$ {Python Compiled Directory}/python -m venv {directory_where_you_want_it}/env    
$ cd {directory_where_you_want_it}
$ source env/bin/activate
$ pip install -r requirements.txt
````
    
Debugging Help    
http://stackoverflow.com/questions/22592686/compiling-python-3-4-is-not-copying-pip     
http://stackoverflow.com/questions/8915296/python-image-library-fails-with-message-decoder-jpeg-not-available-pil
