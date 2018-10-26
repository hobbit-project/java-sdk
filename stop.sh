IDS=$(sudo docker ps -a | grep $1 | awk '{print $1}')
sudo docker rm $IDS --force