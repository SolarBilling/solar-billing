package com.sapienter.jbilling.server.user;

public interface MutableUser extends UserWithCredentials {
	public void setPassword(String password);
}
