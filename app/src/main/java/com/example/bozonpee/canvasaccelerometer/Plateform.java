package com.example.bozonpee.canvasaccelerometer;

/**
 * <b>Plateform est la classe qui définie une plateforme.</b>
 * <p>
 * Une Plateform est caractérisée par les informations suivantes :
 * <ul>
 * <li>Une coordonnée X.</li>
 * <li>Une coordonnée Y.</li>
 * </ul>
 * </p>
 * <p>
 * Ces deux coordonnées forment le point de départ en haut à gauche, du tracé de la Plateform.
 * </p>
 *
 * @author Elisa BOZON & Camille VIVIER
 */


public class Plateform {
    /**
     * La coordonnée X de la Plateform.
     *
     * @see Plateform#Plateform(int, int)
     * @see Plateform#getPlateformX()
     * @see Plateform#setPlateformX(int)
     */
    private int plateformX;

    /**
     * La coordonnée Y de la Plateform.
     *
     * @see Plateform#Plateform(int, int)
     * @see Plateform#getPlateformY()
     * @see Plateform#setPlateformY(int)
     */
    private int plateformY;

    /**
     * Constructeur Plateform.
     *
     * @param plateformX
     *            La coordonnée X de la Plateform
     * @param plateformY
     *            La coordonnée Y de la Plateform
     *
     * @see Plateform#plateformX
     * @see Plateform#plateformY
     */
    public Plateform(int plateformX, int plateformY) {
        //this.idPlateform = idPlateform;
        this.plateformX = plateformX;
        this.plateformY = plateformY;
    }


    /**
     * Retourne la coordonnée X de la plateforme.
     *
     * @return La coordonnée X de la plateforme.
     */
    public int getPlateformX() {
        return plateformX;
    }


    /**
     * Retourne la coordonnée Y de la plateforme.
     *
     * @return La coordonnée Y de la plateforme.
     */
    public int getPlateformY() { return plateformY; }

    /**
     * Met à jour la coordonnée X de la plateforme.
     *
     * @param plateformX La nouvelle coordonnée X de la plateforme.
     */
    public void setPlateformX(int plateformX) {
        this.plateformX = plateformX;
    }

    /**
     * Met à jour la coordonnée Y de la plateforme.
     *
     * @param plateformY La nouvelle coordonnée Y de la plateforme.
     */

    public void setPlateformY(int plateformY) {
        this.plateformY = plateformY;
    }

}
