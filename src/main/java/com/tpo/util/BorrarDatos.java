package com.tpo.util;


public class BorrarDatos {
    public static void main(String[] args) {
        try {
            DataLoader.vaciarSQL();
            DataLoader.vaciarMongo();
            DataLoader.vaciarRedis();
            DataLoader.vaciarCassandra();
            System.out.println("Todas las Bases de Datos vaciadas.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}
