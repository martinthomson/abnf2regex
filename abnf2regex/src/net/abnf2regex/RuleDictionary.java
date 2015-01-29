package net.abnf2regex;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A set of rules, indexed by name. Contains functions for parsing an ABNF file.
 */
public class RuleDictionary
{
    /** The location of the ABNF file containing core ABNF rules. */
    private static final String CORE_RULES_FILE = "core.abnf"; //$NON-NLS-1$
    /** A dictionary containing the core rules. */
    private static final RuleDictionary predefinedRules = new RuleDictionary();

    /** The set of rules in this dictionary. */
    private final Map<String, Rule> rules = new LinkedHashMap<String, Rule>();

    /**
     * Contains all the recursing rules that we have already warned the user
     * about
     */
    private static Set<String> warned = new HashSet<String>();

    static
    {
        String useCore = System.getProperty(RuleDictionary.class.getName() + ".core"); //$NON-NLS-1$
        if (useCore == null || !useCore.equalsIgnoreCase("false")) //$NON-NLS-1$
        {
            try
            {
                InputStream coreRules = RuleDictionary.class.getResourceAsStream(RuleDictionary.CORE_RULES_FILE);
                RuleDictionary.predefinedRules.parse(coreRules, RuleDictionary.CORE_RULES_FILE);
                RuleDictionary.predefinedRules.resolve();
                for (Rule r : RuleDictionary.predefinedRules.rules.values())
                {
                    r.setInlineRule(true);
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
            catch (AbnfParseException aex)
            {
                aex.printStackTrace();
            }
        }
    }

    /**
     * Adds a rule to the dictionary. Overwrites any preexisting rule by the
     * same name. Name comparisons are case-insensitive.
     *
     * @param rule the rule to add
     */
    public void addRule(Rule rule)
    {
        this.rules.put(rule.getName().toLowerCase(), rule);
    }

    /**
     * Resolve all {@link NamedFragment} instances in all rules in the
     * dictionary.
     *
     * @return true if all rules were resolved.
     */
    public boolean resolve()
    {
        boolean success = true;
        for (Rule r : this.rules.values())
        {
            success &= resolveRule(r.getMainFragment());
        }
        return success;
    }

    private boolean resolveRule(GroupFragment main)
    {
        boolean success = true;
        for (RuleFragment rf : main.getFragments())
        {
            if (rf instanceof NamedFragment)
            {
                NamedFragment named = (NamedFragment) rf;
                Rule resolved = this.getRule(named.getName());
                named.resolve(resolved);
                success &= (resolved != null);
            }
            else if (rf instanceof GroupFragment)
            {
                success &= this.resolveRule((GroupFragment) rf);
            }
        }
        return success;
    }

    /**
     * Creates a copy of a rule that has all referenced rules expanded. Any
     * instances of recursion are terminated by wildcard fragments.
     *
     * @param br the original rule.
     * @return a copy of the original rule that is completely resolved.
     */
    public Rule expandRule(Rule br)
    {
        return expandRule(br, new HashSet<String>());
    }

    private Rule expandRule(Rule br, Set<String> usedNames)
    {
        Rule copy = new Rule(br.getName());

        GroupFragment mainFrag = br.getMainFragment();
        this.resolveRule(mainFrag);
        GroupFragment copyFrag = copy.getMainFragment();

        expandCopyFragments(mainFrag, copyFrag, usedNames);

        copy.getMainFragment().simplify();
        return copy;
    }

    /**
     * When expanding a rule, copy all of the fragments from a given group into
     * the new group. Make sure that {@link NamedFragment} instances don't get
     * recursively expanded infinitely.
     *
     * @param from the source group
     * @param to the target group
     * @param usedNames the names that we've seen so far, so that recursive
     *            rules don't cause infinite recursion
     */
    private void expandCopyFragments(GroupFragment from, GroupFragment to, Set<String> usedNames)
    {
        to.setOccurences(from.getOccurences());

        for (RuleFragment rf : from.getFragments())
        {
            if (rf instanceof GroupFragment)
            {
                try
                {
                    GroupFragment group = (GroupFragment) rf;
                    GroupFragment copy = group.getClass().newInstance();
                    this.expandCopyFragments(group, copy, usedNames);
                    to.append(copy);
                }
                catch (InstantiationException ex)
                {
                    throw new IllegalStateException("Unable to instantiate GroupFragment class: " + rf.getClass(), ex); //$NON-NLS-1$
                }
                catch (IllegalAccessException ex)
                {
                    throw new IllegalStateException("Unable to instantiate GroupFragment class: " + rf.getClass(), ex); //$NON-NLS-1$
                }
            }
            else if (rf instanceof NamedFragment)
            {
                expandCopyNamed(to, (NamedFragment) rf, usedNames);
            }
            else
            {
                to.append((RuleFragment) rf.clone());
            }
        }
    }

    private void expandCopyNamed(GroupFragment to, NamedFragment named, Set<String> usedNames)
    {
        Rule resolvedRule = named.getResolvedRule();
        String name = named.getName();
        if (resolvedRule == null)
        {
            String reason = name + " does not exist"; //$NON-NLS-1$
            if (!RuleDictionary.warned.contains(name))
            {
                System.err.println("; Warning: rule " + reason); //$NON-NLS-1$
                RuleDictionary.warned.add(name);
            }

            WildcardFragment wildcard = new WildcardFragment(reason);
            to.append(wildcard);
        }
        else if (resolvedRule.isInlineRule())
        {
            this.expandCopyInlineNamed(named, to, usedNames);
        }
        else if (usedNames.contains(name))
        {
            to.append(named);
        }
        else
        {
            NamedFragment namedCopy = new NamedFragment(name);
            usedNames.add(name);
            namedCopy.resolve(this.expandRule(resolvedRule, usedNames));
            usedNames.remove(name);
            to.append(namedCopy);
        }
    }

    /**
     * Copy an expanded {@link NamedFragment} instance by replacing it with a
     * copy of the contents of its resolved rule.
     *
     * @param named the named fragment to expand and copy
     * @param to the target group to add to
     * @param usedNames for tracking recursion
     */
    private void expandCopyInlineNamed(NamedFragment named, GroupFragment to, Set<String> usedNames)
    {
        Rule rule = this.getRule(named.getName());

        GroupFragment main = rule.getMainFragment();
        try
        {
            GroupFragment inner = main.getClass().newInstance();
            this.expandCopyFragments(main, inner, usedNames);

            SequenceFragment sf = new SequenceFragment();
            sf.setOccurences(named.getOccurences());
            sf.append(inner);
            to.append(sf);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Unknown GroupFragment class: " + main.getClass(), ex); //$NON-NLS-1$
        }
        finally
        {
            usedNames.remove(named.getName());
        }
    }

    /**
     * Finds a rule by name. Looks in the standard predefined rule dictionary if
     * none are found in this dictionary.
     *
     * @param name the name of the rule, which is case-insensitive.
     * @return the rule that was found, or null
     */
    public Rule getRule(String name)
    {
        Rule r = this.rules.get(name.toLowerCase());
        if (r == null && this != RuleDictionary.predefinedRules)
        {
            r = RuleDictionary.predefinedRules.getRule(name);
        }
        return r;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder bld = new StringBuilder();
        for (Rule r : this.rules.values())
        {
            bld.append(r.toString());
        }
        return bld.toString();
    }

    /**
     * Write out the entire dictionary.
     *
     * @param out where to write to
     */
    public void write(PrintWriter out)
    {
        for (Rule r : this.rules.values())
        {
            out.print(r.toString());
            try
            {
                // rule expansion is helpful to ensure that the rule is properly
                // simplified for printing as a
                // regular expression. It means that you get [a-zA-Z] rather
                // than (?:[a-z]|[A-Z])
                Rule expanded = this.expandRule(r);
                out.print(" ; Expanded: "); //$NON-NLS-1$
                out.print(expanded.toString());
                out.print(" ; Regex: "); //$NON-NLS-1$
                expanded.writeRegex(out, new HashSet<String>());
            }
            catch (RuleResolutionException ex)
            {
                out.print(ex);
            }
            out.println();
        }
        out.flush();
    }

    /**
     * Generate a compact regex for a given rule.
     *
     * @param name the name of the rule
     * @return a regular expression
     * @throws RuleResolutionException If the rule can't be found or the rule
     *             contains references to rules that can't be found.
     */
    public String ruleToRegex(String name) throws RuleResolutionException
    {
        Rule rule = this.getRule(name);
        if (rule == null)
        {
            throw new RuleResolutionException("Can't find rule '" + name + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        Rule expanded = this.expandRule(rule);

        StringWriter sw = new StringWriter();
        expanded.writeRegex(new PrintWriter(sw), new HashSet<String>());
        return sw.toString();
    }

    /**
     * Convenience method for {@link #parse(AbnfReader)}.
     *
     * @param abnf an {@link InputStream} to read from
     * @param filename the name of the file/stream that is being read
     * @throws IOException when there are errors reading from the stream.
     */
    public void parse(InputStream abnf, String filename) throws IOException, AbnfParseException
    {
        this.parse(new InputStreamReader(abnf), filename);
    }

    /**
     * Convenience method for {@link #parse(AbnfReader)}.
     *
     * @param abnf an {@link Reader} to read from
     * @param filename the name of the file/stream that is being read
     * @throws IOException when there are errors reading from the stream.
     */
    public void parse(Reader abnf, String filename) throws IOException, AbnfParseException
    {
        this.parse(new AbnfReader(abnf, filename));
    }

    /**
     * Parse an ABNF file. Loads all rules from the file into this dictionary.
     * Once a complete set of ABNF files are loaded, callers should call
     * {@link #resolve()} to ensure that all rules are resolved.
     *
     * @param abnf a specialized reader instance, used by this package only.
     * @throws IOException when there are errors reading from the stream.
     */
    public void parse(AbnfReader abnf) throws IOException, AbnfParseException
    {
        Rule currentRule = null;
        Deque<SequenceFragment> seqStack = new ArrayDeque<SequenceFragment>();

        while (!abnf.eof())
        {
            int ws = abnf.gobbleWhitespace();
            if (abnf.peek() == ';')
            {
                abnf.findNextLine();
                continue;
            }

            if (currentRule == null && ws > 0)
            {
                throw new AbnfParseException("Whitespace before first rule.", abnf); //$NON-NLS-1$
            }

            // if this is a new line and there is no leading whitespace: new
            // rule
            currentRule = this.continueRule(ws, currentRule);

            if (currentRule == null)
            {
                String name = abnf.parseName();
                if (name.length() == 0)
                {
                    abnf.findNextLine();
                    continue;
                }

                currentRule = startNewRule(abnf, seqStack, name);
            }

            parseFragments(abnf, seqStack);

        }
        this.continueRule(0, currentRule);
    }

    /**
     * Used by {@link #parse(AbnfReader)} to check whether a new rule needs to
     * be started.
     *
     * @param abnf the reader to read
     * @param seqStack a stack of {@link SequenceFragment} representing the
     *            current position in the current set of nested sequences
     * @param name the name of the new rule that is either being started or
     *            continued
     * @return the rule that is either being continued or started
     * @throws IOException when there are IO troubles
     */
    private Rule startNewRule(AbnfReader abnf, Deque<SequenceFragment> seqStack, String name) throws IOException,
    AbnfParseException
    {
        abnf.gobbleWhitespace();
        if (abnf.read() != '=')
        {
            throw new AbnfParseException("No '=' after rule: " + name, abnf); //$NON-NLS-1$
        }
        Rule rule = null;
        boolean choice = false;
        if (abnf.peek() == '/')
        {
            abnf.read();
            rule = this.getRule(name);
            choice = (rule != null);
        }
        if (rule == null)
        {
            rule = new Rule(name);
        }
        seqStack.clear();
        seqStack.push((SequenceFragment) rule.getMainFragment());
        if (choice)
        {
            this.handleChoice(seqStack);
        }
        return rule;
    }

    /**
     * Check if the new line indicates a continuation of the previous rule, and,
     * if it does, keep going.
     *
     * @param ws the amount of whitespace skipped at the start of the line
     * @param currentRule the rule from the previous line, or null if there was
     *            none
     * @return currentRule, as passed in, or null if a new rule must be started
     */
    private Rule continueRule(int ws, Rule currentRule)
    {
        if ((currentRule != null) && (ws <= 0))
        {
            currentRule.getMainFragment().simplify();
            this.addRule(currentRule);
            currentRule = null;
        }
        return currentRule;
    }

    /**
     * Parses fragments from an ABNF file.
     *
     * @param abnf the reader to use
     * @param seqStack a stack of {@link SequenceFragment} representing the
     *            current position in the current set of nested sequences
     * @throws IOException if there are IO troubles
     * @throws AbnfParseException if unexpected characters are found
     */
    private void parseFragments(AbnfReader abnf, Deque<SequenceFragment> seqStack) throws IOException,
    AbnfParseException
    {
        while (!abnf.eof())
        {
            abnf.gobbleWhitespace();

            OccurrenceRange range = OccurrenceRange.parse(abnf);

            RuleFragment newFrag = null;

            int peek = abnf.peek();
            if (peek == ';' || peek == '\n' || peek == '\r')
            {
                abnf.findNextLine();
                return;
            }

            if (peek == '"')
            {
                StringFragment strFrag = StringFragment.parse(abnf);
                newFrag = strFrag;
            }
            else if (peek == '%')
            {
                LiteralFragment litFrag = LiteralFragment.parse(abnf);
                newFrag = litFrag;
            }
            else if (peek == '<')
            {
                WildcardFragment wc = WildcardFragment.parse(abnf);
                newFrag = wc;
            }
            else if (peek == '/')
            {
                abnf.read();
                this.handleChoice(seqStack);
            }
            else if (peek == '(')
            {
                abnf.read();
                SequenceFragment seqFrag = new SequenceFragment();
                seqFrag.setOccurences(range);
                seqStack.peek().nest(seqFrag);
                seqStack.push(seqFrag);
            }
            else if (peek == '[')
            {
                abnf.read();
                SequenceFragment seqFrag = new SequenceFragment();
                seqFrag.setOccurences(new OccurrenceRange(0, range.getMax()));
                seqStack.peek().nest(seqFrag);
                seqStack.push(seqFrag);
            }
            else if (peek == ')' || peek == ']')
            {
                abnf.read();
                seqStack.pop();
            }
            else
                // named rule
            {
                String name = abnf.parseName();
                if (name.length() == 0)
                {
                    String str = '(' + new String(Character.toChars(peek)) + ')';
                    throw new AbnfParseException("Unexpected character: U+" + Integer.toHexString(peek) + str, abnf); //$NON-NLS-1$
                }
                newFrag = new NamedFragment(name);
            }

            if (newFrag != null)
            {
                newFrag.setOccurences(range);

                seqStack.peek().append(newFrag);
            }
        }
    }

    /**
     * Special processing for choice fragments. When a '/' character is found,
     * this function wraps the top of the sequence stack in a
     * {@link ChoiceFragment} and starts from a new {@link SequenceFragment}
     * that is added to that choice.
     *
     * @param seqStack a stack of {@link SequenceFragment} representing the
     *            current position in the current set of nested sequences
     */
    private void handleChoice(Deque<SequenceFragment> seqStack)
    {
        GroupFragment choice = new ChoiceFragment();
        SequenceFragment last = seqStack.pop();
        if (last.length() == 1)
        {
            RuleFragment single = last.removeLast();
            choice.append(single);
        }
        else
        {
            SequenceFragment copy = new SequenceFragment();
            copy.extractAll(last);
            choice.append(copy);
        }
        last.append(choice);

        SequenceFragment seqFrag = new SequenceFragment();
        choice.append(seqFrag);
        seqStack.push(seqFrag);
    }
}
