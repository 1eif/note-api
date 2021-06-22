package com.leif.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leif.exception.ServiceException;
import com.leif.mapper.MemoFilesMapper;
import com.leif.mapper.MemoMapper;
import com.leif.mapper.MemoTagsMapper;
import com.leif.mapper.TagsMapper;
import com.leif.model.dto.request.CreateMemoDto;
import com.leif.model.dto.request.EditMemoDto;
import com.leif.model.dto.respons.CreateMemoRespDto;
import com.leif.model.dto.respons.DailyMemoCountRespDto;
import com.leif.model.entity.Memo;
import com.leif.model.entity.MemoFiles;
import com.leif.model.entity.MemoTags;
import com.leif.model.entity.Tags;
import com.leif.service.MemoService;
import com.leif.util.QiniuUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MemoServiceImpl implements MemoService {

    @Autowired
    private MemoMapper memoMapper;

    @Autowired
    private TagsMapper tagsMapper;

    @Autowired
    private MemoTagsMapper memoTagsMapper;

    @Autowired
    private MemoFilesMapper memoFilesMapper;

    @Autowired
    private QiniuUtil qiniuUtil;

    /**
     * 创建新的Memo
     *
     * @param createMemoDto
     * @return
     */
    @Override
    @Transactional //事务：保证此部分代码全部运行成功或失败，不允许部分失败部分成功，出错则回滚
    public CreateMemoRespDto createNewMemo(CreateMemoDto createMemoDto) {
        //1. 创建memo
        Memo memo = new Memo();
        memo.setUserId(createMemoDto.getUserId());
        memo.setContent(createMemoDto.getContent());
        memo.setCreateTime(DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
        memo.setDevice(createMemoDto.getDevice());

        //2. 判断是否有关联的memo
        if (StringUtils.isNoneEmpty(createMemoDto.getParentId())) {
            //如果有关联parentid且可以找到对应memo，就设置关联，否则忽略
            Memo parentMemo = memoMapper.selectById(createMemoDto.getParentId());
            if (parentMemo != null) {
                memo.setParentId(parentMemo.getId());
            }
        }

        //3. 保存Memo
        memoMapper.insert(memo);

        //4. 保存Memo中的tag
        List<String> tagNameList = saveMemoTags(memo);

        //5. 查看Memo是否有关联图片
        MemoFiles[] fileList = createMemoDto.getFileList();
        for(MemoFiles memoFiles : fileList) {
            memoFiles.setMemoId(memo.getId());
            memoFilesMapper.insert(memoFiles);
            log.info("创建Memo文件：{}", memoFiles);
        }

        log.info("用户：{} 新建Memo：{}", createMemoDto.getUserId(), memo);
        return new CreateMemoRespDto(memo.getContent(), memo.getCreateTime(), memo.getId(), memo.getDevice(), memo.getParentId(), tagNameList);
    }

    /**
     * 保存memo中的tag
     * @param memo
     * @return
     */
    private List<String> saveMemoTags (Memo memo) {

        // 提取出html中文本内容
        String text = Jsoup.parse(memo.getContent()).body().text();
        // 提取memo中的 #xxx 标记
        List<String> tagNameList = extractTagNames(memo.getContent());
        for (String tag : tagNameList) {
            //根据当前用户id和tag查询是否存在
            Tags tags = tagsMapper.selectOne(new QueryWrapper<Tags>().eq("tag", tag).eq("user_id", memo.getUserId()));
            //如果tag在数据库不存在，则创建
            if (tags == null) {
                tags = new Tags();
                tags.setTag(tag);
                tags.setUserId(memo.getUserId());
                tagsMapper.insert(tags);
            }
            // 映射Memo和tag
            MemoTags memoTags = new MemoTags();
            memoTags.setMemoId(memo.getId());
            memoTags.setTagsId(tags.getId());
            memoTagsMapper.insert(memoTags);
            log.info("新增Memo Tag：{}", memoTags);
        }

        return tagNameList;
    }

    /**
     * 从memo内容中提取Tag列表
     * @param memoContent
     * @return
     */
    private List<String> extractTagNames (String memoContent) {
        //正则匹配
        Matcher matcher = Pattern.compile("#(\\w|[^\\x00-\\xff]|/|\\\\)+(\\s)?", Pattern.DOTALL).matcher(memoContent);
        List<String> tagNameList = new LinkedList<>();
        while (matcher.find()) {
            String tag = matcher.group().replace(" ", ""); //去掉标签后面的空格
            tagNameList.add(tag);
        }
        return tagNameList;
    }

    /**
     * 根据ID查询tag的集合
     *
     * @param userId
     * @return
     */
    @Override
    public List<String> findUserTagNames(String userId) {
        List<Tags> tagList = tagsMapper.selectList(new QueryWrapper<Tags>().eq("user_id", userId));
        //MapReduce
        return tagList.stream().map(tag -> tag.getTag()).collect(Collectors.toList());
    }

    /**
     * 显示当前用户所有Memo
     *
     * @param userId
     * @param queryTag
     * @return
     */
    @Override
    public List<Memo> findAllMemo(String userId, String queryTag) {
//        if (StringUtils.isNoneEmpty(queryTag)) {
//            return memoMapper.findMemoByTagName(userId, "#" + queryTag);
//        }
//        return memoMapper.selectList(new QueryWrapper<Memo>().eq("user_id", userId).orderByDesc("id"));

        return memoMapper.findMemoByTagName(userId, StringUtils.isNoneEmpty(queryTag) ? "#" + queryTag : "");
    }

    /**
     * 根据MemoId删除该用户的Memo
     * @param userId
     * @param memoId
     */
    @Override
    @Transactional //事务：保证此部分代码全部运行成功或失败，不允许部分失败部分成功，出错则回滚
    public void delMemo(String userId, String memoId) {
        Memo memo = memoMapper.selectById(memoId);
        if(memo == null) {
            throw new ServiceException("笔记不存在");
        }

        if(!memo.getUserId().equals(userId)) {
            throw new ServiceException("没有权限删除");
        }

        memoMapper.deleteById(memoId);
        log.info("用户：{}，删除Memo：{}", userId, memo);

        //根据MemoID删除对应Tag
        deleteTagByMemoID(memoId);

        //删除Memo对应的图片资源
        deleteFilesByMemoID(memoId);
    }

    /**
     * 删除Memo中的文件
     * @param memoId
     */
    private void deleteFilesByMemoID(String memoId) {
        List<MemoFiles> list = memoFilesMapper.selectList(new QueryWrapper<MemoFiles>().eq("memo_id", memoId));
        if(!list.isEmpty()) {
            List<String> fileKeys = list.stream().map(memoFiles -> memoFiles.getFileKey()).collect(Collectors.toList());
            qiniuUtil.batchDeleteFile(fileKeys.stream().toArray(String[]::new));
            log.info("七牛删除文件：{}", fileKeys);
        }

        memoFilesMapper.delete(new QueryWrapper<MemoFiles>().eq("memo_id", memoId));
        log.info("删除：{}对应的图片文件", list);
    }

    /**
     * 修改Memo
     * @param editMemoDto
     * @return
     */
    @Override
    @Transactional //事务：保证此部分代码全部运行成功或失败，不允许部分失败部分成功，出错则回滚
    public Memo editMemo(EditMemoDto editMemoDto) {
        Memo memo = findByUserIdAndMemoId(editMemoDto.getUserId(), editMemoDto.getMemoId());
        if(memo == null) {
            throw new ServiceException("笔记不存在");
        }
        // 1. 删除当前旧Memo中对应的所有Tag
        deleteTagByMemoID(memo.getId());

        // 2. 设置新的内容
        memo.setContent(editMemoDto.getContent());
        memoMapper.updateById(memo);

        log.info("用户：{}修改了Memo：{} device：{}", editMemoDto.getUserId(), memo.getContent(), editMemoDto.getDevice());

        // 3. 保存新Memo中的Tag
        saveMemoTags(memo);

        updateMemoFileByMemo(editMemoDto);

        return memo;
    }

    /**
     * 更新图片文件
     * 闪念笔记9-云存储-2 32：00
     * @param editMemoDto
     */
    private void updateMemoFileByMemo(EditMemoDto editMemoDto) {
        //获取当前数据库中的依赖文件列表
        List<MemoFiles> memoFilesList = memoFilesMapper.selectList(new QueryWrapper<MemoFiles>().eq("memo_id", editMemoDto.getMemoId()));
        //获取当前客户端传入的文件列表
        List<MemoFiles> clientMemoFiles = Arrays.asList(editMemoDto.getFileList());

        /**
         * 例：
         * 原本图片：A B C
         * 修改后图片：A B D E
         * 删除了C 新增了E
         */
        //获取客户端中新增的文件列表（id为空）
        List<MemoFiles> newFileList = clientMemoFiles.stream().filter(files -> StringUtils.isEmpty(files.getId())).collect(Collectors.toList());
        // 获取客户端中已上传的文件列表（id不为空）
        List<MemoFiles> uploadedFileList = clientMemoFiles.stream().filter(files -> StringUtils.isNotEmpty(files.getId())).collect(Collectors.toList());

        //寻找已经被删除的文件（在数据库memo_files中，不在uploadedFileList中）   （前端编辑后 删除的文件信息不会传回api）
        if(memoFilesList.size() != uploadedFileList.size()) {
            List<MemoFiles> deletedFiles = new ArrayList<>();//?
            for(MemoFiles dbFile : memoFilesList) {
                boolean flag = false;
                for(MemoFiles clientFile : uploadedFileList) {
                    if(dbFile.getId().equals(clientFile.getId())) {
                        flag = true;
                        break;
                    }
                }
                if(!flag) {
                    deletedFiles.add(dbFile);
                }
            }

            //从数据库删除已被删除的文件
            List<String> deleteIdList = deletedFiles.stream().map(file -> file.getId()).collect(Collectors.toList());
            memoFilesMapper.deleteBatchIds(deleteIdList);

            //从七牛删除
            qiniuUtil.batchDeleteFile(deletedFiles.stream().map(file -> file.getFileKey()).collect(Collectors.toList()).stream().toArray(String[]::new));

            log.info("修改Memo：{}，删除文件：{}", editMemoDto.getMemoId(), deletedFiles);

        }
        //新增文件
        if(!newFileList.isEmpty()) {
            for(MemoFiles newFile : newFileList) {
                newFile.setMemoId(editMemoDto.getMemoId());
                memoFilesMapper.insert(newFile);
            }

            log.info("修改Memo：{}，新增文件：{}", editMemoDto.getMemoId(), newFileList);
        }


    }

    /**
     * 查询60日内Memo数量
     * @param userId
     * @return
     */
    @Override
    public DailyMemoCountRespDto dailyMemoCount(String userId) {
        List<Map> list = memoMapper.findDailyCount(userId);

        DailyMemoCountRespDto dailyMemoCountRespDto = new DailyMemoCountRespDto();
        dailyMemoCountRespDto.setDailyCount(list);
        return dailyMemoCountRespDto;
    }

    /**
     * 根据MemoID删除对应Tag
     * @param memoID
     */
    private void deleteTagByMemoID(String memoID) {
        // 查看Memo中是否有Tag，如果有，判断这个Tag是否有其他Memo在用，如果没有，则删除
        List<MemoTags> memoTagsList = memoTagsMapper.selectList(new QueryWrapper<MemoTags>().eq("memo_id", memoID));
        for(MemoTags memoTags :memoTagsList) {
            //ne:除了当前这个Memo
            List<MemoTags> subList = memoTagsMapper.selectList(new QueryWrapper<MemoTags>().eq("tags_id", memoTags.getTagsId()).ne("memo_id",memoID));
            if(subList.isEmpty()) {
                tagsMapper.deleteById(memoTags.getTagsId());
                log.info("级联删除无引用的Tag：{}",memoTags.getTagsId());
            }

            memoTagsMapper.deleteById(memoTags.getId());
            log.debug("删除Memo和Tag的对应关系表");

        }
    }

    /**
     * 确保Memo为此用户所有
     * @param userId
     * @param memoId
     * @return
     */
    private Memo findByUserIdAndMemoId(String userId, String memoId) {
        Memo memo = memoMapper.selectById(memoId);
        if(memo.getUserId().equals(userId)){
            return memo;
        }

        return null;
    }


}
