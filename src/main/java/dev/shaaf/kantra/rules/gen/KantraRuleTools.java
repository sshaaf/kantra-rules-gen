package dev.shaaf.kantra.rules.gen;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KantraRuleTools {

    /**
     * Creates a Kantra rule to detect a specific Java import statement.
     * Use this when a user wants to flag or migrate a package or a class at the import level.
     *
     * @param pattern The fully-qualified class name or package pattern to match (e.g., "com.old.package.OldClass" or "javax.persistence.*").
     * @param description A brief explanation of what the rule does.
     * @param beforeExample An example of the code before the change.
     * @param afterExample An example of the code after the change.
     * @return A fully-formed Kantra rule in YAML format.
     */
    @Tool("Creates a rule to detect specific Java IMPORT statements.")
    public String createImportRule(String pattern, String description, String beforeExample, String afterExample) {
        String ruleId = generateRuleId("import", pattern);
        return createRuleYaml(ruleId, description, "IMPORT", pattern, null, beforeExample, afterExample);
    }

    /**
     * Creates a Kantra rule to detect a specific Java field declaration.
     *
     * @param pattern The fully-qualified type of the field to match.
     * @param annotatedWith (Optional) The fully-qualified name of an annotation on the field.
     * @param description A brief explanation of what the rule does.
     * @param beforeExample An example of the code before the change.
     * @param afterExample An example of the code after the change.
     * @return A fully-formed Kantra rule in YAML format.
     */
    @Tool("Creates a rule to detect specific Java FIELD declarations, optionally checking for an annotation.")
    public String createFieldRule(String pattern, String annotatedWith, String description, String beforeExample, String afterExample) {
        String ruleId = generateRuleId("field", pattern);
        return createRuleYaml(ruleId, description, "FIELD", pattern, annotatedWith, beforeExample, afterExample);
    }

    /**
     * Creates a Kantra rule to detect a specific Java method call.
     *
     * @param pattern The pattern of the method call to match (e.g., "com.myapp.Service.oldMethod").
     * @param description A brief explanation of what the rule does.
     * @param beforeExample An example of the code before the change.
     * @param afterExample An example of the code after the change.
     * @return A fully-formed Kantra rule in YAML format.
     */
    @Tool("Creates a rule to detect specific Java METHOD_CALL invocations.")
    public String createMethodCallRule(String pattern, String description, String beforeExample, String afterExample) {
        String ruleId = generateRuleId("method-call", pattern);
        return createRuleYaml(ruleId, description, "METHOD_CALL", pattern, null, beforeExample, afterExample);
    }

    /**
     * Creates a Kantra rule to detect a specific Java constructor call.
     *
     * @param pattern The fully-qualified name of the class whose constructor is being called.
     * @param description A brief explanation of what the rule does.
     * @param beforeExample An example of the code before the change.
     * @param afterExample An example of the code after the change.
     * @return A fully-formed Kantra rule in YAML format.
     */
    @Tool("Creates a rule to detect specific Java CONSTRUCTOR_CALL invocations.")
    public String createConstructorCallRule(String pattern, String description, String beforeExample, String afterExample) {
        String ruleId = generateRuleId("constructor-call", pattern);
        return createRuleYaml(ruleId, description, "CONSTRUCTOR_CALL", pattern, null, beforeExample, afterExample);
    }

    /**
     * Creates a Kantra rule to detect the usage of a specific Java annotation.
     *
     * @param pattern The fully-qualified name of the annotation to match.
     * @param description A brief explanation of what the rule does.
     * @param beforeExample An example of the code before the change.
     * @param afterExample An example of the code after the change.
     * @return A fully-formed Kantra rule in YAML format.
     */
    @Tool("Creates a rule to detect the usage of a specific Java ANNOTATION.")
    public String createAnnotationRule(String pattern, String description, String beforeExample, String afterExample) {
        String ruleId = generateRuleId("annotation", pattern);
        return createRuleYaml(ruleId, description, "ANNOTATION", pattern, null, beforeExample, afterExample);
    }

    /**
     * Creates a Kantra rule to detect a specific Java type reference in variable declarations, parameters, or return types.
     *
     * @param pattern The fully-qualified name of the type to match.
     * @param description A brief explanation of what the rule does.
     * @param beforeExample An example of the code before the change.
     * @param afterExample An example of the code after the change.
     * @return A fully-formed Kantra rule in YAML format.
     */
    @Tool("Creates a rule to detect specific Java TYPE_REFERENCE usages.")
    public String createTypeReferenceRule(String pattern, String description, String beforeExample, String afterExample) {
        String ruleId = generateRuleId("type-reference", pattern);
        return createRuleYaml(ruleId, description, "TYPE_REFERENCE", pattern, null, beforeExample, afterExample);
    }

    /**
     * Creates a Kantra rule to detect any usage of a Java package, either in an import or as a fully qualified name.
     *
     * @param pattern The package pattern to match (e.g., "org.apache.lucene.search*").
     * @param description A brief explanation of what the rule does.
     * @param beforeExample An example of the code before the change.
     * @param afterExample An example of the code after the change.
     * @return A fully-formed Kantra rule in YAML format.
     */
    @Tool("Creates a rule to detect any usage of a Java PACKAGE, including in imports and fully qualified names.")
    public String createPackageRule(String pattern, String description, String beforeExample, String afterExample) {
        String ruleId = generateRuleId("package", pattern);
        return createRuleYaml(ruleId, description, "PACKAGE", pattern, null, beforeExample, afterExample);
    }


    // --- Helper Methods ---

    private String createRuleYaml(String ruleId, String description, String location, String pattern, String annotatedWith, String beforeExample, String afterExample) {
        StringBuilder whenClause = new StringBuilder();
        whenClause.append(String.format("""
                  when:
                    java.referenced:
                      location: %s
                      pattern: "%s"
                """, location, escapeYamlString(pattern)));

        if (annotatedWith != null && !annotatedWith.isBlank()) {
            whenClause.append(String.format("""
                      annotated:
                        pattern: "%s"
                    """, escapeYamlString(annotatedWith)));
        }

        return String.format("""
            - ruleID: %s
              description: "%s"
              category: mandatory
              effort: 1
            %s
              message: |
                %s
                **Before:**
                ```java
                %s
                ```
                **After:**
                ```java
                %s
                ```
              labels:
                - "konveyor.io/source=java"
                - "konveyor.io/target=java"
            """, ruleId, escapeYamlString(description), whenClause.toString(), escapeYamlString(description), beforeExample, afterExample);
    }

    private String generateRuleId(String prefix, String pattern) {
        String sanitizedPattern = pattern.replaceAll("[^a-zA-Z0-9]", "-").toLowerCase();
        return String.format("%s-%s-001", prefix, sanitizedPattern);
    }

    private String escapeYamlString(String value) {
        return value.replace("\"", "\\\"");
    }
}