from __future__ import division
from django.core.management.base import BaseCommand, CommandError
from cc.models import *

import ipdb

import random
import csv
import sys
import argparse

from collections import Counter

class Command(BaseCommand):
    """
    Collects the fertility of each expression and fact 
            -- how often are they relevant, useful?
            -- how many facts do they pair with usefully?
    """

    def add_arguments(self, parser):
        parser.add_argument('--output', type=argparse.FileType('w'), default=sys.stdout, help="Path to save output to")
        parser.add_argument('--fact', type=str, default='cost of a laptop', help="Fact to get mentions for.")

    def handle(self, *args, **options):
        writer = csv.writer(options['output']) 
        facts = map(str.strip, options['fact'].split('*'))
        datums = [NumericData.objects.get(name=f) for f in facts]
        args = set([tuple(e.arguments) for e in NumericExpression.objects.filter(arguments__contains=[d.id for d in datums])])
        exprs = [e.id for e in NumericExpression.objects.filter(arguments__overlap=[d.id for d in datums])]

        # Get all possible mentiontasks
        resps = NumericExpressionResponse.objects.filter(expression__in = exprs)

        # Iterate through all the data:
        useful = []
        for me in NumericMentionExpressionTask.objects.filter(candidates__overlap = [r.id for r in resps]):
            n = me.responses.count()
            votes = me.get_candidates_votes()
            for expr, v in votes.items():
                if expr is None: continue

                is_positive = v/n > 0.5
                if is_positive and tuple(expr.expression.arguments) in args:
                    useful.append((me.mention, expr, v/n))
        writer.writerow(["mention.id", "expression.id", "mention.sentence.gloss", "mention.value", "mention.unit", "expression.description", "score"])
        for mention, expression, score in reversed(sorted(useful, key=lambda p:p[2])):
            writer.writerow([mention.id, expression.id, mention.sentence.gloss, mention.value, mention.unit, expression.description, score])


