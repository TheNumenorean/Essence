#!/bin/sh

if [ -f songs/current/next.mp3 ]; then
  mv -f songs/current/next.mp3 songs/current/tmp.mp3
fi

echo "songs/current/tmp.mp3"
