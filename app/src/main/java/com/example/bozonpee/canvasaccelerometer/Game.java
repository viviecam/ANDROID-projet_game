package com.example.bozonpee.canvasaccelerometer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <b>Game est la classe qui définie l'activité du jeu.</b>
 * <p>
 * Elle gère les points suivants :
 * <ul>
 * <li>Genération des éléments (fond, Plateform, Jack).</li>
 * <li>Déplacement de Jack.</li>
 * <li>Dessin des éléments.</li>
 * <li>Gestion du sensor</li>
 * </ul>
 * </p>
 *
 * @author Elisa BOZON & Camille VIVIER
 */


public class Game extends AppCompatActivity implements SensorEventListener {

    /**
     * Le score du joueur.
     *
     * @see Game#
     */
    private int score;

    /**
     * Le Canvas dans lequel on dessine nos éléments.
     *
     * @see Game#
     */
    private Game.CanvasView canvas;


    // IMAGES
    /**
     * L'image utilisée pour le fonc.
     *
     * @see Game#
     */
    private Bitmap backgroundImg;

    /**
     * L'image utilisée pour une plateforme.
     *
     * @see Game#
     */
    private Bitmap plateformImg;

    /**
     * <p>L'image utilisée pour Jack.</p>
     * <p>Largeur de l'image : 100</p>
     * <p>Hauteur de l'image : 30</p>
     *
     * @see Game#
     */
    private Bitmap jackImg;

    /**
     * <p>L'id de l'image qui sera utilisée pour Jack, récupérée depuis les autres activitées.</p>
     * <p>Largeur de l'image : 64</p>
     * <p>Hauteur de l'image : 100</p>
     *
     * @see Game#
     */
    private String idImgJack;



    // LES PLATEFORMES
    /**
     * La liste plateforms, composée de Plateform.
     *
     * @see Game#
     */
    private List<Plateform> plateforms;

    /**
     * Le nombre de Plateform à générer.
     *
     * @see Game#
     */
    private int nbPlatforms;


    // LA ZONE DE JEU
    /**
     * La coordonnée X minimum de la zone de jeu (à gauche).
     *
     * @see Game#
     */
    private int minX = 0; // car on part toujours du bord gauche de l'écran

    /**
     * La coordonnée X maximum de la zone de jeu (à droite).
     *
     * @see Game#
     */
    private int maxX;

    /**
     * La coordonnée Y minimum de la zone de jeu (en haut).
     *
     * @see Game#
     */
    private int minY;

    /**
     * La coordonnée X maximum de la zone de jeu (en bas).
     *
     * @see Game#
     */
    private int maxY;


    // JACK
    /**
     * La position de Jack sur l'axe X
     *
     * @see Game#
     */
    private int jackX;

    /**
     * La position de Jack sur l'axe Y
     *
     * @see Game#
     */
    private int jackY;

    /**
     * Le "pas" du rebond de Jack, initialisé en négatif pour qu'il commence par monter.
     *
     * @see Game#
     */
    private int dir_y = -15;

    /**
     * La hauteur du saut de Jack.
     *
     * @see Game#
     */
    private int jumpHeight;

    /**
     * Le point de départ du saut de Jack, sur l'axe X
     *
     * @see Game#
     */
    private int startingPointJumpY;

    /**
     * Le boolean qui indique si Jack est en train de descendre.
     *
     * @see Game#
     */
    private boolean isGoingDown;


    // LE CAPTEUR SENSOR
    /**
     * La valeur du sensor sur l'axe X. ?
     *
     * @see Game#
     */
    private float sensorX;
    /**
     * Le timer. ?
     *
     * @see Game#
     */
    //private Timer timer;
    /**
     * Le handler. ?
     *
     * @see Game#
     */
    private Handler handler;
    /**
     * Le SensorManager ?
     *
     * @see Game#
     */
    private SensorManager sensorManager;
    /**
     * L'accelerometer. ?
     *
     * @see Game#
     */
    private Sensor accelerometer;

    /**
     * L'instant où le Sensor à été mis à jour pour la dernière fois. ?
     *
     * @see Game#
     */
    private long lastSensorUpdateTime = 0;





    /**
     * Méthode appelée au chargement de l'activité Main.
     * <p>Bloque le auto-lock de l'écran. Charge la vue game.</p>
     * <p>Initialise le score du joueur à 0.</p>
     * <p>Charge les paramètres du sensor.</p>
     * <p>Récupère les mesures de la zone de jeu calculées dans l'activité précédente, et on stocke les valeurs dans les
     * variables correspondantes : maxX, minY, maxY.</p>
     * <p>Initialise les plateformes, générées aléatoirement, en plus d'une ligne de plateforme définie pour tout
     * commencement du jeu.</p>
     * <p><Initialise Jack, son image, sa position sur l'écran, sa hauteur de saut./p>
     * <p>Charge le Canvas.</p>
     * <p>Puis immédiatement invalide le Canvas, ce qui déclenche l'appel de la fonction onDraw()??</p>
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.game);

        score = 0;

        // IMAGES
        backgroundImg = BitmapFactory.decodeResource(getResources(), R.drawable.fond);
        plateformImg = BitmapFactory.decodeResource(getResources(), R.drawable.plateforme);
        // Image de Jack
        idImgJack = getIntent().getStringExtra("id");
        if (idImgJack.equals("verdillon")) {
            jackImg = BitmapFactory.decodeResource(getResources(), R.drawable.verdillon);
        }
        if (idImgJack.equals("deruyter")) {
            jackImg = BitmapFactory.decodeResource(getResources(), R.drawable.deruyter);
        }

        // SENSOR
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //accelerometer = sensorManager.getDefaultSensor(TYPE_GRAVITY);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);


        // DIMENSIONS DE LA ZONE DE JEU
        // Pour l'axe x, on prend la largeur de l'écran
        Display currentDisplay = getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        currentDisplay.getSize(screenSize);
        maxX = screenSize.x;

        // Pour l'axe y, récupéré depuis l'activité précédente
        minY = getIntent().getIntExtra("yMinimum", 0);
        maxY = getIntent().getIntExtra("yMaximum", 0);


        // INITIALISATION DES PLATEFORMES
        // On enlève 2 fois la hauteur d'une plateforme, 2x30, au maxY,
        // pour laisser la place pour la ligne de plateforme du départ
        plateforms = generatePlateformsPositions(5, minX, minY, maxX, maxY - 360);

        // Ligne de plateformes de départ
        // On calcule le nombre de plateformes qui rentre dans la largeur de l'écran
        int nbPlatformOnStart = maxX / 200;
        int next = 0;
        // Pour le nombre de plateformes calculé plus haut, + 1 (car on part de 0)
        for (int j = 0; j <= nbPlatformOnStart; j++) {
            // On enlève 300 au maxY, valeur choisie arbitrairement en testant, pour s'assurer que
            // la ligne de plateforme ne sera pas visuellement cachée par la bar de contrôle android
            // qui existe sur certains mobiles/tablettes
            plateforms.add(new Plateform(minX + next, maxY - 300));
            next = next + 200;
        }


        // INITIALISATION DE JACK
        // Position de Jack
        // On place jack au centre de l'écran horizontalement
        // Jack fait 64 de large, donc on décale de 64/2 = 32 * 2 pour que son milieu soit au milieu de l'écran
        jackX = (maxX / 2) - 64;
        // Et à 360 plus haut que le bas de la zone de jeu
        jackY = maxY - 360;
        // Ce Y est le point de départ de notre saut
        startingPointJumpY = jackY;
        // On calcule la hauteur du saut de Jack
        int layoutHeight = getIntent().getIntExtra("layoutHeight", 0);
        jumpHeight = layoutHeight / 2;


        // CANVAS
        canvas = new Game.CanvasView(Game.this);
        setContentView(canvas);

        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                canvas.invalidate();
            }
        };

    }



    /**
     * Class CanvasView.
     * <p>C'est ici que s'affiche les éléments.</p>
     * <p>C'est ici que s'affiche les éléments.</p>
     * <p>C'est ici que s'affiche les éléments.</p>
     * <p>C'est ici que s'affiche les éléments.</p>
     * <p>C'est ici que s'affiche les éléments.</p>
     * <p>C'est ici que s'affiche les éléments.</p>
     *
     * @see Game#moveJackX()
     */
    private class CanvasView extends View {
        /**
         * Le stylo Pen qui dessine les éléments.
         *
         * @see CanvasView#
         */
        private Paint Pen;

        /**
         * Constructeur CanvasView.
         *
         * @param context
         *
         * @see Plateform#
         *
         */
        public CanvasView(Context context) {
            super(context);
            //setFocusable(true);
            Pen = new Paint();
        }

        /**
         * Méthode appelée à chaque fois que le Canvas est invalidé.
         * <p>Dessigne le fond.</p>
         * <p>Fait bouger Jack de sa valeur dir_y.</p>
         * <p>Gère la position Y de Jack : son saut, son rebond sur les plateformes,
         * via la fonction characterMeetsPlatform().</p>
         * <p>Appelle la fonction hasJumpedOnPlateform() gérant le fait que les éléments "décsendent" visuellement</p>
         * <p>Verifie que le joeur n'ai pas perdu en faisant tomber Jack hors de l'écran en bas.</p>
         * <p>Dessine Jack en fonction des vérifications précédentes.</p>
         * <p>Invalide le Canvas pour relancer immédiatement onDraw()</p>
         *
         * @param screen
         *
         */
        public void onDraw(Canvas screen) {
            // DESSIN DU FOND
            screen.drawBitmap(backgroundImg, 0, 0, Pen);

            jackY = jackY + dir_y;

            // QUAND JACK EST EN TRAIN DE MONTER
            // Si jack a atteint sa hauteur de saut maximale
            // (sa hauteur de saut, ENLEVEE à son point de départ, car on va vers le haut)
            // On veut qu'il redescende, donc on lui ajoute 15
            if (jackY <= startingPointJumpY - jumpHeight) {
                dir_y = 15;
                isGoingDown = true;
            }

            // QUAND JACK EST EN TRAIN DE DESCENDRE
            // Alors on vérifie qu'il ne rencontre pas de plateforme sur son passage
            if (isGoingDown == true) {
                // On vérifie que que Jack rencontre une plateforme
                int newStartingPointJumpY = characterMeetsPlatform(jackX, jackY);

                // Si la valeur retournée est différente de -10, cela veut dire qu'une plateforme a été rencontrée
                // Et que la valeur Y de cette plateforme a été retournée
                if (newStartingPointJumpY != -10) {
                    // On relance un nouveau saut à partir de ce nouveau point (+ 2*100, la hauteur de Jack)
                    // Ce qui donne l'impression qu'il rebondit sur la platforme
                    startingPointJumpY = newStartingPointJumpY - 200;

                    // On veut maintenant que Jack remonte, donc on lui enlève 15
                    dir_y = -15;
                    isGoingDown = false;

                    //On appelle la fonction hasJumpedOnPlateform, qui va faire défiler le fond et
                    // gérer la création de nouvelles plateformes
                    //En lui passant en paramètre, le y de la plaeform sur laquelle à été fait le rebond
                    hasJumpedOnPlateform(newStartingPointJumpY);
                }
            }

            // SI JACK EST TOMBE HORS DE L'ECRAN EN BAS
            if (jackY >= maxY) {
                isGameOver();
            }

            // DESSIN DE JACK
            screen.drawBitmap(jackImg, jackX, jackY, Pen);

            // DESSIN DES PLATEFORMES
            for (int k = 0; k < plateforms.size(); k++) {
                Plateform currentItem = plateforms.get(k);
                screen.drawBitmap(plateformImg, currentItem.getPlateformX(), currentItem.getPlateformY(), Pen);
            }

            invalidate();
        }

    }






    /**
     * Méthode appelée pour générer aléatoirement des coordonnées de Plateform
     *
     * <p>Génère des nombres randomX et randomY.</p>
     * <p>Parcours la liste de plateforms locale. Vérifie s'il n'y à pas déj une plateforme avec des coordonnées en commun</p>
     *
     * @param nbPlatforms
     *            Le nombre de plateforme à générer.
     * @param minX
     *            La coordonnée X minimum de la zone où générer les plateformes.
     * @param minY
     *            La coordonnée Y minimum de la zone où générer les plateformes.
     * @param maxX
     *            La coordonnée X maximum de la zone où générer les plateformes.
     * @param maxY
     *            La coordonnée Y maximum de la zone où générer les plateformes.
     *
     * @return listeProvisoire
     *            La liste des Plateform générées.
     *
     */

    public List<Plateform> generatePlateformsPositions(int nbPlatforms, int minX, int minY, int maxX, int maxY) {
        int i = 0;
        // Nouvelle liste locale dans la zone ou l'on veut créer les plateformes
        List<Plateform> listeProvisoire = new ArrayList<>();

        while (i <= nbPlatforms - 1) {
            boolean invalidCoordonate = false;
            // On génère un nombre aléatoire entre les valeurs des coordonées (largeur et hauteur) de la zone
            // où l'on veut rajouter des plateformes
            // Moins la longueur (100*2) et épaisseur(35*2) d'une plateforme
            // Pour eviter qu'une plateforme soit générée au bord de l'écran, et donc ne soit pas visible
            Random randomCoordonate = new Random();
            // Par défaut, nextInt retourne un nombre aléatoire entre 0 et le nombre qu'on lui passe en paramètre
            // Avec 0 inclus, et le nombre en paramètre exclus
            // Donc, pour gérérer un nombre entre a et b, les deux inclus et a différent de 0
            int randomX = randomCoordonate.nextInt((maxX - 200) - minX + 1) + minX;
            int randomY = randomCoordonate.nextInt((maxY - 60) - minY + 1) + minY;

            // Si la liste provisoire n'est pas vide
            if (listeProvisoire.isEmpty() == false) {
                // On parcours la liste provisoire
                // Pour tous les élements de liste
                for (int j = 0; j < listeProvisoire.size(); j++) {
                    // On récupère la plateforme courante dans la liste (comme si listeProvisoire[j] sur un tableau)
                    Plateform item = listeProvisoire.get(j);
                    // On vérifie que les coordonnées générées ne soient pas communs à une plateforme déjà existante
                    // (-200), (+200), (-60), (+60) sont la pour être sur que les plateformes ne se chevauchent pas
                    // Car largeur plateform = 100 et hauteur plateforme = 30
                    if (randomX >= item.getPlateformX() + 200 && randomX <= item.getPlateformX() - 200
                            && randomY >= item.getPlateformY() + 60 && randomX <= item.getPlateformY() - 60) {
                        invalidCoordonate = true;
                        // On a trouvé une plateforme qui est déjà au même endroit, donc on sort de la boucle for
                        break;
                    }
                }

                //Si on a pas trouvé de point de contact avec une autre plateform
                if (invalidCoordonate == false) {
                    //Alors on ajoute la nouvelle plateforme avec les coordonnées générés, au tableau plateforms
                    listeProvisoire.add(new Plateform(randomX, randomY));
                    // Et on passe à la case suivante du tableau
                    i++;
                }
                //Sinon, on reste dans la même case et on va générer de nouveaux coordonnées

            }

            // Si la liste provisoire est vide
            else {
                // On peut ajouter directement les valeurs générées
                listeProvisoire.add(new Plateform(randomX, randomY));
                // Et on peut passer à la génération de la prochaine plateforme
                i++;
            }

        }
        // On ordonne la liste selon les coordonnées Y


        return listeProvisoire;

    }


    /**
     * Méthode appelée si Jack a rebondi sur une plateforme,
     *
     * <p>Parcours la liste plateforms</p>
     * <p>Vérifie si la position de Jack coincide avec une plateforme</p>
     * <p>Si oui, change valeur du point de départ du saut newStartingPointForJump et ajoute 1 au score.</p>
     *
     * @param Xjack
     *            La position de Jack sur l'axe X.
     * @param Yjack
     *            La position de Jack sur l'axe Y.
     *
     * @return newStartingPointForJump
     *            La coordonnée Y du nouveau point de départ du saut de Jack.
     *
     */
    public int characterMeetsPlatform(int Xjack, int Yjack) {
        int newStartingPointForJump = -10;
        for (int k = 0; k < plateforms.size(); k++) {
            Plateform currentPlatform = plateforms.get(k);
            int xPlatform = currentPlatform.getPlateformX();
            int yPlatform = currentPlatform.getPlateformY();
            //On vérifie si le point en bas à gauche de jack (Y+100)
            if ((Yjack + 100) >= yPlatform && (Yjack + 100) < (yPlatform + 15)
                    && Xjack >= (xPlatform - 64) && Xjack <= (xPlatform + 136)                     ) {
                // On considère que jack rebondi si il a au moins la moitié de son corps sur la platform,
                // d'ou - 64 à gauche et +200 - 64 à droite (valeurs de taille doublées)
                newStartingPointForJump = yPlatform;

                // On ajoute 1 au score
                score = score + 1;
                System.out.println(score);
                return newStartingPointForJump;
            }
        }
        return newStartingPointForJump;
    }



    /**
     * Méthode appelée à chaque onDraw, pour vérifier si Jack touche une plateforme
     *
     * <p>Calcule la différence heightDif entre le bas de la zone de jeu, et la coordonnée de la Plateform sur laquelle Jack a rebondi.</p>
     * <p>Parcours la liste plateforms</p>
     * <p>Si la Plateform courante est en dessous de celle du rebond, elle est effacée, et ajoute 1 au nombre de Plateform effacées.</p>
     * <p>Si la Plateform courante est au dessus, on descend sa coordonnée Y de la différence heightDif.</p>
     * <p>Applique la différence à Jack.</p>
     * <p>Appelle generatePlateformsPositions() avec de nouveau paramètre nbPlateforms et de zone.</p>
     *
     * @param YplatformJumped
     *            La coordonnée Y de la Plateform sur laquelle Jack a rebondi.
     *
     */
    public void hasJumpedOnPlateform(int YplatformJumped) {
        //Ligne de base définie à 360
        //Hauteur entre la ligne de base (maxY-360) et la position y de la plateform sur laquelle le personnage a rebondi
        int heightDif = maxY - YplatformJumped + 360;

        int nbPlateformsDeleted = 0;

        //On veut ensuite appliquer cette différence à toutes les plateformes visibles, et donc donner l'impression de monter
        //Et effacer dans le tableau plateforms, les plateformes qui sortent en bas de l'écran
        /*for (int k = 0; k < plateforms.size(); k++) {
            Plateform currentPlatform = plateforms.get(k);
            //Si le y de la plateform courante est supérieur ou égal (donc qu'elle est visuellement en dessous)
            // au y de la plateform sur laquelle jack a sauté
            // alors on supprime la plateform de la liste
            if (currentPlatform.getPlateformY() >= YplatformJumped) {
                plateforms.remove(k);
                nbPlateformsDeleted = nbPlateformsDeleted+1;
                System.out.println(nbPlateformsDeleted);
            }
            else {
                // Sinon, si la plateform courante est visuellement au dessus,
                // On lui ajoute la valeur de la différence heightDiff pour donner l'impression de la faire descendre
                currentPlatform.setPlateformY(currentPlatform.getPlateformY() + heightDif);
            }
        }*/
        // Et on applique la différence à jack également
        //startingPointJumpY = startingPointJumpY + heightDif -360 ;

        // Puis on veut générer de nouvelles plateformes sur toute la zone de jeu, avec un nbPlatforms fixe
        plateforms = generatePlateformsPositions(5, minX, minY, maxX, maxY-360);
        // Puis on veut générer de nouvelles plateformes sur la zone en haut de l'écran qui vient d'apparaitre
        //plateforms = generatePlateformsPositions(nbPlateformsDeleted, minX, minY, maxX, minY+heightDif-360);

    }



    /**
     * Méthode appelée lorsque l'utilisateur perd (lorsqu'il fait tomber Jack hors de l'écran en bas.
     *
     * <p>Créé un intent. Lui ajoute la valeur du score.</p>
     * <p>Démarre l'activité Result, en lui passant l'Intent result </p>
     *
     *
     */
    public void isGameOver() {
        Intent result = new Intent(this, Result.class);

        // On passe la valeur du score dans notre intent
        result.putExtra("score", score);

        // On démarre la nouvelle activité Result
        startActivity(result);
    }




    /**
     * Méthode appelée à chaque changement sur le Sensor.
     * <p>Récupère l'évènement de changement sur le Sensor.</p>
     * <p>Définie un intervalle de temps.</p>
     * <p>Selon cet intervalle, récupère les valeurs du sensor et appelle la fonction moveJack</p>
     *
     * @param sensorEvent
     * @see Game#moveJackX()
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            long currentTime = System.currentTimeMillis();

            if ((currentTime - lastSensorUpdateTime) > 5) {
                lastSensorUpdateTime = currentTime;
                sensorX = sensorEvent.values[0];
                moveJackX();
            }

        }

    }

    /**
     * Méthode qui doit être implémentée, car la class Game implémente SensorEventListener.
     *
     * @param sensor
     * @param i
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }


    /**
     * Méthode appelée à chaque changement sur le Sensor, dans l'intervalle définie dans onSensorChanged().
     * <p>Définie des paliers sur les valeurs de sensorX.</p>
     * <p>Modifie la valeur de jackX</p>
     *
     *
     */
    public void moveJackX() {
        // Paliers en fonction de l'inclinaison que donne l'user sur le device
        // Plus l'inclinaison est forte, plus Jack se déplacera d'une plus grande distance
        if (sensorX > 1 && sensorX < 3) {
            if (jackX > 5) {
                jackX -= 5;
            }
        }

        if (sensorX < -1 && sensorX > -3) {
            if (jackX < maxX - 150) {
                jackX += 5;
            }
        }

        if (sensorX < -3) {
            if (jackX < maxX - 150) {
                jackX += 15;
            } else {
                jackX = maxX - 150;
            }
        }

        if (sensorX > 3) {
            if (jackX > 15) {
                jackX -= 15;
            } else {
                jackX = 5;
            }
        }

        handler.sendEmptyMessage(0);

    }


}
