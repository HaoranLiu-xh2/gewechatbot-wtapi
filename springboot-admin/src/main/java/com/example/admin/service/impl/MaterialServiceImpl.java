package com.example.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.admin.common.exception.BusinessException;
import com.example.admin.common.page.PageQuery;
import com.example.admin.common.page.PageResult;
import com.example.admin.common.result.ResultCode;
import com.example.admin.common.utils.UserContext;
import com.alibaba.fastjson2.JSONObject;
import com.example.admin.dto.MaterialDTO;
import com.example.admin.entity.Material;
import com.example.admin.entity.WxMessage;
import com.example.admin.mapper.MaterialMapper;
import com.example.admin.mapper.WxMessageMapper;
import com.example.admin.service.MaterialService;
import com.example.admin.vo.MaterialVO;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * 素材库业务实现类
 *
 * @author example
 */
@Service
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper, Material> implements MaterialService {

    private final WxMessageMapper wxMessageMapper;

    public MaterialServiceImpl(WxMessageMapper wxMessageMapper) {
        this.wxMessageMapper = wxMessageMapper;
    }

    @Override
    public PageResult<MaterialVO> pageList(PageQuery pageQuery, Integer type) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Page<Material> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());

        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Material::getUserId, currentUserId);
        wrapper.eq(type != null, Material::getType, type)
                .and(StrUtil.isNotBlank(pageQuery.getKeyword()), w -> w
                        .like(Material::getName, pageQuery.getKeyword()))
                .orderByDesc(Material::getCreateTime);

        Page<Material> materialPage = baseMapper.selectPage(page, wrapper);

        Page<MaterialVO> voPage = new Page<>();
        BeanUtil.copyProperties(materialPage, voPage);
        voPage.setRecords(materialPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));

        return PageResult.build(voPage);
    }

    @Override
    public MaterialVO getById(Long id) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Material material = baseMapper.selectById(id);
        if (material == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!currentUserId.equals(material.getUserId())) {
            throw new BusinessException("无权查看该素材");
        }
        return convertToVO(material);
    }

    @Override
    public void add(MaterialDTO materialDTO) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        validateContent(materialDTO.getContent());

        Material material = new Material();
        BeanUtil.copyProperties(materialDTO, material);
        material.setUserId(currentUserId);
        baseMapper.insert(material);
    }

    @Override
    public void update(MaterialDTO materialDTO) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (materialDTO.getId() == null) {
            throw new BusinessException("素材 ID 不能为空");
        }

        Material existMaterial = baseMapper.selectById(materialDTO.getId());
        if (existMaterial == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!currentUserId.equals(existMaterial.getUserId())) {
            throw new BusinessException("无权修改该素材");
        }

        validateContent(materialDTO.getContent());

        LambdaUpdateWrapper<Material> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Material::getId, materialDTO.getId())
                .set(Material::getName, materialDTO.getName())
                .set(Material::getType, materialDTO.getType())
                .set(Material::getContent, materialDTO.getContent());

        baseMapper.update(null, updateWrapper);
    }

    @Override
    public void delete(Long id) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Material material = baseMapper.selectById(id);
        if (material == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!currentUserId.equals(material.getUserId())) {
            throw new BusinessException("无权删除该素材");
        }

        baseMapper.deleteById(id);
    }

    @Override
    public Long saveFromMessage(Long messageId) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (messageId == null) {
            throw new BusinessException("消息 ID 不能为空");
        }

        WxMessage message = wxMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!currentUserId.equals(message.getUserId())) {
            throw new BusinessException("无权操作该消息");
        }

        Integer msgType = message.getMsgType();
        Integer materialType;
        String url;
        String fileName = null;
        Integer videoDuration = 0;

        if (Integer.valueOf(3).equals(msgType)) {
            materialType = 1;
            url = extractUrl(message.getContent(), "url");
            if (StrUtil.isBlank(url)) {
                url = extractUrl(message.getContent(), "thumb");
            }
            if (StrUtil.isBlank(url)) {
                url = cleanBackticks(message.getContent());
            }
        } else if (Integer.valueOf(43).equals(msgType)) {
            materialType = 4;
            url = extractUrl(message.getContent(), "url");
            if (StrUtil.isBlank(url)) {
                url = cleanBackticks(message.getContent());
            }
        } else if (Integer.valueOf(6).equals(msgType)) {
            materialType = 6;
            url = extractUrl(message.getContent(), "url");
            fileName = extractUrl(message.getContent(), "fileName");
            if (StrUtil.isBlank(url)) {
                url = cleanBackticks(message.getContent());
            }
        } else {
            throw new BusinessException("仅支持保存图片、视频、文件消息为素材");
        }

        if (StrUtil.isBlank(url) || !isHttpUrl(url)) {
            throw new BusinessException("消息中未找到可保存的媒体地址");
        }

        JSONObject contentObj = new JSONObject();
        contentObj.put("url", url);
        if (StrUtil.isNotBlank(fileName)) {
            contentObj.put("fileName", fileName);
        }
        if (Integer.valueOf(43).equals(msgType)) {
            contentObj.put("videoDuration", videoDuration);
        }

        Material material = new Material();
        material.setUserId(currentUserId);
        material.setType(materialType);
        material.setName(buildMaterialName(msgType, fileName, messageId));
        material.setContent(contentObj.toJSONString());
        baseMapper.insert(material);
        return material.getId();
    }

    /**
     * 校验素材内容是否为合法 JSON
     */
    private void validateContent(String content) {
        if (StrUtil.isBlank(content)) {
            return;
        }
        if (!JSON.isValid(content)) {
            throw new BusinessException("素材内容必须是合法的 JSON 格式");
        }
    }

    /**
     * 将 Material 实体转换为 MaterialVO
     */
    private MaterialVO convertToVO(Material material) {
        MaterialVO vo = new MaterialVO();
        BeanUtil.copyProperties(material, vo);
        return vo;
    }

    /**
     * 从 JSON 内容中提取指定字段，内容不是 JSON 时返回原字符串
     */
    private String extractUrl(String content, String key) {
        if (StrUtil.isBlank(content)) {
            return null;
        }
        String trimmed = content.trim();
        if (!trimmed.startsWith("{")) {
            return null;
        }
        try {
            JSONObject json = JSON.parseObject(trimmed);
            String value = json.getString(key);
            return StrUtil.isNotBlank(value) ? cleanBackticks(value) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断是否为 http/https 地址
     */
    private boolean isHttpUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return false;
        }
        String lower = url.trim().toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    /**
     * 去除第三方接口常见的反引号包裹
     */
    private String cleanBackticks(String value) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        String str = value.trim();
        if (str.startsWith("`") && str.endsWith("`")) {
            return str.substring(1, str.length() - 1).trim();
        }
        return str;
    }

    /**
     * 根据消息类型构建默认素材名称
     */
    private String buildMaterialName(Integer msgType, String fileName, Long messageId) {
        if (StrUtil.isNotBlank(fileName)) {
            return fileName;
        }
        String suffix = messageId != null ? String.valueOf(messageId) : String.valueOf(System.currentTimeMillis());
        if (Integer.valueOf(3).equals(msgType)) {
            return "图片_" + suffix;
        }
        if (Integer.valueOf(43).equals(msgType)) {
            return "视频_" + suffix;
        }
        return "文件_" + suffix;
    }
}
