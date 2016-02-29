#!/usr/bin/env python2.7
# -*- coding: utf-8 -*-
from __future__ import print_function
from django.core.management.base import BaseCommand, CommandError
from cc.models import NumericData, NumericMention, NumericExpression, NumericExpressionResponse, NumericMentionExpression

import sys
import random
import csv

class Command(BaseCommand):
    """Generates a greppable output."""

    def handle(self, *args, **options):
        mentions = NumericMention.objects.filter(id__in = NumericMentionExpression.objects.values_list('mention'))
        for mention in mentions:
            print(mention.id, file=sys.stderr)
            print("m: " + mention.html())
            exprs = NumericExpression.objects.filter(id__in = mention.numericmentionexpression_set.values_list('expression'))
            for expr in exprs:
                print ("\te: " + expr.get_prompt())
                for persp in expr.numericexpressionresponse_set.all():
                    print ("\t\tp: " + persp.description)

