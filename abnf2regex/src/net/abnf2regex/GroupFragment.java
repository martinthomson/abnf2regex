package net.abnf2regex;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;

/**
 * A rule fragment that contains one or more other rule fragments. The extending
 * classes, {@link SequenceFragment} and {@link ChoiceFragment}, define
 * behaviour specific to the method of combining the child fragments.
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
     * Get all the fragments in this group.
     *
     * @return an unmodifiable copy of the fragments owned by this group.
     */
    public Collection<RuleFragment> getFragments()
    {
        return Collections.unmodifiableCollection(this.fragments);
    }

    /**
     * adds a new group to the end of the group, without invoking any
     * simplification. The group is only appended to the group. Used in parsing
     * to prevent empty groups from disappearing.
     *
     * @param frag The new group to add.
     */
    public void nest(GroupFragment frag)
    {
        this.fragments.addLast(frag);
    }

    /**
     * Removes unnecessary groupings from a set recursively. For instance,
     * groups that have unitary cardinality are removed from their enclosing
     * group if it is of the same type.
     *
     * @see #canCollapse(GroupFragment)
     */
    public void simplify()
    {
        moveOccurencesToGroup();

        Deque<RuleFragment> rebuild = this.fragments;
        this.fragments = new ArrayDeque<RuleFragment>();
        for (RuleFragment rf : rebuild)
        {
            if (rf instanceof GroupFragment)
            {
                collapseGroup((GroupFragment) rf);
            }
            else
            {
                this.append(rf);
            }
        }

        moveOccurencesToGroup();
    }

    /**
     * The recursive component of {@link #simplify()}.
     *
     * @param group the group to collapse.
     */
    private void collapseGroup(GroupFragment group)
    {
        group.simplify();
        if (canCollapse(group))
        {
            for (RuleFragment rf : group.fragments)
            {
                rf.setOccurences(group.getOccurences().multiply(rf.getOccurences()));
                if (rf instanceof GroupFragment)
                {
                    collapseGroup((GroupFragment) rf);
                }
                else
                {
                    this.append(rf);
                }
            }
        }
        else
        {
            this.append(group);
        }
    }

    /**
     * If the rule can be collapsed into the current group
     *
     * @param group the group that is to be collapsed into this
     * @return if the contents of the group can be safely added to this
     */
    private boolean canCollapse(GroupFragment group)
    {
        for (RuleFragment grf : group.fragments)
        {
            if (group.getOccurences().multiply(grf.getOccurences()) == null)
            {
                return false;
            }
        }
        return group.getOccurences().isOnce() && ((group.length() == 1) || (group.getClass() == this.getClass()));
    }

    /**
     * If all fragments have the same number of occurrences, move that number up
     * to this group, if possible.
     */
    private void moveOccurencesToGroup()
    {
        if (this.length() == 0)
        {
            return;
        }
        Iterator<RuleFragment> it = this.fragments.iterator();
        OccurrenceRange range = it.next().getOccurences();
        OccurrenceRange product = this.getOccurences().multiply(range);
        if (product != null)
        {
            if (!it.hasNext())
            {
                // For a group of one, move the occurrences downwards
                this.setOccurences(OccurrenceRange.ONCE);
                this.fragments.getFirst().setOccurences(product);
            }
            else
            {
                // For a group of two or more, move the occurrences upwards
                moveOccurencesUp(it, range, product);
            }
        }
    }

    /**
     * Move occurrence ranges up, if all fragments have the same range (and
     * there are more than one fragment in this group).
     *
     * @param it an iterator for the list of fragments, starting at the second
     *            one
     * @param range the range for the first fragment
     * @param product the product of the first fragment range and the range on
     *            this
     */
    private void moveOccurencesUp(Iterator<RuleFragment> it, OccurrenceRange range, OccurrenceRange product)
    {
        // For a group of more than one, move the occurrences upwards
        boolean allSame = true;
        while (it.hasNext())
        {
            allSame &= it.next().getOccurences().equals(range);
        }
        if (allSame)
        {
            this.setOccurences(product);
            for (RuleFragment rf : this.fragments)
            {
                rf.setOccurences(OccurrenceRange.ONCE);
            }
        }
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
            throw new IllegalStateException("Unable to instantiate GroupFragment class: " + this.getClass(), ex); //$NON-NLS-1$
        }
        catch (IllegalAccessException ex)
        {
            throw new IllegalStateException("Unable to instantiate GroupFragment class: " + this.getClass(), ex); //$NON-NLS-1$
        }
    }

    /**
     * Append all fragments from the other group
     *
     * @param group a group that is to be broken open and added to this group.
     * @return true if the group was split and added
     */
    protected boolean appendAll(GroupFragment group)
    {
        if (group.getOccurences().isOnce())
        {
            for (RuleFragment rf : group.fragments)
            {
                this.append(rf); // this never returns false for a group
            }
            return true;
        }
        return false;
    }
}
