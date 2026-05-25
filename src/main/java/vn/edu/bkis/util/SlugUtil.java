package vn.edu.bkis.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Tien ich chuan hoa chuoi de tao slug va folder path an toan.
 */
public final class SlugUtil {
    private static final Pattern DIACRITIC_PATTERN = Pattern.compile("\\p{M}+");
    private static final Pattern NON_WORD_PATTERN = Pattern.compile("[^a-zA-Z0-9]+");
    private static final Pattern MULTI_DASH_PATTERN = Pattern.compile("-{2,}");

    private SlugUtil() {
    }

    /**
     * Chuan hoa chuoi thanh slug khong dau.
     *
     * @param rawValue chuoi dau vao co the chua dau va ky tu dac biet
     * @param fallback gia tri fallback neu ket qua rong
     * @return slug chi gom [a-z0-9-]
     */
    public static String toSlug(String rawValue, String fallback) {
        String normalized = normalizeText(rawValue);
        if (normalized.isBlank()) {
            return fallback;
        }

        String slug = NON_WORD_PATTERN.matcher(normalized).replaceAll("-");
        slug = MULTI_DASH_PATTERN.matcher(slug).replaceAll("-");
        slug = trimDash(slug).toLowerCase(Locale.ROOT);
        return slug.isBlank() ? fallback : slug;
    }

    /**
     * Chuan hoa duong dan folder theo tung segment.
     *
     * @param rawFolder duong dan folder dau vao, co the chua dau gach cheo dau/cuoi
     * @param fallbackFolder folder fallback neu duong dan rong
     * @return folder da chuan hoa, khong bat dau bang '/'
     */
    public static String toSafeFolderPath(String rawFolder, String fallbackFolder) {
        String normalizedFolder = normalizeFolder(rawFolder);
        if (normalizedFolder.isBlank()) {
            return normalizeFolder(fallbackFolder);
        }

        String[] segments = normalizedFolder.split("/");
        List<String> safeSegments = new ArrayList<>();
        for (String segment : segments) {
            String safeSegment = toSlug(segment, "");
            if (safeSegment.isBlank() || ".".equals(safeSegment) || "..".equals(safeSegment)) {
                continue;
            }
            safeSegments.add(safeSegment);
        }

        if (safeSegments.isEmpty()) {
            return normalizeFolder(fallbackFolder);
        }
        return String.join("/", safeSegments);
    }

    /**
     * Chuan hoa ten file de tranh ky tu nguy hiem.
     *
     * @param rawFileName ten file goc
     * @param fallback fallback neu ten file khong hop le
     * @return ten file da chuan hoa, giu lai dau cham cho extension
     */
    public static String toSafeFileName(String rawFileName, String fallback) {
        String normalized = normalizeText(rawFileName).replaceAll("[^a-zA-Z0-9._-]+", "-");
        normalized = MULTI_DASH_PATTERN.matcher(normalized).replaceAll("-");
        normalized = trimDash(normalized);
        if (normalized.isBlank() || ".".equals(normalized) || "..".equals(normalized)) {
            return fallback;
        }
        return normalized;
    }

    /**
     * Chuan hoa text thong thuong de phuc vu slug.
     *
     * @param rawValue chuoi dau vao bat ky
     * @return chuoi bo dau, trim va khong null
     */
    public static String normalizeText(String rawValue) {
        if (rawValue == null) {
            return "";
        }
        String normalized = Normalizer.normalize(rawValue, Normalizer.Form.NFD);
        normalized = DIACRITIC_PATTERN.matcher(normalized).replaceAll("");
        return normalized.trim();
    }

    private static String normalizeFolder(String rawFolder) {
        if (rawFolder == null) {
            return "";
        }
        return rawFolder.replace('\\', '/').replaceAll("^/+", "").replaceAll("/+$", "").trim();
    }

    private static String trimDash(String value) {
        return value.replaceAll("^-+", "").replaceAll("-+$", "");
    }
}
