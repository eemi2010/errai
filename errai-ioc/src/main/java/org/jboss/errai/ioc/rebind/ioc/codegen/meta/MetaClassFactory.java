/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.rebind.ioc.codegen.meta;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.LoadClassReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.gwt.GWTClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java.JavaReflectionClass;

import javax.enterprise.util.TypeLiteral;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public final class MetaClassFactory {
  private static final Map<String, MetaClass> CLASS_CACHE = new HashMap<String, MetaClass>();

  public static MetaClass get(String fullyQualifiedClassName) {
    return createOrGet(fullyQualifiedClassName);
  }

  public static MetaClass get(TypeOracle typeOracle, String fullyQualifiedClassName) {
    return createOrGet(typeOracle, fullyQualifiedClassName);
  }

  public static MetaClass get(TypeOracle typeOracle, Class<?> clazz) {
    return get(typeOracle, clazz.getName());
  }

  public static MetaClass get(JType clazz) {
    return createOrGet(clazz);
  }

  public static MetaClass get(Class<?> clazz) {
    return createOrGet(clazz);
  }

  public static MetaClass get(TypeLiteral<?> literal) {
    return createOrGet(literal);
  }

  public static Statement getAsStatement(Class<?> clazz) {
    final MetaClass metaClass = createOrGet(clazz);
    return new Statement() {
      @Override
      public String generate(Context context) {
        return LoadClassReference.getClassReference(metaClass, context);
      }

      @Override
      public MetaClass getType() {
        return MetaClassFactory.get(Class.class);
      }

      public Context getContext() {
        return null;
      }
    };
  }

  public static boolean isCached(String name) {
    return CLASS_CACHE.containsKey(name);
  }

  private static MetaClass createOrGet(String fullyQualifiedClassName) {
    if (!CLASS_CACHE.containsKey(fullyQualifiedClassName)) {
      return createOrGet(load(fullyQualifiedClassName));
    }

    return CLASS_CACHE.get(fullyQualifiedClassName);
  }

  private static MetaClass createOrGet(TypeOracle oracle, String fullyQualifiedClassName) {
    if (!CLASS_CACHE.containsKey(fullyQualifiedClassName)) {
      return createOrGet(load(oracle, fullyQualifiedClassName));
    }

    return CLASS_CACHE.get(fullyQualifiedClassName);
  }

  private static MetaClass createOrGet(TypeLiteral type) {
    if (type == null) return null;

    if (!CLASS_CACHE.containsKey(type.toString())) {
      MetaClass gwtClass = JavaReflectionClass.newUncachedInstance(type);

      addLookups(type, gwtClass);
      return gwtClass;
    }

    return CLASS_CACHE.get(type.toString());
  }


  private static MetaClass createOrGet(JType type) {
    if (type == null) return null;

    if (type.isParameterized() != null) {
      return GWTClass.newUncachedInstance(type);
    }

    if (!CLASS_CACHE.containsKey(type.getQualifiedSourceName())) {
      MetaClass gwtClass = GWTClass.newUncachedInstance(type);

      addLookups(type, gwtClass);
      return gwtClass;
    }

    return CLASS_CACHE.get(type.isClassOrInterface().getName());
  }


  private static MetaClass createOrGet(Class cls) {
    if (cls == null) return null;

    if (cls.getTypeParameters() != null) {
      return JavaReflectionClass.newUncachedInstance(cls);
    }

    if (!CLASS_CACHE.containsKey(cls.getName())) {
      MetaClass javaReflectionClass = JavaReflectionClass.newUncachedInstance(cls);

      addLookups(cls, javaReflectionClass);
      return javaReflectionClass;
    }

    return CLASS_CACHE.get(cls.getName());
  }

  private static void addLookups(TypeLiteral literal, MetaClass metaClass) {
    CLASS_CACHE.put(literal.toString(), metaClass);
  }

  private static void addLookups(Class cls, MetaClass metaClass) {
    CLASS_CACHE.put(cls.getName(), metaClass);
  }

  private static void addLookups(JType cls, MetaClass metaClass) {
    CLASS_CACHE.put(cls.getQualifiedSourceName(), metaClass);
  }

  private static Class<?> load(String fullyQualifiedName) {
    try {
      return Class.forName(fullyQualifiedName, false, Thread.currentThread().getContextClassLoader());
    }
    catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not load class: " + fullyQualifiedName);
    }
  }

  private static JClassType load(TypeOracle oracle, String fullyQualifiedName) {
    try {
      return oracle.getType(fullyQualifiedName);
    }
    catch (NotFoundException e) {
      throw new RuntimeException("Could not load class: " + fullyQualifiedName);
    }
  }

  public static MetaClass[] fromClassArray(Class<?>[] classes) {
    MetaClass[] newClasses = new MetaClass[classes.length];
    for (int i = 0; i < classes.length; i++) {
      newClasses[i] = createOrGet(classes[i]);
    }
    return newClasses;
  }

  public static Class<?>[] asClassArray(MetaClass[] cls) {
    Class<?>[] newClasses = new Class<?>[cls.length];
    for (int i = 0; i < cls.length; i++) {
      newClasses[i] = cls[i].asClass();
    }
    return newClasses;
  }
}
