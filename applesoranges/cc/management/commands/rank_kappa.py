from __future__ import division
from django.core.management.base import BaseCommand, CommandError
from cc.models import *

import ipdb
import random
import csv
import sys
import argparse

class Command(BaseCommand):
    """
    Compute IAA and Cohen's kappa for rank dataset.
    """

    def compute_iaa(self):
        tasks = NumericMentionExpressionTask.objects.filter(response__id__gte = 0).distinct()
        iaa, cnt = 0, 0
        for task in tasks:
            agreements_, disagreements_, cnt_ = 0, 0, 0
            votes = list(task.responses.all())

            for c in votes:
                for c_ in votes:
                    # Do c and c_ agree on all their labels?
                    ipdb.set_trace()
                    if len(c.chosen) == 0 and len(c_.chosen) == 0 or not set.isdisjoint(set(c.chosen), c_.chosen):
                        agreements_ += 1
                    cnt_ += 1

            # update agreements, disagreements with their average.
            iaa_ = agreements_ / cnt_
            iaa += (iaa_ - iaa) / (cnt + 1)
            cnt += 1
        print("IAA on %d tasks is: %.2f%%"%(cnt, iaa*100))

    def compute_majority_iaa(self):
        tasks = NumericMentionExpressionTask.objects.filter(response__id__gte = 0).distinct()
        iaa, cnt = 0, 0
        for task in tasks:
            agreements_, disagreements_, cnt_ = 0, 0, 0
            majority_choices = set(None if c is None else c.id for c in task.get_positive_candidates())
            if len(majority_choices) == 0:
                continue
            votes = list(task.responses.all())

            for v in votes:
                if None in majority_choices and len(v.chosen) == 0 or not set.isdisjoint(majority_choices, v.chosen):
                    agreements_ += 1
                cnt_ += 1

            # update agreements, disagreements with their average.
            iaa_ = agreements_ / cnt_
            iaa += (iaa_ - iaa) / (cnt + 1)
            cnt += 1
        print("IAA (majority) on %d tasks is: %.2f%%"%(cnt, iaa*100))


    def handle(self, *args, **options):
        #self.compute_iaa()
        self.compute_majority_iaa()
