package com.leif.util;

import com.leif.config.QiniuConfigProperty;
import com.leif.exception.ServiceException;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QiniuUtil {

    @Autowired
    private QiniuConfigProperty qiniuConfigProperty;

    /**
     * 获取上传token
     * @return
     */
    public String getUploadToken() {
        Auth auth = getAuth();

        StringMap stringMap = new StringMap();
        stringMap.put("returnBody", "{\"fileKey\":\"$(key)\",\"name\":\"$(fname)\",\"url\":\""+qiniuConfigProperty.getDomain()+"/$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\"\"fileSize\":\"$(fsize)\",\"uid\":\"$(x:uid)\"}");
        //自定义返回体格式
        String upToken = auth.uploadToken(qiniuConfigProperty.getBucket(), null, 3600, stringMap);

        return upToken;
    }

    /**
     * 批量删除七牛文件
     * @param keyList
     */
    public void batchDeleteFile(String[] keyList) {
        //构造一个带指定Region对象的配置类
        Configuration configuration = new Configuration(Region.huabei());

        Auth auth = getAuth();
        BucketManager bucketManager = new BucketManager(auth, configuration);
        try {
            BucketManager.BatchOperations batchOperations = new BucketManager.BatchOperations();
            batchOperations.addDeleteOp(qiniuConfigProperty.getBucket(), keyList);
            bucketManager.batch(batchOperations);
        } catch (QiniuException e) {
            throw new ServiceException("七牛删除文件失败", e);
        }

    }

    private Auth getAuth() {
        return Auth.create(qiniuConfigProperty.getAccessKey(), qiniuConfigProperty.getSecretKey());
    }
}
