package uk.nhs.adaptors.gp2gp.common.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.gp2gp.ehr.SendEhrExtractCoreTaskDefinition;
import uk.nhs.adaptors.gp2gp.gpc.GetGpcDocumentTaskDefinition;
import uk.nhs.adaptors.gp2gp.gpc.GetGpcStructuredTaskDefinition;

@Getter
@RequiredArgsConstructor
public enum TaskType {
    GET_GPC_DOCUMENT(GetGpcDocumentTaskDefinition.class),
    GET_GPC_STRUCTURED(GetGpcStructuredTaskDefinition.class),
    SEND_EHR_EXTRACT_CORE(SendEhrExtractCoreTaskDefinition.class);

    private final Class<? extends TaskDefinition> classOfTaskDefinition;

    public String getTaskTypeHeaderValue() {
        return classOfTaskDefinition.getName();
    }
}
