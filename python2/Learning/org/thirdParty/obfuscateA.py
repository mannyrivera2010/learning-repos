
import obfuscate


print obfuscate.rot47("96==@`lgbo")



instance=obfuscate.Affine(101,5000);
print instance.encrypt("Hello my name is manny")

print instance.decrypt("Pevvg qi lyqe km qylli")


print obfuscate.Vigenere.encrypt('All is NOT what it seems!', 'paranoia')





class Vigenere(object):
    def __init__(self):
        pass
        
    def encrypt(self,plaintext,keyinput):
        return obfuscate.Vigenere.encrypt(plaintext)
    
    def decrypt(self,ciphertext, keyinput):
        return obfuscate.Vigenere.decrypt(ciphertext, keyinput)

        
        
class ma(object):
    __MY_DEFAULT_NAME = 'foo'
    
    def __init__(self, arg1, arg2):
        pass
    
    @classmethod #called Decorators not "Annotations" 
    def get_default_name(cls):
        return cls.__MY_DEFAULT_NAME
    
    @staticmethod
    def do_utility_func(arg1, arg2):
        pass





