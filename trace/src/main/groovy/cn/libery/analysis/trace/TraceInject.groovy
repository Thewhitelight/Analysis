package cn.libery.analysis.trace

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.Modifier
import javassist.bytecode.CodeAttribute
import javassist.bytecode.LocalVariableAttribute
import org.gradle.api.Project

class TraceInject {

    static String TAG = "Trace"
    static ClassPool POOL = ClassPool.getDefault()

    static void injectDirCode(String path, Project project) {
        if (!project.Trace.enabled) {
            return
        }
        project.Trace.enabled
        POOL.appendClassPath(path)
        POOL.appendClassPath(project.android.bootClasspath[0].toString())
        File dir = new File(path)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                if (filePath.endsWith(".class")
                        && !filePath.contains('R$')
                        && !filePath.contains('R.class')
                        && !filePath.contains("BuildConfig.class")) {
                    modifyClass(project, path, filePath)
                }
            }
        }
    }

    static void modifyClass(Project project, String path, String filePath) {
        String classPath
        def packageName = project.Trace.packageName
        filePath = filePath.replace("/", ".")

        if (classExcludes(project, filePath)) {
            return
        }
        if (packageExcludes(project, filePath)) {
            return
        }

        if (filePath.contains(packageName)) {
            int index = filePath.indexOf(packageName)
            classPath = filePath.substring(index, filePath.length())
        } else {
            println("project can not inject")
            return
        }
        String className = classPath.substring(0, classPath.length() - 6)
                .replace('/', '.')
                .replace('/', '.')
        CtClass c = POOL.getCtClass(className)
        if (c.isFrozen()) {
            c.defrost()
        }
        CtMethod[] methods = c.getDeclaredMethods()
        int i = 0
        for (CtMethod method : methods) {
            if (method.isEmpty() || Modifier.isNative(method.getModifiers())) {
                return
            }
            i++
            insertTime(project.Trace.logLevel, className.replace(packageName + ".", ""), method)
        }
        c.writeFile(path)
        c.detach()
    }

    static void insertTime(String logLevel, String className, CtMethod method) {
        try {
            //int pos = 1

            CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute()
            LocalVariableAttribute attribute = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag)
            int size = method.getParameterTypes().length
            String[] paramTypes = new String[size]
            if (attribute == null) {
                return
            }
            int pos = Modifier.isStatic(method.getModifiers()) ? 0 : 1
            for (int i = 0; i < size; i++) {
                paramTypes[i] =  attribute.variableName(i + pos)
            }

            def stringType = POOL.getCtClass("java.lang.String")
            def objType = POOL.getCtClass("java.lang.Object")
            method.addLocalVariable("startTime", CtClass.longType)
            method.addLocalVariable("endTime", CtClass.longType)
            method.addLocalVariable("className", stringType)
            method.addLocalVariable("methodName", stringType)
            method.addLocalVariable("lineNumber", CtClass.intType)
            method.addLocalVariable("returnObj", objType)

            StringBuilder startInjectSB = new StringBuilder()
            startInjectSB.append("    startTime = System.nanoTime();\n")
            startInjectSB.append("    className = \"" + className + "\";\n")
            startInjectSB.append("    methodName = \"" + method.name + "\";\n")
            startInjectSB.append("    lineNumber = " + (method.getMethodInfo().getLineNumber(pos) - 1) + ";\n")
            startInjectSB.append("    android.util.Log.${logLevel}(\"${TAG}\",")
            startInjectSB.append("\"-> \"+className+\" \"+lineNumber+\" \"+methodName+")
            startInjectSB.append("\"(\"")
            for (int i = 0; i < size; i++) {
                startInjectSB.append("+")
                if (i > 0) {
                    startInjectSB.append("\",\" + ")
                }
                int x = (i + pos)
                startInjectSB.append("\"${paramTypes[i]}")
                startInjectSB.append("=\"")
                startInjectSB.append("+\$${x}")
            }
            startInjectSB.append("+")
            startInjectSB.append("\")\"")
            startInjectSB.append(");")

            method.insertBefore(startInjectSB.toString())

            StringBuilder endInjectSB = new StringBuilder()
            endInjectSB.append("    endTime = System.nanoTime();\n")
            endInjectSB.append("    returnObj=\$_==null?\"\":\$_;")
            endInjectSB.append("    android.util.Log.${logLevel}(\"${TAG}\",")
            endInjectSB.append("\"<- \"+className+\" \"+lineNumber+\" \"+methodName+\" \"+")
            endInjectSB.append("java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(endTime - startTime) + \"ms \"+returnObj);")

            method.insertAfter(endInjectSB.toString())

        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    private static boolean classExcludes(Project project, String filePath) {
        boolean isExclude = false
        for (String target : project.Trace.classExcludes) {
            if (filePath.contains(target + ".class")) {
                isExclude = true
                break
            }
        }
        return isExclude
    }

    private static boolean packageExcludes(Project project, String filePath) {
        boolean isExclude = false
        for (String target : project.Trace.packageExcludes) {
            if (filePath.contains(target)) {
                isExclude = true
                break
            }
        }
        return isExclude
    }

}