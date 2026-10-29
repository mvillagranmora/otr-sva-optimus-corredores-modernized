"""Writes every FRESH case input into the scratch copy's cases/ (T:\\otr\\BcsTesoreriaJobs\\...). Run: py -3 gen_fresh.py"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_fresh1
import gen_fresh2
import gen_fresh3
from gen_common import total

if __name__ == "__main__":
    gen_fresh1.generar()
    gen_fresh2.generar()
    gen_fresh3.generar()
    print("cases written:", total())
