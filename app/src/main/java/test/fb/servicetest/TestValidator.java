package test.fb.servicetest;

import com.fernandobarillas.redditservice.links.validators.LinkValidator;
import com.fernandobarillas.redditservice.models.Link;

import net.dean.jraw.models.Thumbnails;

/**
 * Created by fb on 5/18/16.
 */
public class TestValidator implements LinkValidator {
    @Override
    public boolean isLinkValid(Link link) {
        return !link.isNsfw() && hasThumbnails(link);
    }

    private boolean hasThumbnails(final Link link) {
        Thumbnails thumbnails = link.getThumbnails();
        if (thumbnails == null || thumbnails.getSource() == null) return false;
        return true;
    }
}
