package vm.phrase.plugin

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.queryparser.classic.QueryParserSettings
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser
import org.apache.lucene.search.*
import org.elasticsearch.common.logging.ESLogger
import org.elasticsearch.index.mapper.MappedFieldType
import org.elasticsearch.index.query.QueryParseContext
import java.util.*

class ComplexPhraseQueryBuilder {
    private var query: Query
    private var logger: ESLogger
    private var parseContext: QueryParseContext?
    private var settings: QueryParserSettings

    constructor(query: Query, logger: ESLogger, parseContext: QueryParseContext?, settings: QueryParserSettings){
        this.query = query;
        this.logger = logger;
        this.parseContext = parseContext;
        this.settings = settings;
    }

    fun build() : Query {
        return traverseQuery(this.query);
    }

    private fun traverseQuery(query: Query): Query {
        if (query is BooleanQuery){
            return traverseBooleanQuery(query);
        } else if (query is DisjunctionMaxQuery){
            return traverseDisjunctionQuery(query);
        }

        if (query is PhraseQuery) {
            return processPhrase(query);
        }

        return query;
    }

    private fun processPhrase(query: PhraseQuery): Query {
        var queryString = query.toString();

        var fieldName = getField(queryString)
        var analyzer = getAnalyzer(fieldName)

        var complexPhraseParser = getComplexPhraseQueryParser(analyzer, fieldName)

        var resultedQuery = complexPhraseParser.parse(queryString);

        this.logger.debug(resultedQuery.toString());

        return resultedQuery;
    }

    private fun getComplexPhraseQueryParser(analyzer: Analyzer?, fieldName: String): ComplexPhraseQueryParser {
        var complexPhraseParser = ComplexPhraseQueryParser(fieldName, analyzer);
        complexPhraseParser.allowLeadingWildcard = this.settings.allowLeadingWildcard();
        complexPhraseParser.defaultOperator = this.settings.defaultOperator();
        complexPhraseParser.autoGeneratePhraseQueries = this.settings.autoGeneratePhraseQueries();
        complexPhraseParser.analyzeRangeTerms = true;
        complexPhraseParser.fuzzyPrefixLength = this.settings.fuzzyPrefixLength();
        return complexPhraseParser
    }

    private fun getAnalyzer(fieldName: String): Analyzer? {
        var fieldMapper: MappedFieldType? = parseContext!!.fieldMapper(fieldName);
        var analyzer = parseContext!!.getSearchAnalyzer(fieldMapper);

        return analyzer;
    }

    private fun getField(queryString: String): String {
        var fieldName = queryString.split(":")?.get(0);
        if (fieldName == null || fieldName.trim().length == 0) {
            fieldName = this.parseContext!!.defaultField();
        }

        return fieldName
    }

    private fun traverseDisjunctionQuery(query: DisjunctionMaxQuery): Query {
        var disjuncts = query.disjuncts;
        var processedDisjuncts : HashSet<Query> = HashSet();

        disjuncts.forEach{ disjunct ->
            processedDisjuncts.add(traverseQuery(disjunct));
        };

        return DisjunctionMaxQuery(processedDisjuncts, query.tieBreakerMultiplier);
    }

    private fun traverseBooleanQuery(query: BooleanQuery) : Query {
        var clauses = query.clauses();
        var booleanQueryBuilder = BooleanQuery.Builder()

        clauses.forEach{ clause -> booleanQueryBuilder.add(traverseQuery(clause.query), clause.occur) };

        return booleanQueryBuilder.build();
    }
}