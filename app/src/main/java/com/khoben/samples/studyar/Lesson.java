package com.khoben.samples.studyar;


public class Lesson {

    private String aud;
    private String subject;
    private String fio;
    private String degree;

    public Lesson() {
    }

    @Override
    public String toString() {
        return "Lesson{" +
                "aud='" + aud + '\'' +
                ", subject='" + subject + '\'' +
                ", fio='" + fio + '\'' +
                ", degree='" + degree + '\'' +
                '}';
    }

    public Lesson(String aud, String fio, String subject, String degree) {
        this.aud = aud;
        this.fio = fio;
        this.subject = subject;
        this.degree = degree;
    }

    public String getAud() {
        return aud;
    }

    public void setAud(String aud) {
        this.aud = aud;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }
}