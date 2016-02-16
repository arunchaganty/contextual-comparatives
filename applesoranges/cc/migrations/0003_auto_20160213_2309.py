# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('cc', '0002_remove_numericdata_type'),
    ]

    operations = [
        migrations.AlterField(
            model_name='numericdata',
            name='qualifiers',
            field=models.TextField(default=b'', help_text=b'Information that qualifies this figure', blank=True),
        ),
    ]
