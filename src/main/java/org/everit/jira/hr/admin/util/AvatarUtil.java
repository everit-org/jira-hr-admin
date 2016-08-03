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
package org.everit.jira.hr.admin.util;

import org.everit.jira.querydsl.schema.QAppUser;
import org.everit.jira.querydsl.schema.QAvatar;
import org.everit.jira.querydsl.schema.QCwdUser;
import org.everit.jira.querydsl.schema.QPropertyentry;
import org.everit.jira.querydsl.schema.QPropertynumber;
import org.everit.jira.querydsl.support.QuerydslSupport;
import org.everit.jira.querydsl.support.ri.QuerydslSupportImpl;

import com.querydsl.core.types.Path;
import com.querydsl.sql.SQLQuery;

public class AvatarUtil {

  public static final long DEFAULT_AVATAR_ID;

  static {
    try {
      QuerydslSupport querydslSupport = new QuerydslSupportImpl();
      DEFAULT_AVATAR_ID = querydslSupport.execute((connection, configuration) -> {
        QAvatar qAvatar = QAvatar.avatar;
        return new SQLQuery<Long>(connection, configuration).select(qAvatar.id).from(qAvatar)
            .where(qAvatar.filename.eq("Avatar-default.svg")).fetchOne();
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static QAvatar joinAvatarToAppUser(final SQLQuery<?> query,
      final QAppUser qAppUser, final String variablePrefix) {

    QPropertyentry qPropertyEntry =
        new QPropertyentry(resolveVariableName(QPropertyentry.propertyentry, variablePrefix));

    QPropertynumber qPropertyNumber =
        new QPropertynumber(resolveVariableName(QPropertynumber.propertynumber, variablePrefix));

    QAvatar qAvatar = new QAvatar(resolveVariableName(QAvatar.avatar, variablePrefix));

    query.leftJoin(qPropertyEntry).on(qPropertyEntry.entityId.eq(qAppUser.id)
        .and(qPropertyEntry.propertyKey.eq("user.avatar.id")))
        .leftJoin(qPropertyNumber).on(qPropertyEntry.id.eq(qPropertyNumber.id))
        .leftJoin(qAvatar).on(qAvatar.id.eq(qPropertyNumber.propertyvalue));

    return qAvatar;
  }

  public static QAvatar joinAvatarToCwdUser(final SQLQuery<?> query,
      final QCwdUser qCwdUser, final String variablePrefix) {

    QAppUser qAppUser = new QAppUser(resolveVariableName(QAppUser.appUser, variablePrefix));

    query.innerJoin(qAppUser).on(qCwdUser.lowerUserName.eq(qAppUser.lowerUserName));

    return joinAvatarToAppUser(query, qAppUser, variablePrefix);
  }

  private static String resolveVariableName(final Path<?> path, final String variablePrefix) {
    String variable = path.getMetadata().getName();
    if (variablePrefix != null) {
      variable = variablePrefix + "_" + variable;
    }
    return variable;
  }
}
