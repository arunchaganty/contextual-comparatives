#!/usr/bin/env python2.7
# -*- coding: utf-8 -*-
from django.core.management.base import BaseCommand, CommandError
from django.db import transaction
from cc.models import *
from javanlp.pgutil import parse_psql_array

import sys, csv

class Command(BaseCommand):
    """Reads inputs from file."""

    def add_arguments(self, parser):
        import argparse
        parser.add_argument('--input', type=argparse.FileType('r'), default=sys.stdin, help="Path to read file from")
        #parser.add_argument('--system', type=str, required=True, help="Path to read file from")
        parser.add_argument('--update-from-db', action='store_true', help="Just update the perspectives from facts")

    def handle(self, *args, **options):
        if options['update_from_db']:
            with transaction.atomic():
                for p in NumericPerspectiveTask.objects.filter(system__in = ['baseline', 'generation_baseline']):
                    p.perspective = p.expression.perspective()
                    p.save()
                print("updated.")
            return
        #elif options['input'] is None:
        #    raise CommandError("Must set input")

        #system = options['system']
        #assert system == "generation"
        #reader = csv.reader(options['input'], delimiter='\t')
        #header = next(reader)
        #assert header == ["id","input","output","score",]
        #with transaction.atomic():
        #    for id, _, output, _ in reader:
        #        try:
        #            p = NumericPerspectiveTask.objects.get(id = id)
        #            assert p.system == system
        #            #print(p.id, p.perspective)
        #            p.perspective = output.strip()
        #            #print(p.id, p.perspective)
        #            p.save()
        #        except NumericPerspectiveTask.DoesNotExist:
        #            print("WARNING: ", id, output)
        #            continue

