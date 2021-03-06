package uk.nhs.adaptors.gp2gp.ehr;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import uk.nhs.adaptors.gp2gp.ehr.model.EhrExtractStatus;

public class EhrExtractStatusValidator {

    public static boolean isPreparingDataFinished(EhrExtractStatus ehrExtractStatus) {
        return isPatientStructuredRecordTranslated(ehrExtractStatus) && areAllDocumentsAssociatedWithPatientFetched(ehrExtractStatus);
    }

    private static boolean isPatientStructuredRecordTranslated(EhrExtractStatus ehrExtractStatus) {
        return ehrExtractStatus.getGpcAccessStructured() != null
            && StringUtils.isNoneBlank(ehrExtractStatus.getGpcAccessStructured().getObjectName());
    }

    private static boolean areAllDocumentsAssociatedWithPatientFetched(EhrExtractStatus ehrExtractStatus) {
        if (ehrExtractStatus.getGpcAccessDocument() != null && ehrExtractStatus.getGpcAccessDocument().getDocuments() != null) {
            List<EhrExtractStatus.GpcAccessDocument.GpcDocument> documents = ehrExtractStatus.getGpcAccessDocument().getDocuments();

            return documents.isEmpty() || documents.stream().allMatch(gpcDocument -> StringUtils.isNoneBlank(gpcDocument.getObjectName()));
        }

        return false;
    }
}
