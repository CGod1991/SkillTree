# GitHub 学习笔记

标签： GitHub

---

## Git 基本知识

- Linux 环境下使用 Git 时的用户名和邮箱保存在 ~/.gitconfig 配置文件中。这里设置的姓名和邮箱会出现在 Git 的提交日志中。
- 每个仓库中的 .git 目录被称为“附属于该仓库的工作树”，文件的编辑等操作在工作树中进行，然后记录到仓库中，以此管理文件的历史快照。
- git status：显示 Git 仓库的状态。
- git add：向暂存区中添加文件。
- git commit：将当前暂存区中的文件实际保存到仓库的历史记录中。
- git log：查看以当前状态为终点的日志。
- git reflog：查看当前仓库的所有操作日志。
- git diff：查看工作树、暂存区和最新提交之间的差别。
- git diff HEAD：查看本次提交和上次提交之间的差别。
- 在执行 git commit 之前，先执行 git diff HEAD ，查看本次提交和上次提交之间有什么差别，等确认完毕后再进行提交。
- git branch：显示分支名列表，同时确认当前所在分支。
- git checkout branch-A：切换至分支 branch-A。
- git checkout -b branch-B：从 master 为基础创建分支 branch-B，并切换至分支 branch-B。
- git merge：合并分支到当前所在分支。如 git merge branch-C：表示将分支 branch-C 合并到分支 master。
- git rest --hard：将仓库的 HEAD、暂存区和当前工作树回溯到指定的状态，需要指定状态的哈希值。
- 如果在合并分支的情况中，有文件内容冲突，在有冲突的文件中，====== 以上的部分为当前 HEAD 的内容，以下的部分是要合并的分支中的内容。
- git remote add：将当前仓库添加到远程仓库。如：git remote add originA git@github.com:user/project-A.git, Git 会自动将远程仓库 project-A 的名称设置为 originA。
- git push：将当前分支下本地仓库中的内容推送给远程仓库。如：git push originA master。
- git clone：获取远程仓库。执行该命令后，会默认处于 master 分支，并且 Git 会自动将 origin 设置为该远程仓库的标识符。
- git pull：获取最新的远程仓库分支。