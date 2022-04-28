# Elasticsearch/Luence Tips

## Tip
To understand how search works it is very important how Elasticsearch does Analysis and how to use analyzers.    
[Analysis Intro](https://www.elastic.co/guide/en/elasticsearch/guide/master/analysis-intro.html)


To understand what is going on how analyzers breake down tokens, you can use the analyze API to see how text is analyzed. 
```
GET /_analyze
{
  "analyzer": "standard",
  "text": "Text to analyze"
}
```
Terms:
```
{
   "tokens": [
      {
         "token":        "text",
         "start_offset": 0,
         "end_offset":   4,
         "type":         "<ALPHANUM>",
         "position":     1
      },
      {
         "token":        "to",
         "start_offset": 5,
         "end_offset":   7,
         "type":         "<ALPHANUM>",
         "position":     2
      },
      {
         "token":        "analyze",
         "start_offset": 8,
         "end_offset":   15,
         "type":         "<ALPHANUM>",
         "position":     3
      }
   ]
}

```

### Testing Mapping Analyzers
Analyze API to test the mapping for string fields by name
```
GET /gb/_analyze
{
  "field": "text_field_1",
  "text": "Yellow Jackets" 
}
```

## Tip
Understand the difference between `Inner Object` and `Nested Object`.    
[How Inner Objects are Indexed](https://www.elastic.co/guide/en/elasticsearch/guide/master/complex-core-fields.html#_how_inner_objects_are_indexed)  
[Nested Objects](https://www.elastic.co/guide/en/elasticsearch/guide/master/nested-objects.html)

### Nested Object Mapping Example
```
PUT /my_index
{
  "mappings": {
    "blogpost": {
      "properties": {
        "comments": {
          "type": "nested", 
          "properties": {
            "name":    { "type": "string"  },
            "age":     { "type": "short"   },
          }
        }
      }
    }
  }
}
```

## Tip
For documents has an ID as a Number. Lucence Query Numbers as Ranges.  Making ID as a keyword value is faster    
Looking for exact value, better to make it has string    

```
PUT my_index
{
  "mappings": {
    "properties": {
      "status_code": { 
        "type":       "keyword"
      },
      "session_id": { 
        "type":       "keyword",
        "doc_values": false
      }
    }
  }
}
```

# Tip
If you are sure that you don’t need to sort or aggregate on a field, or access the field value from a script, you can `disable doc` values in order to save disk space.

# Tip
Dynamic mapping determines the datatype for the field and automatically adds the new field to the type mapping.    

It is a good idea to make the `dynamic mapping` as `strict`.
`strict` Throw an exception if an unknown field is encountered

```
PUT /my_index
{
    "mappings": {
        "my_type": {
            "dynamic":      "strict", 
            "properties": {
                "title":  { "type": "string"}
            }
        }
    }
}
```

# Tip 
Luence Field Types

[Lucene Field JavaDoc](https://lucene.apache.org/core/7_3_1/core/org/apache/lucene/document/Field.html)
```
TextField: Reader or String indexed for full-text search
StringField: String indexed verbatim as a single token
IntPoint: int indexed for exact/range queries.
LongPoint: long indexed for exact/range queries.
FloatPoint: float indexed for exact/range queries.
DoublePoint: double indexed for exact/range queries.
SortedDocValuesField: byte[] indexed column-wise for sorting/faceting
SortedSetDocValuesField: SortedSet<byte[]> indexed column-wise for sorting/faceting
NumericDocValuesField: long indexed column-wise for sorting/faceting
SortedNumericDocValuesField: SortedSet<long> indexed column-wise for sorting/faceting
StoredField: Stored-only value for retrieving in summary results
```


# Links
[Lucene Upgrade in Jira 8.0 How we fought and killed the code debt beast ](https://youtu.be/WDPe4cjURNI)
