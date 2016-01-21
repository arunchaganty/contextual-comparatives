# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('javanlp', '0002_auto_20151105_2216'),
    ]

    operations = [
        migrations.CreateModel(
            name='NumericData',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('name', models.TextField(help_text=b'Name of the object')),
                ('relation', models.TextField(help_text=b'Relation of the object to value')),
                ('value', models.FloatField(help_text=b'Value of measurement')),
                ('unit', models.CharField(help_text=b'Value of measurement', max_length=128)),
                ('type', models.CharField(help_text=b'Broad category of unit', max_length=128)),
                ('qualifiers', models.TextField(help_text=b'Information that qualifies this figure', blank=True)),
            ],
        ),
        migrations.CreateModel(
            name='NumericMention',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('value', models.FloatField(help_text=b'Stores the absolute value of the mention')),
                ('unit', models.CharField(help_text=b'Stores the unit of the mention', max_length=128)),
                ('type', models.CharField(help_text=b'Broad category of unit', max_length=128)),
                ('doc_char_begin', models.IntegerField()),
                ('doc_char_end', models.IntegerField()),
                ('token_begin', models.IntegerField()),
                ('token_end', models.IntegerField()),
                ('sentence', models.ForeignKey(help_text=b'Sentence mention was extracted from', to='javanlp.Sentence')),
            ],
        ),
    ]
