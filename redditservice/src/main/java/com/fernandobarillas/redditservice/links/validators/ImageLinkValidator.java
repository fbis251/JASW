package com.fernandobarillas.redditservice.links.validators;

import com.fernandobarillas.redditservice.models.Link;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by fb on 12/15/15.
 */
public class ImageLinkValidator implements LinkValidator {
    private boolean mShowNsfw;
    private static final String[] HOST_WHITELIST      = {"imgur.com", "vidble.com", "eroshare.com"};
    private static final String[] EXTENSION_BLACKLIST = {".gif", ".gifv",};
    private static final String[] EXTENSION_WHITELIST = {".jpg", ".jpeg", ".png", ".bmp"};

    public ImageLinkValidator(boolean showNsfw) {
        mShowNsfw = showNsfw;
    }

    /**
     * Attempts to validate URLs that appear to be image links
     * @param link
     * @return
     */
    @Override
    public boolean isLinkValid(Link link) {
        // Don't display links marked as NSFW unless the user wants to
        if (link.isNsfw() && !mShowNsfw) {
            return false;
        }

        URL url;
        try {
            url = new URL(link.getUrl());
        } catch (MalformedURLException e) {
            return false;
        }

        for (String extension : EXTENSION_BLACKLIST) {
            if (url.getPath().endsWith(extension)) {
                return false;
            }
        }

        for (String extension : EXTENSION_WHITELIST) {
            if (url.getPath().endsWith(extension)) {
                return true;
            }
        }

        if (link.getThumbnails() != null && link.getThumbnails().getSource() != null) {
            return validateDomain(link.getDomain());
        }

        return false;
    }

    /**
     * Validates domain assuming the passed in domain exists in HOST_WHITELIST
     *
     * @param domain The domain name to validate
     * @return True if the domain was found in HOST_WHITELIST, false otherwise
     */
    private boolean validateDomain(String domain) {
        for (String validHost : HOST_WHITELIST) {
            if (domain.equals(validHost) || domain.endsWith("." + validHost)) {
                return true;
            }
        }

        return false;
    }
}
