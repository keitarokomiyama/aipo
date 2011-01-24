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

package com.aimluck.eip.modules.screens;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.jetspeed.services.logging.JetspeedLogFactoryService;
import org.apache.jetspeed.services.logging.JetspeedLogger;
import org.apache.turbine.modules.screens.RawScreen;
import org.apache.turbine.util.RunData;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

import com.aimluck.eip.common.ALEipConstants;
import com.aimluck.eip.modules.actions.common.ALAction;
import com.aimluck.eip.util.ALEipUtils;

/**
 * ブラウザにJSONデータを返すクラスです。 <br />
 * 
 */
public abstract class ALJSONScreen extends RawScreen implements ALAction {

  /** <code>logger</code> loger */
  private static final JetspeedLogger logger = JetspeedLogFactoryService
    .getLogger(ALJSONScreen.class.getName());

  /** コンテントタイプ */
  private static final String CONTENT_TYPE = "text/json;charset="
    + ALEipConstants.DEF_CONTENT_ENCODING;

  /** 表示モード */
  private String mode = "";

  /** result */
  private Object result;

  /** 検索結果を格納するリスト */
  private List<Object> resultList;

  /** 正常系のメッセージを格納するリスト */
  private List<String> msgList;

  /** 異常系のメッセージを格納するリスト */
  private List<String> errmsgList;

  /**
   * 
   * @param rundata
   * @throws Exception
   */
  @Override
  protected void doOutput(RunData rundata) throws Exception {
    ServletOutputStream out = null;

    // Todo: ログイン確認処理
    VelocityContext context = new VelocityContext();

    if (rundata.getParameters().containsKey("mode")) {
      mode = rundata.getParameters().getString("mode");
    } else {
      mode = "";
    }

    try {

      ALEipUtils.setupContext(rundata, context);

      StringBuffer result =
        new StringBuffer()
          .append("/* ")
          .append(getJSONString(rundata, context))
          .append(" */");
      byte[] byteResult =
        result.toString().getBytes(ALEipConstants.DEF_CONTENT_ENCODING);

      HttpServletResponse response = rundata.getResponse();
      out = response.getOutputStream();
      out.write(byteResult);
      out.flush();
      out.close();
    } catch (Exception e) {
      logger.error("[ERROR]", e);
    }

  }

  /**
   * 
   * @param rundata
   * @return
   */
  @Override
  protected String getContentType(RunData rundata) {
    return CONTENT_TYPE;
  }

  protected abstract String getJSONString(RunData rundata, Context context)
      throws Exception;

  /**
   * 
   * @param obj
   */
  public void setResultData(Object obj) {
    result = obj;
  }

  /**
   * 
   * @param obj
   */
  public void addResultData(Object obj) {
    if (resultList == null) {
      resultList = new ArrayList<Object>();
    }
    resultList.add(obj);
  }

  /**
   * 
   * @param objList
   */
  public void setResultDataList(List<Object> objList) {
    resultList = objList;
  }

  /**
   * 
   * @param msg
   */
  public void addErrorMessage(String msg) {
    if (errmsgList == null) {
      errmsgList = new ArrayList<String>();
    }
    errmsgList.add(msg);
  }

  /**
   * 
   * @param msg
   */
  public void addErrorMessages(List<String> msgs) {
    if (errmsgList == null) {
      errmsgList = new ArrayList<String>();
    }
    errmsgList.addAll(msgs);
  }

  /**
   * 
   * @param msgs
   */
  public void setErrorMessages(List<String> msgs) {
    errmsgList = msgs;
  }

  /**
   * 
   * @param mode
   */
  public void setMode(String mode) {
    this.mode = mode;
  }

  /**
   * 
   * @return
   */
  public String getMode() {
    return mode;
  }

  /**
   * 
   * @param context
   */
  public void putData(RunData rundata, Context context) {
    context.put(ALEipConstants.MODE, mode);
    context.put(ALEipConstants.RESULT, result);
    context.put(ALEipConstants.RESULT_LIST, resultList);
    context.put(ALEipConstants.MESSAGE_LIST, msgList);
    context.put(ALEipConstants.ERROR_MESSAGE_LIST, errmsgList);
    context.put(ALEipConstants.ENTITY_ID, ALEipUtils.getTemp(
      rundata,
      context,
      ALEipConstants.ENTITY_ID));
  }

}
