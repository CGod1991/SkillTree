# Hive 常用配置参数

标签：Hive

---

- `hive.exec.reducers.bytes.per.reducer`：该参数控制一个 job 会有多少个 reducer 来处理，依据的是输入文件的总大小。默认为 1G。

- `mapreduce.input.fileinputformat.split.maxsize`：

- `mapreduce.input.fileinputformat.split.minsize`：

- `mapreduce.input.fileinputformat.split.minsize.per.node`：

- `mapreduce.input.fileinputformat.split.minsize.per.rack`：