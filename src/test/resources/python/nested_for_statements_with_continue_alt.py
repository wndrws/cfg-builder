for i in range(10):
    for j in range(20):
        if i == 10:
            continue
        print(i)
        print(j)
        for k in range(3):
            print(k)
        continue
