package com.mycompany.quanlysieuthi.employee;

import com.mycompany.quanlysieuthi.position.Position;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

/**
 * JPA Entity mapping table NHANVIEN in MySQL.
 */
@Entity
@Table(name = "NHANVIEN")
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "maNhanVien", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "maChucVu", nullable = false)
    private Position position;

    @Column(name = "hoTen", length = 100, nullable = false)
    private String fullName;

    @Column(name = "tenDangNhap", length = 255, nullable = false, unique = true)
    private String username;

    @Column(name = "matKhau", length = 255, nullable = false)
    private String password;

    @Column(name = "soDienThoai", length = 15, nullable = false, unique = true)
    private String phone;

    @Column(name = "trangThai")
    private Boolean status;

    public Employee() {
    }

    public Employee(String id, Position position, String fullName, String username,
                    String password, String phone, Boolean status) {
        this.id = id;
        this.position = position;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id='" + id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", username='" + username + '\'' +
                ", phone='" + phone + '\'' +
                ", status=" + status +
                '}';
    }
}
