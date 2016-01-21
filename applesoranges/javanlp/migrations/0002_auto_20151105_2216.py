# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('javanlp', '0001_initial'),
    ]

    operations = [
        migrations.AddField(
            model_name='sentence',
            name='constituencies',
            field=models.TextField(null=True),
        ),
        migrations.AlterField(
            model_name='sentence',
            name='dependencies',
            field=models.TextField(null=True),
        ),
    ]
