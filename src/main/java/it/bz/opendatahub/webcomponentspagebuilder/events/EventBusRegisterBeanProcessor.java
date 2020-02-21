package it.bz.opendatahub.webcomponentspagebuilder.events;

import java.lang.reflect.Method;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@Component
public class EventBusRegisterBeanProcessor
		implements InstantiationAwareBeanPostProcessor, DestructionAwareBeanPostProcessor {

	@Autowired
	EventBus eventBus;

	private boolean hasListeners(Object bean) {
		Class<?> objectType = bean.getClass();
		if (org.springframework.aop.support.AopUtils.isAopProxy(bean)) {
			objectType = org.springframework.aop.support.AopUtils.getTargetClass(bean);
		}

		for (Method m : objectType.getDeclaredMethods()) {
			if (m.isAnnotationPresent(Subscribe.class)) {
				return true;
			}
		}

		return false;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (hasListeners(bean)) {
			eventBus.register(bean);
		}

		return bean;
	}

	@Override
	public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
		if (hasListeners(bean)) {
			eventBus.unregister(bean);
		}
	}

}