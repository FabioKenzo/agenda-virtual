package br.com.kenzowebstudio.agenda_virtual.service;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.springframework.core.io.Resource;
import br.com.kenzowebstudio.agenda_virtual.exception.ResourceNotFoundException;

class MediaStorageServiceTest {

        @TempDir
        Path tempDir;

        @Test
        void deveArmazenarImagemPngValida() throws IOException {

                MediaStorageService service = new MediaStorageService(tempDir.toString());

                // Cria uma imagem PNG real de 100 x 100 pixels
                BufferedImage image = new BufferedImage(
                                100,
                                100,
                                BufferedImage.TYPE_INT_RGB);

                ByteArrayOutputStream output = new ByteArrayOutputStream();

                ImageIO.write(image, "png", output);

                byte[] png = output.toByteArray();

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "banner.png",
                                "image/png",
                                png);

                String storedFilename = service.store(file);

                assertTrue(storedFilename.endsWith(".png"));
                assertTrue(Files.exists(tempDir.resolve(storedFilename)));

                assertArrayEquals(
                                png,
                                Files.readAllBytes(tempDir.resolve(storedFilename)));
        }

        @Test
        void deveRejeitarArquivoComConteudoFalsificado() {

                MediaStorageService service = new MediaStorageService(tempDir.toString());

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "banner.png",
                                "image/png",
                                "conteudo falso".getBytes());

                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> service.store(file));

                assertEquals(
                                "O conteúdo do arquivo não corresponde a uma imagem válida.",
                                exception.getMessage());
        }

        @Test
        void deveRejeitarArquivoMaiorQueCincoMegabytes() {

                MediaStorageService service = new MediaStorageService(tempDir.toString());

                byte[] conteudo = new byte[5 * 1024 * 1024 + 1];

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "banner.png",
                                "image/png",
                                conteudo);

                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> service.store(file));

                assertEquals(
                                "A imagem não pode ultrapassar 5 MB.",
                                exception.getMessage());
        }

        @Test
        void deveRejeitarImagemPngCorrompida() {

                MediaStorageService service = new MediaStorageService(tempDir.toString());

                // Assinatura PNG correta, mas arquivo incompleto
                byte[] pngCorrompido = {
                                (byte) 0x89, 0x50, 0x4E, 0x47,
                                0x0D, 0x0A, 0x1A, 0x0A
                };

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "banner-corrompido.png",
                                "image/png",
                                pngCorrompido);

                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.store(file));

                // Nenhum arquivo deve ter sido armazenado
                try (var arquivos = Files.list(tempDir)) {
                        assertEquals(0, arquivos.count());
                } catch (IOException e) {
                        fail("Não foi possível verificar o diretório temporário.", e);
                }
        }

        @Test
        void deveRejeitarImagemComDimensoesExcessivas() throws IOException {

                MediaStorageService service = new MediaStorageService(tempDir.toString());

                // Imagem com largura acima do limite permitido
                BufferedImage image = new BufferedImage(
                                4097,
                                10,
                                BufferedImage.TYPE_INT_RGB);

                ByteArrayOutputStream output = new ByteArrayOutputStream();

                assertTrue(ImageIO.write(image, "png", output));

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "imagem-grande.png",
                                "image/png",
                                output.toByteArray());

                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> service.store(file));

                assertEquals(
                                "A imagem deve ter dimensões de até 4096 x 4096 pixels.",
                                exception.getMessage());

                // Verifica se nenhum arquivo foi armazenado
                try (var arquivos = Files.list(tempDir)) {
                        assertEquals(0, arquivos.count());
                }
        }

        @Test
        void deveRejeitarUploadWebp() throws IOException {

                MediaStorageService service = new MediaStorageService(tempDir.toString());

                // Cabeçalho com assinatura WebP
                byte[] webp = {
                                'R', 'I', 'F', 'F',
                                0x04, 0x00, 0x00, 0x00,
                                'W', 'E', 'B', 'P'
                };

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "banner.webp",
                                "image/webp",
                                webp);

                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> service.store(file));

                assertEquals(
                                "O formato WebP ainda não está disponível para upload.",
                                exception.getMessage());

                try (var arquivos = Files.list(tempDir)) {
                        assertEquals(0, arquivos.count());
                }
        }

        @Test
        void deveCarregarImagemArmazenada() throws IOException {

                MediaStorageService service = new MediaStorageService(tempDir.toString());

                // Gera uma imagem PNG válida
                BufferedImage image = new BufferedImage(
                                100,
                                100,
                                BufferedImage.TYPE_INT_RGB);

                ByteArrayOutputStream output = new ByteArrayOutputStream();

                assertTrue(ImageIO.write(image, "png", output));

                byte[] imageBytes = output.toByteArray();

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "banner.png",
                                "image/png",
                                imageBytes);

                // Armazena a imagem
                String storedFilename = service.store(file);

                // Recupera a imagem armazenada
                Resource resource = service.load(storedFilename);

                assertTrue(resource.exists());
                assertTrue(resource.isReadable());

                try (InputStream inputStream = resource.getInputStream()) {

                        assertArrayEquals(
                                        imageBytes,
                                        inputStream.readAllBytes());
                }
        }

        @Test
        void deveRejeitarNomeDeArquivoInvalidoAoCarregar() {

                MediaStorageService service = new MediaStorageService(tempDir.toString());

                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> service.load("../../arquivo-secreto.txt"));

                assertEquals(
                                "Nome de arquivo inválido.",
                                exception.getMessage());
        }

        @Test
        void deveLancarExcecaoQuandoArquivoFisicoNaoExistir() {

                MediaStorageService service = new MediaStorageService(tempDir.toString());

                String storedFilename = "550e8400-e29b-41d4-a716-446655440000.png";

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> service.load(storedFilename));

                assertEquals(
                                "Arquivo de mídia não encontrado.",
                                exception.getMessage());
        }
}
