package com.idega.user.sync;

import java.rmi.RemoteException;
import java.util.List;

import com.idega.user.data.Group;

public interface UnionMemberSynchronizer {

	String WS_MSG_SUCCESS = "success";

	public Group getClub(String clubNumber);

	public String registerMemberToClub(String ssn, Group club, String clubMembershipType) throws RemoteException;

	public Group getClubByAbbreviation(String abbreviation);
	public Group getClubByUniqueId(String uniqueId);
	public Group getClubByUniqueId(String uniqueId, List<Group> leagues);
	public Group getClubByUniqueIdAndLeaguesName(String uniqueId, List<String> leaguesNames);

	public String disableMemberInClub(String ssn, Group club) throws RemoteException;

}