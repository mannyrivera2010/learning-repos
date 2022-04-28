from rdflib import Graph, Literal, BNode, Namespace, RDF, URIRef
from rdflib.namespace import DC, FOAF

family_ns = Namespace("http://earasoft.com/family/0.1/")

g = Graph()

g.bind("family", family_ns)
g.bind("foaf", FOAF)


def createPerson(id, name):
    """
    method responsible for creating person to graph
    """
    current_person = URIRef(id)
    g.add( (current_person, RDF.type, FOAF.Person) )
    g.add( (current_person, FOAF.name, Literal(name)) )
    return current_person


def createFamily(id):
    """
    method responsible for creating families
    """
    current_person = URIRef(id)
    g.add( (current_person, RDF.type, family_ns.Family) )
    return current_person


def linkFamily(father, mom, family, sons=[], daugthers=[]):
    """
    method responsible for linking families
    """
    g.add( (mom, family_ns.isMother, family) )
    g.add( (father, family_ns.isFather, family) )

    for son in sons:
        g.add( (son, family_ns.isSon, family) )

    for daugther in daugthers:
        g.add( (daugther, family_ns.isDaugther, family) )


# gen - family - id
entities = {
    # ----------------------------------------------
    # ----------- GEN 1 - sub 1 -----------------------------
    # ----------------------------------------------
    # gen1 - family 00
    "m00": createPerson("http://earasoft.com/dataset1/person/m00", "Christos Bates"), # father
    "w00": createPerson("http://earasoft.com/dataset1/person/w00", "Rebecca Berger"), # mother
    "f00": createFamily("http://earasoft.com/dataset1/family/f00"),
    "f00-tree": [ "m00", "w00", "f00", ["m01"], [] ],
    "m01": createPerson("http://earasoft.com/dataset1/person/m01", "David Smith"), # son - Donna Fales

    # gen1 - family 10
    "m10": createPerson("http://earasoft.com/dataset1/person/m10", "Conner Watkins"), # father
    "w10": createPerson("http://earasoft.com/dataset1/person/w10", "Kelise Beck"), # mother
    "f10": createFamily("http://earasoft.com/dataset1/family/f10"),
    "f10-tree": [ "m10", "w10", "f10", [], ["w11"] ],
    "w11": createPerson("http://earasoft.com/dataset1/person/w11", "Donna Fales"), # daugther - David Smith (gen 2 - family 1)

    # gen1 - family 20
    "m20": createPerson("http://earasoft.com/dataset1/person/m20", "Conan Mendez"),  # father
    "w20": createPerson("http://earasoft.com/dataset1/person/w20", "Katrina Hartley"), # mother
    "f20": createFamily("http://earasoft.com/dataset1/family/f20"),
    "f20-tree": [ "m20", "w20", "f20", ["m21"], [] ],
    "m21": createPerson("http://earasoft.com/dataset1/person/m21", "Kayne House"), # son - Emmy Bannister (gen 2 - family 2)

    # gen1 - family 40
    "m30": createPerson("http://earasoft.com/dataset1/person/m30", "Darien Murphy"), # father
    "w30": createPerson("http://earasoft.com/dataset1/person/w30", "Susan Schneider"),  # mother
    "f30": createFamily("http://earasoft.com/dataset1/family/f30"),
    "f30-tree": [ "m30", "w30", "f30", [], ["w31"] ],
    "w31": createPerson("http://earasoft.com/dataset1/person/w31", "Emmy Bannister"), # daugther - Kayne House

    # ----------------------------------------------
    # ----------- GEN 1 - sub 2 -----------------------------
    # ----------------------------------------------
    # # gen1 - family 40
    # "m40": createPerson("http://earasoft.com/dataset1/person/m40", "Ray Carver"), # father
    # "w40": createPerson("http://earasoft.com/dataset1/person/w40", "Jazmyn Schmitt"), # mother
    # "f40": createFamily("http://earasoft.com/dataset1/family/f40"),
    # "f40-tree": [ "m40", "w40", "f40", ["m41"], [] ],
    # "m41": createPerson("http://earasoft.com/dataset1/person/m41", "Jacob Head"), # son - Donna Fales
    #
    # # gen1 - family 50
    # "m50": createPerson("http://earasoft.com/dataset1/person/m50", "Carlie Marquez"), # father
    # "w50": createPerson("http://earasoft.com/dataset1/person/w50", "Kiana Muir"), # mother
    # "f50": createFamily("http://earasoft.com/dataset1/family/f50"),
    # "f50-tree": [ "m50", "w50", "f50", [], ["w51"] ],
    # "w51": createPerson("http://earasoft.com/dataset1/person/w51", "Donna Fales"), # daugther - David Smith (gen 2 - family 1)
    #
    # # gen1 - family 60
    # "m60": createPerson("http://earasoft.com/dataset1/person/m60", "Lamar Jacobs"),  # father
    # "w60": createPerson("http://earasoft.com/dataset1/person/w60", "Emma-Louise Scott"), # mother
    # "f60": createFamily("http://earasoft.com/dataset1/family/f60"),
    # "f60-tree": [ "m60", "w60", "f60", ["m61"], [] ],
    # "m61": createPerson("http://earasoft.com/dataset1/person/m61", "Kayne House"), # son - Emmy Bannister (gen 2 - family 2)
    #
    # # gen1 - family 70
    # "m70": createPerson("http://earasoft.com/dataset1/person/m70", "Edie Peel"), # father
    # "w70": createPerson("http://earasoft.com/dataset1/person/w70", "Emilia Archer"),  # mother
    # "f70": createFamily("http://earasoft.com/dataset1/family/f70"),
    # "f70-tree": [ "m70", "w70", "f70", [], ["w71"] ],
    # "w71": createPerson("http://earasoft.com/dataset1/person/w71", "Coco Phelps"), # daugther - Kayne House

    # ----------------------------------------------
    # ----------- GEN 2 -----------------------------
    # ----------------------------------------------
    # gen 2 - family1  David Smith (m01) - Donna Fales
    "f500": createFamily("http://earasoft.com/dataset1/family/f40"),
    "f500-tree": [ "m01", "w11", "f500", ["m501", "m502", "m503"], ["w501","w502","w503"] ],
    "m501": createPerson("http://earasoft.com/dataset1/person/m501", "Clinton Yates"), # son
    "m502": createPerson("http://earasoft.com/dataset1/person/m502", "Allan Montes"), # son
    "m503": createPerson("http://earasoft.com/dataset1/person/m503", "Campbell Rhodes"), # son
    "w501": createPerson("http://earasoft.com/dataset1/person/w501", "Jamie Lee Woodley"), # daugther
    "w502": createPerson("http://earasoft.com/dataset1/person/w502", "Bayley Fletcher"), # daugther
    "w503": createPerson("http://earasoft.com/dataset1/person/w503", "Milla Haigh"), # daugther

    # gen 3 - family 3  Kayne House -
    # "f2": createFamily("http://earasoft.com/dataset1/family/2"),
    # "m2": createPerson("http://earasoft.com/dataset1/person/25", "Kayne House"), # father
    # "w2": createPerson("http://earasoft.com/dataset1/person/5", "Emmy Bannister"), # mother
    #
    # "m2.2": createPerson("http://earasoft.com/dataset1/person/26", "Ewen Mcphee"), # son
    #
    # "w2.2": createPerson("http://earasoft.com/dataset1/person/6", "Keeley Fuentes"), # daugther
    # "w2.3": createPerson("http://earasoft.com/dataset1/person/7", "Eiliyah Rivera"),  # daugther
    #
    # # family3
    # "f3": createFamily("http://earasoft.com/dataset1/family/3"),
    #
    #
    # "f4": createFamily("http://earasoft.com/dataset1/family/4"),
    #
    #
    # "f2.1": createFamily("http://earasoft.com/dataset1/family/5"),
    # # "m2.2": createPerson("http://earasoft.com/dataset1/person/26", "Ewen Mcphee"), # son
    # # "w1.2": createPerson("http://earasoft.com/dataset1/person/2", "Jamie Lee Woodley"), # daugther
    #
    # "w50": createPerson("http://earasoft.com/dataset1/person/8", "Rebecca Berger"),
    # "m11": createPerson("http://earasoft.com/dataset1/person/31", "Nicholas Cairns"),
    # "m12": createPerson("http://earasoft.com/dataset1/person/32", "Harper Gutierrez"),
    # "m13": createPerson("http://earasoft.com/dataset1/person/33", "Darnell Horne")
}


def e(key):
    return entities[key]


def t(key):
    first_list = e(key)
    return tree_recur_help(first_list)


def tree_recur_help(input_list):
    output_list = []

    for i in input_list:
        if isinstance(i, str):
            output_list.append(e(i))
        else:
            output_list.append(tree_recur_help(i))

    return output_list


def query_and_print(query, format):
    print(q)
    print("-------------")
    qres = g.query(q)

    for row in qres:
        if format:
            print(format % row)
        else:
            print(row)


def print_div(input):
    print("\n=======================")
    print("----- {} -----".format(input))


families = [
    t("f00-tree"),
    t("f10-tree"),
    t("f20-tree"),
    t("f30-tree"),
    # t("f40-tree"),
    t("f500-tree")
]

for current_family in families:
    linkFamily(current_family[0], current_family[1], current_family[2], current_family[3], current_family[4])

print("graph has %s statements." % len(g))
# prints graph has 79 statements.

for subj, pred, obj in g:
   if (subj, pred, obj) not in g:
       raise Exception("It better be!")

s = g.serialize(format='turtle')

print(s.decode("utf-8"))

# ----------------------
print_div("Families")
q = """
SELECT
    ?aname
WHERE {
    ?aname a family:Family .
}
"""
query_and_print(q, "%s")
# ----------------------

print_div("All People Names")
q = """
SELECT
    ?person_name
WHERE {
    ?person a foaf:Person .
    ?person foaf:name ?person_name.
}
"""
query_and_print(q, "%s")
# ----------------------

print_div("Mothers Names")
q = """
SELECT
    ?person_name  ?family
WHERE {
    ?person a foaf:Person .
    ?person family:isMother ?family .
    ?person foaf:name ?person_name.
}
order by asc(?person_name)
"""
query_and_print(q, "%s is the mother of  %s")
# ----------------------

print_div("Fathers Names with kids names")
q = """
SELECT
    ?person_name
    ?family
    ?daugther_name
    ?son_name
WHERE {
    ?person a foaf:Person .
    ?person family:isFather ?family .
    ?person foaf:name ?person_name .

    {
        ?son family:isSon ?family .
        ?son foaf:name ?son_name .
    }
    UNION
    {
        ?daugther family:isDaugther ?family .
        ?daugther foaf:name ?daugther_name .
    }

}
order by asc(?person_name)
"""
query_and_print(q, "%s \t %s \n\t %s \n\t %s")
# ----------------------

print_div("Parents Names with kids (sons/daugthers) names")
q = """
SELECT
    ?father_name
    ?mother_name
    ?family
    (group_concat(?son_name; separator=", ") as ?son_names)
    (group_concat(?daugther_name; separator=", ") as ?d_names)
WHERE {
    ?person a foaf:Person .
    ?person family:isFather ?family .
    ?person foaf:name ?father_name .

    ?person1 a foaf:Person .
    ?person1 family:isMother ?family .
    ?person1 foaf:name ?mother_name .

    {
        ?son family:isSon ?family .
        ?son foaf:name ?son_name .
    }
    UNION
    {
        ?daugther family:isDaugther ?family .
        ?daugther foaf:name ?daugther_name .
    }

}
group by ?father_name ?mother_name ?family
order by asc(?person_name)
"""
query_and_print(q, "%s \t %s \t %s \n\t Sons: %s \n\t Daugthers: %s")
# ----------------------

print_div("Parents Names with kids (kids) names")
q = """
SELECT
  ?father_name
  ?mother_name
  ?family
  (group_concat(?kids_name; separator=", ") as ?kids_names)
WHERE {
  ?person a foaf:Person .
  ?person family:isFather ?family .
  ?person foaf:name ?father_name .

    ?person1 a foaf:Person .
    ?person1 family:isMother ?family .
    ?person1 foaf:name ?mother_name .

  {
     ?son family:isSon ?family .
     ?son foaf:name ?kids_name .
  }
  UNION
  {
    ?daugther family:isDaugther ?family .
    ?daugther foaf:name ?kids_name .
  }

}
group by ?father_name ?mother_name ?family
order by asc(?person_name)
"""
query_and_print(q, "%s \t %s \t %s \n\t Kids: %s")
# ----------------------

print_div("Bayley Fletcher Siblings")
q = """
SELECT
  ?target_name ?siblings_name
WHERE {
    ?person a foaf:Person;
        foaf:name "Bayley Fletcher".

    ?person foaf:name ?target_name  .

    { ?person family:isSon ?family . }
    UNION
    { ?person family:isDaugther ?family . }

     {
        ?person1 family:isSon ?family .
        ?person1 foaf:name ?siblings_name .
     }
     UNION
     {
       ?person1 family:isDaugther ?family .
       ?person1 foaf:name ?siblings_name .
     }

  FILTER ( ?person != ?person1  )
}
"""
query_and_print(q, "%s \t %s")
