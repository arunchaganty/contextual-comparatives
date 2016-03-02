from django.core.management.base import BaseCommand, CommandError
from cc.models import NumericExpression

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

            expressions = list(NumericExpression.objects.all())

            # deterministically shuffle.
            random.seed(42)
            random.shuffle(expressions)

            # Group expressions into groups of 5.
            writer.writerow(["id", "taskids", "prompts", "zs"])
            for i, lst in enumerate([expressions[i:i+5] for i in range(0, len(expressions), 5)]):
                tasks = "\t".join(map(str, [e.id for e in lst]))
                prompts = "\t".join(e.get_prompt() for e in lst)
                zs = "\t".join(e.get_z() for e in lst)
                writer.writerow([i, tasks, prompts, zs])

            writer.close()

