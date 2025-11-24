import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Archivator {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Použití: java FileToBits <výstupní_soubor> <vstup1> <vstup2> ...");
            return;
        }

        String outputFile = args[0];

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {

            // FÁZE 1: Zápis počtu souborů
            System.out.println("PRVNÍ FÁZE - ZÁPIS POČTU SOUBORŮ");
            int numberOfFiles = args.length - 1; // -1, protože 0 je cesta k outputFile
            String numberOfFilesInBinary = String.format("%8s", Integer.toBinaryString(numberOfFiles)).replace(' ', '0');
            fos.write(numberOfFilesInBinary.getBytes());
            System.out.println("Počet souborů: " + numberOfFiles);
            System.out.println("Hotovo!\n");

            // FÁZE 2: Pro každý soubor zapsat DÉLKU NÁZVU, NÁZEV, DÉLKU SOUBORU a pak OBSAH
            System.out.println("DRUHÁ FÁZE - ZÁPIS DÉLKY NÁZVU, NÁZVU, DÉLKY A OBSAHU KAŽDÉHO SOUBORU");
            for (int i = 1; i < args.length; i++) { // Začínám na 1, protože 0 je cesta k outputFile
                String inputFile = args[i];

                // Zapsat délku názvu souboru
                fos.write(String.format("%8s", Integer.toBinaryString(inputFile.length())).replace(' ', '0').getBytes());

                // Zapsat název souboru
                for (int j = 0; j < inputFile.length(); j++) {
                    char c = inputFile.charAt(j);
                    String charInBinary = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
                    fos.write(charInBinary.getBytes());
                }

                //Zapsat délku souboru
                int size = 0;
                try (FileInputStream fis = new FileInputStream(inputFile)) {
                    while (fis.read() != -1) {
                        size++;
                    }
                }
                String sizeInBinary = String.format("%32s", Integer.toBinaryString(size)).replace(' ', '0');
                fos.write(sizeInBinary.getBytes());
                System.out.println("Soubor: " + inputFile + " --> délka: " + (size) + " znaků");

                // Zapsat obsah souboru
                try (FileInputStream fis = new FileInputStream(inputFile)) {
                    int byteRead;
                    while ((byteRead = fis.read()) != -1) {
                        String byteInBinary = String.format("%8s", Integer.toBinaryString(byteRead)).replace(' ', '0');
                        fos.write(byteInBinary.getBytes());
                    }
                }
                System.out.println("Obsah souboru " + inputFile + " zapsán.");
            }
            System.out.println("Hotovo!\n");

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Archiv vytvořen v souboru: " + outputFile);
    }
}