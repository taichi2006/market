package com.mycompany.quanlysieuthi.position;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

/**
 * JPA Entity mapping table CHUCVU in MySQL.
 */
@Entity
@Table(name = "CHUCVU")
public class Position implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "maChucVu", length = 36, nullable = false)
    private String id;

    @Column(name = "tenChucVu", length = 50)
    private String name;

    public Position() {
    }

    public Position(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return Objects.equals(id, position.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Position{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
