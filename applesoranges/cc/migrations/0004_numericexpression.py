# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models
import django.contrib.postgres.fields


class Migration(migrations.Migration):

    dependencies = [
        ('cc', '0003_auto_20160213_2309'),
    ]

    operations = [
        migrations.CreateModel(
            name='NumericExpression',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('multiplier', models.FloatField(help_text=b'multiplier for expression')),
                ('arguments', django.contrib.postgres.fields.ArrayField(help_text=b'Arguments to this expression (always multiplied)', base_field=models.IntegerField(), size=None)),
                ('value', models.FloatField(help_text=b'value for expression')),
                ('unit', models.FloatField(help_text=b'unit for expression')),
            ],
        ),
    ]
