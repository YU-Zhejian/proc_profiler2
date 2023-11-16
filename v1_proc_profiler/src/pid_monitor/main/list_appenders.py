from pid_monitor._dt_mvc.appender import list_table_appender
from typing import List


def main(_: List[str]):
    for appender in list_table_appender():
        print(": ".join(appender))
    return 0
