package com.ifan112.demo.springcloud.y.repo;

import com.ifan112.demo.springcloud.y.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Integer> {
}
