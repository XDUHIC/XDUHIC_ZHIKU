//package com.example.backend.service;
//
////import com.example.backend.entity.Resource;
////
////import java.util.List;
////import java.util.Map;
////
////public interface ResourceService {
////    List<Resource> listAll(int page, int size, String category, String type, String search);
////    long count(String category, String type, String search);
////    Resource getById(Long id, boolean increaseView);
////
////    Integer testDynamicCountFixed(String category);
////
////    List<Map<String, Object>> getDbData();
////
////}
//
//import com.example.backend.entity.Resource;
//import java.util.List;
//import java.util.Map;
//
//public interface ResourceService {
//    List<Resource> listAll(int page,
//                           int size,
//                           String category,
//                           String type,
//                           String search,
//                           List<String> tags);           // 新增 tags 参数
//
//    long count(String category,
//               String type,
//               String search,
//               List<String> tags);                      // 新增 tags 参数
//
//    Resource getById(Long id, boolean increaseView);
//
//    Integer testDynamicCountFixed(String category);
//    List<Map<String, Object>> getDbData();
//}


package com.example.backend.service;

import com.example.backend.dto.ResourceDtos;
import com.example.backend.entity.Resource;

import java.util.List;
import java.util.Map;

public interface ResourceService {
    List<Resource> listAll(int page, int size, String category, String type, String search, List<String> tags, String source);
    long count(String category, String type, String search, List<String> tags, String source);
    Resource getById(Long id, boolean increaseView);

    /** 列表返回 DTO，tags 为 List&lt;String&gt; */
    List<ResourceDtos.ResourceListResp> listAllAsDto(int page, int size, String category, String type, String search, List<String> tags, String source);
    /** 详情返回 DTO，tags 为 List&lt;String&gt; */
    ResourceDtos.ResourceDetailResp getDetailDto(Long id, boolean increaseView);
    /** 将 Entity 转为详情 DTO（用于已有 Resource 时避免二次查询） */
    ResourceDtos.ResourceDetailResp toDetailResp(Resource resource);

    Integer testDynamicCountFixed(String category);

    List<Map<String, Object>> getDbData();

}