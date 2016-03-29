# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('cc', '0021_auto_20160317_0709'),
    ]

    operations = [
        migrations.RenameField(
            model_name='numericperspectivetaskresponse',
            old_name='usefulness',
            new_name='helpfulness',
        ),
        migrations.RemoveField(
            model_name='numericperspectivetaskresponse',
            name='familiarity',
        ),
    ]
