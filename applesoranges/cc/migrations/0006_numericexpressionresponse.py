# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('cc', '0005_auto_20160213_2355'),
    ]

    operations = [
        migrations.CreateModel(
            name='NumericExpressionResponse',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('prompt', models.TextField(help_text=b'prompt given to the turker')),
                ('description', models.TextField(help_text=b'description returned by the turker')),
                ('worker_id', models.CharField(max_length=1024)),
                ('worker_time', models.DurationField()),
                ('expression', models.ForeignKey(help_text=b'expression', to='cc.NumericExpression')),
            ],
        ),
    ]
