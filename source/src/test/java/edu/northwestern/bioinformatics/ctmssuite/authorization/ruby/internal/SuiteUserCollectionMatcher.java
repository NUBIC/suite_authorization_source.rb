package edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.internal;

import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUser;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Collection;
import java.util.Iterator;

/**
* @author Rhett Sutphin
*/
class SuiteUserCollectionMatcher extends BaseMatcher<Collection<SuiteUser>> {
    private final String[] usernames;

    public SuiteUserCollectionMatcher(String... usernames) {
        this.usernames = usernames;
    }

    public static Matcher<Collection<SuiteUser>> suiteUsers(final String... usernames) {
        return new SuiteUserCollectionMatcher(usernames);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public boolean matches(Object item) {
        if (!(item instanceof Collection)) return false;
        Collection<SuiteUser> users = (Collection<SuiteUser>) item;
        if (users.size() != usernames.length)  return false;
        int i = 0;
        for (Iterator<SuiteUser> iterator = users.iterator(); iterator.hasNext(); i++) {
            SuiteUser next = iterator.next();
            if (! next.getUsername().equals(usernames[i])) return false;
        }
        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendValueList("a SuiteUser collection with usernames ", ", ", "", usernames);
    }
}
