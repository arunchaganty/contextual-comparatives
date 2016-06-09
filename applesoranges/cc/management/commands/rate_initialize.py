#!/usr/bin/env python2.7
# -*- coding: utf-8 -*-
"""
Initialize the rating task from an existing qualitative description task.
"""

from django.core.management.base import BaseCommand, CommandError
from cc.models import *
from django.db import transaction
from django.utils.html import escape
import ipdb

import random
import csv

class Command(BaseCommand):
    """Generates a dataset from expressions."""

    def add_arguments(self, parser):
        import argparse
        import sys
        #parser.add_argument('--output', type=argparse.FileType('w'), default=sys.stdout, help="Path to write to")
        #parser.add_argument('--system', choices=('baseline', 'generation_baseline', 'generation'), required=True, help="System to use")
        pass

    def handle(self, *args, **options):
        # For every mention in NumericPerspectiveTask, with the systems
        # "baseline" and "generation", create a
        # NumericPerspectiveRankTask by choosing the best candidate from
        # each of these two systems.

        mention_ids = NumericPerspectiveTask.objects.all().values_list('mention_id')

        with transaction.atomic():
            for mention in NumericMention.objects.filter(id__in = mention_ids).order_by('id'):
                expressions = []
                perspectives = []
                systems = []
                scores = []

                # Get the best baseline perspective (scores are
                # exp(-distance)
                # from target value, so larger is better).
                # Get the best generation perspective (scores are on a 0-1
                # scale, 1 is best, so larger is better).
                for system in ["baseline", "generation"]:
                    perpsective = mention.perspective_tasks.filter(system=system).order_by('-score').first()
                    if perpsective is not None:
                        expressions.append(perpsective.expression_id)
                        perspectives.append(perpsective.perspective)
                        systems.append(perpsective.system)
                        scores.append(perpsective.score)
                NumericPerspectiveRatingTask(
                    mention = mention,
                    expressions = expressions,
                    perspectives = perspectives,
                    systems = systems,
                    scores = scores,
                    ).save()

