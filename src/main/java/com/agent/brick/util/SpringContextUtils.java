package com.agent.brick.util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
/**
 * <p>
 *     spring bean
 * </p>
 * @since  2025/6/22
 * @author cKnight
 */
@Component
@Slf4j
public class SpringContextUtils implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static <T> T getBean(String beanName,Class<T> clazz) {
        return process(()->context.getBean(beanName,clazz));
    }

    public static <T> T getBean(Class<T> clazz) {
        return process(()->context.getBean(clazz));
    }

    public static Object getBean(String beanName) {
        return process(()->context.getBean(beanName));
    }

    private static  <T> T process(ObjectFactory<T> objectFactory) {
        try {
            return objectFactory.getObject();
        } catch (Exception e) {
            log.error("获取bean失败:", e);
            return null;
        }
    }
}