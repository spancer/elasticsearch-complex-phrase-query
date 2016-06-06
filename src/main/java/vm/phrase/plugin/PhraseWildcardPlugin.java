package vm.phrase.plugin;

import org.elasticsearch.indices.IndicesModule;
import org.elasticsearch.plugins.Plugin;

public class PhraseWildcardPlugin extends Plugin {
    @Override
    public String name() {
        return "phrase-wildcard-query";
    }

    @Override
    public String description() {
        return "Plugin intended to perform phrase search with wildcards";
    }

    public void onModule(IndicesModule indicesModule){
        indicesModule.registerQueryParser(PhraseWildcardParser.class);
    }
}
