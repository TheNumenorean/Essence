#!/bin/bash

while [ ! -s tracks/next.txt ]; do
sleep 1
done

cat tracks/next.txt

> tracks/next.txt

