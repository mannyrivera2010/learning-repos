
repo_git_list = [

]

import os

for i in repo_git_list:
    print("-------------------------------").format(i)
    i_a = i.split("/")
    i_b = i_a[1].split(".")
    repo = i_b[0]

    # remove_remote = "git remote remove {}".format(repo)
    # print(remove_remote)
    # os.system(remove_remote)

    add_remote = "git remote add {} {}".format(repo, i)
    print(add_remote)
    os.system(add_remote)

    # gitpu = "git pu"
    # print(add_remote)
    # os.system(add_remote)

    git_reset = "git reset --hard"
    os.system(git_reset)

    git_clean = "git clean -fd"
    os.system(git_clean)


    checkout_remote = "git checkout {}/master".format(repo)
    print(checkout_remote)
    os.system(checkout_remote)

    checkout_remote_local = "git checkout -b {}".format(repo)
    os.system(checkout_remote_local)

    git_push_origin = "git push --set-upstream origin {}" .format(repo)
    os.system(git_push_origin)
