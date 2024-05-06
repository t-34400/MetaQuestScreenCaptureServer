package com.t34400.questscreencapture.test;
import android.annotation.TargetApi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ClazzAnalyzer {
    @TargetApi(android.os.Build.VERSION_CODES.O)
    public static void printClazzDetails(Class<?> clazz) {
        System.out.println("Class: " + clazz.getName());

        System.out.println("\nDeclared Constructors: ");
        Constructor[] declaredConstructors = clazz.getDeclaredConstructors();
        for (Constructor constructor : declaredConstructors) {
            System.out.println("Declared Constructor: " + constructor.getName());
            Parameter[] parameters = constructor.getParameters();
            for (Parameter parameter : parameters) {
                System.out.println("    Parameter: " + parameter.getName() + ", Type: " + parameter.getType().getName());
            }
        }

        System.out.println("\nConstructors: ");
        Constructor[] constructors = clazz.getConstructors();
        for (Constructor constructor : constructors) {
            System.out.println("Constructor: " + constructor.getName());
            Parameter[] parameters = constructor.getParameters();
            for (Parameter parameter : parameters) {
                System.out.println("    Parameter: " + parameter.getName() + ", Type: " + parameter.getType().getName());
            }
        }

        System.out.println("\nDeclared Methods: ");
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            System.out.println("Declared Method: " + method.getName());
            Parameter[] parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                System.out.println("    Parameter: " + parameter.getName() + ", Type: " + parameter.getType().getName());
            }
        }

        System.out.println("\nDeclared Fields: ");
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            System.out.println("Declared Field: " + field.getName() + ", Type: " + field.getType());
        }

        System.out.println("\nMethods: ");
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            System.out.println("Method: " + method.getName());
            Parameter[] parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                System.out.println("    Parameter: " + parameter.getName() + ", Type: " + parameter.getType().getName());
            }
        }

        System.out.println("\nFields: ");
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            System.out.println("Field: " + field.getName() + ", Type: " + field.getType());
        }
    }
}