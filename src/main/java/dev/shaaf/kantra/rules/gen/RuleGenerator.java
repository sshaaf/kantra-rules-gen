package dev.shaaf.kantra.rules.gen;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.SessionScoped;

@RegisterAiService(tools = KantraRuleTools.class)
@SessionScoped
public interface RuleGenerator {

    @UserMessage("""
        You are an intelligent dispatcher. Based on the user's command, call the appropriate tool to generate a Kantra rule.
        If the user wants to move a class, you should call the createImportRule tool.
        If the user wants to rename a method, call the createMethodCallRule tool.
        If the user mentions an annotation, call the createAnnotationRule tool.
        If the user wants to flag an entire package, call the createPackageRule tool.
        You must extract the required parameters for the tool from the user's command.
        
        User Command: "{{userMessage}}"
    """)
    String chat(@MemoryId Object session, @V("userMessage") String message);
}