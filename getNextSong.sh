#!/bin/sh

if [ -f tracks/current/next.mp3 ]; then
  mv -f tracks/current/next.mp3 tracks/current/tmp.mp3
fi

echo "tracks/current/tmp.mp3"
