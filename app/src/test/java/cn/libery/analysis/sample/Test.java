package cn.libery.analysis.sample;

/**
 * @author shizhiqiang on 2019/1/15.
 * @description
 */
public class Test {
    @org.junit.Test
    public void test() {
        String url = "/Users/shizhiqiang/Github/Analysis/app/build/intermediates/javac/devDebug/compileDevDebugJavaWithJavac/classes/cn/libery/analysis/sample/TestActivity.class";
        url = url.replace("/", ".");
        String classPath;
        if (url.contains("cn.libery.analysis.sample")) {
            int index = url.indexOf("cn.libery.analysis.sample");
            classPath = url.substring(index, url.length());
        } else {
            System.out.println("project can not inject");
            return;
        }
        System.out.println(classPath);
        String className = classPath.substring(0, classPath.length() - 6).replace('/', '.').replace('/', '.');
        System.out.print(classPath + " " + className);

    }
}
