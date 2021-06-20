package com.leif.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leif.mapper.MemoMapper;
import com.leif.mapper.MemoTagsMapper;
import com.leif.mapper.TagsMapper;
import com.leif.model.dto.request.CreateMemoDto;
import com.leif.model.dto.respons.CreateMemoRespDto;
import com.leif.model.entity.Memo;
import com.leif.model.entity.MemoTags;
import com.leif.model.entity.Tags;
import com.leif.service.MemoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class MemoServiceImpl implements MemoService {

    @Autowired
    private MemoMapper memoMapper;

    @Autowired
    private TagsMapper tagsMapper;

    @Autowired
    private MemoTagsMapper memoTagsMapper;
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

        //TODO 5. 查看Memo是否有关联图片

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
        return null;
    }

    /**
     * 显示当前用户所有Memo
     *
     * @param userId
     * @param queryTag
     * @return
     */
    @Override
    public List<String> findAllMemo(String userId, String queryTag) {
        return null;
    }
}
