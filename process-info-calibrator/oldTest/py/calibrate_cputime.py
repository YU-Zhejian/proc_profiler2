import math
import time

import multiprocessing

def workload():
    ts = time.time()
    while time.time() - ts < 10:
        _ = math.sqrt(1000)


if __name__ == "__main__":
    processes = []
    for i in range(10):
        processes.append(multiprocessing.Process(target=workload))
    for i in range(10):
        processes[i].start()
    for i in range(10):
        processes[i].join()
