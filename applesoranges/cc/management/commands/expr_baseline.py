from django.core.management.base import BaseCommand, CommandError
from cc.models import *
from cc.util import easy_number, round_multiplier, easy_unit

import random
import csv

class Command(BaseCommand):
    """Generates baseline descriptions for each formula."""

    def add_arguments(self, parser):
        parser.add_argument('--output', type=str, required=True, help="Path to write to")
        parser.add_argument('--dataset', type=str, required=True, choices=["train", "test"], help="Path to write to")
        parser.add_argument('--devsplit', type=float, default="0.2", help="faction for dev")

    def handle(self, *args, **options):
        if options['output'] is None:
            raise CommandError("Must set output")

        out = options['output']

        # Get only the train expressions.
        if options['dataset'] == 'test':
            expressions = list(NumericExpression_Test.objects.order_by('id')) 
            options['devsplit'] = 0.
        else:
            expressions = list(NumericExpression_Train.objects.order_by('id'))

        # Get arguments that should be in the train and dev splits.
        groups = [tuple(e.arguments) for e in expressions]
        random.seed(42)
        random.shuffle(groups)
        pivot = round(len(groups) * (1. - options['devsplit']))
        train = set(groups[:pivot]) # everything not in train is in dev.

        if options['dataset'] == 'test':
            writer = csv.writer(open(out+'-test.tsv','w'), delimiter='\t')
            for expr in expressions:
                candidate = expr.perspective()
                refs = [r.description for r in expr.responses.all()]
                writer.writerow([candidate] + refs)
        else:
            writer = csv.writer(open(out+'-train.tsv','w'), delimiter='\t')
            for expr in expressions:
                if tuple(expr.arguments) not in train: continue
                candidate = expr.perspective()
                refs = [r.description for r in expr.responses.all()]
                writer.writerow([candidate] + refs)

            writer = csv.writer(open(out+'-dev.tsv','w'), delimiter='\t')
            for expr in expressions:
                if tuple(expr.arguments) in train: continue
                candidate = expr.perspective()
                refs = [r.description for r in expr.responses.all()]
                writer.writerow([candidate] + refs)

