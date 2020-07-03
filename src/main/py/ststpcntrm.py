#!/usr/bin/python

import json
import os

DATA_DIR = '/var/tmp/ststpcnt.com'
EMPTY = ''
START = 'START'
STOP = 'STOP'
CONTINUE = 'CONTINUE'
TEXT = 'text'
DELETE_TEXT = [
    EMPTY,
    START,
    STOP,
    CONTINUE]
LOCK = 'lock'

for root, dirs, files in os.walk(DATA_DIR, topdown=False):

    for name in files:

        if name == LOCK:
          continue

        path = os.path.join(root, name)
        with open(path) as f:
            text = json.loads(f.read()).get(TEXT, EMPTY).strip().upper()
        if text in DELETE_TEXT:
            print(path)
            os.remove(path)

    for name in dirs:
        path = os.path.join(root, name)
        start = os.path.join(path, START)
        stop = os.path.join(path, STOP)
        cont = os.path.join(path, CONTINUE)
        if all([os.path.exists(start), os.path.exists(stop), os.path.exists(cont)]) \
            and not any([len(os.listdir(start)), len(os.listdir(stop)), len(os.listdir(cont))]):
            os.rmdir(start)
            os.rmdir(stop)
            os.rmdir(cont)
            print(path)
            os.rmdir(path)
