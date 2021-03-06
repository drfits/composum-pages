package com.composum.pages.commons.model.properties;

import com.composum.pages.commons.model.ResourceReference;
import com.composum.pages.commons.util.ResolverUtil;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * a multi value string property 'allowed...' with a set of resource type patterns
 */
public class AllowedTypes {

    protected List<Pattern> patternList = null;

    public AllowedTypes(ResourceReference reference, String propertyName) {
        String[] defaultValue = new String[0];
        String[] typeRules = reference.getProperty(propertyName, defaultValue);
        if (typeRules != defaultValue) {
            buildPatterns(typeRules);
        }
    }

    public AllowedTypes(ResourceResolver resolver, String resourceType, String propertyName) {
        String[] defaultValue = new String[0];
        String[] typeRules = ResolverUtil.getTypeProperty(resolver, resourceType, propertyName, defaultValue);
        if (typeRules != defaultValue) {
            buildPatterns(typeRules);
        }
    }

    protected void buildPatterns(String[] typeRules) {
        patternList = new ArrayList<>();
        for (String rule : typeRules) {
            rule = rule.trim();
            if (rule.length() > 2 && // complete a regex if not always a regex
                    "^.[(".indexOf(rule.charAt(0)) < 0 &&
                    ".*+])?$".indexOf(rule.charAt(rule.length() - 1)) < 0) {
                if (!rule.startsWith("/")) {
                    rule = ".*" + rule;
                }
                rule = "^" + rule + "$";
            }
            patternList.add(Pattern.compile(rule));
        }
    }

    /** is valid if the property is present */
    public boolean isValid() {
        return patternList != null;
    }

    /** is empty if no property found or the value set contains no value */
    public boolean isEmpty() {
        return !isValid() || patternList.isEmpty();
    }

    /**
     * matches if property is valid and one of the rules is matching
     */
    public boolean matches(String resourceType) {
        if (!isEmpty()) {
            for (Pattern pattern : patternList) {
                if (pattern.matcher(resourceType).matches()) {
                    return true;
                }
            }
            return false;
        } else {
            return !isValid();
        }
    }
}
