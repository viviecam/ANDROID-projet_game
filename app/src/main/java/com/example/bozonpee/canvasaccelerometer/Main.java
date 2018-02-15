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

import java.util.Timer;
import java.util.TimerTask;

import static android.hardware.Sensor.TYPE_GRAVITY;


public class Main extends AppCompatActivity implements SensorEventListener {
    //Initialisation de notre canvas
    private CanvasView canvas;

    //Initialisation de notre point qui sera dans le canevas
    private int circleRadius = 30; //Diamètre
    private float circleX; //Position sur l'axe X
    private float circleY; //Position sur l'axe Y


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
                run();
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    //Canvas
    private class CanvasView extends View {
        private Paint pen;

        Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.clouds);

        public CanvasView(Context context) {
            super(context);
            setFocusable(true);

            pen = new Paint();

        }

        public void onDraw(Canvas screen) {
            pen.setStyle(Paint.Style.FILL);
            pen.setAntiAlias(true);
            pen.setTextSize(30f);


            //Canvas background
            //screen.drawColor(Color.MAGENTA);
            screen.drawBitmap(background, 0, 0, pen);

            //screen.drawPicture(background);

            //Génération du point
            screen.drawCircle(circleX, circleY, circleRadius, pen);

            screen.drawLine(600, 600, 800, 600, pen);

        }

    }

    //Fonction de génération du personnage (point)
    //public void generateCharacter() {

    //}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //accelerometer = sensorManager.getDefaultSensor(TYPE_GRAVITY  );
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int screenWidth = size.x;
        int screenHeight = size.y;

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

    public void run() {
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


