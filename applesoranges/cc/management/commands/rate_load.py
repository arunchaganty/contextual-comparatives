from django.core.management.base import BaseCommand, CommandError
from cc.util import HeaderedRow
from cc.models import *
from django.db import transaction
from urllib.parse import unquote
from collections import Counter
import ipdb

import os, stat, csv
import json

class Command(BaseCommand):
    """Takes mturk repsonse CSV from [source], and loads into database."""

    def add_arguments(self, parser):
        parser.add_argument('--input', type=str, help="Path to read file from")

    def handle(self, *args, **options):
        if options['input'] is None:
            raise CommandError("Must set input")

        with open(options['input']) as f, transaction.atomic():
            data = HeaderedRow.from_iterable(csv.reader(f))

            counter = Counter()
            task_responses = []
            for datum in data:
                assignment_id = datum["AssignmentId"]
                worker_id = datum["WorkerId"]
                worker_time = datum["WorkTimeInSeconds"]

                comments = datum["Answer.comments"] if datum["Answer.comments"] != '{}' else ""
                #ids = [r['task_id'] for r in json.loads(unquote(datum["Answer.responses"]))]
                responses = json.loads(unquote(datum["Answer.responses"]))

                #if len(responses) == 0:
                #    print(assignment_id, worker_id, worker_time, comments, responses)

                #for task_id in ids:
                for response in responses:
                    task_id = response["task_id"]
                    #response = int(datum['Answer.response-'+str(task_id)])
                    winner = response["response"]
                    counter[winner] += 1

                    task_response = NumericPerspectiveRatingTaskResponse(
                        task_id = task_id,
                        winner = winner,
                        assignment_id = assignment_id,
                        worker_id = worker_id,
                        worker_time = worker_time,
                        approval = False,
                        inspected = False,
                        comments = comments
                        )
                    task_responses.append(task_response)

            print(counter)
            NumericPerspectiveRatingTaskResponse.objects.bulk_create(task_responses)
