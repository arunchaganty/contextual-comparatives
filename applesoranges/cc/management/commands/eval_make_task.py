#!/usr/bin/env python2.7
# -*- coding: utf-8 -*-
"""
Make task to evaluate.
"""

from django.core.management.base import BaseCommand, CommandError
from cc.models import *
from django.utils.html import escape

import random
import csv

class Command(BaseCommand):
    """Generates a dataset from expressions."""

    def add_arguments(self, parser):
        import argparse
        import sys
        parser.add_argument('--output', type=argparse.FileType('w'), default=sys.stdout, help="Path to write to")
        #parser.add_argument('--system', choices=('baseline', 'generation_baseline', 'generation'), required=True, help="System to use")

    def handle(self, *args, **options):
        if options['output'] is None:
            raise CommandError("Must set output")

        #system = options['system']
        writer = csv.writer(options['output'])

        random.seed(42)
        mention_ids = NumericPerspectiveTask.objects.all().values_list('mention_id')

        tasks = []
        for unit in ["person", "area", "time", "weight", "length", "gun", "car", "volume", "money",]:
            mentions = list(NumericMention.objects.filter(id__in = mention_ids, normalized_unit = unit).order_by('id'))
            random.shuffle(mentions)
            mentions = mentions[:25]
            tasks +=  NumericPerspectiveTask.objects.filter(mention__in = mentions)

        tasks = [t.to_json() for t in tasks]
        # deterministically shuffle.
        random.seed(42)
        random.shuffle(tasks)

        # Group expressions into groups of 5.
        writer.writerow(["task_index", "tasks"])
        for i, lst in enumerate([tasks[i:i+5] for i in range(0, len(tasks), 5)]):
            tasks_str = escape("\t".join(lst))
            writer.writerow([i, tasks_str])

