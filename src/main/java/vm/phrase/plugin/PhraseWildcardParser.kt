package vm.phrase.plugin

import org.apache.lucene.queryparser.classic.QueryParserSettings
import org.apache.lucene.search.*
import org.elasticsearch.common.Strings
import org.elasticsearch.common.inject.Inject
import org.elasticsearch.common.logging.ESLogger
import org.elasticsearch.common.logging.Loggers
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.index.query.QueryParseContext
import org.elasticsearch.index.query.QueryStringQueryParser

class PhraseWildcardParser  : QueryStringQueryParser {

    private var Logger: ESLogger
    private var settings: Settings

    @Inject
    constructor(settings: Settings):super(settings){
        this.settings = settings;
        Logger = Loggers.getLogger(this.javaClass, settings);
    }

    override fun names(): Array<out String>? {
        var name = "phrase_wildcard_query";

        return arrayOf(name, Strings.toCamelCase(name));
    }

    override fun parse(parseContext: QueryParseContext?): Query? {
        var settingsParser = SettingsParser(settings);
        var qpSettings = QueryParserSettings()

        var query = settingsParser.parse(parseContext, qpSettings)

        var builder = ComplexPhraseQueryBuilder(query, Logger, parseContext, qpSettings);
        var builtQuery = builder.build();

        return builtQuery;
    }
}