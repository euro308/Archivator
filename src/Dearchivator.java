import java.io.*;

class Dearchivator {
    public static void main(String[] args) {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(args[0]))) {
            // Zjištění počtu souborů
            byte[] bufferFileCount = new byte[8]; // Čtení 8 znaků najednou
            bis.read(bufferFileCount);
            int fileCount = Integer.parseInt(new String(bufferFileCount), 2); // Převod na dekadické čislo

            // Čtení jednotlivých souborů
            for (int i = 0; i < fileCount; i++) {
                // Délka názvu souboru
                byte[] bufferNameLength = new byte[8];
                bis.read(bufferNameLength);
                int fileNameSize = Integer.parseInt(new String(bufferNameLength), 2);

                // Název souboru
                byte[] bufferFileName = new byte[fileNameSize * 8]; // Délka názvu krát 8, protože jeden znak má 8 čísel v ASCII
                bis.read(bufferFileName);
                String binaryFileName = new String(bufferFileName);
                StringBuilder fileName = new StringBuilder();
                for (int j = 0; j < binaryFileName.length(); j += 8) { // Jedu po osmi, tedy po jednom písmenu
                    String chunk = binaryFileName.substring(j, j + 8);
                    fileName.append((char) Integer.parseInt(chunk, 2));
                }

                // Délka obsahu souboru
                byte[] bufferFileSize = new byte[32];
                bis.read(bufferFileSize);
                int fileSize = Integer.parseInt(new String(bufferFileSize), 2);

                // Obsah souboru
                byte[] bufferFile = new byte[fileSize * 8]; // Délka názvu krát 8, protože jeden znak má 8 čísel v ASCII
                bis.read(bufferFile);
                String binaryFileContent = new String(bufferFile);
                StringBuilder fileContent = new StringBuilder();
                for (int j = 0; j < binaryFileContent.length(); j += 8) { // // Jedu po osmi, tedy po jednom písmenu
                    String chunk = binaryFileContent.substring(j, j + 8);
                    fileContent.append((char) Integer.parseInt(chunk, 2));
                }

                try(FileWriter fw = new FileWriter(args[1], true)) {
                 fw.write("Obsah souboru " + fileName + " -> " + fileContent + "\n");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Hotovo! Dearchivovaný soubor naleznete v " +args[1]);
    }
}
