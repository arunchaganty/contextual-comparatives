# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models
import django.contrib.postgres.fields


class Migration(migrations.Migration):

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='Sentence',
            fields=[
                ('id', models.AutoField(auto_created=True, verbose_name='ID', primary_key=True, serialize=False)),
                ('doc_id', models.TextField()),
                ('sentence_index', models.IntegerField()),
                ('words', django.contrib.postgres.fields.ArrayField(size=None, base_field=models.TextField())),
                ('lemmas', django.contrib.postgres.fields.ArrayField(size=None, base_field=models.TextField())),
                ('pos_tags', django.contrib.postgres.fields.ArrayField(size=None, base_field=models.TextField())),
                ('ner_tags', django.contrib.postgres.fields.ArrayField(size=None, base_field=models.TextField())),
                ('doc_char_begin', django.contrib.postgres.fields.ArrayField(size=None, base_field=models.IntegerField())),
                ('doc_char_end', django.contrib.postgres.fields.ArrayField(size=None, base_field=models.IntegerField())),
                ('dependencies', models.TextField()),
                ('gloss', models.TextField()),
            ],
        ),
    ]
