package pansharpener.gui;

import javax.swing.UnsupportedLookAndFeelException;

public class Main {
    public static void main(String[] args) {
        try {
            GUI gui = new GUI();
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException |
                InstantiationException | IllegalAccessException e) {
            System.out.println("Unable to start program");
        }
    }
}
