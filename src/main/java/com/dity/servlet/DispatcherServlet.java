package com.dity.servlet;

import com.dity.annotation.Autowired;
import com.dity.annotation.Controller;
import com.dity.annotation.RequestMapping;
import com.dity.annotation.Service;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author:yuhang
 * @Date:2018/4/22
 */
public class DispatcherServlet extends HttpServlet {

    private Properties properties=new Properties();

    private Set<String> classNames=new HashSet<>();

    private Map<String,Object> ioc=new HashMap<>();

    private List<Handler> handlerList=new ArrayList<>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doDisparcher(request,response);
    }

    private void doDisparcher(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setCharacterEncoding("UTF-8");
            String requestURI = request.getRequestURI();
            boolean flag=true;
            for(Handler handler:handlerList){
                if(requestURI.equals(handler.getUrlMapping())){
                    Method method = handler.getMethod();
                    method.invoke(handler.getObject(),request,response);
                    flag=false;
                    break;
                }
            }
            if(flag){
                response.getWriter().println("404,not found");
            }
        }catch (Exception e){
            e.printStackTrace();
            try {
                response.getWriter().println("exception");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }


    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1.加载配置文件
        doLoadConfig(config);
        System.out.println("加载配置文件完成");
        //2.读取配置文件
        doScanner(properties.getProperty("basePackage"));

        System.out.println("读取配置文件完成");
        System.out.println(classNames);

        //3.创建ioc容器
        doCreateIoc();
        System.out.println("创建ioc容器完成");
        System.out.println(ioc);

        //4.实现依赖注入
        doAutowired();
        System.out.println("注入完成");
        //5.实现url映射
        doHandlerMapping();
        System.out.println("映射完成");
        System.out.println(handlerList);
    }

    private void doLoadConfig(ServletConfig config) {
        String contextConfig = config.getInitParameter("contextConfig");
        InputStream rs = this.getClass().getClassLoader().getResourceAsStream(contextConfig);
        try {
            properties.load(rs);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void doScanner(String basePackage) {
        String basePath=basePackage.replace(".","/");
        URL url=this.getClass().getClassLoader().getResource(basePath);
        String baseDir = url.getFile();
        File file=new File(baseDir);
        for(File f:file.listFiles()){
            if(f.isDirectory()){
                doScanner(basePackage+"."+f.getName());
            }else{
                String fileName = f.getName();
                if(fileName.endsWith(".class")){
                    String className=basePackage+"."+fileName.replace(".class","");
                    classNames.add(className);
                }
            }
        }

    }
    private void doCreateIoc() {
        for(String className:classNames){
            try {
                Class<?> aClass = Class.forName(className);
                if(aClass.isAnnotationPresent(Controller.class)){
                    Object o = aClass.newInstance();
                    ioc.put(getBeanName(aClass.getName()),o);
                }
                if(aClass.isAnnotationPresent(Service.class)){
                    Object o = aClass.newInstance();
                    Service annotation = aClass.getAnnotation(Service.class);
                    if(!"".equals(annotation.value())){
                        ioc.put(annotation.value(),o);
                    }
                    ioc.put(getBeanName(aClass.getSimpleName()),o);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    private void doAutowired() {
        try{
            for(Map.Entry<String,Object> entry:ioc.entrySet()){
                Field[] fields = entry.getValue().getClass().getDeclaredFields();
                for(Field field:fields){
                    Autowired annotation = field.getAnnotation(Autowired.class);
                    if(annotation==null){
                        continue;
                    }
                    field.setAccessible(true);
                    if(!"".equals(annotation.value())){
                        field.set(entry.getValue(),ioc.get(annotation.value()));
                    }else {
                        field.set(entry.getValue(),ioc.get(field.getName()));
                    }
                    field.setAccessible(false);
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void doHandlerMapping() {
        try{
            for(Map.Entry<String,Object> entry:ioc.entrySet()){
                Class beanClass=entry.getValue().getClass();
                RequestMapping beanRequestMapping = (RequestMapping) beanClass.getAnnotation(RequestMapping.class);
                Method[] declaredMethods = beanClass.getDeclaredMethods();
                if(beanRequestMapping!=null){
                    for(Method method:declaredMethods){
                        RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
                        if(methodRequestMapping!=null){
                            Handler handler=new Handler();
                            handler.setObject(entry.getValue());
                            handler.setMethod(method);
                            handler.setUrlMapping(beanRequestMapping.value()+methodRequestMapping.value());
                            handler.setArg(method.getParameterTypes());
                            handlerList.add(handler);
                        }
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getBeanName(String sourceName){
        return sourceName.substring(0,1).toLowerCase()+sourceName.substring(1);
    }

    class Handler{
        private String urlMapping;//url映射
        private Method method;//方法对象
        private Object[] arg;//参数对象
        private Object object;//该类对象

        public String getUrlMapping() {
            return urlMapping;
        }

        public void setUrlMapping(String urlMapping) {
            this.urlMapping = urlMapping;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public Object[] getArg() {
            return arg;
        }

        public void setArg(Object[] arg) {
            this.arg = arg;
        }

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }
    }

}
