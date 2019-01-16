#!/bin/sh

mvn package -DskipTests

echo "Started at $(date)"

run () {
    java \
        -jar target/logistic-regression-jar-with-dependencies.jar \
        -p1:localhost:8871 \
        -p2:localhost:8872 \
        $@
}

main() {
    out1File=$(mktemp)
    out2File=$(mktemp)

    run -i1 < "target/classes/$1_party1.txt" > ${out1File} &
    run -i2 < "target/classes/$1_party2.txt" > ${out2File} &
    wait

    out1=$(cat ${out1File})
    out2=$(cat ${out2File})

    if [[ "${out1}" != "${out2}" ]]; then
        echo "Output is not the same!"
        echo "Party 1: ${out1}"
        echo "Party 2: ${out2}"
    else
        echo "Output: ${out1}"
    fi
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
