#!/usr/bin/env python2.7
# -*- coding: utf-8 -*-
"""

"""

def first(lst):
    """
    Get first element of an iterable
    """
    return next(iter(lst))

def index_list(l):
    """
    Make an index out of a list of keys.
    """
    return dict([(x, i) for i, x in enumerate(l)])


class HeaderedRow(list):
    """
    A list which can be indexed by strings.
    """

    def __init__(self, hdr, idx, *args, **kwargs):
        list.__init__(self, *args, **kwargs)
        self.hdr = hdr
        self.idx = idx

    def __getitem__(self, key):
        if isinstance(key, int):
            return list.__getitem__(self, key)
        else:
            return list.__getitem__(self, self.idx[key])

    def __setitem__(self, key, val):
        if isinstance(key, int):
            return list.__setitem__(self, key, val)
        else:
            return list.__setitem__(self, self.idx[key], val)

    @staticmethod
    def from_iterable(lst):
        """
        Assumes first row of iterable is the header and the rest is the data.
        """
        hdr = next(lst)
        idx = index_list(hdr)
        return [HeaderedRow(hdr, idx, row) for row in lst]

def parse_unit(unit):
    num = []
    denom = []
    in_denom = False
    for u in unit.split():
        if u == "per":
            in_denom = True
        else:
            if in_denom:
                denom.append(u)
            else:
                num.append(u)
            in_denom = False    
    return num, denom

def make_unit(num, denom):
    return " ".join(sorted(num) + sorted("per " + u for u in denom))

def easy_unit(unit):
    converter = {"weight" : "kilogram",
                 "length" : "meter",
                 "area" : "square-meter",
                 "money" : "USD",
                 "volume" : "liter",
                 "time" : "second",
                 }


    plural = {"foot" : "feet",
              "feet" : "feet",
              "USD" : "USD",
              "person" : "people",
             }

    # Convert parts
    unit = " ".join([converter.get(u, u) for u in unit.split()])
    numerator, denominator = parse_unit(unit)
    # plurality
    numerator = [plural.get(u, u + "s") for u in numerator]
    return make_unit(numerator, denominator)

def round_multiplier(n):
    if n >= 0.9:
        n = round(n)
        if n >= 10: # Round to nearest multiple of 10.
            n = round(n / 10) * 10
        if n >= 100: # Round to nearest multiple of 10.
            n = round(n / 100) * 100
        if n >= 1000: # Round to nearest multiple of 10.
            n = round(n / 1000) * 1000
        return str(int(n))
    else:
        return "1/" + round_multiplier(1./n)

def easy_number(n):
    if n >= 1e9:
        return "%d billion"%(round(n/1e9))
    elif n >= 1e6:
        return "%d million"%(round(n/1e6))
    elif n >= 1e3:
        return "%d thousand"%(round(n/1e3))
#    elif n >= 1e2:
#        return "%d hundred"%(round(n/1e2))
    elif n >= 1:
        return "%d"%(round(n))
    elif n > 0.01:
        return "%.2f"%(n)
    else: # Cheating.
        return "0.001"

