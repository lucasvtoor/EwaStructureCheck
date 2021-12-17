package com.fullstacked.Fullstacked;

import com.google.common.reflect.ClassPath;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.*;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


@SpringBootTest
class FullstackedApplication {
    private static final String CONTROLLER_PACKAGE = "REPLACE THIS WITH THE SOURCE PATH OF YOUR CONTROLLER PACKAGE";
    private static final String MODEL_PACKAGE = "REPLACE THIS WITH THE SOURCE PATH OF YOUR CONTROLLER PACKAGE";
    private static final String REPOSITORY_PACKAGE = "com.fullstacked.Fullstacked.Package";
    ClassPath classpath;

    FullstackedApplication() {
        try {
            classpath = ClassPath.from(FullstackedApplication.class.getClassLoader());
            classpath.getTopLevelClasses(MODEL_PACKAGE).forEach(System.out::println);
        } catch (IOException e) {
            classpath = null;
            e.printStackTrace();
            return;
        }


    }

    @Test
    void contextLoads() {
    }


    @Test
    void allModelsCorrectlyAnnotatedWithModel() {
        assertEquals(false, abstractMethod(MODEL_PACKAGE, (i) -> false,false,
                Entity.class, Embeddable.class,MappedSuperclass.class));

    }

    @Test
    void allControllersCorrectlyAnnotatedWithController() {
        assertEquals(false, abstractMethod(CONTROLLER_PACKAGE, (i) -> false,false, Controller.class, RestController.class));

    }

    @Test
    void allRepositoriesCorrectlyAnnotatedWithRepository() {
        assertEquals(false, abstractMethod(REPOSITORY_PACKAGE, (i) -> false,false, Repository.class));

    }



    /**
     * @author Lucas van Toorenburg & Ruben Wolterbeek.
     * @param packageName This should be one of your constants.
     * @param edgeCase  This is a Function that you use to exclude a class from the check
     * @param ignoreInterface boolean that dictates if you want to include interfaces in the check.
     * @param annotations  This is the approved list of annotations the test should check for.
     * @return
     */
    public boolean abstractMethod(String packageName, Function<Class, Boolean> edgeCase, boolean ignoreInterface, Class ...annotations) {
        boolean hitError = false;
        Set<ClassPath.ClassInfo> classInfos = classpath.getTopLevelClasses(packageName);
        for (ClassPath.ClassInfo item : classInfos) {
            Class aClass = item.load();
            if(aClass.isInterface() && ignoreInterface){
                continue;
            }

            if (edgeCase.apply(aClass)) {
                continue;
            }
            boolean checkClass = false;
            for(Class a: annotations){
                if(Objects.nonNull(aClass.getAnnotation(a))){
                    checkClass = true;
                }

            }
            if (!checkClass) {
                System.err.printf("%s is not Annotated as a required annotation, please do or move it out of Models%n", aClass.getSimpleName());
                hitError = true;
            }
        }
        return hitError;
    }

    /**
     * These should be filled with Duplicate checks, ¯\_(ツ)_/¯
     */
    @Test
    public void assertOneToMany() {
        AtomicBoolean hasError = new AtomicBoolean(false);
        Set<ClassPath.ClassInfo> classInfos = classpath.getTopLevelClasses(MODEL_PACKAGE);
        classInfos.forEach(item -> {
            Class model = item.load();
            for (Field field : model.getFields()) {
                if (field.getAnnotation(OneToMany.class) != null) {
                    ParameterizedType list = (ParameterizedType) field.getGenericType();
                    Class other = (Class<?>) list.getActualTypeArguments()[0];
                    boolean otherClassHasMatchingManyToOne = false;
                    for (Field field2 : other.getFields()) {
                        if (field2.getAnnotation(ManyToOne.class) != null) {
                            if (field2.getType() == model) {
                                otherClassHasMatchingManyToOne = true;
                            }
                        }
                    }
                    if (!otherClassHasMatchingManyToOne) {
                        hasError.set(true);
                        System.out.printf(" OneToMany %s in %s has no matching ManyToOne in %s%n",
                                field.getName(), model.getSimpleName(), other.getSimpleName());
                    }
                }
            }
        });
        assertFalse(hasError.get());

    }
    @Test
    public void assertManyToOne() {
        AtomicBoolean hasError = new AtomicBoolean(false);
        Set<ClassPath.ClassInfo> classInfos = classpath.getTopLevelClasses(MODEL_PACKAGE);
        classInfos.forEach(item -> {
            Class model = item.load();
            for (Field field : model.getFields()) {
                if (field.getAnnotation(ManyToOne.class) != null) {
                   Class other = field.getType();
                    boolean otherClassHasMatchingOneToMany = false;
                    for (Field field2 : other.getFields()) {
                        if (field2.getAnnotation(OneToMany.class) != null) {
                            ParameterizedType list = (ParameterizedType) field2.getGenericType();
                            Class checksout = (Class<?>) list.getActualTypeArguments()[0];
                            if (checksout == model) {
                                otherClassHasMatchingOneToMany = true;
                            }
                        }
                    }
                    if (!otherClassHasMatchingOneToMany) {
                        hasError.set(true);
                        System.err.printf(" ManyToOne %s in %s has no matching OneToMany in %s%n",
                                field.getName(), model.getSimpleName(), other.getSimpleName());
                    }
                }
            }
        });
        assertFalse(hasError.get());

    }
    @Test
    public void assertManyToMany() {
        AtomicBoolean hasError = new AtomicBoolean(false);
        Set<ClassPath.ClassInfo> classInfos = classpath.getTopLevelClasses(MODEL_PACKAGE);
        classInfos.forEach(item -> {
            Class model = item.load();
            for (Field field : model.getFields()) {
                if (field.getAnnotation(ManyToMany.class) != null) {
                    ParameterizedType list = (ParameterizedType) field.getGenericType();
                    Class other = (Class<?>) list.getActualTypeArguments()[0];
                    boolean otherClassHasMatchingManyToMany = false;
                    for (Field field2 : other.getFields()) {
                        if (field2.getAnnotation(ManyToMany.class) != null) {
                            ParameterizedType list2 = (ParameterizedType) field2.getGenericType();
                            Class checksout = (Class<?>) list2.getActualTypeArguments()[0];
                            if (checksout == model) {
                                otherClassHasMatchingManyToMany = true;
                            }
                        }
                    }
                    if (!otherClassHasMatchingManyToMany) {
                        hasError.set(true);
                        System.err.printf(" ManyToMany %s in %s has no matching ManyToMany in %s%n",
                                field.getName(), model.getSimpleName(), other.getSimpleName());
                    }
                }
            }
        });
        assertFalse(hasError.get());
    }


}
