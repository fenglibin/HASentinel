#由于当前工程是从其它项目远程fork过来的，该Shell用于从远程更新当前工程，以确保和远程工程保持一致样
#切换到Master分支
git checkout master
#增加远程分支做为上游分支
git remote add upstream https://gitee.com/laofeng/hasentinel.git
#获取远程分支的更新
git fetch upstream
#将远程分支与本地文件进行合并
git merge upstream/master
#推送合并更新到当前分支的远程
git push origin master

