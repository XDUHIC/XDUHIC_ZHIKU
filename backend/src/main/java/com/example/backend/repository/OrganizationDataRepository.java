package com.example.backend.repository;

import com.example.backend.entity.OrganizationData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrganizationDataRepository extends JpaRepository<OrganizationData, Long> {

    // 自定义查询：获取最新的一条记录（按 id 降序）
    @Query(value = "SELECT * FROM organization_data ORDER BY id DESC LIMIT 1", nativeQuery = true)
    OrganizationData findLatest();
}
