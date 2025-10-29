package cau_1;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PUF {
    public void pack(String folder, String packedFile) {
        List<File> listFiles = getListFiles(folder);
        if (listFiles == null || listFiles.isEmpty()) return;
        try (RandomAccessFile raf = new RandomAccessFile(packedFile, "rw")) {
            raf.setLength(0);
            raf.writeInt(listFiles.size());
            List<Long> offsets = new ArrayList<>();
            for (File f : listFiles) {
                long hPos = raf.getFilePointer();
                offsets.add(hPos);
                raf.writeLong(f.length());
                raf.writeUTF(f.getName());
            }
            for (int j = 0; j < listFiles.size(); j++) {
                File currentFile = listFiles.get(j);
                try (FileInputStream fis = new FileInputStream(currentFile)) {
                    long dataPos = raf.getFilePointer();
                    raf.seek(offsets.get(j));
                    raf.writeLong(dataPos);
                    raf.seek(raf.length());
                    copyFrom(fis, raf);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private List<File> getListFiles(String path) {
        List<File> re = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) return null;
        File[] files = file.listFiles(File::isFile);
        if (files != null) for (File f : files) re.add(f);
        return re;
    }

    private void copyFrom(FileInputStream fis, RandomAccessFile raf) throws IOException {
        byte[] buffer = new byte[102400];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            raf.write(buffer, 0, bytesRead);
        }
    }

    public void copyFrom(RandomAccessFile raf, FileOutputStream fos, long size) throws IOException {
        int remaining = (int) size;
        byte[] buffer = new byte[102400];

        while (remaining > 0) {
            int byToRead = remaining > buffer.length ? buffer.length : remaining;
            int bytesRead = raf.read(buffer, 0, byToRead);
            if (bytesRead == -1) break;
            fos.write(buffer, 0, bytesRead);
            remaining -= bytesRead;
        }
    }

    public void unPack(String packedFile, String extractFile, String destFile) {
        try (RandomAccessFile raf = new RandomAccessFile(packedFile, "r")) {
            int num = raf.readInt();
            for (int i = 0; i < num; i++) {
                long pos = raf.readLong();
                long size = raf.readLong();
                String name = raf.readUTF();
                if (extractFile.equalsIgnoreCase(name)) {
                    try (FileOutputStream fos = new FileOutputStream(destFile)) {
                        raf.seek(pos);
                        copyFrom(raf, fos, size);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {
        PUF puf = new PUF();
        String folder = "E:\\LapTrinhMang\\A\\BaiThi4";
        String packedFile = "E:\\LapTrinhMang\\A\\BaiThi4.zip";
        String extractFile = "";
        String destFile = "";
        puf.pack(folder, packedFile);
    }
}
