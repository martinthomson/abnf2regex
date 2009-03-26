/**
 * Copyright (c) Andrew Corporation,
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Andrew Corporation. You shall not disclose such confidential
 * information and shall use it only in accordance with the terms
 * of the license agreement you entered into with Andrew Corporation.
 */
package net.abnf2regex;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
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

    /** Contains all the recursing rules that we have already warned the user about */
    private static Set<String> warned = new HashSet<String>();

    static
    {
        try
        {
            InputStream coreRules = RuleDictionary.class.getResourceAsStream(RuleDictionary.CORE_RULES_FILE);
            RuleDictionary.predefinedRules.parse(coreRules);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Adds a rule to the dictionary. Overwrites any preexisting rule by the same name. Name comparisons are
     * case-insensitive.
     *
     * @param rule the rule to add
     */
    public void addRule(Rule rule)
    {
        this.rules.put(rule.getName().toLowerCase(), rule);
    }

    /**
     * Resolve all {@link NamedFragment} instances in all rules in the dictionary.
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
     * Creates a copy of a rule that has all referenced rules expanded. Any instances of recursion are terminated by
     * wildcard fragments.
     *
     * @param br the original rule.
     * @return a copy of the original rule that is completely resolved.
     */
    public Rule expandRule(Rule br)
    {
        Rule copy = new Rule(br.getName());

        SequenceFragment mainFrag = br.getMainFragment();
        this.resolveRule(mainFrag);
        SequenceFragment copyFrag = copy.getMainFragment();

        Set<String> usedNames = new HashSet<String>();
        expandCopyFragments(mainFrag, copyFrag, usedNames);

        copy.getMainFragment().simplify();
        return copy;
    }

    /**
     * When expanding a rule, copy all of the fragments from a given group into the new group. Make sure that
     * {@link NamedFragment} instances don't get recursively expanded infinitely.
     *
     * @param from the source group
     * @param to the target group
     * @param usedNames the set of rule names that have already been used on the call stack.
     */
    private void expandCopyFragments(GroupFragment from, GroupFragment to, Set<String> usedNames)
    {
        for (RuleFragment rf : from.getFragments())
        {
            if (rf instanceof NamedFragment)
            {
                expandCopyNamed((NamedFragment) rf, to, usedNames);
            }
            else if (rf instanceof GroupFragment)
            {
                GroupFragment gf;
                if (rf instanceof SequenceFragment)
                {
                    gf = new SequenceFragment();
                }
                else if (rf instanceof ChoiceFragment)
                {
                    gf = new ChoiceFragment();
                }
                else
                {
                    throw new IllegalStateException("Unknown GroupFragment type:" + rf); //$NON-NLS-1$
                }
                gf.setOccurences(rf.getOccurences());
                this.expandCopyFragments((GroupFragment) rf, gf, usedNames);
                to.append(gf);
            }
            else
            {
                to.append((RuleFragment) rf.clone());
            }
        }
    }

    /**
     * Copy an expanded {@link NamedFragment} instance by replacing it with a copy of the contents of its resolved rule.
     *
     * @param named the named fragment to expand and copy
     * @param to the target group to add to
     * @param usedNames a set of names of rules that have already been used on the call stack, that should not be
     *            expanded again. Instances of these are replaced by {@link WildcardFragment}s instead.
     */
    private void expandCopyNamed(NamedFragment named, GroupFragment to, Set<String> usedNames)
    {
        Rule rule = this.getRule(named.getName());

        if (usedNames.contains(named.getName()) || rule == null)
        {
            String reason = named.getName() + ((rule == null) ? " does not exist" : " recurses.");  //$NON-NLS-1$//$NON-NLS-2$
            if (!RuleDictionary.warned.contains(named.getName()))
            {
                System.err.println("Warning: rule " + reason); //$NON-NLS-1$
                RuleDictionary.warned.add(named.getName());
            }

            WildcardFragment wildcard = new WildcardFragment(reason);
            to.append(wildcard);
            return;
        }

        usedNames.add(named.getName());

        SequenceFragment sf = new SequenceFragment();
        sf.setOccurences(named.getOccurences());
        this.expandCopyFragments(rule.getMainFragment(), sf, usedNames);
        to.append(sf);

        usedNames.remove(named.getName());
    }

    /**
     * Finds a rule by name. Looks in the standard predefined rule dictionary if none are found in this dictionary.
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
                // rule expansion is helpful to ensure that the rule is properly simplified for printing as a
                // regular expression. It means that you get [a-zA-Z] rather than (?:[a-z]|[A-Z])
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
     * Convenience method for {@link #parse(AbnfReader)}.
     *
     * @param abnf an {@link InputStream} to read from
     * @throws IOException when there are errors reading from the stream.
     */
    public void parse(InputStream abnf) throws IOException
    {
        this.parse(new InputStreamReader(abnf));
    }

    /**
     * Convenience method for {@link #parse(AbnfReader)}.
     *
     * @param abnf an {@link Reader} to read from
     * @throws IOException when there are errors reading from the stream.
     */
    public void parse(Reader abnf) throws IOException
    {
        this.parse(new AbnfReader(abnf));
    }

    /**
     * Parse an ABNF file. Loads all rules from the file into this dictionary. Once a complete set of ABNF files are
     * loaded, callers should call {@link #resolve()} to ensure that all rules are resolved.
     *
     * @param abnf a specialized reader instance, used by this package only.
     * @throws IOException when there are errors reading from the stream.
     */
    public void parse(AbnfReader abnf) throws IOException
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

            // if this is a new line and there is no leading whitespace: new rule
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

            abnf.findNextLine();
        }
        this.continueRule(0, currentRule);
    }

    /**
     * Used by {@link #parse(AbnfReader)} to check whether a new rule needs to be started.
     *
     * @param abnf the reader to read
     * @param seqStack a stack of {@link SequenceFragment} representing the current position in the current set of
     *            nested sequences
     * @param name the name of the new rule that is either being started or continued
     * @return the rule that is either being continued or started
     * @throws IOException when there are IO troubles
     */
    private Rule startNewRule(AbnfReader abnf, Deque<SequenceFragment> seqStack, String name) throws IOException
    {
        abnf.gobbleWhitespace();
        if (abnf.read() != '=')
        {
            throw new IllegalArgumentException("No '=' after rule: " + name); //$NON-NLS-1$
        }
        if (abnf.peek() == '/')
        {
            abnf.read();
            Rule rule = this.getRule(name);
            if (rule == null)
            {
                return new Rule(name);
            }

            seqStack.clear();
            seqStack.push(rule.getMainFragment());
            this.handleChoice(seqStack);
            return rule;
        }

        Rule currentRule = new Rule(name);
        seqStack.clear();
        seqStack.push(currentRule.getMainFragment());
        return currentRule;
    }

    /**
     * Check if the new line indicates a continuation of the previous rule, and, if it does, keep going.
     *
     * @param ws the amount of whitespace skipped at the start of the line
     * @param currentRule the rule from the previous line, or null if there was none
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
     * @param seqStack a stack of {@link SequenceFragment} representing the current position in the current set of
     *            nested sequences
     * @throws IOException if there are IO troubles
     */
    private void parseFragments(AbnfReader abnf, Deque<SequenceFragment> seqStack) throws IOException
    {
        while (!abnf.eof() && abnf.peek() != '\n' && abnf.peek() != '\r')
        {
            abnf.gobbleWhitespace();

            OccurenceRange range = OccurenceRange.parse(abnf);

            RuleFragment newFrag = null;

            if (abnf.peek() == '"')
            {
                StringFragment strFrag = StringFragment.parse(abnf);
                newFrag = strFrag;
            }
            else if (abnf.peek() == '%')
            {
                LiteralFragment litFrag = LiteralFragment.parse(abnf);
                newFrag = litFrag;
            }
            else if (abnf.peek() == '<')
            {
                WildcardFragment wc = WildcardFragment.parse(abnf);
                newFrag = wc;
            }
            else if (abnf.peek() == '/')
            {
                abnf.read();
                this.handleChoice(seqStack);
            }
            else if (abnf.peek() == '(')
            {
                abnf.read();
                SequenceFragment seqFrag = new SequenceFragment();
                seqFrag.setOccurences(range);
                seqStack.peek().nest(seqFrag);
                seqStack.push(seqFrag);
            }
            else if (abnf.peek() == '[')
            {
                abnf.read();
                SequenceFragment seqFrag = new SequenceFragment();
                seqFrag.setOccurences(new OccurenceRange(0, range.getMax()));
                seqStack.peek().nest(seqFrag);
                seqStack.push(seqFrag);
            }
            else if (abnf.peek() == ')' || abnf.peek() == ']')
            {
                abnf.read();
                seqStack.pop();
            }
            else if (abnf.peek() == ';')
            {
                // break out - the next line is found by the caller
                break;
            }
            else
            {
                String name = abnf.parseName();
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
     * Special processing for choice fragments. When a '/' character is found, this function wraps the top of the
     * sequence stack in a {@link ChoiceFragment} and starts from a new {@link SequenceFragment} that is added to that
     * choice.
     *
     * @param seqStack a stack of {@link SequenceFragment} representing the current position in the current set of
     *            nested sequences
     */
    private void handleChoice(Deque<SequenceFragment> seqStack)
    {
        ChoiceFragment choice = new ChoiceFragment();
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
