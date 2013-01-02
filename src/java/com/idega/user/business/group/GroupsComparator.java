package com.idega.user.business.group;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import com.idega.user.data.Group;

public class GroupsComparator implements Comparator<Group> {

	private Locale locale;
	private Collator collator;
	
	public GroupsComparator(Locale locale) {
		this.locale = locale;
		collator = Collator.getInstance(locale);
	}
	
	@Override
	public int compare(Group g1, Group g2) {
		if (g1 == null || g2 == null)
			return 0;
		
		return collator.compare(g1.getNodeName(locale), g2.getNodeName(locale));
	}

}