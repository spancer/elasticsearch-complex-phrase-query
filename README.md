# Elasticsearch Complex Phrase Queries
Elasticsearch plugin intended to support complex phrase queries.
This is regular query string search, with *ComplexPhraseQueryParser* usage for
phrases in expression tree.
Usage
-----
    {
        "query": {
            "phrase_wildcard_query": {
                "query": "\"foo * baz\""
            }
        }
    }
