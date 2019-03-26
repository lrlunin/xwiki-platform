/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xpn.xwiki.internal.render;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.block.match.MacroBlockMatcher;
import org.xwiki.rendering.block.match.OrBlockMatcher;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;

import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Helper component to extract blocks ({@link LinkBlock}, {@link MacroBlock}) that link to documents or spaces from a
 * given XDOM and to generically read and write the resources from and to these blocks.
 *
 * @version $Id$
 * @since 7.4.1
 * @since 8.0M1
 */
@Component(roles = LinkedResourceHelper.class)
@Singleton
public class LinkedResourceHelper
{
    private static final String DOCUMENT_MACRO_PARAMETER = "document";

    private static final String REFERENCE_MACRO_PARAMETER = "reference";

    /**
     * Used to serialize link references.
     *
     * @see #updateRelativeLinks(XWikiDocument, DocumentReference)
     */
    @Inject
    @Named("compact")
    private EntityReferenceSerializer<String> compactEntityReferenceSerializer;

    /**
     * @param xdom the XDOM to extract from
     * @return the list of blocks that link to documents or spaces.
     */
    public List<Block> getBlocks(XDOM xdom)
    {
        // @formatter:off
        return xdom.getBlocks(
            new OrBlockMatcher(
                new ClassBlockMatcher(LinkBlock.class),
                new ClassBlockMatcher(ImageBlock.class),
                new MacroBlockMatcher("include"),
                new MacroBlockMatcher("display")
            ), Block.Axes.DESCENDANT);
        // @formatter:on
    }

    /**
     * @param block the block to extract from
     * @return the resource reference string pointing to a document or space
     */
    public String getResourceReferenceString(Block block)
    {
        // Determine the reference string and reference type for each block type.
        String referenceString = null;
        if (block instanceof LinkBlock) {
            LinkBlock linkBlock = (LinkBlock) block;
            ResourceReference linkReference = linkBlock.getReference();

            referenceString = linkReference.getReference();
        } else if (block instanceof ImageBlock) {
            ImageBlock imageBlock = (ImageBlock) block;
            ResourceReference imageReference = imageBlock.getReference();

            referenceString = imageReference.getReference();
        } else if (block instanceof MacroBlock) {
            referenceString = block.getParameter(REFERENCE_MACRO_PARAMETER);
            if (StringUtils.isBlank(referenceString)) {
                referenceString = block.getParameter(DOCUMENT_MACRO_PARAMETER);
            }

            if (StringUtils.isBlank(referenceString)) {
                // If the reference is not set or is empty, we have a recursive include which is not valid anyway.
                // Skip it.
                referenceString = null;
            }
        }

        return referenceString;
    }

    /**
     * @param block the block to extract from
     * @return the type of the resource pointing to a document or space
     */
    public ResourceType getResourceType(Block block)
    {
        // Determine the reference string and reference type for each block type.
        ResourceType resourceType = null;
        if (block instanceof LinkBlock) {
            LinkBlock linkBlock = (LinkBlock) block;
            ResourceReference linkReference = linkBlock.getReference();

            resourceType = linkReference.getType();
        } else if (block instanceof ImageBlock) {
            ImageBlock imageBlock = (ImageBlock) block;
            ResourceReference imageReference = imageBlock.getReference();

            resourceType = imageReference.getType();
        } else if (block instanceof MacroBlock) {
            // We still have to look at the reference string to see if it is a valid include (i.e. non-recursive).
            String referenceString = block.getParameter(REFERENCE_MACRO_PARAMETER);
            if (StringUtils.isBlank(referenceString)) {
                referenceString = block.getParameter(DOCUMENT_MACRO_PARAMETER);
            }

            if (StringUtils.isBlank(referenceString)) {
                // If the reference is not set or is empty, we have a recursive include which is not valid anyway.
                // Skip it.
                return null;
            }

            // FIXME: this may be SPACE once we start hiding "WebHome" from macro reference parameters.
            resourceType = ResourceType.DOCUMENT;
        }

        return resourceType;
    }

    /**
     * @param block the block to write to
     * @param newReferenceString the new resource reference string that the block should point to
     */
    public void setResourceReferenceString(Block block, String newReferenceString)
    {
        if (block instanceof LinkBlock) {
            LinkBlock linkBlock = (LinkBlock) block;
            ResourceReference linkReference = linkBlock.getReference();

            linkReference.setReference(newReferenceString);
        } else if (block instanceof ImageBlock) {
            ImageBlock imageBlock = (ImageBlock) block;
            ResourceReference imageReference = imageBlock.getReference();

            imageReference.setReference(newReferenceString);
        } else if (block instanceof MacroBlock) {
            if (StringUtils.isNotBlank(block.getParameter(DOCUMENT_MACRO_PARAMETER))) {
                // Backwards compatibility check.
                block.setParameter(DOCUMENT_MACRO_PARAMETER, newReferenceString);
            } else {
                block.setParameter(REFERENCE_MACRO_PARAMETER, newReferenceString);
            }
        }
    }

    /**
     * @param block the block to write to
     * @param newResourceType the new type of resource that the block should point to
     */
    public void setResourceType(Block block, ResourceType newResourceType)
    {
        if (block instanceof LinkBlock) {
            LinkBlock linkBlock = (LinkBlock) block;
            ResourceReference linkReference = linkBlock.getReference();

            linkReference.setType(newResourceType);
        } else if (block instanceof ImageBlock) {
            ImageBlock imageBlock = (ImageBlock) block;
            ResourceReference imageReference = imageBlock.getReference();

            imageReference.setType(newResourceType);
        } else if (block instanceof MacroBlock) {
            // N/A yet.
        }
    }

    /**
     * @param block the block to extract from
     * @return the resource reference corresponding to the given block or {@code null} if none was found or if the block
     *         type is unsupported
     */
    public ResourceReference getResourceReference(Block block)
    {
        ResourceReference result = null;

        if (block instanceof LinkBlock) {
            result = ((LinkBlock) block).getReference();
        } else if (block instanceof ImageBlock) {
            result = ((ImageBlock) block).getReference();
        } else if (block instanceof MacroBlock) {
            // Wrap it up as a ResourceReference.
            String referenceString = getResourceReferenceString(block);
            if (referenceString != null) {
                // FIXME: At some point we might want to allow space references too, so the resource reference might
                // need to become untyped.
                result = new ResourceReference(referenceString, ResourceType.DOCUMENT);
                result.setTyped(true);
            }
        }

        return result;
    }
}
