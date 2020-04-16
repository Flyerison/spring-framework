// package com.flyerison.spring;
//
// import java.lang.reflect.Constructor;
// import java.lang.reflect.InvocationHandler;
// import java.lang.reflect.InvocationTargetException;
// import java.lang.reflect.Method;
// import java.lang.reflect.Proxy;
//
// public class Test {
//     public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
//         /**
//          * 有两个参数 一个是将TestBean加载进内存的类加载器 一个是他们共同实现的接口类
//          */
//         Class<?> proxyClass = Proxy.getProxyClass(TestBean.class.getClassLoader(), TestBean.class);
//
//         // 获取一个参数为 InvocationHandler 类型的构造器
//         Constructor constructor = proxyClass.getConstructor(InvocationHandler.class);
//
//         // 反射创建实例
//         TestBean bean = (TestBean) constructor.newInstance(new InvocationHandler() {
//             public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                 TestBean bean = new TestBeanImpl();
//                 bean.setAge(1);
//                 Object result = method.invoke(bean, args);
//                 return result;
//             }
//         });
//
//         System.out.println(bean.getAge());
//     }
// }
