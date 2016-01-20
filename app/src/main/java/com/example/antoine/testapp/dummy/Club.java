package com.example.antoine.testapp.dummy;

/**
 * Created by Antoine on 18/01/2016.
 */
public class Club {

        public final String id;
        public final String nom;
        public final String details;
        public final String logo = "";



        public Club(String id, String content, String details) {
            this.id = id;
            this.nom = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return nom;
        }
}
