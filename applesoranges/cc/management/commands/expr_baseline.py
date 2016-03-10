from django.core.management.base import BaseCommand, CommandError
from cc.models import *
from cc.util import easy_number, round_multiplier, easy_unit

import random
import csv

class Command(BaseCommand):
    """Generates baseline descriptions for each formula."""

    def baseline_generation(self, expr):
        """
        Generate a natural language description of a expr.
        If multiplier > 1: prepend "multipler times the "
        If multiplier < 1: prepend "multipler of the "
        If multiplier == 1: prepend "the "
        """
        output = ""

        # say the multiplier
        multiplier = round_multiplier(expr.multiplier)
        if multiplier == "1/1" or multiplier == "1":
            pass
        elif "/" in multiplier:
            output += multiplier + " of "
        else:
            output += multiplier + " times "
        output += "the "
        
        # append descriptions of each expression to the output
        output += " ".join(arg.name for arg in reversed(expr.get_arguments()))
        return output

    def add_arguments(self, parser):
        parser.add_argument('--output', type=str, required=True, help="Path to write to")
        parser.add_argument('--dataset', type=str, required=True, choices=["train", "test"], help="Path to write to")

    def handle(self, *args, **options):
        if options['output'] is None:
            raise CommandError("Must set output")

        with open(options['output'], 'w') as f:
            writer = csv.writer(f, delimiter='\t')

            # Get only the train expressions.
            expressions = list(NumericExpression_Test.objects.all()) if options['dataset'] == 'test' else list(NumericExpression_Train.objects.all())

            for expr in expressions:
                candidate = self.baseline_generation(expr)
                refs = [r.description for r in expr.responses.all()]
                writer.writerow([candidate] + refs)
