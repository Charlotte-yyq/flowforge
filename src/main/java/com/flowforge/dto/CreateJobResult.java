package com.flowforge.dto;

import com.flowforge.model.Job;

public record CreateJobResult(Job job, boolean created) {
}
