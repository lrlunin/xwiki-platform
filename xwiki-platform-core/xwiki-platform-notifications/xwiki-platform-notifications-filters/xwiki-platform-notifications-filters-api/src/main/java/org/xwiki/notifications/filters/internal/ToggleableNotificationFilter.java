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
package org.xwiki.notifications.filters.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.xwiki.notifications.NotificationFormat;
import org.xwiki.notifications.filters.NotificationFilter;

/**
 * This interface characterise a notification filter that can be enabled or disabled in the preferences of every user.
 *
 * @version $Id$
 * @since 10.1RC1
 */
public interface ToggleableNotificationFilter extends NotificationFilter
{
    /**
     * @return either or not this filter should be enabled by default
     */
    default boolean isEnabledByDefault()
    {
        return true;
    }

    /**
     * @return the formats handled by this filter
     */
    default List<NotificationFormat> getFormats()
    {
        return Arrays.asList(NotificationFormat.values());
    }

    /**
     * @return the events handled by this filter
     */
    default List<String> getEventTypes()
    {
        return Collections.emptyList();
    }
}
