/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.admin;

import java.lang.management.ManagementFactory;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link SpringApplicationAdminMXBeanRegistrar}.
 *
 * @author Stephane Nicoll
 * @author Andy Wilkinson
 */
public class SpringApplicationAdminMXBeanRegistrarTests {

	private static final String OBJECT_NAME = "org.springframework.boot:type=Test,name=SpringApplication";

	private MBeanServer mBeanServer;

	private ConfigurableApplicationContext context;

	@Before
	public void setup() {
		this.mBeanServer = ManagementFactory.getPlatformMBeanServer();
	}

	@After
	public void closeContext() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void validateReadyFlag() {
		final ObjectName objectName = createObjectName(OBJECT_NAME);
		SpringApplication application = new SpringApplication(Config.class);
		application.setWebApplicationType(WebApplicationType.NONE);
		/**
		 * 监听器最终是在{@link SimpleApplicationEventMulticaster#doInvokeListener(ApplicationListener, ApplicationEvent)}方法里触发
		 */
		application.addListeners((ContextRefreshedEvent event) -> {
			try {
				System.out.println("监听器内：" + isApplicationReady(objectName));
				assertThat(isApplicationReady(objectName)).isFalse();
			}
			catch (Exception ex) {
				throw new IllegalStateException(
						"Could not contact spring application admin bean", ex);
			}
		});
		this.context = application.run();
		System.out.println("监听器外：" + isApplicationReady(objectName));
		assertThat(isApplicationReady(objectName)).isTrue();
	}

	/**
	 * 如果用JRebel Debug测试，会出现如下错误：
	 * 2018-12-05 18:36:12 JRebel: ERROR Class 'net.bytebuddy.dynamic.loading.ClassInjector$UsingReflection' could not be processed by org.zeroturnaround.javarebel.integration.proxy.bytebuddy.ClassInjectorUsingCBP@null: org.zeroturnaround.bundled.javassist.NotFoundException: inject(..) is not found in net.bytebuddy.dynamic.loading.ClassInjector$UsingReflection
	 at org.zeroturnaround.bundled.javassist.CtClassType.getDeclaredMethod(SourceFile:1306)
	 at org.zeroturnaround.javarebel.integration.support.CBPs$DirectProcessorImpl.instrument(SourceFile:235)
	 at org.zeroturnaround.javarebel.integration.proxy.bytebuddy.ClassInjectorUsingCBP.process(SourceFile:14)
	 at org.zeroturnaround.javarebel.integration.support.JavassistClassBytecodeProcessor.process(SourceFile:105)
	 at org.zeroturnaround.javarebel.integration.support.CacheAwareJavassistClassBytecodeProcessor.process(SourceFile:34)
	 at org.zeroturnaround.javarebel.integration.support.JavassistClassBytecodeProcessor.process(SourceFile:74)
	 at com.zeroturnaround.javarebel.xx.a(SourceFile:359)
	 at com.zeroturnaround.javarebel.xx.a(SourceFile:348)
	 at com.zeroturnaround.javarebel.xx.a(SourceFile:315)
	 at com.zeroturnaround.javarebel.SDKIntegrationImpl.runBytecodeProcessors(SourceFile:45)
	 at com.zeroturnaround.javarebel.vh.transform(SourceFile:134)
	 at java.lang.ClassLoader.defineClass(ClassLoader.java:41009)
	 at java.security.SecureClassLoader.defineClass(SecureClassLoader.java:142)
	 at java.net.URLClassLoader.defineClass(URLClassLoader.java:467)
	 at java.net.URLClassLoader.access$100(URLClassLoader.java:73)
	 at java.net.URLClassLoader$1.run(URLClassLoader.java:368)
	 at java.net.URLClassLoader$1.run(URLClassLoader.java:362)
	 at java.security.AccessController.doPrivileged(Native Method)
	 at java.net.URLClassLoader.findClass(URLClassLoader.java:361)
	 at java.lang.ClassLoader.loadClass(ClassLoader.java:424)
	 at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:331)
	 at java.lang.ClassLoader.loadClass(ClassLoader.java:357)
	 at org.mockito.internal.creation.bytebuddy.SubclassInjectionLoader.<init>(SubclassInjectionLoader.java:28)
	 at org.mockito.internal.creation.bytebuddy.SubclassByteBuddyMockMaker.<init>(SubclassByteBuddyMockMaker.java:33)
	 at org.mockito.internal.creation.bytebuddy.ByteBuddyMockMaker.<init>(ByteBuddyMockMaker.java:21)
	 at sun.reflect.GeneratedConstructorAccessor140.newInstance(Unknown Source)
	 at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45005)
	 at java.lang.reflect.Constructor.newInstance(Constructor.java:423)
	 at java.lang.Class.newInstance(Class.java:442)
	 at org.mockito.internal.configuration.plugins.DefaultMockitoPlugins.create(DefaultMockitoPlugins.java:66)
	 at org.mockito.internal.configuration.plugins.DefaultMockitoPlugins.getDefaultPlugin(DefaultMockitoPlugins.java:43)
	 at org.mockito.internal.configuration.plugins.PluginLoader.loadPlugin(PluginLoader.java:67)
	 at org.mockito.internal.configuration.plugins.PluginLoader.loadPlugin(PluginLoader.java:44)
	 at org.mockito.internal.configuration.plugins.PluginRegistry.<init>(PluginRegistry.java:21)
	 at org.mockito.internal.configuration.plugins.Plugins.<clinit>(Plugins.java:18)
	 at org.mockito.internal.util.MockUtil.<clinit>(MockUtil.java:24)
	 at org.mockito.internal.util.MockCreationValidator.validateType(MockCreationValidator.java:22)
	 at org.mockito.internal.creation.MockSettingsImpl.validatedSettings(MockSettingsImpl.java:238)
	 at org.mockito.internal.creation.MockSettingsImpl.build(MockSettingsImpl.java:226)
	 at org.mockito.internal.MockitoCore.mock(MockitoCore.java:68)
	 at org.mockito.Mockito.mock(Mockito.java:1896)
	 at org.mockito.Mockito.mock(Mockito.java:1805)
	 at org.springframework.boot.admin.SpringApplicationAdminMXBeanRegistrarTests.eventsFromOtherContextsAreIgnored(SpringApplicationAdminMXBeanRegistrarTests.java:99)
	 at sun.reflect.GeneratedMethodAccessor5.invoke(Unknown Source)
	 at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:45005)
	 at java.lang.reflect.Method.invoke(Method.java:498)
	 at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
	 at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	 at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
	 at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	 at org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:26)
	 at org.junit.internal.runners.statements.RunAfters.evaluate(RunAfters.java:27)
	 at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)
	 at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)
	 at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)
	 at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
	 at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
	 at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
	 at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
	 at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
	 at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
	 at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
	 at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:68)
	 at com.intellij.rt.execution.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:47)
	 at com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:242)
	 at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:70)

	 Disconnected from the target VM, address: '127.0.0.1:50969', transport: 'socket'

	 Process finished with exit code 0

	 * @throws MalformedObjectNameException
	 */
	@Test
	public void eventsFromOtherContextsAreIgnored() throws MalformedObjectNameException {
		SpringApplicationAdminMXBeanRegistrar registrar = new SpringApplicationAdminMXBeanRegistrar(
				OBJECT_NAME);
		ConfigurableApplicationContext context = mock(
				ConfigurableApplicationContext.class);
		registrar.setApplicationContext(context);
		registrar.onApplicationReadyEvent(
				new ApplicationReadyEvent(new SpringApplication(), null,
						mock(ConfigurableApplicationContext.class)));
		assertThat(isApplicationReady(registrar)).isFalse();
		registrar.onApplicationReadyEvent(
				new ApplicationReadyEvent(new SpringApplication(), null, context));
		assertThat(isApplicationReady(registrar)).isTrue();
	}

	private boolean isApplicationReady(SpringApplicationAdminMXBeanRegistrar registrar) {
		return (Boolean) ReflectionTestUtils.getField(registrar, "ready");
	}

	@Test
	public void environmentIsExposed() {
		final ObjectName objectName = createObjectName(OBJECT_NAME);
		SpringApplication application = new SpringApplication(Config.class);
		application.setWebApplicationType(WebApplicationType.NONE);
		this.context = application.run("--foo.bar=blam");
		assertThat(isApplicationReady(objectName)).isTrue();
		assertThat(isApplicationEmbeddedWebApplication(objectName)).isFalse();
		assertThat(getProperty(objectName, "foo.bar")).isEqualTo("blam");
		assertThat(getProperty(objectName, "does.not.exist.test")).isNull();
	}

	@Test
	public void shutdownApp() throws InstanceNotFoundException {
		final ObjectName objectName = createObjectName(OBJECT_NAME);
		SpringApplication application = new SpringApplication(Config.class);
		application.setWebApplicationType(WebApplicationType.NONE);
		this.context = application.run();
		assertThat(this.context.isRunning()).isTrue();
		invokeShutdown(objectName);
		assertThat(this.context.isRunning()).isFalse();
		// JMX cleanup
		assertThatExceptionOfType(InstanceNotFoundException.class)
				.isThrownBy(() -> this.mBeanServer.getObjectInstance(objectName));
	}

	private Boolean isApplicationReady(ObjectName objectName) {
		return getAttribute(objectName, Boolean.class, "Ready");
	}

	private Boolean isApplicationEmbeddedWebApplication(ObjectName objectName) {
		return getAttribute(objectName, Boolean.class, "EmbeddedWebApplication");
	}

	private String getProperty(ObjectName objectName, String key) {
		try {
			return (String) this.mBeanServer.invoke(objectName, "getProperty",
					new Object[] { key }, new String[] { String.class.getName() });
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		}
	}

	private <T> T getAttribute(ObjectName objectName, Class<T> type, String attribute) {
		try {
			Object value = this.mBeanServer.getAttribute(objectName, attribute);
			assertThat(value == null || type.isInstance(value)).isTrue();
			return type.cast(value);
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		}
	}

	private void invokeShutdown(ObjectName objectName) {
		try {
			this.mBeanServer.invoke(objectName, "shutdown", null, null);
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		}
	}

	private ObjectName createObjectName(String jmxName) {
		try {
			return new ObjectName(jmxName);
		}
		catch (MalformedObjectNameException ex) {
			throw new IllegalStateException("Invalid jmx name " + jmxName, ex);
		}
	}

	@Configuration
	static class Config {

		@Bean
		public SpringApplicationAdminMXBeanRegistrar springApplicationAdminRegistrar()
				throws MalformedObjectNameException {
			return new SpringApplicationAdminMXBeanRegistrar(OBJECT_NAME);
		}

	}

}
