# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models
import django.contrib.postgres.fields


class Migration(migrations.Migration):

    dependencies = [
        ('cc', '0017_numericmentionexpressiontask'),
    ]

    operations = [
        migrations.CreateModel(
            name='NumericMentionExpressionTaskResponse',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('chosen', django.contrib.postgres.fields.ArrayField(help_text=b'Candidates chosen by turker', base_field=models.IntegerField(), size=None)),
                ('assignment_id', models.CharField(max_length=1024)),
                ('worker_id', models.CharField(max_length=1024)),
                ('worker_time', models.DurationField()),
                ('approval', models.BooleanField(default=True)),
                ('inspected', models.BooleanField(default=False)),
                ('task', models.ForeignKey(help_text=b'task turker rated', to='cc.NumericMentionExpressionTask')),
            ],
        ),
        migrations.AlterUniqueTogether(
            name='numericmentionexpressiontaskresponse',
            unique_together=set([('assignment_id', 'task')]),
        ),
    ]
