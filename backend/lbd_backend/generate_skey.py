import random
import string

skey = ''
characters = string.ascii_letters + string.digits + string.punctuation

for i in range(50):
    skey += random.SystemRandom().choice(characters)

print skey