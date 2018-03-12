package com.example.bozonpee.canvasaccelerometer;

/**
 * Created by Camille on 15/02/2018.
 */

public class Plateform {
    //private int idPlateform;
    private int plateformX;
    private int plateformY;


    //public Plateform(int idPlateform, int plateformX, int plateformY) {
    public Plateform(int plateformX, int plateformY) {
        //this.idPlateform = idPlateform;
        this.plateformX = plateformX;
        this.plateformY = plateformY;
    }

    //public int getIdPlateform() {
        //return idPlateform;
    //}

    public int getPlateformX() {
        return plateformX;
    }

    public int getPlateformY() {
        return plateformY;
    }

    //public void setIdPlateform(int idPlateform) {
        //this.idPlateform = idPlateform;
    //}

    public void setPlateformX(int plateformX) {
        this.plateformX = plateformX;
    }

    public void setPlateformY(int plateformY) {
        this.plateformY = plateformY;
    }

}
