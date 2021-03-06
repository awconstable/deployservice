package team.deployservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class LeadTime
    {
    private final String applicationId;
    @JsonFormat(pattern="yyyy-MM-dd")
    private final Date reportingDate;
    private final long leadTimeSeconds;
    private final DORALevel leadTimePerfLevel;

    public LeadTime(String applicationId, Date reportingDate, long leadTimeSeconds, DORALevel leadTimePerfLevel)
        {
        this.applicationId = applicationId;
        this.reportingDate = reportingDate;
        this.leadTimeSeconds = leadTimeSeconds;
        this.leadTimePerfLevel = leadTimePerfLevel;
        }

    public String getApplicationId()
        {
        return applicationId;
        }

    public Date getReportingDate()
        {
        return reportingDate;
        }

    public long getLeadTimeSeconds()
        {
        return leadTimeSeconds;
        }

    public DORALevel getLeadTimePerfLevel()
        {
        return leadTimePerfLevel;
        }

    @Override
    public String toString()
        {
        return "LeadTime{" +
            "applicationId='" + applicationId + '\'' +
            ", reportingDate=" + reportingDate +
            ", leadTimeSeconds=" + leadTimeSeconds +
            ", leadTimePerfLevel=" + leadTimePerfLevel +
            '}';
        }
    }
