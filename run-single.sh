#!/bin/bash

#test -f target/logistic-regression-jar-with-dependencies.jar || mvn package
#mvn package

echo "Started at $(date)"

run () {
    java \
        -jar target/logistic-regression-jar-with-dependencies.jar \
        -p1:localhost:8871 \
        -p2:localhost:8872 \
        --privacy-budget 0 \
        --sensitivity 0.001 \
        --unsafe-debug-log \
        $@
}

main() {
    run -i${1} < "target/classes/mtcars_party$1.txt"
}

if [[ -z "$1" ]]; then
    echo "Usage: "
    echo "  $0 <test-set>"
    echo
    echo "Example: $0 mtcars"
    echo
    echo "Available test sets:"
    echo "  mtcars"
    echo "  breast_cancer"
    exit 1
fi

time main $1
