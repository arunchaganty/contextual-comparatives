from django.core.management.base import BaseCommand, CommandError
from cc.util import HeaderedRow
from cc.models import *
from collections import Counter

import ipdb


import os, stat, csv
import json

class Command(BaseCommand):
    """Takes mturk repsonse CSV from [source], and loads into database."""

    def handle(self, *args, **options):
        SYSTEMS = ('baseline', 'generation_baseline', 'generation')
        tasks = NumericPerspectiveTaskResponse.object.all().values_list('task_id')
        tasks = NumericPerspectiveTask.objects.filter(id__int = tasks)

        # counter by system, system, length, system, rank, system rank
        # length x attribute (of course)
        stats = Counter()
        # We care about ordering, so have to go by mention.
        mentions = NumericMention.objects.filter(id__in = tasks.values_list('mention_id'))

        #ipdb.set_trace()

        for mention in mentions:
            stats['count'] += 1
            for system in SYSTEMS:

                score, fhrl = 0, None
                for rank, task in enumerate(mention.perspective_tasks.filter(system = system).order_by('-score')):
                    if task.responses.count() == 0: continue # weird stuff.
                    f, h, r = task.framing(), task.helpfulness(), task.relevance()
                    arglen = len(task.expression.arguments)

                    if h >= score:
                        score, fhrl = h, (f,h,r,arglen, rank)

                    stats[(system, 'count')] += 1
                    stats[(system, 'f')]  += f
                    stats[(system, 'h')]  += h
                    stats[(system, 'r')]  += r

                    stats[(system, rank, 'count')]  += 1
                    stats[(system, rank, 'f')]  += f
                    stats[(system, rank, 'h')]  += h
                    stats[(system, rank, 'r')]  += r

                    stats[(system, '*', arglen, 'count')] += 1
                    stats[(system, '*', arglen, 'f')]  += f
                    stats[(system, '*', arglen, 'h')]  += h
                    stats[(system, '*', arglen, 'r')]  += r

                    stats[(system, rank, arglen, 'count')] += 1
                    stats[(system, rank, arglen, 'f')]  += f
                    stats[(system, rank, arglen, 'h')]  += h
                    stats[(system, rank, arglen, 'r')]  += r

                if fhrl is None: continue

                if system == 'baseline' and (fhrl is None or fhrl[3] != 1):
                    ipdb.set_trace()
                    #assert fhrl[3] == 1

                stats[(system, 'best', 'count')] += 1
                stats[(system, 'best', 'f')]  += fhrl[0]
                stats[(system, 'best', 'h')]  += fhrl[1]
                stats[(system, 'best', 'r')]  += fhrl[2]

                stats[(system, 'best', fhrl[3], 'count')] += 1
                stats[(system, 'best', fhrl[3], 'f')]  += fhrl[0]
                stats[(system, 'best', fhrl[3], 'h')]  += fhrl[1]
                stats[(system, 'best', fhrl[3], 'r')]  += fhrl[2]


        def pstats(c, *root):
            f, h, r, t = c[root + ('f',)], c[root + ('h',)], c[root + ('r',)], c[root + ('count',)]
            if t != 0:
                print("%s\t%.2f\t%.2f\t%.2f\t%d"%(root, f/t, h/t, r/t, t))



        print(stats['count'])
        # print stats.
        print("system")
        for system in SYSTEMS:
            pstats(stats, system)
        print("system")
        for system in SYSTEMS:
            pstats(stats, system, 'best')
        for rank in range(3):
            print("system, rank")
            for system in SYSTEMS:
                pstats(stats, system, rank)
            for arglen in range(1,4):
                print("system, rank, arglen")
                for system in SYSTEMS:
                    pstats(stats, system, rank, arglen)
        for arglen in range(1,4):
            print("system, arglen")
            for system in SYSTEMS:
                pstats(stats, system, '*', arglen)
            print("system, arglen")
            for system in SYSTEMS:
                pstats(stats, system, 'best', arglen)
