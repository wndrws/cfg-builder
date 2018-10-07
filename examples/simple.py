# import aenum

def sum(a, b):
    # import numpy
    if a > b:
        print(1)
        for i in range(1, 20):
            if i == 10:
                continue
            print(i)
            if i == 11:
                break
    else:
        print(2)

    return a + b


print("The sum of %i and %i is %i" % (5, 3, sum(5, 3)))


class Foo:
    print("This is a class")

    def __init__(self):
        super().__init__()
        self.somevar = "bla"

    def somefunc(self):
        pass


if __name__ == "__main__":
    omg = True
    while omg:
        print("This is main!")
        omg = False
    else:
        print("Oh yeah!")
