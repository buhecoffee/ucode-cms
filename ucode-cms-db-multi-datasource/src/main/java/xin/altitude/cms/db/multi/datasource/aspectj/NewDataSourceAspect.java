/*
 *
 * Copyright (c) 2020-2022, Java知识图谱 (http://www.altitude.xin).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package xin.altitude.cms.db.multi.datasource.aspectj;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import xin.altitude.cms.db.multi.datasource.annotation.Ds;
import xin.altitude.cms.db.multi.datasource.core.DataSourceContextHolder;

import java.util.Objects;

/**
 * 关系型数据库多数据源处理
 *
 * @author <a href="http://www.altitude.xin" target="_blank">Java知识图谱</a>
 * @author <a href="https://gitee.com/decsa/ucode-cms-vue" target="_blank">UCode CMS</a>
 * @author <a href="https://space.bilibili.com/1936685014" target="_blank">B站视频</a>
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NewDataSourceAspect {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Pointcut("@annotation(xin.altitude.cms.db.multi.datasource.annotation.Ds)"
        + "|| @within(xin.altitude.cms.db.multi.datasource.annotation.Ds)")
    public void dsPointCut() {

    }

    @Around("dsPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        try {
            Ds dataSource = getDataSource(point);

            if (Objects.nonNull(dataSource)) {
                DataSourceContextHolder.setDsName(dataSource.name());
            }
            return point.proceed();
        } finally {
            // 在执行方法之后 销毁数据源
            DataSourceContextHolder.clearDsName();
        }
    }

    /**
     * 获取需要切换的数据源
     */
    private Ds getDataSource(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Ds dataSource = AnnotationUtils.findAnnotation(signature.getMethod(), Ds.class);
        if (Objects.nonNull(dataSource)) {
            return dataSource;
        }

        return AnnotationUtils.findAnnotation(signature.getDeclaringType(), Ds.class);
    }
}
