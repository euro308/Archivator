import java.io.*;
import java.util.Scanner;


public class ArchivacniSoftware {

    static class Archivator {
        public Archivator(String[] args) {
            if (new File(args[0]).exists()) {
                System.out.println("Soubor s názvem " + args[0] + " již existuje, vyberte, prosím, jiný název.");
                return;
            }
            File outputFile = new File(args[0]);

            try (
                    FileOutputStream fos = new FileOutputStream(args[0])) {

                // FÁZE 1: Zápis počtu souborů
                // System.out.println("PRVNÍ FÁZE - ZÁPIS POČTU SOUBORŮ");
                int numberOfFiles = args.length - 1; // -1, protože 0 je cesta k outputFile
                String numberOfFilesInBinary = String.format("%8s", Integer.toBinaryString(numberOfFiles)).replace(' ', '0');
                fos.write(numberOfFilesInBinary.getBytes());
                // System.out.println("Počet souborů: " + numberOfFiles);
                // System.out.println("Hotovo!\n");

                // FÁZE 2: Pro každý soubor zapsat DÉLKU NÁZVU, NÁZEV, DÉLKU SOUBORU a pak OBSAH
                // System.out.println("DRUHÁ FÁZE - ZÁPIS DÉLKY NÁZVU, NÁZVU, DÉLKY A OBSAHU KAŽDÉHO SOUBORU");
                for (int i = 1; i < args.length; i++) { // Začínám na 1, protože 0 je cesta k outputFile
                    String inputFile = args[i];
                    File file = new File(inputFile);

                    // Zapsat délku názvu souboru (POUZE NÁZVU, NE CELÉ CESTY)
                    fos.write(String.format("%8s", Integer.toBinaryString(file.getName().length())).replace(' ', '0').getBytes());

                    // Zapsat název souboru (POUZE NÁZEV, NE CELOU CESTU)
                    for (int j = 0; j < file.getName().length(); j++) {
                        char c = file.getName().charAt(j);
                        String chara = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
                        fos.write(chara.getBytes());
                    }

                    // Zapsat délku souboru
                    int size = Math.toIntExact(new File(inputFile).length()); // Délka souboru bez toho, abych ho musel celý číst
                    String sizeInBinary = String.format("%32s", Integer.toBinaryString(size)).replace(' ', '0');
                    fos.write(sizeInBinary.getBytes());
                    // System.out.println("Soubor: " + inputFile + " --> délka: " + size + " bajtů");

                    // Zapsat obsah souboru
                    try (FileInputStream fis = new FileInputStream(inputFile)) {
                        int byteRead;
                        while ((byteRead = fis.read()) != -1) {
                            String byteInBinary = String.format("%8s", Integer.toBinaryString(byteRead)).replace(' ', '0');
                            fos.write(byteInBinary.getBytes());
                        }
                    }
                    // System.out.println("Obsah souboru " + inputFile + " zapsán.");
                }
            } catch (
                    IOException e) {
                e.printStackTrace();
            }

            System.out.println("Archiv vytvořen v souboru: " + outputFile);
        }
    }

    static class Dearchivator {
        public Dearchivator(String archivedFile) {
            // Neexistuje archivovaný soubor
            if (!(new File(archivedFile).exists())) {
                System.out.println("Archivovaný soubor s názvem " + archivedFile + "  neexistuje, vyberte, prosím, jiný název.");
                return;
            }

            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(archivedFile))) {
                // Zjištění počtu souborů
                byte[] bufferFileCount = new byte[8]; // Čtení 8 znaků najednou
                int bytesRead = bis.read(bufferFileCount);
                if (bytesRead != 8) {
                    throw new IOException("Chyba při čtení počtu souborů!");
                }
                int fileCount = Integer.parseInt(new String(bufferFileCount), 2); // Převod na dekadické čislo

                // Čtení jednotlivých souborů
                for (int i = 0; i < fileCount; i++) {
                    // Délka názvu souboru
                    byte[] bufferNameLength = new byte[8];
                    bytesRead = bis.read(bufferNameLength);
                    if (bytesRead != 8) {
                        throw new IOException("Chyba při čtení délky názvu souboru!");
                    }
                    int fileNameSize = Integer.parseInt(new String(bufferNameLength), 2);

                    // Název souboru
                    byte[] bufferFileName = new byte[fileNameSize * 8]; // Délka názvu krát 8, protože jeden znak má 8 čísel v ASCII
                    bytesRead = bis.read(bufferFileName);
                    if (bytesRead != fileNameSize * 8) {
                        throw new IOException("Chyba při čtení názvu souboru!");
                    }
                    String binaryFileName = new String(bufferFileName);
                    StringBuilder fileName = new StringBuilder();
                    for (int j = 0; j < binaryFileName.length(); j += 8) { // Jedu po osmi, tedy po jednom písmenu
                        String chunk = binaryFileName.substring(j, j + 8);
                        fileName.append((char) Integer.parseInt(chunk, 2));
                    }

                    // Délka obsahu souboru
                    byte[] bufferFileSize = new byte[32];
                    bytesRead = bis.read(bufferFileSize);
                    if (bytesRead != 32) {
                        throw new IOException("Chyba při čtení délky souboru!");
                    }
                    int fileSize = Integer.parseInt(new String(bufferFileSize), 2);

                    // Obsah souboru
                    byte[] bufferFile = new byte[fileSize * 8]; // Délka názvu krát 8, protože jeden znak má 8 čísel v ASCII
                    bytesRead = bis.read(bufferFile);
                    if (bytesRead != fileSize * 8) {
                        throw new IOException("Chyba při čtení obsahu souboru!");
                    }
                    String binaryFileContent = new String(bufferFile);
                    StringBuilder fileContent = new StringBuilder();
                    for (int j = 0; j < binaryFileContent.length(); j += 8) { // Jedu po osmi, tedy po jednom písmenu
                        String chunk = binaryFileContent.substring(j, j + 8);
                        fileContent.append((char) Integer.parseInt(chunk, 2));
                    }

                    // Kontrola existence souboru
                    if (new File(fileName.toString()).exists()) {
                        System.out.println("Soubor " + fileName + " již existuje, chcete jej přeskočit, nebo přepsat? \n 1. Přeskočit \n 2. Přepsat");
                        System.out.print("Vaše volba: ");
                        Scanner sc = new Scanner(System.in);
                        int choice = sc.nextInt();

                        while (choice != 1 && choice != 2) {
                            System.out.print("\nChybně zadané číslo - zkuste to znovu: ");
                            choice = sc.nextInt();
                        }

                        if (choice == 1) {
                            System.out.println("Soubor " + fileName + " byl přeskočen.");
                            continue;
                        }
                    }

                    // Vytvoření a zápis do výstupního souboru
                    try (FileWriter fw = new FileWriter(fileName.toString())) {
                        fw.write(fileContent.toString());
                        System.out.println("Soubor " + fileName + " byl vytvořen/přepsán.");
                    }
                }
            } catch (IOException e) {
                System.err.println("Chyba při dearchivaci: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("Hotovo! Dearchivovaný(é) soubor(y) naleznete v ./");
        }
    }

    public static void printCLI() {
        System.out.println("------ ARCHIVAČNÍ PROGRAM ------");
        System.out.println("1. Archivovat");
        System.out.println("2. Dearchivovat");
        System.out.println("3. Ukončit");
    }

    public static int getChoice() {
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                System.out.println("Použití: java Archivator <výstupní_soubor> <vstup1> <vstup2> ...");
                System.out.print("Vaše soubory k archivaci: ");
                Scanner sc = new Scanner(System.in);
                String[] parameters = sc.nextLine().split(" ");
                if (parameters.length >= 3) {
                    new Archivator(parameters);
                    break;
                }
                System.out.println("Nedostatek souborů - napište alespoň tři cesty (výstupní soubor, vstup1, vstup2, ...)");
                break;

            case 2:
                System.out.println("Použití: java Dearchivator <archivní_soubor>");
                System.out.print("Váš soubor k dearchivaci: ");
                Scanner sc2 = new Scanner(System.in);
                String parameters2 = sc2.nextLine();
                new Dearchivator(parameters2);
                break;
            case 3:
                System.out.println("Ukončuji program...");
                break;
            default:
                System.out.println("Špatný input, zkus to znovu.");
        }
        return choice;
    }

    public static void main(String[] args) {
        boolean isProgramEnded = false;

        while (!isProgramEnded) {
            printCLI();
            if (getChoice() == 3) {
                isProgramEnded = true;
            }
        }
    }
}


