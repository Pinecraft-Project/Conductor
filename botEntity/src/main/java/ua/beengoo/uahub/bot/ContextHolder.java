package ua.beengoo.uahub.bot;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Simple Spring {@code ApplicationContext} holder.
 *
 * <p>Intended for limited cases where obtaining beans inside framework-managed listeners is
 * difficult (e.g. JDA command initializers). Prefer constructor injection wherever possible.
 */
@Component
public class ContextHolder implements ApplicationContextAware {
  private static ApplicationContext context;

  @Override
  public void setApplicationContext(@NotNull ApplicationContext applicationContext)
      throws BeansException {
    ContextHolder.context = applicationContext;
  }

  /**
   * Resolve a Spring bean by type.
   *
   * @param beanClass bean type
   * @param <T> bean generic type
   * @return the resolved bean instance
   */
  public static <T> T getBean(Class<T> beanClass) {
    return context.getBean(beanClass);
  }

  /**
   * Resolve a Spring bean by name.
   *
   * @param beanName bean name/id in the context
   * @return the resolved bean instance
   */
  public static Object getBean(String beanName) {
    return context.getBean(beanName);
  }
}
