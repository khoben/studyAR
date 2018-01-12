package com.khoben.samples.studyar.MyIterator;


import java.util.List;

public class TargetContainer<E> implements MyContainer<E> {
    private List<E> objectList;

    public TargetContainer(List<E> objectList){
        set(objectList);
    }

    @Override
    public MyIterator getIterator() {
        return new TargetIterator();
    }

    @Override
    public void add(E obj) {
        objectList.add(obj);
    }

    @Override
    public void remove(E obj) {
        objectList.remove(obj);
    }

    @Override
    public void set(List objectList) {
        this.objectList = objectList;
    }


    private class TargetIterator implements MyIterator<E> {

        private int index;

        @Override
        public boolean hasNext() {

            if(index < objectList.size()){
                return true;
            }
            return false;
        }

        @Override
        public E next() {

            if(this.hasNext()){
                return objectList.get(index++);
            }
            return null;
        }
    }
}