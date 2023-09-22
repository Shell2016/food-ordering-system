package com.food.ordering.system.domain.entity;

public abstract class BaseEntity<T> {
    private T t;

    public T getId() {
        return t;
    }

    public void setId(T t) {
        this.t = t;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity<?> that = (BaseEntity<?>) o;
        return t.equals(that.t);
    }

    @Override
    public int hashCode() {
        return t.hashCode();
    }
}
