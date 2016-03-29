from django.core.management.base import BaseCommand, CommandError
from cc.util import HeaderedRow
from cc.models import *
from urllib.parse import unquote
from collections import Counter

import os, stat, csv
import json

class Command(BaseCommand):
    """Takes mturk repsonse CSV from [source], and loads into database."""

    def add_arguments(self, parser):
        parser.add_argument('--input', type=str, help="Path to read file from")

    def handle(self, *args, **options):
        if options['input'] is None:
            raise CommandError("Must set input")

        with open(options['input']) as f:
            data = HeaderedRow.from_iterable(csv.reader(f))

            counter = Counter()

            task_responses = []
            for datum in data:
                assignment_id = datum["AssignmentId"]
                worker_id = datum["WorkerId"]
                worker_time = datum["WorkTimeInSeconds"]

                comments = datum["Answer.comments"] if datum["Answer.comments"] != '{}' else ""
                responses = json.loads(unquote(datum["Answer.responses"]))

                if len(responses) == 0:
                    print(assignment_id, worker_id, worker_time, comments, responses)


                for response in responses:
                    counter[len(response['responses'])] += 1
            #        task = NumericPerspectiveTask.objects.get(id=response['task_id'])
            #        framing, helpfulness, relevance = map(int, response['responses'])

            #        task_response = NumericPerspectiveTaskResponse(
            #            task = task,
            #            framing = framing,
            #            helpfulness = helpfulness,
            #            relevance = relevance,
            #            assignment_id = assignment_id,
            #            worker_id = worker_id,
            #            worker_time = worker_time,
            #            approval = False,
            #            inspected = False,
            #            comments = comments
            #            )
            #        task_responses.append(task_response)

            #NumericPerspectiveTaskResponse.objects.bulk_create(task_responses)
            print(counter)
