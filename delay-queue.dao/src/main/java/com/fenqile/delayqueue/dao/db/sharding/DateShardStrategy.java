package com.fenqile.delayqueue.dao.db.sharding;

import com.fenqile.dao.sharding.plugin.ShardCondition;
import com.fenqile.dao.sharding.plugin.ShardStrategy;
import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created with IntelliJ IDEA.
 * User: gavinlu
 * Date: 2017/7/12
 * Time: 20:07
 */
public class DateShardStrategy implements ShardStrategy {

    /**
     * Date 分库分表策略
     *
     * @param params
     * @return
     */
    public ShardCondition parse(Map<String, Object> params) {

        String year = null;
        String month = null;
        String day = null;

        String msgId = (String)params.get("msgId");

        String regEx = "[^0-9]";
        Pattern pattern = Pattern.compile(regEx);
        String conditionStr = "";
        if (StringUtils.isNotBlank(msgId)) {
            Matcher matcher = pattern.matcher(msgId);
            conditionStr = matcher.replaceAll("").trim();
        }

        if (conditionStr.length() > 8) {
            year = conditionStr.substring(0, 4);
            month = conditionStr.substring(4, 6);
            day = conditionStr.substring(6, 8);
        } else {
            Calendar cal = Calendar.getInstance();
            year = cal.get(Calendar.YEAR) + "";
            month = cal.get(Calendar.MONTH) + 1 + "";
            if (month.length() == 1) {
                month = "0" + month;
            }
            day = cal.get(Calendar.DATE) + "";
            if (day.length() == 1) {
                day = "0" + day;
            }
        }

        ShardCondition condition = new ShardCondition();
        condition.setDatabaseSuffix(year);
        condition.setTableSuffix(month + day);
        return condition;
    }

}