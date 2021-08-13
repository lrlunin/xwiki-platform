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
package org.xwiki.netflux;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.xwiki.component.annotation.Role;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.stability.Unstable;

/**
 * The component used to create and associate channels to XWiki entities.
 * 
 * @version $Id$
 * @since 13.7RC1
 */
@Role
@Unstable
public interface EntityChannelStore
{
    /**
     * @param entityReference an entity reference
     * @return all existing channels associated to the specified entity
     */
    List<EntityChannel> getChannels(EntityReference entityReference);

    /**
     * Looks for a channel associated to the specified entity and having the specified type.
     * 
     * @param entityReference the entity the channel is associated with
     * @param type the channel type (since multiple channels can be associated to the same entity)
     * @return the found channel
     */
    default Optional<EntityChannel> getChannel(EntityReference entityReference, String type)
    {
        return getChannels(entityReference).stream().filter(Objects::nonNull)
            .filter(channel -> Objects.equals(channel.getType(), type)).findFirst();
    }

    /**
     * Create a new channel of the given type and associate it with the specified entity.
     * 
     * @param entityReference the entity to associate the channel with
     * @param type the channel type (since multiple channels can be associated to the same entity)
     * @return information about the created channel
     */
    EntityChannel createChannel(EntityReference entityReference, String type);
}