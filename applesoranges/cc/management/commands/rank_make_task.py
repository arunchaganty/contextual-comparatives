from django.core.management.base import BaseCommand, CommandError
from cc.models import *

import random
import csv

class Command(BaseCommand):
    """Generates a dataset from expressions."""

    def handle(self, *args, **options):
        GROUP_SIZE = 4
        WINDOW_SHIFT = 4

        mentions = NumericMention.objects.filter(id__in = NumericMentionExpression.objects.values_list('mention'))

        for mention in mentions:
            expressions = NumericExpression.objects.filter(id__in = mention.numericmentionexpression_set.values_list('expression'))
            perspectives = [expr.numericexpressionresponse_set.order_by('?').first() for expr in expressions] # Choose a random delegate.
            # sliding window
            if len(perspectives) > GROUP_SIZE:
                choice_sets = [perspectives[i:i+GROUP_SIZE] for i in range(0, len(perspectives), WINDOW_SHIFT)]
            else:
                choice_sets = [perspectives,]
            for choice_set in choice_sets:
                task = NumericMentionExpressionTask.objects.create(
                    mention = mention,
                    candidates = [choice.id for choice in choice_set])
                task.save()
