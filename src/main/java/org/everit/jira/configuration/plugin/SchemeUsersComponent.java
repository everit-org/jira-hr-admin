/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.jira.configuration.plugin;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.everit.jira.configuration.plugin.schema.qdsl.QDateRange;
import org.everit.jira.configuration.plugin.util.AvatarUtil;
import org.everit.jira.configuration.plugin.util.AvatarUtil.JoinAvatarQueryExtension;
import org.everit.jira.configuration.plugin.util.LocalizedTemplate;
import org.everit.jira.configuration.plugin.util.QueryResultWithCount;
import org.everit.jira.querydsl.schema.QCwdUser;
import org.everit.jira.querydsl.support.QuerydslSupport;
import org.everit.jira.querydsl.support.ri.QuerydslSupportImpl;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.sql.SQLQuery;

public class SchemeUsersComponent {

  public static class NewSchemeUserDTO {

    public Date endDateExcluded;

    public Date startDate;

    public String userName;

  }

  public static class QUserSchemeEntityParameter {
    public NumberPath<Long> dateRangeId;

    public EntityPath<?> entityPath;

    public NumberPath<Long> schemeId;

    public NumberPath<Long> userId;

    public NumberPath<Long> userSchemeId;
  }

  public static class SchemeUserDTO extends NewSchemeUserDTO {

    public Long avatarId;

    public Long avatarOwner;

    public long dateRangeId;

    public String userDisplayName;

    public long userSchemeId;

  }

  private static final int PAGE_SIZE = 50;

  private static final LocalizedTemplate TEMPLATE =
      new LocalizedTemplate("/META-INF/component/scheme_users",
          ManageSchemeComponent.class.getClassLoader());

  private final QuerydslSupport querydslSupport;
  private final QUserSchemeEntityParameter qUserSchemeEntityParameter;

  public SchemeUsersComponent(final QUserSchemeEntityParameter qUserSchemeEntityParameter) {
    this.qUserSchemeEntityParameter = qUserSchemeEntityParameter;
    try {
      this.querydslSupport = new QuerydslSupportImpl();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public QueryResultWithCount<SchemeUserDTO> querySchemeUsers(final int pageIndex,
      final long schemeId, final String userName, final boolean currentTimeRanges) {
    return querydslSupport.execute((connection, configuration) -> {
      QDateRange qDateRange = QDateRange.dateRange;
      QCwdUser qCwdUser = QCwdUser.cwdUser;

      SQLQuery<SchemeUserDTO> query = new SQLQuery<>();
      query.from(qUserSchemeEntityParameter.entityPath)
          .innerJoin(qDateRange)
          .on(qDateRange.dateRangeId.eq(qUserSchemeEntityParameter.dateRangeId))
          .innerJoin(qCwdUser).on(qCwdUser.id.eq(qUserSchemeEntityParameter.userId));

      JoinAvatarQueryExtension avatarQueryExtension =
          AvatarUtil.joinAvatarToCwdUser(query, qCwdUser, "avatar");

      List<Predicate> predicates = new ArrayList<>();
      predicates.add(qUserSchemeEntityParameter.schemeId.eq(schemeId));
      predicates.add(avatarQueryExtension.predicate);

      if (currentTimeRanges) {
        java.sql.Date currentDate = new java.sql.Date(new java.util.Date().getTime());
        predicates.add(qDateRange.startDate.loe(currentDate)
            .and(qDateRange.endDateExcluded.gt(currentDate)));
      }

      if (userName != null) {
        predicates.add(qCwdUser.userName.eq(userName));
      }

      StringExpression userDisplayNameExpression = qCwdUser.displayName.as("userDisplayName");
      query.select(Projections.fields(SchemeUserDTO.class,
          qUserSchemeEntityParameter.userSchemeId.as("userSchemeId"), qDateRange.dateRangeId,
          qCwdUser.userName, userDisplayNameExpression, qDateRange.startDate),
          qDateRange.endDateExcluded, avatarQueryExtension.qAvatar.id.as("avatarId"),
          avatarQueryExtension.qAvatar.owner.as("avatarOwner"));

      query.where(predicates.toArray(new Predicate[0]));

      long count = query.fetchCount();

      query.orderBy(userDisplayNameExpression.asc(), qDateRange.startDate.desc());
      long offset = PAGE_SIZE * (pageIndex - 1);
      if (offset >= count) {
        offset = PAGE_SIZE * (count / PAGE_SIZE - 1);
        if (offset < 0) {
          offset = 0;
        }
      }
      List<SchemeUserDTO> resultSet = query.limit(PAGE_SIZE).offset(offset).fetch();

      QueryResultWithCount<SchemeUserDTO> result = new QueryResultWithCount<>(resultSet, count);
      return result;
    });
  }
}
