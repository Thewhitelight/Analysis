package cn.libery.analysis.runtime;

import android.os.Looper;
import android.util.Log;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.concurrent.TimeUnit;

/**
 * @author shizhiqiang on 2018/12/31.
 * @description
 */
@Aspect
public class TrackAspectJ {

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
        Log.v(asTag(cls), builder.toString());
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

        Log.v(asTag(cls), builder.toString());
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
