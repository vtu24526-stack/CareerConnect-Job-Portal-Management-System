package com.jobportal.jpm.repository;

import com.jobportal.jpm.model.Job;
import com.jobportal.jpm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByEmployer(User employer);
    
    @Query("SELECT j FROM Job j WHERE " +
           "(:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:jobType IS NULL OR :jobType = '' OR LOWER(j.jobType) = LOWER(:jobType)) AND " +
           "(:minSalary IS NULL OR j.salary >= :minSalary)")
    List<Job> searchJobs(@Param("keyword") String keyword, 
                         @Param("location") String location, 
                         @Param("jobType") String jobType, 
                         @Param("minSalary") Double minSalary);
}
