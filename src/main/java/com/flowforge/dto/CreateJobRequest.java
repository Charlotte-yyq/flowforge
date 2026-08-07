/*Enable FlowForge to accept JSON when users submit jobs.*/
package com.flowforge.dto;

public record CreateJobRequest(String type, String payload){

}
