import hashlib

f =open('319kb.dat', 'r')
ll =[]
for line in f:
    temp =line.lower()
    temp =temp.replace('\n', ' ')
    temp =temp.replace('!', '')
    temp =temp.replace(',', '')
    temp =temp.replace('\'', '')
    temp =temp.replace('=', '')
    temp =temp.replace('/', '')
    temp =temp.replace('{', '')
    temp =temp.replace('}', '')
    temp =temp.replace('#', '')
    temp =temp.replace('$', '')
    temp =temp.replace('%', '')
    temp =temp.replace('^', '')
    temp =temp.replace('*', '')
    temp =temp.replace(';', '')
    temp =temp.replace('[', '')
    temp =temp.replace(']', '')
    temp =temp.replace('\\', '')
    temp =temp.replace('/', '')
    temp =temp.replace('<', '')
    temp =temp.replace('>', '')
    temp =temp.replace('_', '')
    temp =temp.replace('+', '')
    temp =temp.replace(':', '')
    temp =temp.replace('.', '')
    temp =temp.replace('"', '')
    temp =temp.replace('-', '')
    temp =temp.replace('?', '')
    temp =temp.replace('|', '')
    temp =temp.replace('&', '')
    temp =temp.replace('(', '')
    temp =temp.replace(')', '')

    ll.append(temp)
f.close()

f =open('dd.dat', 'w')
for line in ll:
    # f.write(hashlib.sha256(bytes(line, 'utf-8')).hexdigest() +'@@@@@' +line)
    temp =hashlib.sha256(bytes(line, 'utf-8')).hexdigest() +'@@@@@' +line

    if(len(temp.split('@@@@@')) >=3):
        temp +='\n'
        f.write(temp)
    
f.close()

# f =open('words.dat', 'r')
# ll =[]

# for line in f:
#     ll.append(line)
# f.close()

# f =open('ww.dat', 'w')

# for i in range(len(ll) // 700):
#     f.write(ll[i])
# f.close()