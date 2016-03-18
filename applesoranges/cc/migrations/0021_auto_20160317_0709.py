# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('cc', '0020_numericexpression_train'),
    ]

    operations = [
        migrations.CreateModel(
            name='NumericPerspectiveTask',
            fields=[
                ('id', models.AutoField(primary_key=True, verbose_name='ID', auto_created=True, serialize=False)),
                ('perspective', models.TextField(help_text='Actual perspective')),
                ('system', models.TextField(help_text='System that generated the perspective')),
                ('score', models.FloatField(help_text='Score assigned by system')),
                ('expression', models.ForeignKey(to='cc.NumericExpression', help_text='linked numeric expression', related_name='perspective_tasks')),
                ('mention', models.ForeignKey(to='cc.NumericMention', help_text='linked numeric mention', related_name='perspective_tasks')),
            ],
        ),
        migrations.CreateModel(
            name='NumericPerspectiveTaskResponse',
            fields=[
                ('id', models.AutoField(primary_key=True, verbose_name='ID', auto_created=True, serialize=False)),
                ('framing', models.IntegerField(help_text="For the question: 'is this number smaller or larger than you thought?'")),
                ('usefulness', models.IntegerField(help_text="For the question: 'how useful was the description in helping appreciate the quantity?'")),
                ('relevance', models.IntegerField(help_text="For the question: 'how relevant were the facts described?'")),
                ('familiarity', models.IntegerField(help_text="For the question: 'how familiar were the facts described?'")),
                ('assignment_id', models.CharField(max_length=1024)),
                ('worker_id', models.CharField(max_length=1024)),
                ('worker_time', models.DurationField()),
                ('approval', models.BooleanField(default=True)),
                ('inspected', models.BooleanField(default=False)),
                ('comments', models.TextField()),
                ('task', models.ForeignKey(to='cc.NumericPerspectiveTask', help_text='Pointer to perspective task', related_name='responses')),
            ],
        ),
        migrations.AlterUniqueTogether(
            name='numericperspectivetaskresponse',
            unique_together=set([('assignment_id', 'task')]),
        ),
    ]
