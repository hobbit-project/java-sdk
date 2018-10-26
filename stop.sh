IDS=$(sudo docker ps -a | grep dummybenchmark | awk '{print $1}')
sudo docker rm $IDS --force