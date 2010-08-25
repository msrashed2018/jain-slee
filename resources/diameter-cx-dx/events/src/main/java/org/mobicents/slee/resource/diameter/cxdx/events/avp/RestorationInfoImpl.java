/*
 * JBoss, Home of Professional Open Source
 * 
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.mobicents.slee.resource.diameter.cxdx.events.avp;

import static net.java.slee.resource.diameter.cxdx.events.avp.DiameterCxDxAvpCodes.*;

import net.java.slee.resource.diameter.cxdx.events.avp.RestorationInfo;
import net.java.slee.resource.diameter.cxdx.events.avp.SubscriptionInfo;

import org.mobicents.slee.resource.diameter.base.events.avp.GroupedAvpImpl;

/**
 *
 * RestorationInfoImpl.java
 *
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public class RestorationInfoImpl extends GroupedAvpImpl implements RestorationInfo {

  public RestorationInfoImpl() {
    super();
  }

  /**
   * @param code
   * @param vendorId
   * @param mnd
   * @param prt
   * @param value
   */
  public RestorationInfoImpl(int code, long vendorId, int mnd, int prt, byte[] value) {
    super(code, vendorId, mnd, prt, value);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.cxdx.events.avp.RestorationInfo#getContact()
   */
  public String getContact() {
    return getAvpAsOctetString(CONTACT, CXDX_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.cxdx.events.avp.RestorationInfo#getPath()
   */
  public String getPath() {
    return getAvpAsOctetString(PATH, CXDX_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.cxdx.events.avp.RestorationInfo#getSubscriptionInfo()
   */
  public SubscriptionInfo getSubscriptionInfo() {
    return (SubscriptionInfo) getAvpAsCustom(SUBSCRIPTION_INFO, CXDX_VENDOR_ID, SubscriptionInfoImpl.class);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.cxdx.events.avp.RestorationInfo#hasContact()
   */
  public boolean hasContact() {
    return hasAvp(CONTACT, CXDX_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.cxdx.events.avp.RestorationInfo#hasPath()
   */
  public boolean hasPath() {
    return hasAvp(PATH, CXDX_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.cxdx.events.avp.RestorationInfo#hasSubscriptionInfo()
   */
  public boolean hasSubscriptionInfo() {
    return hasAvp(SUBSCRIPTION_INFO, CXDX_VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.cxdx.events.avp.RestorationInfo#setContact(java.lang.String)
   */
  public void setContact(String contact) {
    addAvp(CONTACT, CXDX_VENDOR_ID, contact);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.cxdx.events.avp.RestorationInfo#setPath(java.lang.String)
   */
  public void setPath(String path) {
    addAvp(PATH, CXDX_VENDOR_ID, path);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.cxdx.events.avp.RestorationInfo#setSubscriptionInfo(net.java.slee.resource.diameter.cxdx.events.avp.SubscriptionInfo)
   */
  public void setSubscriptionInfo(SubscriptionInfo subscriptionInfo) {
    addAvp(SUBSCRIPTION_INFO, CXDX_VENDOR_ID, subscriptionInfo.byteArrayValue());
  }

}
