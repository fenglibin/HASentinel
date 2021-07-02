echo "Did you add all your changes and commited them? Y/N"
read DOIT
if [[ $DOIT =~ "Y" || $DOIT =~ "y" ]] ; then
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
else
    echo "Please add all your changes and commit the changes before update from remote, and then come back later."
fi
