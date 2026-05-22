#!/bin/bash
# --- 配置区 ---
SERVER_USER="root"
SERVER_IP="47.93.86.248"
LOCAL_JAR="./target/wyjqwy-0.0.1.jar" # 本地打包后的路径
REMOTE_PATH="/home/java/wyjqwy"
# --------------

echo "--- 1. 开始上传 JAR 包 ---"
scp $LOCAL_JAR $SERVER_USER@$SERVER_IP:$REMOTE_PATH

echo "--- 2. 远程执行部署脚本 ---"
ssh $SERVER_USER@$SERVER_IP "bash $REMOTE_PATH/start.sh restart"

echo "--- 全部搞定！ ---"