package com.zyy.servlet;

import com.zyy.annotation.Controller;
import com.zyy.annotation.RequestMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author Administrator
 * @create 2019/3/14
 * @since 1.0.0
 */
public class DispatcherServlet extends HttpServlet {

    private Properties properties=new Properties();

    private List<String> classNames=new ArrayList<>();

    private Map<String,Object> iocMap=new HashMap<>();

    private Map<String, Method> handlerMapping = new  HashMap<>();

    private Map<String, Object> controllerMap  =new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500!! Server Exception");
        }
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        //1.加载配置文件
        doLoadConfig(servletConfig.getInitParameter("contextConfigLocation"));
        //2.初始化所有相关联的类,扫描用户设定的包下面所有的类
        doScanner(properties.getProperty("scanPackage"));
        //3.拿到扫描到的类,通过反射机制,实例化,并且放到ioc容器中(k-v  beanName-bean) beanName默认是首字母小写
        doInstance();
        //4.初始化HandlerMapping(将url和method对应上)
        initHandlerMapping();
    }


    private void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (handlerMapping.isEmpty()){
            return;
        }
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        //请求路径去掉项目名称
        uri=uri.replace(contextPath,"");
        if (!handlerMapping.containsKey(uri)){
            response.getWriter().write("404 NOT FOUND!");
            return;
        }
        Method method = handlerMapping.get(uri);
        //获取方法的参数类型列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        //获取请求的参数
        Map<String, String[]> parameterMap = request.getParameterMap();
        //保存参数值
        Object [] paramValues= new Object[parameterTypes.length];
        for (int i=0;i<parameterTypes.length;i++){
            //根据参数类型,做处理
            String simpleName = parameterTypes[i].getSimpleName();
            if (simpleName.equals("HttpServletRequest")){
                paramValues[i]=request;
                continue;
            }
            if (simpleName.equals("HttpServletResponse")){
                paramValues[i]=response;
                continue;
            }
            if (simpleName.equals("String")){
                for (Map.Entry<String,String[]> entry:parameterMap.entrySet()) {
                    String value = Arrays.toString(entry.getValue()).replaceAll("\\[|\\]","");
                    paramValues[i]=value;
                }
            }
        }
        //利用反射机制来调用
        method.invoke(controllerMap.get(uri),paramValues);
    }

    private void doLoadConfig(String config){
        //把web.xml中的contextConfigLocation对应value值的文件加载到流里面
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(config);
        try {
            //用Properties文件加载文件里的内容
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doScanner(String packageName){
        //把所有的.替换成/
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        File files=new File(url.getFile());
        for (File file:files.listFiles()){
            if (file.isDirectory()){
                //递归读取包
                doScanner(packageName+"."+file.getName());
            }else{
                String className=packageName+"."+file.getName().replace(".class","");
                classNames.add(className);
            }
        }
    }

    private void doInstance() {
        if (classNames.isEmpty()){
            return;
        }
        for (String className:classNames){
            Class<?> clazz = null;
            try {
                clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(Controller.class)){
                    iocMap.put(toLowerFirstWord(clazz.getSimpleName()),clazz.newInstance());
                }else{
                    continue;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    private void initHandlerMapping() {
        if(iocMap.isEmpty()){
            return;
        }
        try {
            for (Map.Entry<String,Object> entry:iocMap.entrySet()) {
                Class<?> clazz = entry.getValue().getClass();
                if (!clazz.isAnnotationPresent(Controller.class)){
                    continue;
                }
                //拼url时,是controller头的url拼上方法上的url
                String baseUrl ="";
                if (clazz.isAnnotationPresent(RequestMapping.class)){
                    baseUrl = clazz.getAnnotation(RequestMapping.class).value();
                }
                Method []methods = clazz.getDeclaredMethods();
                for (Method method:methods){
                    if (!method.isAnnotationPresent(RequestMapping.class)){
                        continue;
                    }
                    String url = baseUrl + method.getAnnotation(RequestMapping.class).value();
                    handlerMapping.put(url,method);
                    controllerMap.put(url,entry.getValue());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 把字符串的首字母小写
     * @param name
     * @return
     */
    private String toLowerFirstWord(String name){
        char[] chars = name.toCharArray();
        chars[0]+=32;
        return String.valueOf(chars);
    }
}
