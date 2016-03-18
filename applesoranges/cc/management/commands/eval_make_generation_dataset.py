#!/usr/bin/env python2.7
# -*- coding: utf-8 -*-
from django.core.management.base import BaseCommand, CommandError
from django.db import transaction
from cc.models import *
from javanlp.pgutil import parse_psql_array

import sys, csv

class Command(BaseCommand):
    """Make a generation dataset."""

    def add_arguments(self, parser):
        import argparse
        parser.add_argument('--output', type=argparse.FileType('w'), default=sys.stdout, help="Path to write to")
        parser.add_argument('--with-units', type=bool, default=False, help="split with units")

    def handle(self, *args, **options):
        writer = csv.writer(options['output'], delimiter='\t')
        writer.writerow(['id', 'expression'])

        for perspective in NumericPerspectiveTask.objects.filter(system='generation'):
            expr = perspective.expression
            writer.writerow([perspective.id, expr.get_easy_prompt_no_units(),])

