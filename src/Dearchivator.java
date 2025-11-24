import java.io.*;

class Dearchivator {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Použití: java Dearchivator <archivní_soubor> <výstupní_soubor>");
            return;
        }

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(args[0]));
             FileWriter fw = new FileWriter(args[1])) {

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

                // Zápis do výstupního souboru
                fw.write("Obsah souboru " + fileName + " -> " + fileContent + "\n");


            }
        } catch (IOException e) {
            System.err.println("Chyba při dearchivaci: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Hotovo! Dearchivovaný soubor naleznete v " + args[1]);
    }
}