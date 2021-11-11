package com.sohu.tv.mq.cloud.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserWarn;
import com.sohu.tv.mq.cloud.bo.UserWarn.WarnType;
import com.sohu.tv.mq.cloud.dao.UserWarnCount;
import com.sohu.tv.mq.cloud.dao.UserWarnDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * 用户警告服务
 * 
 * @author yongfeigao
 * @date 2021年9月13日
 */
@Service
public class UserWarnService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserWarnDao userWarnDao;

    @Autowired
    private UserService userService;

    /**
     * 保存警告信息
     * 
     * @param users
     * @param warnType
     * @param resource
     * @param content
     * @return
     */
    public Result<?> save(Collection<User> users, WarnType warnType, String resource, String content) {
        if(content == null) {
            return null;
        }
        if (CollectionUtils.isEmpty(users)) {
            users = userService.queryMonitorUser();
        }
        if (CollectionUtils.isEmpty(users)) {
            return null;
        }
        // 保存警告内容
        Result<UserWarn> result = saveWarnContent(new UserWarn(content));
        if (!result.isOK()) {
            return result;
        }
        // 保存警告用户
        long wid = result.getResult().getWid();
        List<UserWarn> userWarnList = users.stream()
                .map(u -> new UserWarn(u.getId(), warnType.getType(), resource, wid)).collect(Collectors.toList());
        return batchSaveUserWarn(userWarnList);
    }

    /**
     * 保存警告内容
     * 
     * @param userWarn
     * @return
     */
    public Result<UserWarn> saveWarnContent(UserWarn userWarn) {
        try {
            userWarnDao.insert(userWarn);
        } catch (Exception e) {
            logger.error("insert err, userWarn:{}", userWarn, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userWarn);
    }

    /**
     * 批量保存用户警告
     * 
     * @param uwList
     * @return
     */
    public Result<Integer> batchSaveUserWarn(List<UserWarn> uwList) {
        Integer count = null;
        try {
            count = userWarnDao.batchInsert(uwList);
        } catch (Exception e) {
            logger.error("insert err, uwList:{}", uwList, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }

    /**
     * 查询用户警告列表
     * 
     * @param uid
     * @param offset
     * @param size
     * @return
     */
    public Result<List<UserWarn>> queryUserWarnList(long uid, int offset, int size) {
        List<UserWarn> list = null;
        try {
            list = userWarnDao.select(uid, offset, size);
        } catch (Exception e) {
            logger.error("queryUserWarnList err, uid:{}", uid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }

    /**
     * 查询用户警告量
     * 
     * @param uid
     * @return
     */
    public Result<Integer> queryUserWarnCount(long uid) {
        Integer count = null;
        try {
            count = userWarnDao.selectCount(uid);
        } catch (Exception e) {
            logger.error("queryUserWarnCount err, uid:{}", uid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }

    /**
     * 查询用户警告量days天前的警告量
     * 
     * @param uid
     * @return
     */
    public Result<List<UserWarnCount>> queryUserWarnCount(long uid, int days) {
        long time = System.currentTimeMillis() - days * 86400000L;
        List<UserWarnCount> list = null;
        try {
            list = userWarnDao.warnCount(uid, new Date(time));
        } catch (Exception e) {
            logger.error("queryUserWarnCount err, uid:{} days:{}", uid, days, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }

    /**
     * 查询警告详情
     * 
     * @param wid
     * @return
     */
    public Result<UserWarn> queryWarnInfo(long wid) {
        UserWarn result = null;
        try {
            result = userWarnDao.selectWarnInfo(wid);
        } catch (Exception e) {
            logger.error("queryWarnInfo err, wid:{}", wid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
}
