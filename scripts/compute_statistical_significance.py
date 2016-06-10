#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Compute statistical significance across classifiers
"""

from __future__ import division

import csv
import sys
import ipdb
import os
from collections import defaultdict
from random import randint
import numpy as np

R_TP = 1
R_TN = 2
R_FP = 3
R_FN = 4

def load_datasets(inputs):
    datasets = {}
    for fname in inputs:
        name, _ = os.path.basename(fname).split('.')
        with open(fname) as f:
            reader = csv.reader(f, delimiter='\t')
            header = next(reader)
            assert header == ['mention_id', 'response_id', 'gold', 'guess']
            entries = []
            for _, _, gold, guess in reader:
                gold, guess = bool(int(gold)), bool(int(guess))
                if gold and guess:
                    entries.append(R_TP)
                elif gold and not guess:
                    entries.append(R_FN)
                elif not gold and not guess:
                    entries.append(R_TN)
                elif not gold and guess:
                    entries.append(R_FP)
        datasets[name] = np.array(entries)
    return datasets

def compute_metrics(guess):
    """
    Compute metrics on a dataset
    """
    tp, tn, fp, fn = np.sum(guess[guess==R_TP]), np.sum(guess[guess==R_TN]), np.sum(guess[guess==R_FP]), np.sum(guess[guess==R_FN])
    p, r = (tp / (tp + fp), tp / (tp + fn)) if tp > 0 else (0, 0)
    f1 = (2 * p * r)/(p+r) if p > 0 and r > 0 else 0
    return p, r, f1

def compute_s_metric(ds1, ds2):
    p1, r1, f11 = compute_metrics(ds1)
    p2, r2, f12 = compute_metrics(ds2)
    return p1 - p2, r1 - r2, f11 - f12 # delta(x)

def check_significance(ds1, ds2, n_samples=int(1e2)):
    # Create a copy of ds1 and ds2.
    # Take the set of all output, shuffle, partition into two sets, and
    # measure.
    assert ds1.shape == ds2.shape
    n_points = ds1.shape[0]
    d_p, d_r, d_f1 = compute_s_metric(ds1, ds2)
    p_p, p_r, p_f1 = 0, 0, 0

    permutations = np.random.randint(0, n_points, (n_samples, n_points))
    for i, perm in enumerate(permutations):
        ds1_ = ds1[perm]
        ds2_ = ds2[perm]

        d_p_, d_r_, d_f1_ = compute_s_metric(ds1_, ds2_)

        p_p += (int(d_p_ > 2 * d_p) - p_p)/(i+1)
        p_r += (int(d_r_ > 2 * d_r) - p_r)/(i+1)
        p_f1 += (int(d_f1_ > 2 * d_f1) - p_f1)/(i+1)

    return p_p, p_r, p_f1

def do_command(args):
    # Parse the data.
    datasets = load_datasets(args.inputs)

    writer = csv.writer(args.output, delimiter='\t')
    writer.writerow(['system1', 'system2', 'p', 'r', 'f1'])

    for dataset in datasets:
        for dataset_ in datasets:
            p, r, f1 = check_significance(datasets[dataset], datasets[dataset_], int(args.n_samples))
            writer.writerow([dataset, dataset_, p, r, f1])

if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser(description='Compute statistical significance across every classifier provided as input')
    parser.add_argument('--output', type=argparse.FileType('w'), default=sys.stdout)
    parser.add_argument('inputs', type=str, nargs='*', help="")
    parser.add_argument('--n_samples', type=float, default=1e2, help="")
    parser.set_defaults(func=do_command)

    #subparsers = parser.add_subparsers()
    #command_parser = subparsers.add_parser('command', help='' )
    #command_parser.set_defaults(func=do_command)

    ARGS = parser.parse_args()
    ARGS.func(ARGS)
