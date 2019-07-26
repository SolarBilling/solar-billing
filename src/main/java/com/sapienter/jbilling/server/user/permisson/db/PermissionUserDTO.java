/*
    jBilling - The Enterprise Open Source Billing System
    Copyright (C) 2003-2009 Enterprise jBilling Software Ltd. and Emiliano Conde

    This file is part of jbilling.

    jbilling is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    jbilling is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.sapienter.jbilling.server.user.permisson.db;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.Objects;
import com.sapienter.jbilling.server.user.db.UserDTO;

@Entity
@Table(name="permission_user")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class PermissionUserDTO  implements java.io.Serializable, PermissionUser {
	private static final long serialVersionUID = 1L;
	private int id;
    private UserDTO baseUser;
    private PermissionDTO permission;
    private short isGrant;

    @Override public String toString()
    {
    	return "PermissionUserDTO " + id;
    }
    
    @Override public int hashCode() {
    	return id;
    }
    
    @Override public boolean equals(final Object o) {
    	if (!(o instanceof PermissionUserDTO)) {
    		return false;
    	}
    	final PermissionUserDTO permission = (PermissionUserDTO)o;
    	return getId() == permission.getId() && Objects.equal(getBaseUser(), permission.getBaseUser()) &&
    		Objects.equal(getPermission(), permission.getPermission()) &&
    		getIsGrant() == permission.getIsGrant();
    }
    
    public PermissionUserDTO() {
    }

	
    public PermissionUserDTO(int id, short isGrant) {
        this.id = id;
        this.isGrant = isGrant;
    }
    public PermissionUserDTO(int id, UserDTO baseUser, PermissionDTO permission, short isGrant) {
       this.id = id;
       this.baseUser = baseUser;
       this.permission = permission;
       this.isGrant = isGrant;
    }
   
     @Id 
    
    @Column(name="id", unique=true, nullable=false)
    public int getId() {
        return this.id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id")
    public UserDTO getBaseUser() {
        return this.baseUser;
    }
    
    public void setBaseUser(UserDTO baseUser) {
        this.baseUser = baseUser;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="permission_id")
    @Override public Permission getPermission() {
        return this.permission;
    }
    
    public void setPermission(PermissionDTO permission) {
        this.permission = permission;
    }
    
    @Column(name="is_grant", nullable=false)
    public short getIsGrant() {
        return this.isGrant;
    }
    
    public void setIsGrant(short isGrant) {
        this.isGrant = isGrant;
    }




}


