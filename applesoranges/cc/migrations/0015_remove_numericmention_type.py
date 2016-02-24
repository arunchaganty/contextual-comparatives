# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('cc', '0014_auto_20160224_0925'),
    ]

    operations = [
        migrations.RemoveField(
            model_name='numericmention',
            name='type',
        ),
    ]
