/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2011 Aimluck,Inc.
 * http://www.aipo.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aimluck.eip.util;

import java.util.Map;

import org.apache.jetspeed.services.rundata.JetspeedRunData;
import org.apache.jetspeed.services.rundata.JetspeedRunDataService;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.rundata.RunDataService;
import org.apache.turbine.util.RunData;

/**
 * 
 */
public final class ALUserContextLocator {

  private static ThreadLocal<Map<String, Object>> UserContexts;

  public final static String KEY_ACL = "key_acl";

  public static Map<String, Object> get() {
    if (UserContexts == null) {
      UserContexts = new ThreadLocal<Map<String, Object>>();
    }
    return UserContexts.get();
  }

  public static void set(Map<String, Object> userContext) {
    UserContexts.set(userContext);
  }

  public static void remove() {
    if (UserContexts != null) {
      UserContexts.remove();
    }
  }

  public static RunData getRunData() {
    JetspeedRunData rundata = null;

    JetspeedRunDataService runDataService =
      (JetspeedRunDataService) TurbineServices.getInstance().getService(
        RunDataService.SERVICE_NAME);

    if (runDataService != null) {
      rundata = runDataService.getCurrentRunData();
    }

    return rundata;
  }

  public static Object getAttribute(String key) {
    return ALUserContextLocator.getRunData().getRequest().getAttribute(key);
  }

  public static void setAttribute(String key, Object obj) {
    ALUserContextLocator.getRunData().getRequest().setAttribute(key, obj);
  }

  public static void removeAttribute(String key) {
    RunData rundata = ALUserContextLocator.getRunData();
    if (rundata != null && rundata.getUser() != null) {
      rundata.getRequest().removeAttribute(key);
    }
  }
}
