# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('cc', '0013_numericexpressionresponse_inspected'),
    ]

    operations = [
        migrations.AddField(
            model_name='numericmention',
            name='normalized_unit',
            field=models.CharField(default='', help_text=b'Stores the normalized unit of the mention', max_length=128),
            preserve_default=False,
        ),
        migrations.AddField(
            model_name='numericmention',
            name='normalized_value',
            field=models.FloatField(default=0, help_text=b'Stores the normalized value of the mention'),
            preserve_default=False,
        ),
    ]
