import threading
import time
from statistics import mean
from typing import List

import psutil

from pid_monitor._dt_mvc.appender import BaseTableAppender
from pid_monitor._dt_mvc.frontend_cache.process_frontend_cache import ProcessFrontendCache
from pid_monitor._dt_mvc.frontend_cache.system_frontend_cache import SystemFrontendCache
from pid_monitor._dt_mvc.pm_config import PMConfig
from pid_monitor._dt_mvc.std_tracer import BaseSystemTracerThread

__all__ = ("SystemCPUTracerThread",)


class AsyncSystemCPUProbeThread(threading.Thread):
    appender: BaseTableAppender

    def __init__(self, appender: BaseTableAppender, frontend_cache: ProcessFrontendCache):
        super().__init__()
        self.appender = appender
        self.frontend_cache = frontend_cache

    def run(self):
        cpu_percents: List[float] = psutil.cpu_percent(interval=1, percpu=True)
        if cpu_percents is None:
            return
        self.frontend_cache.cpu_percent = mean(cpu_percents)
        cpu_value_array = [time.time()]
        cpu_value_array.extend(cpu_percents)
        self.appender.append(cpu_value_array)


class SystemCPUTracerThread(BaseSystemTracerThread):
    """
    System-level CPU utilization tracer, traces CPU utilization of all logical cores.
    """

    def __init__(
            self,
            trace_pid: int,
            pmc: PMConfig,
            frontend_cache: SystemFrontendCache
    ):
        super().__init__(
            trace_pid=trace_pid,
            pmc=pmc,
            frontend_cache=frontend_cache
        )
        cpu_name_array = ['TIME']
        cpu_name_array.extend(map(str, range(psutil.cpu_count())))
        self._init_setup_hook(
            tracer_type="cpu",
            table_appender_header=cpu_name_array
        )

    def probe(self):
        acp = AsyncSystemCPUProbeThread(
            appender=self._appender,
            frontend_cache=self.frontend_cache
        )
        acp.start()
