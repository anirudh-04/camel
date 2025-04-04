/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.impl.engine;

import java.util.function.Supplier;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.spi.CamelBeanPostProcessor;
import org.apache.camel.spi.CamelDependencyInjectionAnnotationFactory;
import org.apache.camel.support.PluginHelper;

/**
 * Default implementation of the {@link CamelDependencyInjectionAnnotationFactory}.
 */
public class DefaultDependencyInjectionAnnotationFactory
        implements CamelDependencyInjectionAnnotationFactory, CamelContextAware {

    private CamelContext camelContext;

    public DefaultDependencyInjectionAnnotationFactory(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Runnable createBindToRegistryFactory(
            String id, Object bean, Class<?> beanType, String beanName, boolean beanPostProcess,
            String initMethod, String destroyMethod) {

        if (beanType.isAssignableFrom(Supplier.class)) {
            beanType = Object.class;
        }
        final Class<?> beanTarget = beanType;

        return () -> {
            if (beanPostProcess) {
                try {
                    final CamelBeanPostProcessor beanPostProcessor = PluginHelper.getBeanPostProcessor(camelContext);
                    beanPostProcessor.postProcessBeforeInitialization(bean, beanName);
                    beanPostProcessor.postProcessAfterInitialization(bean, beanName);
                } catch (Exception e) {
                    throw RuntimeCamelException.wrapRuntimeException(e);
                }
            }
            CamelContextAware.trySetCamelContext(bean, camelContext);
            if (bean instanceof Supplier) {
                // must be Supplier<Object> to ensure correct binding
                Supplier<Object> sup = (Supplier<Object>) bean;
                if (initMethod != null || destroyMethod != null) {
                    camelContext.getRegistry().bind(id, beanTarget, sup, initMethod, destroyMethod);
                } else {
                    camelContext.getRegistry().bind(id, beanTarget, sup);
                }
            } else {
                if (initMethod != null || destroyMethod != null) {
                    camelContext.getRegistry().bind(id, bean, initMethod, destroyMethod);
                } else {
                    camelContext.getRegistry().bind(id, bean);
                }
            }
        };
    }
}
