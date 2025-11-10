import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileToBits {
    // Pomocná metoda: převede číslo (bajt) na 8bitový binární řetězec
    public static String toBinaryString(int value) {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 7; i >= 0; i--) {
            sb.append(((value >> i) & 1) == 1 ? '1' : '0');
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String[] digitSizes = new String[args.length - 2]; // Args.length - 2 kvůli outputFile a tempFile cestě, drží cifry čísel (pro 2 soubory budou dva Stringy, každý s počtem cifer čísla uvnitř filu);
        if (args.length < 3) {
            System.out.println("Použití: java FileToBits <výstupní_soubor> <dočasný soubor> <vstup1> <vstup2> ...");
            return;
        }

        String outputFile = args[0];
        String tempFile = args[1];

        // FÁZE 1: Zápis hodnot znaků v 8bitovém řetězci ze souborů do dočasného souboru
        try (FileOutputStream fosTemp = new FileOutputStream(tempFile)) {
            System.out.println("PRVNÍ FÁZE - VÝTAH HODNOT VŠECH ZNAKŮ");
            // Pro každý vstupní soubor
            for (int i = 2; i < args.length; i++) { // Začíná na 2, protože 0 je cesta k outputFile a 1 je cesta k tempFile
                String inputFile = args[i];

                try (FileInputStream fis = new FileInputStream(inputFile)) {
                    int byteRead;
                    while((byteRead = fis.read()) != -1) {
                        fosTemp.write((String.format("%8s", Integer.toBinaryString(byteRead)).replace(' ', '0')).getBytes());

                    }
                }
            }
            System.out.println("Hotovo!\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // FÁZE 2: Zápis počtu input souborů a poté délku (počet cifer) každého souboru
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            System.out.println("DRUHÁ FÁZE - ZÁPIS POČTU INPUT SOUBORŮ A POČTU ZNAKŮ V KAŽDÉM SOUBORU");
            // Kolik je souborů v 8bitovém binárním řetězci (2 soubory = "00000010")
            String numberOfFilesInBinary = String.format("%8s", Integer.toBinaryString(digitSizes.length)).replace(' ', '0');
            fos.write(numberOfFilesInBinary.getBytes());

            // Pro každý vstupní soubor
            for (int i = 2; i < args.length; i++) { // Začíná na 2, protože 0 je cesta k outputFile a 1 je cesta k tempFile
                String inputFile = args[i];


                // Zapisuje délku input souboru do output souboru
                try (FileInputStream fis = new FileInputStream(inputFile)) {
                    int size = 0;
                    while (fis.read() != -1) {
                        size++;
                    }
                    System.out.println("Soubor: " + inputFile+ " --> Počet znaků: " +size);
                    // Size * 8 proto, že využíváme 8bitový řetězec (soubor s tříciferným číslem --> size = 3, ale každá cifra se reprezentuje 8bitovým řetězcem, tedy 3 * 8 = 24)
                    digitSizes[i - 2] = String.format("%32s", Integer.toBinaryString(size*8)).replace(' ', '0');
                    fos.write(digitSizes[i - 2].getBytes());
                }
            }
            System.out.println("Hotovo!\n");

            // FÁZE 3: Spojení temp souboru a output souboru (veškeré informace z temp souboru se zapíšou do output souboru)
            try (FileInputStream fis = new FileInputStream(tempFile)) {
                System.out.println("TŘETÍ FÁZE - PŘEPIS HODNOT ZNAKŮ Z DOČASNÉHO SOUBORU DO TRVALÉHO");
                fis.transferTo(fos);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Hotovo! Spojený výstup je v souboru: " + outputFile);
    }
}