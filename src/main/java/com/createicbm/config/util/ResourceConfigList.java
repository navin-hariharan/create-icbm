package com.createicbm.config.util;

import com.createicbm.CreateICBM;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Prefab for anything that allows blacklist based on {@link ResourceLocation}
 * <p>
 * Life Cycle:
 * - unlock
 * - clear existing
 * - load(external inputs)
 * - batch(registry)
 * - lock
 */
public abstract class ResourceConfigList<CONFIG extends ResourceConfigList, CONTENT, VALUE> {
    // TODO once ContentBuilder Json system is published for MC replace this with JSON version to support programmatic and more complex entries
    //      entries to consider once JSON is allowed: time/date specific, conditional statements such as IF(MOD) IF(WORLD) IF(MATH) IF(GEO_AREA), block sets/lists

    protected static final Pattern SORTING_REGEX = Pattern.compile("^@sort\\((\\d+),(\\S*)\\)$");
    protected static final Pattern DOMAIN_VALE_REGEX = Pattern.compile("^@domain:([^=]*)(?:=(\\S+))?$");

    protected static final Pattern KEY_VALUE_REGEX = Pattern.compile("^([^\\s@:=]*):([^\\s=]*)(?:=(\\S*))?");
    protected static final Pattern META_KEY_REGEX = Pattern.compile("^([^\\s@:=]*):([^\\s=@]*)@([0-9]+)(?:=(\\S*))?");

    // Constructor
    @Getter
    private final String name;
    /** Short description of the entry format, logged when an entry can't be parsed */
    private final String formatHelp;
    private final Consumer<CONFIG> reloadCallback;

    /** Matchers for handling input entries */
    protected final List<Pair<Pattern, MatcherHandler<CONTENT, VALUE>>> matcherHandlers = new ArrayList<>();

    // Block lists
    protected final Map<ResourceLocation, List<ResourceConfigEntry<CONTENT, VALUE>>> contentMatchers = new HashMap();
    protected final Map<ResourceLocation, List<ResourceConfigEntry<CONTENT, VALUE>>> defaultMatchers = new HashMap();
    protected final List<ResourceConfigEntry<CONTENT, VALUE>> generalMatchers = new ArrayList<>();

    /** Issues with parsing data is stored as source -> entries -> issues as string & is-error  */
    private final Map<String, Map<String, List<Pair<String, Boolean>>>> parsingIssues = new HashMap<>(); //TODO maybe a data structure with sorting?

    // States
    @Getter
    private boolean isLocked = false;

    public ResourceConfigList(final String name, final String formatHelp, final Consumer<CONFIG> reloadCallback) {
        this.name = name;
        this.formatHelp = formatHelp;
        this.reloadCallback = reloadCallback;
        addMatcher(DOMAIN_VALE_REGEX, this::handleDomain);
    }

    protected void addMatcher(Pattern pattern, MatcherHandler<CONTENT, VALUE> handler) {
        this.matcherHandlers.add(Pair.of(pattern, handler));
    }

    public void reload() {
        this.unlock();
        this.reset();
        // Let the parent load defaults and pull in configs
        this.reloadCallback.accept((CONFIG) this);
        this.processData();
        // Sort functions
        sort(generalMatchers);
        contentMatchers.values().forEach(this::sort);
        defaultMatchers.values().forEach(this::sort);
        this.lock();

        outputParsingIssues();
        outputResults();
    }

    protected void outputParsingIssues() {
        //TODO configs to ignore issues
        if(!parsingIssues.isEmpty()) {
            CreateICBM.logger().info("{}: entry format is {}", this.getName(), this.formatHelp);
            CreateICBM.logger().error("{}: Parsing errors detected:", this.getName());

            //TODO abc sort source, entry, and issues
            parsingIssues.forEach((source, entries) -> {
                CreateICBM.logger().error("\t{}", source);
                entries.forEach((entry, issues) -> {
                    CreateICBM.logger().error("\t\t{}", entry);
                    issues.forEach(p -> {
                        if(p.getValue()) {
                            CreateICBM.logger().error("\t\t-{}", p.getKey());
                        }
                        else {
                            CreateICBM.logger().warn("\t\t-{}", p.getKey());
                        }
                    });
                });
            });
        }
    }

    protected void outputResults() {
        //TODO implement debugging output of results. Likely want to make a map of keys to matches showing type and sort order
    }

    protected void sort(List<ResourceConfigEntry<CONTENT, VALUE>> list) {
        // Generate defaults
        int highestUserSort = list.stream().map(ResourceConfigEntry::getOrder).mapToInt(i -> i == null ? 0 : i).max().orElse(0);

        int index = 0;
        for (ResourceConfigEntry<CONTENT, VALUE> func : list) {
            if (func.getOrder() == null) {
                index++;
                func.setOrder(highestUserSort + index);
            }
        }

        // Do sort
        list.sort(this::compare);
    }

    protected int compare(ResourceConfigEntry<CONTENT, VALUE> a, ResourceConfigEntry<CONTENT, VALUE> b) {
        int sortA = a.getOrder();
        int sortB = b.getOrder();
        return Integer.compare(sortB, sortA);
    }

    protected void processData() {

    }

    private void unlock() {
        this.isLocked = false;
    }

    private void reset() {
        contentMatchers.clear();
        generalMatchers.clear();
        defaultMatchers.clear();
        parsingIssues.clear();
    }

    private void lock() {
        this.isLocked = true;
    }

    public void setDefault(ResourceLocation key, VALUE value, int order) {
        defaultMatchers
            .computeIfAbsent(key, k -> new ArrayList<>())
            .add(new ResourceConfigEntry<>("default", order, getSimpleValue(key, value)));
    }

    public VALUE getValue(CONTENT state) {
        if (state == null) {
            return null;
        }
        final ResourceLocation key = getContentKey(state);

        final VALUE contentValue = getValue(state, contentMatchers.get(key));
        if (contentValue != null) return contentValue;

        for (Function<CONTENT, VALUE> matcher : generalMatchers) {
            final VALUE result = matcher.apply(state);
            if (result != null) {
                matcherHit(state, matcher);
                return result;
            }
        }

        return getValue(state, this.defaultMatchers.get(key));
    }

    private VALUE getValue(CONTENT state, List<ResourceConfigEntry<CONTENT, VALUE>> matchers) {
        if (matchers != null) {
            for (Function<CONTENT, VALUE> matcher : matchers) {
                final VALUE result = matcher.apply(state);
                if (result != null) {
                    matcherHit(state, matcher);
                    return result;
                }
            }
        }
        return null;
    }

    protected void matcherHit(CONTENT content, Function<CONTENT, VALUE> matcher) {

    }

    protected abstract ResourceLocation getContentKey(CONTENT content);

    /**
     * Loads entries converting them into key:value functions
     *
     * @param entries to process
     */
    public void load(String source, String... entries) {
        //TODO rethink loading to abstract away from caller and better track source(s)
        if (checkLock("entries", () -> String.join(", ", entries))) {
            //TODO trim string to not dump 1000s of lines into logs
            return;
        }

        for (String str : entries) {
            try {
                handleEntry(source, str, null);
            }
            catch (RuntimeException e) {
                CreateICBM.logger().error("{}: Unexpected error parsing entry '{}' from '{}'", name, str, source, e);
                error(source, String.valueOf(str), "Unexpected error while parsing entry: " + e);
            }
        }
    }

    private boolean checkLock(String type, Supplier<String> entry) {
        if (this.isLocked) {
            CreateICBM.logger().error("{}: list is locked. Unable to add '{}' entry '{}'", name, type, entry.get(), new IllegalArgumentException());
            return true;
        }
        return false;
    }

    /**
     * Handles the entry and parses it for conversion to a matcher
     *
     * @param source of the entry, usually path to a config
     * @param entryRaw to parse
     * @param index of the entry
     * @return true if the entry was accepted, even if it failed to parse
     */
    protected final boolean handleEntry(String source, String entryRaw, Integer index) {

        final String entry = entryRaw.replaceAll("\\s", ""); //Kill spaces off -> "M I N E C R A F T" turns into "MINECRAFT"

        if(entry.length() != entryRaw.length()) {
            warn(source, entry, "Entry contains extra whitespace characters '" + entryRaw + "'");
        }

        // TODO allow permutation arguments
        final Matcher sortMatcher = SORTING_REGEX.matcher(entry);
        if (sortMatcher.matches()) {
            final int order;
            try {
                order = Integer.parseInt(sortMatcher.group(1));
            }
            catch (NumberFormatException e) {
                error(source, entry, "Invalid sort number '" + sortMatcher.group(1) + "'");
                return false;
            }
            return handleEntry(source, sortMatcher.group(2), order);
        }

        for(Pair<Pattern, MatcherHandler<CONTENT, VALUE>> handlerEntry: this.matcherHandlers) {
            final Matcher regexMatcher = handlerEntry.getKey().matcher(entry);
            if(regexMatcher.matches()) {
                final ResourceConfigEntry<CONTENT, VALUE> resourceConfigEntry = handlerEntry.getValue().handle(regexMatcher, source, entry, index);
                if (resourceConfigEntry != null) {
                    if (resourceConfigEntry.getKey() == null) {
                        this.generalMatchers.add(resourceConfigEntry);
                    } else {
                        this.contentMatchers
                            .computeIfAbsent(resourceConfigEntry.getKey(), (ResourceLocation k) -> new ArrayList<>())
                            .add(resourceConfigEntry);
                    }
                }
                return true;
            }
        }

        error(source, entry, "Unknown format");
        return false;
    }

    protected ResourceConfigEntry<CONTENT, VALUE> handleDomain(Matcher domainMatcher, String source, String entry, Integer index) {

        final String domain = domainMatcher.group(1);

        if(!isDomainValid(domain)) {
            error(source, entry, "No matching mod domain found for '" + domain + "'");
            return null;
        }

        final String valueStr = domainMatcher.group(2);
        VALUE value = null;
        if(valueStr != null) {
            value = parseValue(source, entry, valueStr);
            if(value == null) {
                return null;
            }
        }

        final Function<CONTENT, VALUE> matcher = getDomainValue(domain, value);
        if(matcher == null) {
            return null;
        }

        return new ResourceConfigEntry<>("resource_domain", index, matcher);
    }

    protected ResourceConfigEntry<CONTENT, VALUE> handleMeta(Matcher metaMatcher, String source, String entry, Integer index) {
        final String domain = metaMatcher.group(1);
        final String resource = metaMatcher.group(2);
        final ResourceLocation key = toKey(domain, resource);
        if (key == null) {
            error(source, entry, "Invalid resource location '" + domain + ":" + resource + "'");
            return null;
        }
        else if (!isDomainValid(key.getNamespace())) {
            error(source, entry, "No matching mod domain found for '" + key + "'");
            return null;
        } else if (!isValidKey(key)) {
            error(source, entry, "No matching content found for '" + key + "'");
            return null;
        }

        final int meta;
        try {
            meta = Integer.parseInt(metaMatcher.group(3));
        }
        catch (NumberFormatException e) {
            error(source, entry, "Invalid number '" + metaMatcher.group(3) + "'");
            return null;
        }

        final String valueStr = metaMatcher.group(4);
        VALUE value = null;
        if(valueStr != null) {
            value = parseValue(source, entry, valueStr);
            if(value == null) {
                return null;
            }
        }

        final Function<CONTENT, VALUE> matcher = getMetaValue(key, meta, value);
        if (matcher == null) {
            error(source, entry, "Metadata is not supported for this list, use properties or the flattened id instead");
            return null;
        }
        return new ResourceConfigEntry<>("resource_metadata", index, matcher).setKey(key);
    }

    protected ResourceConfigEntry<CONTENT, VALUE> handleSimple(Matcher regexMatcher, String source, String entry, Integer index) {
        final String domain = regexMatcher.group(1);
        final String resource = regexMatcher.group(2);

        if(resource.contains("~")) {
            return handleFuzzy(source, entry, index, domain.toLowerCase(Locale.ROOT), resource.toLowerCase(Locale.ROOT), regexMatcher.group(3));
        }

        final ResourceLocation key = toKey(domain, resource);
        if(key == null) {
            error(source, entry, "Invalid resource location '" + domain + ":" + resource + "'");
            return null;
        }
        else if(!isDomainValid(key.getNamespace())) {
            error(source, entry, "No matching mod domain found for '" + key + "'");
            return null;
        }
        else if(!isValidKey(key)) {
            error(source, entry, "No matching content found for '" + key + "'");
            return null;
        }

        final String valueStr = regexMatcher.group(3);
        VALUE value = null;
        if(valueStr != null) {
            value = parseValue(source, entry, valueStr);
            if(value == null) {
                return null;
            }
        }
        final Function<CONTENT, VALUE> matcher = getSimpleValue(key, value);
        if(matcher == null) {
            return null;
        }
        return new ResourceConfigEntry<>("resource_simple", index, matcher).setKey(key);
    }

    protected Function<CONTENT, VALUE> getDomainValue(String domain, @Nullable VALUE value) {
        return (content) -> domain.equalsIgnoreCase(getContentKey(content).getNamespace()) ? value : null;
    }

    private ResourceConfigEntry<CONTENT, VALUE> handleFuzzy(String source, String entry, Integer index, String domain, String resource, @Nullable String valueStr) {
        if(!isDomainValid(domain)) {
            error(source, entry, "No matching mod domain found for '" + domain + "'");
            return null;
        }

        VALUE value = null;
        if(valueStr != null) {
            value = parseValue(source, entry, valueStr);
            if(value == null) {
                return null;
            }
        }
        final Function<CONTENT, VALUE> matcher = getFuzzyValue(domain, resource, value);
        if(matcher == null) {
            return null;
        }
        return new ResourceConfigEntry<>("resource_fuzzy", index, matcher);
    }

    @Nullable
    protected static ResourceLocation toKey(String domain, String path) {
        return ResourceLocation.tryBuild(domain.toLowerCase(Locale.ROOT), path.toLowerCase(Locale.ROOT));
    }

    /**
     * Fuzzy matcher for paths containing '~'
     *
     * @param domain   of the content
     * @param path     with ~ markers
     * @param value    to return on match
     * @return matcher
     */
    protected Function<CONTENT, VALUE> getFuzzyValue(String domain, String path, @Nullable VALUE value) {
        // Contains: ~stone~ ->  dark_stone_deep
        if(path.startsWith("~") && path.endsWith("~") && path.length() > 1) {
            final String checkStr = path.substring(1, path.length() - 1);
            return (content) -> {
                final ResourceLocation contentKey = getContentKey(content);
                if(!domain.equalsIgnoreCase(contentKey.getNamespace())) {
                    return null;
                }
                return contentKey.getPath().contains(checkStr) ? value : null;
            };
        }
        // Starts: ~stone -> dark_stone
        else if(path.startsWith("~")) {
            final String checkStr = path.substring(1);
            return (content) -> {
                final ResourceLocation contentKey = getContentKey(content);
                if(!domain.equalsIgnoreCase(contentKey.getNamespace())) {
                    return null;
                }
                return contentKey.getPath().endsWith(checkStr) ? value : null;
            };
        }
        // Ends: stone~ -> stone_dark
        else if(path.endsWith("~")) {
            final String checkStr = path.substring(0, path.length() - 1);
            return (content) -> {
                final ResourceLocation contentKey = getContentKey(content);
                if(!domain.equalsIgnoreCase(contentKey.getNamespace())) {
                    return null;
                }
                return contentKey.getPath().startsWith(checkStr) ? value : null;
            };
        }
        return null;
    }

    protected Function<CONTENT, VALUE> getSimpleValue(ResourceLocation targetKey, @Nullable VALUE value) {
        return (content) -> targetKey.equals(getContentKey(content)) ? value : null;
    }

    protected Function<CONTENT, VALUE> getMetaValue(ResourceLocation key, int metadata, @Nullable VALUE value) {
        return null;
    }

    protected abstract VALUE parseValue(String source, String entry, String value);

    /**
     * Checks if the resource key is valid. For content registries
     * this should check contains.
     *
     * @param key to check
     * @return true if valid
     */
    protected boolean isValidKey(ResourceLocation key) {
        return true;
    }

    protected boolean isDomainValid(String domain) {
        //TODO fuzzy domains?
        return "minecraft".equalsIgnoreCase(domain) || ModList.get().isLoaded(domain.toLowerCase(Locale.ROOT));
    }

    protected void error(String source, String entry, String error) {
        issue(source, entry, error, true);
    }

    protected void warn(String source, String entry, String error) {
        issue(source, entry, error, false);
    }

    protected void issue(String source, String entry, String error, boolean isError) {
        parsingIssues
            .computeIfAbsent(source, (k) -> new HashMap<>())
            .computeIfAbsent(entry, (k) -> new ArrayList<>())
            .add(Pair.of(error, isError));
    }

    @FunctionalInterface
    protected interface MatcherHandler<CONTENT, VALUE> {
        ResourceConfigEntry<CONTENT, VALUE> handle(Matcher regexMatcher, String source, String entry, Integer index);
    }
}
