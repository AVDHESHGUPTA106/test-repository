package com.sivalabs.bookmarks.services;

import com.sivalabs.bookmarks.repositories.FileProcessingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Java equivalent of SP_GetDocumentsForFileProcessing_genAi_multi_company.
 *
 * <p>
 * Differences vs stored procedure:
 * <ul>
 *   <li>The SP uses a cursor to iterate companies from a CSV input.</li>
 *   <li>This service does that iteration in Java, and executes set-based jOOQ queries per company.</li>
 * </ul>
 */
@Service
public class FileProcessingService {

    private static final Pattern UUID_DASHED = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private final FileProcessingRepository repo;

    public FileProcessingService(FileProcessingRepository repo) {
        this.repo = repo;
    }

    public List<FileProcessingDocumentDTO> getDocumentsForFileProcessing(
        String loggedUserEmail,
        List<String> companyIds,
        LocalDate startDate,
        LocalDate endDate
    ) {
        if (companyIds == null || companyIds.isEmpty()) {
            return List.of();
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate and endDate are required");
        }

        byte[] userIdBin = repo.findUserIdBinByEmail(loggedUserEmail);
        if (userIdBin == null) {
            return List.of();
        }

        boolean isAdmin = repo.isSuperUser(userIdBin);

        return companyIds.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .flatMap(companyIdStr -> {
                byte[] companyIdBin = uuidStringToBin(companyIdStr);
                if (companyIdBin == null) {
                    return java.util.stream.Stream.empty();
                }

                boolean isManager = repo.isPgManagerForCompany(userIdBin, companyIdBin);
                boolean isAdminOrManager = isAdmin || isManager;
                boolean isUserInCompany = repo.isUserInCompany(userIdBin, companyIdBin);

                List<FileProcessingDocumentDTO> rows = repo.fetchDocumentsForCompany(
                    userIdBin,
                    isAdminOrManager,
                    isUserInCompany,
                    companyIdBin,
                    startDate,
                    endDate
                );
                return rows.stream();
            })
            .toList();
    }

    /**
     * Convert a dashed UUID string into MySQL's UUID_TO_BIN() byte order.
     *
     * <p>
     * This method matches the standard UUID layout used by MySQL UUID_TO_BIN(UUID).
     */
    static byte[] uuidStringToBin(String uuid) {
        if (uuid == null) return null;
        String s = uuid.trim();
        if (!UUID_DASHED.matcher(s).matches()) return null;

        // Remove dashes and parse as hex
        String hex = s.replace("-", "");
        byte[] out = new byte[16];
        for (int i = 0; i < 16; i++) {
            int idx = i * 2;
            out[i] = (byte) Integer.parseInt(hex.substring(idx, idx + 2), 16);
        }
        return out;
    }
}



