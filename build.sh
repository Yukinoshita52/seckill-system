#!/bin/bash
# seckill-system 构建脚本
# 用法: ./build.sh [compile|install|run-engine|run-gateway|run-admin|clean]

set -e

export JAVA_HOME="E:/develop_tools/JDK/jdk-17"
MVN="E:/develop_tools/apache-maven-3.9.9/bin/mvn"
SETTINGS="E:/develop_tools/apache-maven-3.9.9/conf/settings.xml"

compile_all() {
    echo ">>> 编译全部模块..."
    "$MVN" clean compile -s "$SETTINGS"
}

install_all() {
    echo ">>> 安装全部模块到本地仓库..."
    "$MVN" clean install -DskipTests -s "$SETTINGS"
}

run_engine() {
    echo ">>> 启动 seckill-engine (端口 11020)..."
    # 先确保 framework 已安装
    "$MVN" install -pl seckill-framework -DskipTests -s "$SETTINGS"
    "$MVN" spring-boot:run -pl seckill-engine -s "$SETTINGS"
}

run_gateway() {
    echo ">>> 启动 seckill-gateway (端口 8080)..."
    "$MVN" spring-boot:run -pl seckill-gateway -s "$SETTINGS"
}

run_admin() {
    echo ">>> 启动 seckill-admin (端口 11010)..."
    "$MVN" install -pl seckill-framework -DskipTests -s "$SETTINGS"
    "$MVN" spring-boot:run -pl seckill-admin -s "$SETTINGS"
}

clean_all() {
    echo ">>> 清理..."
    "$MVN" clean -s "$SETTINGS"
}

case "${1:-compile}" in
    compile)     compile_all ;;
    install)     install_all ;;
    run-engine)  run_engine ;;
    run-gateway) run_gateway ;;
    run-admin)   run_admin ;;
    clean)       clean_all ;;
    *)           echo "用法: $0 [compile|install|run-engine|run-gateway|run-admin|clean]" ;;
esac
