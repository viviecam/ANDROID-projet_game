package com.example.bozonpee.canvasaccelerometer;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * <b>Main est la classe qui définie l'activité au lancement de l'application.</b>
 * <p>
 * Elle effectue quatre actions :
 * <ul>
 * <li>Elle charge la vue main.</li>
 * <li>Elle récupère les coordonnées Y de la zone de jeu..</li>
 * <li>En fonction du bouton sur lequel l'utilisateur clique, elle génère une information qui dicte quel icône utiliser pour le Jack.</li>
 * <li>Enfin, elle transmet les informations sur les coordonnées de la zone de jeu, et sur l'icîne de Jack, à l'activité Game</li>
 * </ul>
 * </p>
 *
 * @author Elisa BOZON & Camille VIVIER
 */


public class Main extends AppCompatActivity {

    /**
     * Méthode appelée au chargement de l'activité Main.
     * <p>Charge la vue main.</p>
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    /**
     * Méthode appelée au clic sur le bouton-image Verdillon.
     * <p>
     * Appelle la fonction calculateGameArea qui récupère les coordonnées Y de la zone de jeu.
     * Récupère l'Intent qui stocke ces coordonées dans l'Intent gameVerdillon.
     * Ajoute à l'Intent gameVerdillon, le paramètre de l'image de Jack.
     * Démarre l'activité Game, en lui passant l'Intent gameVerdillon qui contient les paramètres de la zone de jeu et de l'image de Jack
     *
     * @param view
     * @see Main#calculateGameArea()
     */
    public void startGameActivityVerdi(View view) {
        Intent gameVerdillon = calculateGameArea();
        gameVerdillon.putExtra("id", "verdillon");
        startActivity(gameVerdillon);
    }


    /**
     * Méthode appelée au clic sur le bouton-image Deruyter.
     * <p>
     * Appelle la fonction calculateGameArea qui récupère les coordonnées Y de la zone de jeu.
     * Récupère l'Intent qui stocke ces coordonées dans l'Intent gameDeruyter.
     * Ajoute à l'Intent gameDeruyter, le paramètre de l'image de Jack.
     * Démarre l'activité Game, en lui passant l'Intent gameVerdillon qui contient les paramètres de la zone de jeu et de l'image de Jack
     *
     * @param view
     * @see Main#calculateGameArea()
     */
    public void startGameActivityDeruyter(View view) {
        Intent gameDeruyter = calculateGameArea();
        gameDeruyter.putExtra("id", "deruyter");
        startActivity(gameDeruyter);
    }

    /**
     * Méthode qui calcule les coordonnées Ymin et Ymax de la zone de jeu.
     * <p>Créé un intent. Récupère la mainlayout.</p>
     * <p>Récupère le point de départ de la mainlayout avec getLocationInWindow ou getLocationOnScreen</p>
     * <p>Récupère la hauteur de la mainlayout.</p>
     * <p>Calcule le Ymax en ajoutant la hauteur de la mainLayout au Ymin</p>
     * <p>Stocke Ymin, Ymax, layoutHeight dans l'intent, et retourne l'intent.</p>
     *
     * @return L'Itent contenant les paramètres de la zone de jeu
     */
    public Intent calculateGameArea() {
        Intent intent = new Intent(this, Game.class);
        ConstraintLayout Layout = findViewById(R.id.mainlayout);

        int topLeft[] = new int[2];
        //Layout.getLocationOnScreen(topLeft);
        Layout.getLocationInWindow(topLeft);
        int layoutHeight = Layout.getHeight();

        int yMin = topLeft[1];
        int yMax = topLeft[1] + layoutHeight;

        intent.putExtra("yMinimum", yMin);
        intent.putExtra("yMaximum", yMax);
        intent.putExtra("layoutHeight", layoutHeight);

        return intent;
    }
}


