package team.deployservice.model;

import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

public class Change
    {
    @Id
    @NotBlank(message = "Change: unique id is mandatory")
    private final String id;
    @NotNull(message = "Change: created date is mandatory")
    private final Date created;
    private final String source;
    private final String eventType;
    private long leadTimeSeconds;

    public Change(String id, Date created, String source, String eventType)
        {
        this.id = id;
        this.created = created;
        this.source = source;
        this.eventType = eventType;
        }

    public String getId()
        {
        return id;
        }

    public Date getCreated()
        {
        return created;
        }

    public String getSource()
        {
        return source;
        }

    public String getEventType()
        {
        return eventType;
        }

    public long getLeadTimeSeconds()
        {
        return leadTimeSeconds;
        }

    public void setLeadTimeSeconds(long leadTimeSeconds)
        {
        this.leadTimeSeconds = leadTimeSeconds;
        }

    @Override
    public boolean equals(Object o)
        {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Change change = (Change) o;
        return id.equals(change.id) &&
            created.equals(change.created) &&
            Objects.equals(source, change.source) &&
            Objects.equals(eventType, change.eventType);
        }

    @Override
    public int hashCode()
        {
        return Objects.hash(id, created, source, eventType);
        }

    @Override
    public String toString()
        {
        return "Change{" +
            "id='" + id + '\'' +
            ", created=" + created +
            ", source='" + source + '\'' +
            ", eventType='" + eventType + '\'' +
            ", leadTimeSeconds=" + leadTimeSeconds +
            '}';
        }
    }
