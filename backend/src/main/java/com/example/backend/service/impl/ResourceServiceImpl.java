//package com.example.backend.service.impl;
//
//import com.example.backend.entity.Resource;
//import com.example.backend.mapper.ResourceMapper;
//import com.example.backend.service.ResourceService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class ResourceServiceImpl implements ResourceService {
//
//    private final ResourceMapper resourceMapper;
//
////    @Override
////    public List<Resource> listAll(int page, int size, String category, String type, String search) {
////        int offset = (page - 1) * size;
////        int limit = size;
////        return resourceMapper.list(offset, limit, category, type, search);
////    }
////
////    @Override
////    public long count(String category, String type, String search) {
////        return resourceMapper.count(category, type, search);
////    }
//    @Override
//    public List<Resource> listAll(int page,
//                                  int size,
//                                  String category,
//                                  String type,
//                                  String search,
//                                  List<String> tags) {   // 新增 tags 参数
//        int offset = (page - 1) * size;
//        int limit = size;
//        return resourceMapper.list(offset, limit, category, type, search, tags);
//    }
//
//    @Override
//    public long count(String category,
//                      String type,
//                      String search,
//                      List<String> tags) {              // 新增 tags 参数
//        return resourceMapper.count(category, type, search, tags);
//    }
//    @Override
//    public Resource getById(Long id, boolean increaseView) {
//        if (increaseView) {
//            resourceMapper.incrementEyeCount(id);
//        }
//        return resourceMapper.findById(id);
//    }
//
//    @Override
//    public Integer testDynamicCountFixed(String category) {
//        return resourceMapper.testDynamicCountFixed(category);
//    }
//
//    @Override
//    public List<Map<String, Object>> getDbData() {
//        return resourceMapper.getDbData();
//    }
//}


package com.example.backend.service.impl;

import com.example.backend.dto.ResourceDtos;
import com.example.backend.entity.Resource;
import com.example.backend.mapper.ResourceMapper;
import com.example.backend.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceMapper resourceMapper;

    private static List<String> tagsFromString(String tagsStr) {
        if (tagsStr == null || tagsStr.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tagsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static ResourceDtos.ResourceListResp toListResp(Resource r) {
        ResourceDtos.ResourceListResp dto = new ResourceDtos.ResourceListResp();
        dto.setId(r.getId());
        dto.setTitle(r.getTitle());
        dto.setDescription(r.getDescription());
        dto.setCategory(r.getCategory());
        dto.setType(r.getType());
        dto.setResourceUrl(r.getResourceUrl());
        dto.setEyeCount(r.getEyeCount());
        dto.setHic(r.getHic());
        dto.setSource(r.getSource());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setTags(tagsFromString(r.getTags()));
        return dto;
    }

    private static ResourceDtos.ResourceDetailResp copyToDetailResp(Resource r) {
        ResourceDtos.ResourceDetailResp dto = new ResourceDtos.ResourceDetailResp();
        dto.setId(r.getId());
        dto.setTitle(r.getTitle());
        dto.setDescription(r.getDescription());
        dto.setCategory(r.getCategory());
        dto.setType(r.getType());
        dto.setResourceUrl(r.getResourceUrl());
        dto.setEyeCount(r.getEyeCount());
        dto.setHic(r.getHic());
        dto.setSource(r.getSource());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setTags(tagsFromString(r.getTags()));
        dto.setContentUrl(r.getContentUrl());
        return dto;
    }

    @Override
    public List<Resource> listAll(int page, int size, String category, String type, String search, List<String> tags, String source) {
        int offset = (page - 1) * size;
        int limit = size;
        return resourceMapper.list(offset, limit, category, type, search, tags, source);
    }

    @Override
    public long count(String category, String type, String search, List<String> tags, String source) {
        return resourceMapper.count(category, type, search, tags, source);
    }

    @Override
    public Resource getById(Long id, boolean increaseView) {
        if (increaseView) {
            resourceMapper.incrementEyeCount(id);
        }
        return resourceMapper.findById(id);
    }

    @Override
    public List<ResourceDtos.ResourceListResp> listAllAsDto(int page, int size, String category, String type, String search, List<String> tags, String source) {
        List<Resource> list = listAll(page, size, category, type, search, tags, source);
        List<ResourceDtos.ResourceListResp> result = new ArrayList<>(list.size());
        for (Resource r : list) {
            result.add(toListResp(r));
        }
        return result;
    }

    @Override
    public ResourceDtos.ResourceDetailResp getDetailDto(Long id, boolean increaseView) {
        Resource r = getById(id, increaseView);
        return r == null ? null : copyToDetailResp(r);
    }

    @Override
    public ResourceDtos.ResourceDetailResp toDetailResp(Resource resource) {
        return resource == null ? null : copyToDetailResp(resource);
    }

    @Override
    public Integer testDynamicCountFixed(String category) {
        return resourceMapper.testDynamicCountFixed(category);
    }

    @Override
    public List<Map<String, Object>> getDbData() {
        return resourceMapper.getDbData();
    }
}