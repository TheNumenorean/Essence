#!/bin/bash

if [ -f tracks/next.txt ]; then
  cat tracks/next.txt
fi

> tracks/next.txt

