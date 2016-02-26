# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models
import django.contrib.postgres.fields


class Migration(migrations.Migration):

    dependencies = [
        ('cc', '0016_auto_20160224_2249'),
    ]

    operations = [
        migrations.CreateModel(
            name='NumericMentionExpressionTask',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('candidates', django.contrib.postgres.fields.ArrayField(help_text=b'Candidate expression responses', base_field=models.IntegerField(), size=None)),
                ('mention', models.ForeignKey(help_text=b'linked numeric mention', to='cc.NumericMention')),
            ],
        ),
    ]
