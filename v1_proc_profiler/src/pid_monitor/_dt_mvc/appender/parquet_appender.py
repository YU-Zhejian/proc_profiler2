import os

import fastparquet as fp
import pandas as pd

from pid_monitor._dt_mvc.appender.typing import PandasDictBufferAppender


class ParquetTableAppender(PandasDictBufferAppender):

    def _get_n_lines_actually_written_hook(self) -> int:
        return pd.read_parquet(self._real_filename).shape[0]

    def _get_real_filename_hook(self):
        self._real_filename = ".".join((self.filename, "parquet"))

    def _create_file_hook(self):
        pass

    def _write_hook(self, df: pd.DataFrame):
        fp.write(self._real_filename, df, append=os.path.exists(self._real_filename))
