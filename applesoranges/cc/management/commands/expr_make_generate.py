#!/usr/bin/env python2.7
# -*- coding: utf-8 -*-
from django.core.management.base import BaseCommand, CommandError
from cc.models import *
from cc.util import easy_number, round_multiplier, easy_unit

import ipdb

import random
import csv
from collections import defaultdict

class Command(BaseCommand):
    """Generates a dataset of (prompt, description) pairs from turker responses."""

    def add_arguments(self, parser):
        parser.add_argument('--output', type=str, required=True, help="Path to write to")
        parser.add_argument('--dataset', type=str, required=True, choices=["train", "test"], help="Which dataset to use")
        parser.add_argument('--devsplit', type=float, default="0.2", help="faction for dev")
        parser.add_argument('--with-units', type=bool, default=True, help="split with units")

    def handle(self, *args, **options):
        if options['output'] is None:
            raise CommandError("Must set output")

        # Get only the train expressions.
        if options['dataset'] == 'test':
            expressions = list(NumericExpression_Test.objects.all()) 
            options['devsplit'] = 0.
        else:
            expressions = list(NumericExpression_Train.objects.all())

        # Get arguments that should be in the train and dev splits.
        groups = [tuple(e.arguments) for e in expressions]
        random.seed(42)
        random.shuffle(groups)
        pivot = round(len(groups) * (1. - options['devsplit']))
        train = set(groups[:pivot]) # everything not in train is in dev.

        with open(options['output']+'-train.tsv', 'w') as f1:
            train_writer = csv.writer(f1, delimiter='\t')
            with open(options['output']+'-dev.tsv', 'w') as f2:
                dev_writer = csv.writer(f2, delimiter='\t')

                for expr in expressions:
                    writer = train_writer if tuple(expr.arguments) in train else dev_writer
                    for response in expr.responses.all():
                        #writer.writerow([expr.id, response.id, expr.get_easy_prompt_no_units(), response.description])
                        # Robin's program doesn't handle ids.
                        #if options['with_units']:
                        #    writer.writerow([expr.get_easy_prompt(), response.description])
                        #else:
                        writer.writerow([expr.get_easy_prompt_no_units(), response.description])

