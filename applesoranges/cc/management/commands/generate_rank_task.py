from django.core.management.base import BaseCommand, CommandError
from cc.models import NumericMentionExpressionTask, NumericMentionExpressionBlacklist
from django.utils.html import escape

import random
import csv

class Command(BaseCommand):
    """Generates a dataset from expressions."""

    def add_arguments(self, parser):
        parser.add_argument('--output', type=str, required=True, help="Path to write to")

    def handle(self, *args, **options):
        if options['output'] is None:
            raise CommandError("Must set output")

        with open(options['output'], 'w') as f:
            writer = csv.writer(f)

            blacklist = set(elem.task_id for elem in NumericMentionExpressionBlacklist.objects.all())
            tasks = [t for t in NumericMentionExpressionTask.objects.order_by('?') if t.id not in blacklist]
            #tasks = NumericMentionExpressionTask.objects.order_by('?')

            # Group expressions into groups of 5.
            writer.writerow(["task_index", "tasks"])
            for i, tasks_ in enumerate([tasks[i:i+5] for i in range(0, len(tasks), 5)]):
                tasks = escape("\t".join(t.to_json() for t in tasks_))
                writer.writerow([i, tasks])

