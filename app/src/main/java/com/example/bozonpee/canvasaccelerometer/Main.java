package com.example.bozonpee.canvasaccelerometer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static android.hardware.Sensor.TYPE_GRAVITY;


public class Main extends AppCompatActivity implements SensorEventListener {
    //Valeurs de la taille de l'écran courant
    private int screenWidth;
    private int screenHeight;


    //Canvas
    private CanvasView canvas;

    //Point qui sera dans le canvas
    private int circleRadius = 30; //Diamètre
    private float circleX; //Position sur l'axe X
    private float circleY; //Position sur l'axe Y

    //Liste de Plateformes
    private List<Plateform> plateforms;

    //Paramètres à prendre en compte pour la génération aléatoire des plateformes
    private int minX = 0;
    private int minY = 0;
    private int maxX;
    private int maxY;
    private int nbPlateforms;

    private Timer timer;
    private Handler handler;


    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float sensorX;
    private float sensorY;
    private float sensorZ;
    private long lastSensorUpdateTime = 0;





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

    //Canvas
    private class CanvasView extends View {
        private Paint characterPen;
        private Paint plateformPen;

        Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.clouds);
        Bitmap plateformimg = BitmapFactory.decodeResource(getResources(), R.drawable.trampoline);

        public CanvasView(Context context) {
            super(context);
            setFocusable(true);

            characterPen = new Paint();
            plateformPen = new Paint();

        }

        public void onDraw(Canvas screen) {
            //On définie les paramètres du "stylo" qui va dessiner le personnage
            characterPen.setStyle(Paint.Style.FILL);
            characterPen.setAntiAlias(true);
            characterPen.setTextSize(30f);

            //On définie les paramètres du "stylo" qui va dessiner les plateformes
            //plateformPen.setStyle(Paint.Style.FILL);

            //Background que l'on place en haut à gauche
            screen.drawBitmap(background, 0, 0, characterPen);

            //Génération du point
            screen.drawCircle(circleX, circleY, circleRadius, characterPen);


            //Génération des plateformes de dim 100x40
            //for (int k = 0; k <= nbPlateforms-1; k++) {
              //  screen.drawBitmap(plateformimg, plateforms[k].getPlateformX() , plateforms[k].getPlateformY(), plateformPen);
            //}

            //screen.drawLine(600, 600, 800, 600, pen);

        }

    }

    //Fonction de génération du personnage (point)
    //public void generateCharacter() {

    //}

    //Au lancement de l'application
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //accelerometer = sensorManager.getDefaultSensor(TYPE_GRAVITY  );
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        //On récupère les dimensions de l'écran sur lequel on est
        Display currentDisplay = getWindowManager().getDefaultDisplay();
        //Que l'on stocke sous forme d'un point
        Point screenSize = new Point();
        //On récupère les coordonnées
        currentDisplay.getSize(screenSize);

        //On stocke séparement la largeur et la hauteur de l'écran
        screenWidth = screenSize.x;
        screenHeight = screenSize.y;

        //On appelle la fonction pour générer les plateformes, en lui passant les paramètres nécéssaires
        //Et on stocke le retour de cette fonction, dans notre liste de plateformes
        plateforms = generatePlateformsPositions(10, 0, 0, screenWidth, screenHeight);

        //On place le personnage (point) au center de l'écran
        circleX = screenWidth / 2 - circleRadius;
        circleY = screenHeight / 2 - circleRadius;

        canvas = new CanvasView(Main.this);
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

    //Fonction pour générer les coordonnées de départ de dessin des plateformes
    public List<Plateform> generatePlateformsPositions(int nbPlateforms, int minX, int minY, int maxX, int maxY) {
        int i = 0;
        //Nouvelle liste dans la zone ou l'on veut créer les plateformes
        List<Plateform> listeProvisoire = new ArrayList<>();

        while (i <= nbPlateforms-1) {
            boolean invalidCoordonate = false;

            //On génère un nombre aléatoire entre les valeurs des coordonées (largeur et hauteur) de la zone
            //ou l'on veut rajouter des plateformes
            //Moins la longueur (100) et épaisseur(40) d'une plateforme
            //Pour eviter qu'une plateforme soit générée au bord de l'écran, et donc ne soit pas visible
            Random randomCoordonate = new Random();
            //Par défaut, nextInt retourne un nombre aléatoire entre 0 et le nombre qu'on lui passe en paramètre
            //Avec 0 inclus, et le nombre en paramètre exclus
            //Donc, pour gérérer un nombre entre a et b, les deux inclus et a différent de 0
            int randomX = randomCoordonate.nextInt((maxX - 99) - minX + 1) + minX;
            int randomY = randomCoordonate.nextInt((maxY - 39) - minY + 1) + minY;

            //Si la liste provisoire n'est pas vide
            if (listeProvisoire.isEmpty() == false) {
                //On parcours la liste provisoire
                //Pour tous les élements de liste
                for (int j = 0; j < listeProvisoire.size(); j++) {
                    //On récupère la plateforme courante dans la liste (comme si listeProvisoire[j] sur un tableau)
                    Plateform item = listeProvisoire.get(j);
                    //On vérifie que les coordonnées générées ne soient pas communs à une plateforme déjà existante
                    // (-100), (+100), (-40), (+40) sont la pour être sur que les plateformes ne se chevauchent pas
                    // Reminder : le x et le y de chaque plateforme correspond à son point de départ en haut à gauche
                    if (randomX >= item.getPlateformX() - 100 && randomX <= item.getPlateformX() + 100
                            && randomY >= item.getPlateformY() - 40 && randomX <= item.getPlateformY() + 40) {
                        invalidCoordonate = true;
                        //On a trouvé une plateforme qui est déjà au même endroit, donc on sort de la boucle for
                        break;
                    }
                }

                //Si on a pas trouvé de point de contact avec une autre plateform
                if (invalidCoordonate = false) {
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

        return listeProvisoire;

    }

    //public void hasJumpedOnPlateform(){
    //  Récupérer la hauteur entre le bas de l'écran (ou la ligne de base!) et la position x de la plateform sur laquelle le
    //  personnage a rebondi : H
    //  Pour appliquer cette différence à toutes les plateformes visibles, et donc donner l'impression de monter

    //  Effacer dans le tableau plateforms, les coordonnées des plateformes qui sortent en bas de l'écran

    //  Générer de nouvelles plateformes sur la zone en haut de l'écran qui vient d'apparaitre
    // minY = 0
    // maxY = H

    //}

    //Fonction pour faire bouger le personnage
    public void moveCharacter() {
        if (sensorX > 1 && sensorX < 3) {
            circleX -= 5;
        }
        if (sensorX < -1 && sensorX > -3) {
            circleX += 5;
        }

        if (sensorX < -3 && sensorX > -15) {
            circleX += 15;
        }

        if (sensorX > 3 && sensorX < 15 ) {
            circleX -= 15;
        }


        /*if(sensorY < 0) {
            circleY -= 15;
        } else {
            circleY += 15;
        }*/
        //circleX--;
        handler.sendEmptyMessage(0);
    }
}


