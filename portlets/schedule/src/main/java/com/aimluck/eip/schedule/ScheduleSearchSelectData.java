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

package com.aimluck.eip.schedule;

import java.util.ArrayList;
import java.util.List;

import org.apache.jetspeed.services.logging.JetspeedLogFactoryService;
import org.apache.jetspeed.services.logging.JetspeedLogger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

import com.aimluck.commons.field.ALStringField;
import com.aimluck.eip.cayenne.om.portlet.EipMFacility;
import com.aimluck.eip.cayenne.om.portlet.VEipTScheduleList;
import com.aimluck.eip.common.ALDBErrorException;
import com.aimluck.eip.common.ALPageNotFoundException;
import com.aimluck.eip.facilities.util.FacilitiesUtils;
import com.aimluck.eip.modules.actions.common.ALAction;
import com.aimluck.eip.orm.Database;
import com.aimluck.eip.orm.query.ResultList;
import com.aimluck.eip.orm.query.SelectQuery;
import com.aimluck.eip.schedule.util.ScheduleUtils;
import com.aimluck.eip.util.ALEipUtils;

/**
 *
 */
public class ScheduleSearchSelectData extends ScheduleMonthlySelectData {

  /** <code>logger</code> logger */
  private static final JetspeedLogger logger = JetspeedLogFactoryService
    .getLogger(ScheduleSearchSelectData.class.getName());

  private final ALStringField target_keyword = new ALStringField();

  private int userid;

  /**
   * 
   * @param action
   * @param rundata
   * @param context
   * @throws ALPageNotFoundException
   * @throws ALDBErrorException
   */
  @Override
  public void init(ALAction action, RunData rundata, Context context)
      throws ALPageNotFoundException, ALDBErrorException {

    super.init(action, rundata, context);// 表示タイプの設定
    viewtype = "search";

    userid = ALEipUtils.getUserId(rundata);

  }

  @Override
  protected ResultList<VEipTScheduleList> selectList(RunData rundata,
      Context context) throws ALPageNotFoundException, ALDBErrorException {
    try {
      target_keyword.setValue(ScheduleUtils.getTargetKeyword(rundata, context));
      setupLists(rundata, context);

      return getScheduleList(rundata, context);
    } catch (Exception e) {
      logger.error("[ScheduleOnedaySelectData]", e);
      throw new ALDBErrorException();
    }
  }

  protected ResultList<VEipTScheduleList> getScheduleList(RunData rundata,
      Context context) {

    List<Integer> tmpUsers = new ArrayList<Integer>();
    List<Integer> tmpFacilities = new ArrayList<Integer>();
    if ("all".equals(target_user_id)) {
      tmpUsers = ALEipUtils.getUserIds(target_group_name);
      if ("Facility".equals(target_group_name)) {
        tmpFacilities = getFacilityIdAllList();
      } else {
        tmpFacilities = FacilitiesUtils.getFacilityIds(target_group_name);
      }
    } else if ((target_user_id != null) && (!target_user_id.equals(""))) {
      if (target_user_id.startsWith(ScheduleUtils.TARGET_FACILITY_ID)) {
        String fid =
          target_user_id.substring(
            ScheduleUtils.TARGET_FACILITY_ID.length(),
            target_user_id.length());
        tmpFacilities.add(Integer.valueOf(fid));
      } else {
        tmpUsers.add(Integer.valueOf(target_user_id));
      }
    } else {
      // 表示できるユーザがいない場合の処理
      return new ResultList<VEipTScheduleList>();
    }

    return (ResultList<VEipTScheduleList>) ScheduleUtils.getScheduleList(
      Integer.valueOf(userid),
      null,
      null,
      tmpUsers,
      tmpFacilities,
      target_keyword.getValue(),
      getCurrentPage(),
      getRowsNum(),
      true);
  }

  /**
   * 
   * @param record
   * @return
   * @throws ALPageNotFoundException
   * @throws ALDBErrorException
   */
  @Override
  protected Object getResultData(VEipTScheduleList record)
      throws ALPageNotFoundException, ALDBErrorException {
    ScheduleSearchResultData rd = new ScheduleSearchResultData();
    rd.initField();
    try {

      boolean is_member = record.isMember();

      if ("C".equals(record.getPublicFlag())
        && (userid != record.getOwnerId().intValue())
        && !is_member) {
        rd.setName("非公開");
        // 仮スケジュールかどうか
        rd.setTmpreserve(false);
      } else {
        rd.setName(record.getName());
        // 仮スケジュールかどうか
        rd.setTmpreserve("T".equals(record.getStatus()));
      }

      // ID
      rd.setScheduleId(record.getScheduleId().intValue());
      // 親スケジュール ID
      rd.setParentId(record.getParentId().intValue());
      // 開始日時
      rd.setStartDate(record.getStartDate());
      // 終了日時
      rd.setEndDate(record.getEndDate());
      // 公開するかどうか
      rd.setPublic("O".equals(record.getPublicFlag()));
      // 非表示にするかどうか
      rd.setHidden("P".equals(record.getPublicFlag()));
      // ダミーか
      rd.setDummy("D".equals(record.getStatus()));
      // ログインユーザかどうか
      // rd.setLoginuser(is_member);
      // オーナーかどうか
      rd.setOwner(record.getOwnerId().intValue() == userid);
      // 共有メンバーかどうか
      rd.setMember(is_member);
      // 繰り返しパターン
      rd.setPattern(record.getRepeatPattern());

      rd.setCreateUser(ALEipUtils.getALEipUser(record.getCreateUserId()));
      rd.setNote(record.getNote());

      if (!rd.getPattern().equals("N") && !rd.getPattern().equals("S")) {
        rd.setRepeat(true);
      }

    } catch (Exception e) {
      logger.error("Exception", e);
      return null;
    }
    return rd;
  }

  private List<Integer> getFacilityIdAllList() {
    List<Integer> facilityIdAllList = new ArrayList<Integer>();

    try {
      SelectQuery<EipMFacility> query = Database.query(EipMFacility.class);
      query.select(EipMFacility.FACILITY_ID_PK_COLUMN);
      List<EipMFacility> aList = query.fetchList();

      int size = aList.size();
      for (int i = 0; i < size; i++) {
        EipMFacility record = aList.get(i);
        facilityIdAllList.add(record.getFacilityId());
      }
    } catch (Exception ex) {
      logger.error("Exception", ex);
    }
    return facilityIdAllList;
  }

  /**
   * 表示タイプを取得します。
   * 
   * @return
   */
  @Override
  public String getViewtype() {
    return viewtype;
  }

  /**
   * @return target_keyword
   */
  public ALStringField getTargetKeyword() {
    return target_keyword;
  }

}
