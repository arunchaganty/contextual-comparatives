# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('cc', '0004_numericexpression'),
    ]

    operations = [
        migrations.AlterField(
            model_name='numericexpression',
            name='unit',
            field=models.TextField(help_text=b'unit for expression'),
        ),
    ]
