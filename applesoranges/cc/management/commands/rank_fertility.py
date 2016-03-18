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

    def handle(self, *args, **options):
        datums = {d.id : d.name for d in NumericData.objects.all()}
        exprs = {tuple(e.arguments) : e.get_easy_prompt_no_units() for e in NumericExpression.objects.all()}

        uses, positive_uses = Counter(), Counter()
        datum_fertility = Counter()
        datum_fertility_positive = Counter()


        # Iterate through all the data:
        for me in NumericMentionExpressionTask.objects.all():
            n = me.responses.count()
            votes = me.get_candidates_votes()
            for expr, v in votes.items():
                if expr is None: continue

                is_positive = v/n > 0.5
                expr = tuple(expr.expression.arguments)
                uses[expr] += 1
                positive_uses[expr] += 1 if is_positive else 0

                for datum in expr:
                    uses[datum] += 1
                    positive_uses[datum] += 1 if is_positive else 0
                    for datum_ in expr:
                        if datum == datum_: continue

                        datum_fertility[tuple(sorted((datum, datum_)))] += 0.5
                        datum_fertility_positive[tuple(sorted((datum, datum_)))] += 0.5 if is_positive else 0

        writer = csv.writer(options['output'], delimiter='\t')
        writer.writerow([
            "expression", "uses", "positive_uses", "fraction"])

        for expr, name in exprs.items():
            writer.writerow([name, positive_uses[expr], uses[expr], positive_uses[expr] / uses[expr] if uses[expr] > 0 else 0,])

        writer.writerow([
            "datum", "uses", "positive_uses", "fraction"])
        for datum, name in datums.items():
            if uses[datum] > 0:
                writer.writerow([name, positive_uses[datum], uses[datum], positive_uses[datum] / uses[datum] if uses[datum] > 0 else 0,])

        writer.writerow([
            "datum", "expression", "uses", "positive_uses", "fraction"])
        for datum, datum_ in datum_fertility:
            writer.writerow([datums[datum], datums[datum_], datum_fertility_positive[(datum, datum_)], datum_fertility[(datum, datum_)],
                datum_fertility_positive[(datum, datum_)]/ datum_fertility[(datum, datum_)]  if datum_fertility[(datum,datum_)] > 0 else 0 ,])

