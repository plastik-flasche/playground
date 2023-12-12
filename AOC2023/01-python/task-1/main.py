text_file = open("../DATA.txt", "r")
lines = text_file.readlines()

sum = 0

for line in lines:
    first = None
    last = None

    for char in line:
        if not char.isdigit():
            continue

        if first is None:
            first = int(char)

        last = int(char)

    if first is None or last is None:
        raise ValueError('Line does not contain any numbers', line)

    sum += first * 10 + last

print(sum)
