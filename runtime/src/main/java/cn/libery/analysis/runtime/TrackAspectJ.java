package cn.libery.analysis.runtime;

import android.os.Looper;
import android.util.Log;
import cn.libery.analysis.annotation.Track;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author shizhiqiang on 2018/12/31.
 * @description
 */
@Aspect
public class TrackAspectJ {

    public static final String PACKAGE_NAME = "";

    @Pointcut("within(@cn.libery.analysis.annotation.Track *)")
    public void withinAnnotatedClass() {
    }

    @Pointcut("execution(* android.app.Activity.onCreate(..))")
    public void withinActivityOnCreateClass() {
    }

    @Pointcut("execution(!synthetic * *(..)) && withinAnnotatedClass()")
    public void methodInsideAnnotatedType() {
    }

    @Pointcut("execution(!synthetic *.new(..)) && withinAnnotatedClass()")
    public void constructorInsideAnnotatedType() {
    }


    @Pointcut("execution(@cn.libery.analysis.annotation.Track * *(..)) || methodInsideAnnotatedType()")
    public void method() {
    }

    @Pointcut("execution(@cn.libery.analysis.annotation.Track *.new(..)) || constructorInsideAnnotatedType()")
    public void constructor() {
    }

    @Pointcut("execution(* " + PACKAGE_NAME + "..*.*(..))")
    public void allTargetMethod() {
    }

    @After("allTargetMethod()")
    public void afterLogAndExecute(JoinPoint joinPoint) {
        if (joinPoint != null) {
            Signature signature = joinPoint.getSignature();
            if (signature instanceof CodeSignature) {
                CodeSignature codeSignature = (CodeSignature) signature;
                Class<?> cls = signature.getDeclaringType();
                String methodName = signature.getName();

                String[] parameterNames = codeSignature.getParameterNames();
                Object[] parameterValues = joinPoint.getArgs();

                StringBuilder builder = new StringBuilder();
                builder.append(methodName).append('(');
                for (int i = 0; i < parameterValues.length; i++) {
                    if (i > 0) {
                        builder.append(", ");
                    }
                    builder.append(parameterNames[i]).append('=');
                    builder.append(Strings.toString(parameterValues[i]));
                }
                builder.append(')');

                if (Looper.myLooper() != Looper.getMainLooper()) {
                    builder.append(" [Thread:\"").append(Thread.currentThread().getName()).append("\"]");
                }

                final int lineNumber = joinPoint.getSourceLocation().getLine() - 1;
                Log.d(asTag(cls) + "-" + methodName + " " + lineNumber, builder.toString());
            }
        }

    }

    @Around("method() || constructor() || withinActivityOnCreateClass()")
    public Object logAndExecute(ProceedingJoinPoint joinPoint) throws Throwable {

        enterMethod(joinPoint);

        long startNanos = System.nanoTime();
        Object result = joinPoint.proceed();
        long stopNanos = System.nanoTime();
        long lengthMillis = TimeUnit.NANOSECONDS.toMillis(stopNanos - startNanos);

        exitMethod(joinPoint, result, lengthMillis);

        return result;
    }

    private void enterMethod(ProceedingJoinPoint joinPoint) {
        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
        Class<?> cls = codeSignature.getDeclaringType();
        String methodName = codeSignature.getName();
        String[] parameterNames = codeSignature.getParameterNames();
        Object[] parameterValues = codeSignature.getParameterTypes();

        StringBuilder builder = new StringBuilder("\u21E2 ");
        builder.append(methodName).append('(');
        for (int i = 0; i < parameterValues.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(parameterNames[i]).append('=');
            builder.append(Strings.toString(parameterValues[i]));
        }
        builder.append(')');

        if (Looper.myLooper() != Looper.getMainLooper()) {
            builder.append(" [Thread:\"").append(Thread.currentThread().getName()).append("\"]");
        }

        printLog(joinPoint, cls, builder.toString());
    }

    private void exitMethod(ProceedingJoinPoint joinPoint, Object result, long lengthMillis) {

        Signature signature = joinPoint.getSignature();

        Class<?> cls = signature.getDeclaringType();
        String methodName = signature.getName();
        boolean hasReturnType = signature instanceof MethodSignature
                && ((MethodSignature) signature).getReturnType() != void.class;

        StringBuilder builder = new StringBuilder("\u21E0 ")
                .append(methodName)
                .append(" [")
                .append(lengthMillis)
                .append("ms]");

        if (hasReturnType) {
            builder.append(" = ");
            builder.append(Strings.toString(result));
        }

        printLog(joinPoint, cls, builder.toString());
    }

    private void printLog(ProceedingJoinPoint joinPoint, Class<?> cls, String msg) {
        String tag = asTag(cls);
        Signature signature = joinPoint.getSignature();
        if (signature instanceof MethodSignature) {
            MethodSignature methodSignature = (MethodSignature) signature;
            try {
                Method realMethod = joinPoint.getTarget().getClass().getDeclaredMethod(signature.getName(),
                        methodSignature.getMethod()
                                .getParameterTypes());
                Track track = realMethod.getAnnotation(Track.class);
                if (track == null) {
                    Log.v(tag, msg);
                    return;
                }
                switch (track.level()) {
                    case Log.VERBOSE:
                        Log.v(tag, msg);
                        break;
                    case Log.DEBUG:
                        Log.d(tag, msg);
                        break;
                    case Log.INFO:
                        Log.i(tag, msg);
                        break;
                    case Log.WARN:
                        Log.w(tag, msg);
                        break;
                    case Log.ERROR:
                        Log.e(tag, msg);
                        break;
                    default:
                        Log.v(tag, msg);
                }
            } catch (NoSuchMethodException e) {
                Log.e(tag, e.getMessage() + " " + msg);
            }
        } else {
            Log.v(tag, msg);
        }
    }

    private static String asTag(Class<?> cls) {
        if (cls.isAnonymousClass()) {
            Class<?> cls2 = cls.getEnclosingClass();
            if (cls2 != null) {
                return asTag(cls2);
            }
        }
        return cls.getSimpleName();
    }

}
