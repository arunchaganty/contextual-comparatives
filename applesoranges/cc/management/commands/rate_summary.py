from django.core.management.base import BaseCommand, CommandError
from cc.util import HeaderedRow
from cc.models import *
from django.db import transaction
from urllib.parse import unquote
from collections import Counter
import ipdb

import os, stat, csv
import json

class Command(BaseCommand):
    """Takes mturk repsonse CSV from [source], and loads into database."""

    def add_arguments(self, parser):
        import argparse, sys
        parser.add_argument('--output', type=argparse.FileType('w'), default=sys.stdout, help="File to write out mentions, generations, and evaluations")

    def handle(self, *args, **options):
        writer = csv.writer(options['output'], delimiter="\t")
        writer.writerow(['id', 'baseline_perspective', 'generation_perspective', 'baseline_votes', 'generation_votes', 'none_votes', 'n_votes', 'error_analysis'])

        tasks = NumericPerspectiveRatingTaskResponse.objects.all().values_list('task_id')
        tasks = NumericPerspectiveRatingTask.objects.filter(id__in = tasks).prefetch_related('responses')

        print("Per task:")
        counter = Counter()
        counter_explain = Counter()
        for task in tasks:
            if "generation" not in task.systems: continue

            writer.writerow([
                task.mention_id,
                task.get_perspective("baseline"),
                task.get_perspective("generation"),
                task.get_votes("baseline"),
                task.get_votes("generation"),
                task.get_votes("none"),
                task.responses.count(),
                task.error_analysis])

            winners = task.best_system()
            if len(winners) > 0:
                counter.update(task.best_system())
                counter[tuple(winners)] += 1
                counter_explain["generation" in winners, tuple(task.error_analysis.split(','))] += 1
            else:
                counter["None"] += 1
                counter_explain["None", tuple(task.error_analysis.split(','))] += 1
            counter["Tasks"] += 1
        print(counter)
        for winner in ["None", True, False]:
            print("Winner: " + str(winner))
            total =  0
            for (winner_, reason), count in counter_explain.items():
                if winner_ != winner: continue
                print(reason, count)
                total += count
            print(total)
            print()

        print("Per response:")
        counter = Counter()
        for response in NumericPerspectiveRatingTaskResponse.objects.all().prefetch_related('task'):
            if "generation" not in response.task.systems: continue
            if response.winner == -1:
                counter["None"] += 1
            elif response.winner == len(response.task.systems):
                for s in response.task.systems:
                    counter[s] += 1
            else:
                counter[response.task.systems[response.winner]] += 1
        print(counter)
