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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;

/**
 * A rule fragment that contains one or more other rule fragments. The extending classes, {@link SequenceFragment} and
 * {@link ChoiceFragment}, define behaviour specific to the method of combining the child fragments.
 */
public abstract class GroupFragment extends RuleFragment
{
    /** Rule fragments within this group, either sequence or choice. */
    protected Deque<RuleFragment> fragments = new ArrayDeque<RuleFragment>();

    /**
     * Removes the last fragment from the list.
     *
     * @return the fragment that was removed.
     */
    public RuleFragment removeLast()
    {
        return this.fragments.removeLast();
    }

    /**
     * Find out how many fragments are in this group.
     *
     * @return the number of fragments directly in this group.
     */
    public int length()
    {
        return this.fragments.size();
    }

    /**
     * Get all the fragments in this grouop.
     *
     * @return an unmodifiable copy of the fragments owned by this group.
     */
    public Collection<RuleFragment> getFragments()
    {
        return Collections.unmodifiableCollection(this.fragments);
    }

    /**
     * adds a new group to the end of the group, without invoking any simplification. The group is only appended to the
     * group. Used in parsing to prevent empy groups from disappearing.
     *
     * @param frag The new group to add.
     */
    public void nest(GroupFragment frag)
    {
        this.fragments.addLast(frag);
    }

    /**
     * Removes unnecessary groupings from a set recursively. For instance, groups that have unitary cardinality are
     * removed from their enclosing group if it is of the same type.
     *
     * @see #canCollapse(RuleFragment)
     */
    public void simplify()
    {
        Deque<RuleFragment> rebuild = new ArrayDeque<RuleFragment>();
        for (RuleFragment rf : this.fragments)
        {
            if (rf instanceof GroupFragment)
            {
                collapseGroup(rebuild, (GroupFragment) rf);
            }
            else
            {
                rebuild.addLast(rf);
            }
        }
        this.fragments = rebuild;

    }

    /**
     * The recursive component of {@link #simplify()}.
     *
     * @param rebuild the copy of the current group that needs rebuilding.
     * @param group the group to collapse.
     */
    private void collapseGroup(Deque<RuleFragment> rebuild, GroupFragment group)
    {
        group.simplify();
        if (canCollapse(group))
        {
            int min = group.getOccurences().getMin();
            int max = group.getOccurences().getMax();
            Deque<RuleFragment> frags = group.fragments;
            while (canCollapse(frags.peekLast()))
            {
                min *= group.getOccurences().getMin();
                max *= group.getOccurences().getMax();

                group = ((GroupFragment) frags.peekLast());
                frags = group.fragments;
            }
            for (RuleFragment f : frags)
            {
                OccurenceRange range = new OccurenceRange(min, max);
                f.setOccurences(range);
            }
            rebuild.addAll(frags);
        }
        else
        {
            rebuild.addLast(group);
        }
    }

    private boolean canCollapse(RuleFragment rf)
    {
        if (rf instanceof GroupFragment)
        {
            GroupFragment group = (GroupFragment) rf;
            return (group.length() == 1) || ((group.getClass() == this.getClass()) && group.getOccurences().isOneOnly());
        }
        return false;
    }

    @Override
    public Object clone()
    {
        try
        {
            GroupFragment copy = this.getClass().newInstance();
            copy.setOccurences(this.getOccurences());
            copy.fragments = new ArrayDeque<RuleFragment>(this.fragments);
            return copy;
        }
        catch (InstantiationException ex)
        {
            ex.printStackTrace();
        }
        catch (IllegalAccessException ex)
        {
            ex.printStackTrace();
        }
        return null; // this will crash the program--as it should
    }
}
