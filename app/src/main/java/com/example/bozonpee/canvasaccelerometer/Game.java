package com.example.bozonpee.canvasaccelerometer;

import android.content.Context;
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
import android.os.PowerManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;

/**
 * Created by Camille on 19/03/2018.
 */

public class Game extends AppCompatActivity implements SensorEventListener {

    //Valeurs de la taille de l'écran courant -> à supprimer avant rendu
    //private int screenWidth = 300;
    //private int screenHeight;

    //Canvas
    private Game.CanvasView canvas;

    //Liste de Plateformes
    private List<Plateform> plateforms;

    //Zone de jeu
    private int minX = 0; // car on part toujours du bord gauche de l'écran
    private int maxX;
    private int minY;
    private int maxY;

    //Nb de plateformes à générer
    private int nbPlatforms;

    //Mouvement de Jack
    Bitmap jack;
    private String perso;
    private int jackX;
    private int jackY;
    private float sensorX; // Horizontal
    private float sensorY;
    private float sensorZ;
    //"Pas" du rebond, initilialisé à -15 pour qu'il commence par monter
    private int dir_y = -15;
    //Hauteur du saut
    int jumpHeight;
    //Point de départ du saut
    int startingPointJumpY;
    //Booléen pour savoir si jack est, à un instant T, en train de monter ou de descendre
    boolean isGoingDown;

    //Sensor
    private Timer timer;
    private Handler handler;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private long lastSensorUpdateTime = 0;

    /*@Override
    protected void onResume() {
        super.onResume();
        PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");
        wakeLock.acquire();
    }*/

    /** AU CHARGEMENT DE L'ACTIVITE **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Bundle extras = getIntent().getExtras();
        // On charge la vue game
        setContentView(R.layout.game);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // On gère les paramètres du senso
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //accelerometer = sensorManager.getDefaultSensor(TYPE_GRAVITY);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);


        /** DIMENSIONS DE LA ZONE DE JEU **/
        // Pour l'axe x, on prend la largeur de l'écran
        // On récupère les dimensions de l'écran sur lequel on est
        Display currentDisplay = getWindowManager().getDefaultDisplay();
        // Que l'on stocke sous forme d'un point
        Point screenSize = new Point();
        // On récupère les coordonnées
        currentDisplay.getSize(screenSize);
        // On stocke la largeur de l'écran comme notre valeur maxX
        maxX = screenSize.x;
        //screenHeight = screenSize.y;
        //System.out.println("Taille " + screenHeight);

        // Pour l'axe y, récupéré depuis l'activité précédente
        minY = getIntent().getIntExtra("yMinimum", 0);
        maxY = getIntent().getIntExtra("yMaximum", 0);
        //System.out.println("minX : " + minX + " et maxX : " + maxX);
        //System.out.println("minY : " + minY + " et maxY : " + maxY);

        /** INITIALISATION DES PLATEFORMES **/
        // On appelle la fonction pour générer les plateformes, en lui passant les paramètres nécéssaires
        // (les coordonnées de la zone à ne pas dépasser, et on enlève 2 fois la hauteur d'une plateforme 2x30
        // pour laisser la place pour la ligne de plateforme du départ)
        // Et on stocke le retour de cette fonction, dans notre liste de plateformes
        plateforms = generatePlateformsPositions(10, minX, minY, maxX, maxY-360);

        // Ligne de plateformes de départ
        // On calcule le nombre de plateformes qui rentre dans la largeur de l'écran
        int nbPlatformOnStart = maxX / 200;
        // On initialise la valeur qui sera rajoutée au X pour décaler chaque plateforme d'une longeur de plateforme
        int next = 0;
        // Pour le nombre de plateformes calculé plus haut, + 1 (car on part de 0)
        for (int j=0; j<=nbPlatformOnStart; j++ ) {
            // On ajoute une plateforme dans notre tableau de plateforme
            plateforms.add(new Plateform(minX + next, maxY-300));
            // On décale la plateforme suivante d'une longueur de plateforme
            next = next+ 200;
        }

        /** INITIALISATION DE JACK **/
        // On récupère l'id du personnage à utiliser
        perso = getIntent().getStringExtra("id");

        if (perso.equals("verdi")) {
            jack = BitmapFactory.decodeResource(getResources(), R.drawable.verdi);
        }
        if (perso.equals("deruyter")) {
            jack = BitmapFactory.decodeResource(getResources(), R.drawable.deruyter);
        }

        // On place jack au centre de l'écran horizontalement
        // Jack fait 64 de large, donc on décale de 64*2/2 = 32 pour que son milieu soit au milieu de l'écran
        jackX = (maxX / 2) - 64;
        // Et à 360 plus haut que le bas de la zone de jeu
        jackY = maxY-360;
        // On stocke cette valeur de Y comme valeur de base de notre saut
        startingPointJumpY = jackY;

        //Au lancement, "pas" pour le mouvement de Jack
        //dir_y = 15;
        //createAnimation();

        //Au lancement, calcul de la hauteur de saut de jack (moitié de la zone de jeu, en fonction de l'écran courant donc)
        int layoutHeight = getIntent().getIntExtra("layoutHeight", 0);
        jumpHeight = layoutHeight/2;
        System.out.println("Point de départ du saut : " + startingPointJumpY + " et jump height : " + jumpHeight);

        canvas = new Game.CanvasView(Game.this);
        setContentView(canvas);

        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                canvas.invalidate();
            }
        };

        //timer = new Timer();
        //timer.schedule(new TimerTask() {
        //@Override

        //}, 0, 100);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
//        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            long currentTime = System.currentTimeMillis();

            if ((currentTime - lastSensorUpdateTime) > 5) {
                lastSensorUpdateTime = currentTime;

                sensorX = sensorEvent.values[0];
                //Log.v("INFO","Sensor X" + sensorX);
                sensorY = sensorEvent.values[1];
                //Log.v("INFO","Sensor Y" + sensorY);
                sensorZ = sensorEvent.values[2];
                //Log.v("INFO","Sensor Z" + sensorZ);
                moveCharacter();
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    /** Canvas **/
    private class CanvasView extends View {
        private Paint characterPen;
        private Paint platformPen;

        // Chargement des images
        Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.fond);
        Bitmap plateformimg = BitmapFactory.decodeResource(getResources(), R.drawable.plateforme);

        public CanvasView(Context context) {
            super(context);
            setFocusable(true);

            characterPen = new Paint();
            platformPen = new Paint();

        }

        public void onDraw(Canvas screen) {
            //On définie les paramètres du "stylo" qui va dessiner
            characterPen.setStyle(Paint.Style.FILL);
            characterPen.setAntiAlias(true);
            //characterPen.setTextSize(30f);

            //Background que l'on place en haut à gauche
            screen.drawBitmap(background, 0, 0, characterPen);

            // A chaque appel de la fonction onDraw, on bouge jack de +/- 15
            jackY = jackY + dir_y;

            // Si jack a atteint le bas (le point de départ de son saut), on veut qu'il remonte, donc on lui enlève 15
            //if (jackY >= canvas.getHeight()-200){
            if (jackY >= startingPointJumpY ){
                dir_y = -15;
                isGoingDown = false;
            }

            //Si jack a atteint la motié de l'écran (sa hauteur de saut, ENLEVEE à son point de départ, car on va vers le haut)
            //on veut qu'il redescende, donc on lui ajoute 15
            if (jackY <= startingPointJumpY - jumpHeight) {
            //if (jackY <= canvas.getHeight()/2) {
                System.out.println("il redescend!");
                dir_y = 15;
                isGoingDown = true;
            }

            // Si jack est en train de descendre, alors on vérifie qu'il ne rencontre pas de plateforme sur son passage
            if (isGoingDown == true) {
                int newStartingPointJumpY = characterMeetsPlatform(jackX, jackY);
                //System.out.println(newStartingPointJumpY);
                // Si la valeur retournée est différente de -10, cela veut dire qu'une plateforme a été rencontrée
                // Et que la valeur Y de cette plateforme a été retournée
                if (newStartingPointJumpY != -10) {
                    //On relance un nouveau saut à partir de ce nouveau point, ce qui donne l'impression qu'il rebondit sur la platforme
                    startingPointJumpY = newStartingPointJumpY;
                    //On appelle la fonction hasJumpedOnPlateform, qui va faire défiler le fond et gérer la création de nouvelles plateformes
                    //En lui passant en paramètre, le y de la plaeform sur laquelle à été fait le rebond
                    //hasJumpedOnPlateform(newStartingPointJumpY);
                }
            }

            /*int newJackY = characterMeetsPlatform(jackX, jackY);

            if (newJackY != 0) {
                jackY = newJackY - dir_y;
                System.out.println("ok");
            }

            jackDescend(jackY);

            jackMonte(jackY);*/


            /*public void jackDescend(int jackY) {
                if (jackY >= canvas.getHeight()-50){
                    dir_y = 15;
                }
            }

            //fonction monté de jack
            public void jackMonte(int jackY) {
                if (jackY <= canvas.getHeight()/2) {
                    dir_y = -15;
                }
            }*/

            // Génération de jack
            screen.drawBitmap(jack, jackX, jackY, characterPen);

            //Génération des plateformes de dim 100(*2?) x 35(*2?)
            for (int k = 0; k < plateforms.size(); k++) {
                //On récupère la plateforme courante dans la liste (comme si listeProvisoire[j] sur un tableau)
                Plateform currentItem = plateforms.get(k);
                screen.drawBitmap(plateformimg, currentItem.getPlateformX() , currentItem.getPlateformY(), platformPen);
                //int x = currentItem.getPlateformX();
                //int y = currentItem.getPlateformY();
                //System.out.println(x + "-" + y);
            }
            //pour actualiser
            invalidate();

        }

    }

    /** FONCTION QUI VERIFIE SI JACK REBONDI SUR UNE PLATFORME**/
    public int characterMeetsPlatform(int Xjack, int Yjack) {
        int newStartingPointForJump = -10;
        for (int k = 0; k < plateforms.size(); k++) {
            Plateform currentPlatform = plateforms.get(k);
            //System.out.println("Y recup : " + Yjack);
            int xPlatform = currentPlatform.getPlateformX();
            int yPlatform = currentPlatform.getPlateformY();
            //System.out.println("Current platform : "+ xPlatform + " / " + yPlatform );
            //System.out.println("Jack : "+ Xjack + " / " + Yjack );
            //On vérifie si le point en bas à gauche de jack (Y+100)
            //if (currentPlatform.getPlateformY() == Yjack+100
            if ((Yjack+100) >= yPlatform && (Yjack+100) < (yPlatform + 15) && Xjack >= (xPlatform-64) && Xjack <= (xPlatform +136)
            //if (yPlatform != (Yjack+100) && Xjack >= (xPlatform-1) && Xjack <= (xPlatform +1)
            ) {
            //if (yPlatform == Yjack+100 && Xjack >= xPlatform - 32 && Xjack <= xPlatform + 168 ) {
                // On considère que jack rebondi si il a au moins la moitié de son corps sur la platform,
                // d'ou -64*2 à gauche et +200 - 64*2 à droite
                //System.out.println("Le if marche !");
                System.out.println("Jack a rebondi sur la plateforme ayant pour X : " + xPlatform + " et pour Y : " + yPlatform);

                newStartingPointForJump = yPlatform-160;
                return newStartingPointForJump;
            }
        }
        //dir_y = -15;
        return newStartingPointForJump;
    }

    /** Fonction pour générer les coordonnées de départ de dessin des plateformes **/
    // On donne les limites de la zone de jeu
    public List<Plateform> generatePlateformsPositions(int nbPlatforms, int minX, int minY, int maxX, int maxY) {
        int i = 0;
        //Nouvelle liste dans la zone ou l'on veut créer les plateformes
        List<Plateform> listeProvisoire = new ArrayList<>();

        while (i <= nbPlatforms-1) {
            boolean invalidCoordonate = false;
            //Log.d("DEBUG","I =" + i);
            //On génère un nombre aléatoire entre les valeurs des coordonées (largeur et hauteur) de la zone
            //ou l'on veut rajouter des plateformes
            //Moins la longueur (100*2) et épaisseur(35*2) d'une plateforme
            //Pour eviter qu'une plateforme soit générée au bord de l'écran, et donc ne soit pas visible
            Random randomCoordonate = new Random();
            //Par défaut, nextInt retourne un nombre aléatoire entre 0 et le nombre qu'on lui passe en paramètre
            //Avec 0 inclus, et le nombre en paramètre exclus
            //Donc, pour gérérer un nombre entre a et b, les deux inclus et a différent de 0
            int randomX = randomCoordonate.nextInt((maxX - 200) - minX + 1) + minX;
            int randomY = randomCoordonate.nextInt((maxY - 70) - minY + 1) + minY;

            //Si la liste provisoire n'est pas vide
            if (listeProvisoire.isEmpty() == false) {
                //On parcours la liste provisoire
                //Pour tous les élements de liste
                for (int j = 0; j < listeProvisoire.size(); j++) {
                    //On récupère la plateforme courante dans la liste (comme si listeProvisoire[j] sur un tableau)
                    Plateform item = listeProvisoire.get(j);
                    //On vérifie que les coordonnées générées ne soient pas communs à une plateforme déjà existante
                    // (-200), (+200), (-70), (+70) sont la pour être sur que les plateformes ne se chevauchent pas
                    // Reminder : le x et le y de chaque plateforme correspond à son point de départ en haut à gauche
                    if (randomX >= item.getPlateformX() + 200 && randomX <= item.getPlateformX() - 200
                            && randomY >= item.getPlateformY() + 70 && randomX <= item.getPlateformY() - 70) {
                        System.out.println("trouvé");
                        invalidCoordonate = true;
                        //On a trouvé une plateforme qui est déjà au même endroit, donc on sort de la boucle for
                        break;
                    }
                }

                //Si on a pas trouvé de point de contact avec une autre plateform
                if (invalidCoordonate == false) {
                    //Alors on ajoute la nouvelle plateforme avec les coordonnées générés, au tableau plateforms
                /*plateforms[i].setPlateformX(randomX);
                plateforms[i].setPlateformY(randomY);*/
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



    public void hasJumpedOnPlateform(int YplatformJumped){
        //Ligne de base définie à 360
        //Récupérer la hauteur entre le bas de l'écran (ou la ligne de base!) et la position y de la plateform sur laquelle le
        //personnage a rebondi : H
        int heightDif = maxY - YplatformJumped + 360;

        int nbPlateformsDeleted = 0;

        //On veut ensuite appliquer cette différence à toutes les plateformes visibles, et donc donner l'impression de monter
        //Et effacer dans le tableau plateforms, les plateformes qui sortent en bas de l'écran
        for (int k = 0; k < plateforms.size(); k++) {
            //On récupère la plateforme courante dans la liste (comme si listeProvisoire[j] sur un tableau)
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
        }
        // et on applique la différence à jack également
        //startingPointJumpY = startingPointJumpY + heightDif -360 ;

        // Puis on veut générer de nouvelles plateformes sur la zone en haut de l'écran qui vient d'apparaitre
        plateforms = generatePlateformsPositions(nbPlateformsDeleted, minX, minY, maxX, minY+heightDif-360);

    }



    /** Fonction pour faire bouger le personnage avec le sensor **/
    public void moveCharacter() {
        // Paliers en fonction de l'inclinaison que donne l'user sur le device
        if (sensorX > 1 && sensorX < 3) {
            if (jackX > 5){
                jackX -= 5;
            }
        }

        if (sensorX < -1 && sensorX > -3) {
            if (jackX < maxX-150){
                jackX += 5;
            }
        }

        if (sensorX < -3) {
            if (jackX < maxX-150){
                jackX += 15;
            } else {
                jackX = maxX-150;
            }
        }

        if (sensorX > 3 ) {
            if (jackX > 15){
                jackX -= 15;
            } else {
                jackX = 5;
            }
        }

        handler.sendEmptyMessage(0);

    }


}
