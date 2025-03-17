package com.example.myapplication;

import java.util.Date;

public class Tarea {
    private String titulo;
    private String descripcion;
    private boolean completada;

    private Priority prioridad;
    private String label;
    private int labelColor;

    // Fecha de plazo (deadline) de la tarea
    private Date fechaPlazo;

    // Constructor principal
    public Tarea(String titulo, String descripcion) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.completada = false;
        this.prioridad = Priority.LOW;  // Por defecto, prioridad baja
        this.label = "";
        this.labelColor = 0xFF000000;   // Negro por defecto
        this.fechaPlazo = null;        // Sin fecha de plazo por defecto
    }

    // Getters y setters

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }

    public Priority getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Priority prioridad) {
        this.prioridad = prioridad;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getLabelColor() {
        return labelColor;
    }

    public void setLabelColor(int labelColor) {
        this.labelColor = labelColor;
    }

    // Getter y setter de fechaPlazo
    public Date getFechaPlazo() {
        return fechaPlazo;
    }

    public void setFechaPlazo(Date fechaPlazo) {
        this.fechaPlazo = fechaPlazo;
    }
}
