from django.core.management.base import BaseCommand, CommandError
from cc.models import *

import random
import csv
import sys
import argparse

class Command(BaseCommand):
    """
    Generates a (useful/not-useful) dataset from turk responses.
    Each datapoint is constructed using majority votes on which perspsectives are useful.
    - If, on any set of candidates, the 'none-of-the-above' has
      majority, then all the above candidates are set to be in the
      negative class.
    - Otherwise, an option is positive iff it has 3+ votes.
    - The output (unfeaturized) consists of
        (a) ids of the example (for bookkeeping)
        (b) mention: sentence, value, unit, token offsets.
        (c) the expression described as the string '[multiplier] [* value (fact)]+
        (d) the expression response (note this hasn't been parsed).
    """

    def add_arguments(self, parser):
        parser.add_argument('--output', type=argparse.FileType('w'), default=sys.stdout, help="Path to save output to")


    def handle(self, *args, **options):
        writer = csv.writer(options['output'], delimiter='\t')
        writer.writerow([
            "id",
            "mention_id", "mention_sentence", "mention_value", "mention_unit", "mention_normalized_value", "mention_normalized_unit", "mention_token_begin", "mention_token_end",
            "expression_id", "expression_value", "expression_unit", "expression",
            "response_id", "response",
            "label"])

        tasks = NumericMentionExpressionTask.objects.filter(response__id__gte = 0).distinct()
        for task in tasks:
            n_responses = task.responses.count()
            for candidate, votes in task.get_candidates_votes().items():
                if candidate is None: continue # ignore me.
                label = int(votes > 0.5 * n_responses)

                writer.writerow([
                    task.id,
                    task.mention.id, task.mention.sentence, task.mention.value, task.mention.unit, task.mention.normalized_value, task.mention.normalized_unit, task.mention.token_begin, task.mention.token_end,
                    candidate.expression.id, candidate.expression.value, candidate.expression.unit, candidate.expression,
                    candidate.id, candidate.description,
                    label])
