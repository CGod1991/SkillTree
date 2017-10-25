# Region 切分流程

标签：Region Split

---

当 regionX 进行切分时，具体的切分流程如下：
1. 在 ZK 中的 /region-in-transition 目录下，创建 regionX 对应的 znode，并标记该 region 的状态为 spliting；
2. 由于 master 一直在 watch ZK 中的 /region-in-transition 目录，所以可以立即感知到 regionX 将要进行切分。然后 master 会修改内存中 regionX 的状态；
3. 在 HDFS 上 regionX 的目录下，创建临时文件夹 .split ，用来保存切分后的子 region 信息；
4. 关闭 regionX：主要是停止 regionX 对外提供写服务，并触发 regionX 的 flush 操作，将 memstore 中的数据全部持久化到磁盘；
5. 在 .split 文件夹中生成两个子文件夹，分别生成引用文件，指向 regionX 的文件。引用文件的文件名格式为：`父 region 对应的 HFile 文件.父 region 名`，引用文件内容为切分点的 splitkey 和表示该引用文件引用的是父文件的上半部分还是下半部分的 boolean 变量；
6. 将两个子文件夹拷贝到和 regionX 同级的目录中，形成两个新的子 region；
7. regionX 进行下线，不再对外提供服务；
8. 两个新的子 region 上线，对外提供服务。