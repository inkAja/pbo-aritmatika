package com.pbo.aritmatika;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "aritmatika")
public class Aritmatika {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aritmatika")
    private Integer idAritmatika;

    @Column(name = "bil_1", nullable = false)
    private Integer bil1;

    @Column(name = "operasi", nullable = false, length = 1, columnDefinition = "char(1)")
    private String operasi;

    @Column(name = "bil_2", nullable = false)
    private Integer bil2;

    // Hasil hanya dipakai untuk ditampilkan di web, bukan disimpan ke database.
    @Transient
    private String hasil;

    public Aritmatika() {
    }

    public Integer getIdAritmatika() {
        return idAritmatika;
    }

    public void setIdAritmatika(Integer idAritmatika) {
        this.idAritmatika = idAritmatika;
    }

    public Integer getBil1() {
        return bil1;
    }

    public void setBil1(Integer bil1) {
        this.bil1 = bil1;
    }

    public String getOperasi() {
        return operasi;
    }

    public void setOperasi(String operasi) {
        this.operasi = operasi;
    }

    public Integer getBil2() {
        return bil2;
    }

    public void setBil2(Integer bil2) {
        this.bil2 = bil2;
    }

    public String getHasil() {
        return hasil;
    }

    public void setHasil(String hasil) {
        this.hasil = hasil;
    }
}
