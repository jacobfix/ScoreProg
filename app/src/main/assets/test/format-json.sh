#!/bin/bash

cat $1 | sed -e 's/^[ \t]*//;s/[ \t]*$//' | tr -d '\n' > tmp
mv tmp $1
