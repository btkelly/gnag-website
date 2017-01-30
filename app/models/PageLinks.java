/*******************************************************************************
 *  Copyright (c) 2011 GitHub Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Kevin Sawicki (GitHub Inc.) - initial API and implementation
 *******************************************************************************/
package models;

import play.libs.ws.WSResponse;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Page link class to be used to determine the links to other pages of request
 * responses encoded in the current response. These will be present if the
 * result set size exceeds the per page limit.
 */
public class PageLinks {

    /** */
    String HEADER_LINK = "Link"; //$NON-NLS-1$

    /** */
    String META_REL = "rel"; //$NON-NLS-1$
    /** */
    String META_LAST = "last"; //$NON-NLS-1$
    /** */
    String META_NEXT = "next"; //$NON-NLS-1$
    /** */
    String META_FIRST = "first"; //$NON-NLS-1$
    /** */
    String META_PREV = "prev"; //$NON-NLS-1$

    private static final String DELIM_LINKS = ","; //$NON-NLS-1$

    private static final String DELIM_LINK_PARAM = ";"; //$NON-NLS-1$

    private PageLink first;
    private PageLink last;
    private PageLink next;
    private PageLink prev;

    /**
     * Parse links from executed method
     *
     * @param response
     */
    public PageLinks(WSResponse response) {
        String linkHeader = response.getHeader(HEADER_LINK);
        if (linkHeader != null) {
            String[] links = linkHeader.split(DELIM_LINKS);
            for (String link : links) {
                String[] segments = link.split(DELIM_LINK_PARAM);
                if (segments.length < 2)
                    continue;

                String linkPart = segments[0].trim();
                if (!linkPart.startsWith("<") || !linkPart.endsWith(">")) //$NON-NLS-1$ //$NON-NLS-2$
                    continue;
                linkPart = linkPart.substring(1, linkPart.length() - 1);

                for (int i = 1; i < segments.length; i++) {
                    String[] rel = segments[i].trim().split("="); //$NON-NLS-1$
                    if (rel.length < 2 || !META_REL.equals(rel[0]))
                        continue;

                    String relValue = rel[1];
                    if (relValue.startsWith("\"") && relValue.endsWith("\"")) //$NON-NLS-1$ //$NON-NLS-2$
                        relValue = relValue.substring(1, relValue.length() - 1);

                    try {
                        if (META_FIRST.equals(relValue))
                            first = new PageLink(linkPart, relValue);
                        else if (META_LAST.equals(relValue))
                            last = new PageLink(linkPart, relValue);
                        else if (META_NEXT.equals(relValue))
                            next = new PageLink(linkPart, relValue);
                        else if (META_PREV.equals(relValue))
                            prev = new PageLink(linkPart, relValue);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * @return first
     */
    public PageLink getFirst() {
        return first;
    }

    /**
     * @return last
     */
    public PageLink getLast() {
        return last;
    }

    /**
     * @return next
     */
    public PageLink getNext() {
        return next;
    }

    /**
     * @return prev
     */
    public PageLink getPrev() {
        return prev;
    }

    public static class PageLink {

        private final URL pageUrl;
        private final String rel;
        private String pageNum;

        public PageLink(String pageUrl, String rel) throws MalformedURLException {
            this.pageUrl = new URL(pageUrl);
            this.rel = rel;

            String[] query = this.pageUrl.getQuery().split("&");
            for (String param : query) {
                if (param.contains("page")) {
                    int index = param.indexOf("=");
                    this.pageNum = param.substring(index + 1);
                }
            }
        }

        public URL getPageUrl() {
            return pageUrl;
        }

        public String getRel() {
            return rel;
        }

        public int getPageNum() {
            try {
                return Integer.parseInt(pageNum);
            } catch (NumberFormatException e) {
                return 1;
            }
        }
    }
}