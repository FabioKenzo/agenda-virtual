package br.com.kenzowebstudio.agenda_virtual.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.util.Iterator;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.net.MalformedURLException;
import br.com.kenzowebstudio.agenda_virtual.exception.ResourceNotFoundException;

@Service
public class MediaStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp");

    private final Path storageLocation;

    public MediaStorageService(
            @Value("${app.media.storage-location}") String storageLocation) {

        this.storageLocation = Path.of(storageLocation)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.storageLocation);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Não foi possível inicializar o diretório de mídia.", e);
        }
    }

    public String store(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Selecione uma imagem para enviar.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "A imagem não pode ultrapassar 5 MB.");
        }

        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Formato inválido. Envie uma imagem JPG, PNG ou WebP.");
        }

        if (!isValidImage(file, contentType)) {
            throw new IllegalArgumentException(
                    "O conteúdo do arquivo não corresponde a uma imagem válida.");

        }
        validateImageContent(file, contentType);

        String extension = switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException(
                    "Formato de imagem não permitido.");
        };

        String storedFilename = UUID.randomUUID() + extension;

        Path destination = storageLocation.resolve(storedFilename).normalize();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Não foi possível armazenar a imagem.", e);
        }

        return storedFilename;
    }

    public void delete(String storedFilename) {

        if (storedFilename == null
                || !storedFilename.matches(
                        "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.(jpg|png|webp)")) {
            throw new IllegalArgumentException(
                    "Nome de arquivo inválido.");
        }

        Path destination = storageLocation
                .resolve(storedFilename)
                .normalize();

        if (!destination.startsWith(storageLocation)) {
            throw new IllegalArgumentException(
                    "Caminho de arquivo inválido.");
        }

        try {
            Files.deleteIfExists(destination);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Não foi possível excluir a imagem.", e);
        }
    }

    private boolean isValidImage(MultipartFile file, String contentType) {

        try (InputStream inputStream = file.getInputStream()) {

            byte[] header = inputStream.readNBytes(12);

            return switch (contentType) {

                case "image/jpeg" ->
                    header.length >= 3
                            && (header[0] & 0xFF) == 0xFF
                            && (header[1] & 0xFF) == 0xD8
                            && (header[2] & 0xFF) == 0xFF;

                case "image/png" ->
                    header.length >= 8
                            && Arrays.equals(
                                    Arrays.copyOf(header, 8),
                                    new byte[] {
                                            (byte) 0x89, 0x50, 0x4E, 0x47,
                                            0x0D, 0x0A, 0x1A, 0x0A
                                    });

                case "image/webp" ->
                    header.length >= 12
                            && Arrays.equals(
                                    Arrays.copyOfRange(header, 0, 4),
                                    new byte[] { 0x52, 0x49, 0x46, 0x46 })
                            && Arrays.equals(
                                    Arrays.copyOfRange(header, 8, 12),
                                    new byte[] { 0x57, 0x45, 0x42, 0x50 });

                default -> false;
            };

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Não foi possível validar o conteúdo da imagem.", e);
        }
    }

    private void validateImageContent(
            MultipartFile file,
            String contentType) {

        if ("image/webp".equals(contentType)) {
            throw new IllegalArgumentException(
                    "O formato WebP ainda não está disponível para upload.");
        }

        try (InputStream inputStream = file.getInputStream();
                ImageInputStream imageStream = ImageIO.createImageInputStream(inputStream)) {

            if (imageStream == null) {
                throw new IllegalArgumentException(
                        "Não foi possível reconhecer a imagem.");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);

            if (!readers.hasNext()) {
                throw new IllegalArgumentException(
                        "O arquivo não contém uma imagem válida.");
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(imageStream);

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width <= 0 || height <= 0
                        || width > 4096 || height > 4096) {
                    throw new IllegalArgumentException(
                            "A imagem deve ter dimensões de até 4096 x 4096 pixels.");
                }

                BufferedImage image = reader.read(0);

                if (image == null) {
                    throw new IllegalArgumentException(
                            "Não foi possível decodificar a imagem.");
                }

            } finally {
                reader.dispose();
            }

        } catch (java.io.IOException e) {
            throw new IllegalArgumentException(
                    "O arquivo contém uma imagem inválida ou corrompida.", e);
        }
    }

    public Resource load(String storedFilename) {

        if (storedFilename == null
                || !storedFilename.matches(
                        "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.(jpg|png|webp)")) {
            throw new IllegalArgumentException(
                    "Nome de arquivo inválido.");
        }

        Path filePath = storageLocation
                .resolve(storedFilename)
                .normalize();

        if (!filePath.startsWith(storageLocation)) {
            throw new IllegalArgumentException(
                    "Caminho de arquivo inválido.");
        }

        try {
            if (Files.isSymbolicLink(filePath)) {
                throw new IllegalStateException(
                        "O arquivo de mídia não está disponível.");
            }

            if (!Files.isRegularFile(filePath)) {
                throw new ResourceNotFoundException(
                        "Arquivo de mídia não encontrado.");
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.isReadable()) {
                throw new IllegalStateException(
                        "Não foi possível ler o arquivo de mídia.");
            }

            return resource;

        } catch (MalformedURLException e) {
            throw new IllegalStateException(
                    "Não foi possível localizar o arquivo de mídia.", e);
        }
    }

}
