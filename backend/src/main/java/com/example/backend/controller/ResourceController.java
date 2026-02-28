package com.example.backend.controller;

import com.example.backend.entity.Resource;
import com.example.backend.service.ResourceService;
import com.example.backend.mapper.ResourceMapper;
import com.example.backend.service.UserService;
import com.example.backend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;
    
    @Autowired
    private ResourceMapper resourceMapper;
    
    @Autowired
    private UserService userService;
    
    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @GetMapping("/resources")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search) {
        
        // 处理"全部"分类
        if ("全部".equals(category) || (category != null && category.trim().isEmpty())) {
            category = null;
        }
        if ("全部".equals(type) || (type != null && type.trim().isEmpty())) {
            type = null;
        }
        if (search != null && search.trim().isEmpty()) {
            search = null;
        }
        
        try {
            List<Resource> resources = resourceService.listAll(page, size, category, type, search);
            long total = resourceService.count(category, type, search);
            int totalPages = (int) Math.ceil((double) total / size);

            Map<String, Object> data = new HashMap<>();
            data.put("data", resources);
            data.put("total", total);
            data.put("totalPages", totalPages);
            data.put("currentPage", page);
            data.put("pageSize", size);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "OK");
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取资源列表失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/test-software")
    public ResponseEntity<Map<String, Object>> testSoftware() {
        try {
            List<Resource> resources = resourceService.listAll(1, 10, "software", null, null);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "OK");
            response.put("data", resources);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "测试查询失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/test-software-mapper")
    public ResponseEntity<Map<String, Object>> testSoftwareMapper() {
        try {
            List<Resource> resources = resourceMapper.testSoftwareQuery();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "OK");
            response.put("data", resources);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "服务器内部错误");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/test-category-param")
    public ResponseEntity<Map<String, Object>> testCategoryParam(@RequestParam String category) {
        try {
            List<Resource> resources = resourceMapper.testCategoryQuery(category);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "OK");
            response.put("data", resources);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "服务器内部错误");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/test-count-param")
    public ResponseEntity<Map<String, Object>> testCountParam(@RequestParam String category) {
        try {
            long count = resourceMapper.testCountQuery(category);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("data", count);
            response.put("message", "OK");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/test-dynamic-fixed")
    public ResponseEntity<Map<String, Object>> testDynamicFixed(@RequestParam String category) {
        try {
            long count = resourceMapper.testDynamicCountFixed(category);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("data", count);
            response.put("message", "OK");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/test-list-simple")
    public ResponseEntity<Map<String, Object>> testListSimple() {
        try {
            List<Resource> resources = resourceMapper.list(0, 5, null, null, null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("data", resources);
            response.put("message", "OK");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/test-db-data")
    public ResponseEntity<Map<String, Object>> testDbData() {
        try {
            List<Map<String, Object>> data = resourceMapper.getDbData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("data", data);
            response.put("message", "OK");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查看数据库数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/test-dynamic-count")
    public ResponseEntity<Map<String, Object>> testDynamicCount(@RequestParam String category) {
        try {
            long count = resourceMapper.count(category, null, null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("data", count);
            response.put("message", "OK");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }





    @GetMapping("/resources/{id}")
    public ResponseEntity<Map<String, Object>> getResourceDetail(@PathVariable Long id, Authentication authentication) {
        try {
            Resource resource = resourceService.getById(id, true);
            if (resource == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "资源不存在");
                return ResponseEntity.status(404).body(response);
            }

            // 检查HIC认证要求
            if (resource.getHic() != null && resource.getHic() == 1) {
                // 需要HIC认证的资源
                if (authentication == null) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", 401);
                    response.put("message", "需要HIC认证才能访问");
                    return ResponseEntity.status(401).body(response);
                }
                
                // 检查用户是否有HIC认证
                String username = authentication.getName();
                User user = userService.getByUsername(username);
                if (user == null || user.getHic() == null || user.getHic() != 1) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", 403);
                    response.put("message", "需要HIC认证才能访问");
                    return ResponseEntity.status(403).body(response);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "OK");
            response.put("data", resource);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取资源详情失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/resources/{id}/view")
    public ResponseEntity<Map<String, Object>> incrementView(@PathVariable Long id) {
        try {
            resourceService.getById(id, true);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "OK");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "增加浏览次数失败");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/resources/{id}/download")
    public ResponseEntity<?> downloadResource(@PathVariable Long id, Authentication authentication) {
        try {
            // 获取资源信息
            Resource resource = resourceService.getById(id, false);
            if (resource == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "资源不存在");
                return ResponseEntity.status(404).body(response);
            }

            // 检查HIC认证要求
            if (resource.getHic() != null && resource.getHic() == 1) {
                // 需要HIC认证的资源
                if (authentication == null) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", 401);
                    response.put("message", "需要HIC认证才能访问");
                    return ResponseEntity.status(401).body(response);
                }
                
                // 检查用户是否有HIC认证
                String username = authentication.getName();
                User user = userService.getByUsername(username);
                if (user == null || user.getHic() == null || user.getHic() != 1) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", 403);
                    response.put("message", "需要HIC认证才能访问");
                    return ResponseEntity.status(403).body(response);
                }
            }

            // 获取文件路径
            String fileUrl = resource.getResourceUrl();
            if (fileUrl == null || fileUrl.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", "该资源没有可下载的文件");
                return ResponseEntity.status(400).body(response);
            }

            // 解析文件路径
            String filePath;
            if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
                // 如果是完整URL，提取相对路径
                if (fileUrl.contains("/uploads/")) {
                    String relativePath = fileUrl.substring(fileUrl.indexOf("/uploads/") + 1);
                    filePath = uploadPath + "/" + relativePath.substring("uploads/".length());
                } else {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", 400);
                    response.put("message", "无效的文件URL格式");
                    return ResponseEntity.status(400).body(response);
                }
            } else if (fileUrl.startsWith("/uploads/")) {
                // 相对路径
                filePath = uploadPath + "/" + fileUrl.substring("/uploads/".length());
            } else {
                // 直接使用绝对路径
                filePath = fileUrl;
            }

            // 检查文件是否存在
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "文件不存在");
                return ResponseEntity.status(404).body(response);
            }

            // 获取文件名 - 优先使用实际文件名，确保包含正确的扩展名
            String originalFileName = file.getName();
            String fileName = originalFileName;
            
            // 如果资源有标题且标题包含扩展名，则使用标题
            String resourceTitle = resource.getTitle();
            if (resourceTitle != null && !resourceTitle.trim().isEmpty() && resourceTitle.contains(".")) {
                fileName = resourceTitle;
            } else if (resourceTitle != null && !resourceTitle.trim().isEmpty()) {
                // 如果标题不为空但没有扩展名，则添加原文件的扩展名
                if (originalFileName.contains(".")) {
                    String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                    fileName = resourceTitle + extension;
                } else {
                    fileName = resourceTitle;
                }
            }

            // 编码文件名以支持中文
            String encodedFileName;
            try {
                encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            } catch (UnsupportedEncodingException e) {
                encodedFileName = fileName;
            }

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName);
            // 允许前端读取关键响应头（跨域情况下）
            headers.add("Access-Control-Expose-Headers", "Content-Disposition, Content-Type");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            // 返回文件
            FileSystemResource fileResource = new FileSystemResource(file);
            return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .body(fileResource);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "文件下载失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/debug-categories")
    public ResponseEntity<Map<String, Object>> debugCategories() {
        try {
            List<String> categories = resourceMapper.getAllCategories();
            List<Resource> allResources = resourceMapper.getAllResourcesInfo();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("categories", categories);
            response.put("resources", allResources);
            response.put("message", "OK");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}