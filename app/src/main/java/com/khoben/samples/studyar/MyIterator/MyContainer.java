package com.khoben.samples.studyar.MyIterator;


import java.util.List;

public interface MyContainer<E> {
   MyIterator getIterator();
   void add(E obj);
   void remove(E obj);
   void set(List<E> objectList);
}