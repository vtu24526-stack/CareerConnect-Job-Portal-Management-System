package com.jobportal.jpm.service;

import com.jobportal.jpm.model.Job;
import com.jobportal.jpm.model.User;
import com.jobportal.jpm.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {
    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public List<Job> findAllJobs() {
        return jobRepository.findAll();
    }

    public List<Job> findJobsByEmployer(User employer) {
        return jobRepository.findByEmployer(employer);
    }

    public Job saveJob(Job job) {
        return jobRepository.save(job);
    }

    public Optional<Job> findJobById(Long id) {
        return jobRepository.findById(id);
    }

    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }

    public List<Job> searchJobs(String keyword, String location, String jobType, Double minSalary) {
        if((keyword == null || keyword.isEmpty()) && 
           (location == null || location.isEmpty()) && 
           (jobType == null || jobType.isEmpty()) && 
           minSalary == null) {
            return findAllJobs();
        }
        return jobRepository.searchJobs(keyword, location, jobType, minSalary);
    }
}
