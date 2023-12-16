text_file = open("AOC2023/01/DATA.txt", "r")
lines = text_file.readlines()

task1 = {
    "1": 1,
    "2": 2,
    "3": 3,
    "4": 4,
    "5": 5,
    "6": 6,
    "7": 7,
    "8": 8,
    "9": 9
}

task2 = {
    "one": 1,
    "two": 2,
    "three": 3,
    "four": 4,
    "five": 5,
    "six": 6,
    "seven": 7,
    "eight": 8,
    "nine": 9,
    "1": 1,
    "2": 2,
    "3": 3,
    "4": 4,
    "5": 5,
    "6": 6,
    "7": 7,
    "8": 8,
    "9": 9
}


def computerSum(map):
    sum = 0

    for line in lines:
        first = None
        last = None

        i = 0
        while i < len(line):
            for key, value in map.items():
                if line.startswith(key, i):
                    if first is None:
                        first = value
                    last = value
                    break
            i += 1

        if first is None or last is None:
            raise ValueError('Line does not contain any numbers', line)

        sum += first * 10 + last

    return sum


print("Task 1: ", computerSum(task1))
print("Task 2: ", computerSum(task2))
