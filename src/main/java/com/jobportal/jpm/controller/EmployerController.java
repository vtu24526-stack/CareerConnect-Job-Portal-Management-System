package com.jobportal.jpm.controller;

import com.jobportal.jpm.config.CustomUserDetails;
import com.jobportal.jpm.model.Application;
import com.jobportal.jpm.model.ApplicationStatus;
import com.jobportal.jpm.model.Job;
import com.jobportal.jpm.model.User;
import com.jobportal.jpm.service.ApplicationService;
import com.jobportal.jpm.service.JobService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/employer")
public class EmployerController {

    private final JobService jobService;
    private final ApplicationService applicationService;

    public EmployerController(JobService jobService, ApplicationService applicationService) {
        this.jobService = jobService;
        this.applicationService = applicationService;
    }

    private User getEmployer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User employer = getEmployer();
        List<Job> jobs = jobService.findJobsByEmployer(employer);
        model.addAttribute("jobs", jobs);
        return "dashboards/employer";
    }

    @GetMapping("/job/add")
    public String showAddJobForm(Model model) {
        model.addAttribute("job", new Job());
        return "jobs/add";
    }

    @PostMapping("/job/add")
    public String addJob(@Valid @ModelAttribute("job") Job job, BindingResult result) {
        if (result.hasErrors()) {
            return "jobs/add";
        }
        job.setEmployer(getEmployer());
        jobService.saveJob(job);
        return "redirect:/employer/dashboard";
    }

    @GetMapping("/job/delete/{id}")
    public String deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return "redirect:/employer/dashboard";
    }
    
    @GetMapping("/job/{id}/applications")
    public String viewApplications(@PathVariable Long id, Model model) {
        Job job = jobService.findJobById(id).orElseThrow(() -> new IllegalArgumentException("Invalid job ID"));
        if (!job.getEmployer().getId().equals(getEmployer().getId())) {
            return "redirect:/employer/dashboard";
        }
        List<Application> applications = applicationService.getApplicationsByJob(job);
        model.addAttribute("job", job);
        model.addAttribute("applications", applications);
        return "jobs/applications";
    }

    @PostMapping("/application/{appId}/status")
    public String updateApplicationStatus(@PathVariable Long appId, @RequestParam("status") ApplicationStatus status, @RequestParam("jobId") Long jobId) {
        applicationService.updateStatus(appId, status);
        return "redirect:/employer/job/" + jobId + "/applications";
    }

    @GetMapping("/resume/download/{fileName}")
    public ResponseEntity<Resource> downloadResume(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get("uploads/resumes/").resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("Could not read file: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + fileName, e);
        }
    }
}
