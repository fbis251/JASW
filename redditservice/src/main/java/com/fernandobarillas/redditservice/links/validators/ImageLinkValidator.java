package com.fernandobarillas.redditservice.links.validators;

import com.fernandobarillas.redditservice.models.Link;

/**
 * Created by fb on 12/15/15.
 */
public class ImageLinkValidator implements LinkValidator {
    private boolean mShowNsfw;

    public ImageLinkValidator(boolean showNsfw) {
        mShowNsfw = showNsfw;
    }

    @Override
    public boolean isLinkValid(Link link) {
        // Don't display links marked as NSFW unless the user wants to
        if (link.isNsfw() && !mShowNsfw) {
            return false;
        }

        // TODO: Temporarily disallow imgur album links
        String[] domainBlacklist = {
                "http://imgur.com/a/",
                "https://imgur.com/a/",
                "http://imgur.com/gallery/",
                "https://imgur.com/gallery/",
        };

        String[] extensionBlacklist = {".gif", ".gifv",};

        String[] extensionWhitelist = {".jpg", ".jpeg", ".png", ".bmp",};

        // TODO: Perform better filtering here
        String linkUrl = link.getUrl();

        for (String extension : extensionBlacklist) {
            if (linkUrl.endsWith(extension)) {
                return false;
            }
        }

        for (String domain : domainBlacklist) {
            if (linkUrl.startsWith(domain)) {
                return false;
            }
        }

        for (String extension : extensionWhitelist) {
            if (linkUrl.endsWith(extension)) {
                return true;
            }
        }

        return false;
    }
}
