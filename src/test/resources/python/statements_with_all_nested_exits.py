if (timeInMicros / 1000000 > 1):
    return "s"
elif (timeInMicros/ 1000 > 1):
    return "ms"
else:
    return "us"