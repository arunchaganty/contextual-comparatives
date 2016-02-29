from django.core.management.base import BaseCommand, CommandError
from cc.util import HeaderedRow
from cc.models import *

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

            for datum in data:
                assignment_id = datum["AssignmentId"]
                worker_id = datum["WorkerId"]
                worker_time = datum["WorkTimeInSeconds"]

                if datum["Answer.comments"] != '{}':
                    print(datum["Answer.comments"])

                for response in json.loads(datum["Answer.responses"]):
                    task = NumericMentionExpressionTask.objects.get(id=response['task_id'])
                    responses = map(int, response['responses'])
                    if len(responses) == 1 and responses[0] == -1:
                        responses = []

                    NumericMentionExpressionTaskResponse.objects.bulk_create([
                        NumericMentionExpressionTaskResponse(
                            task = task,
                            chosen = responses,
                            assignment_id = assignment_id,
                            worker_id = worker_id,
                            worker_time = worker_time,
                            approval = False,
                            inspected = False,
                            )])

