import numpy
if a > b:
    print(1)
    for i in range(1, 20):
        if i == 10:
            return 123
        print(i)
        if i == 11:
            break
else:
    print(2)

return a + b
